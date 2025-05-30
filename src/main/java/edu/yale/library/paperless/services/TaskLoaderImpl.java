package edu.yale.library.paperless.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import edu.yale.library.alma.api.client.ApiClient;
import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.alma.api.client.ObjectNodeAndMarc;
import edu.yale.library.alma.api.client.bib.BibApi;
import edu.yale.library.alma.api.client.task.TaskApi;
import edu.yale.library.alma.api.client.user.UserApi;
import edu.yale.library.paperless.entities.Barcode;
import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.repositories.*;
import edu.yale.library.paperless.utilities.DateParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.utils.StringInputStream;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskLoaderImpl implements TaskLoader {
    public static Pattern almaSruMarcPatter = Pattern.compile("<record .*?>(.*?)</record>", Pattern.DOTALL);
    private final TaskRepository taskRepository;
    private final LocationRepository locationRepository;
    private final CirculationDeskRepository circulationDeskRepository;
    public static List<String> taskRequestTypes = List.of("HOLD", "WORK_ORDER", "MOVE", "DIGITIZATION");
    public static List<String> taskRequestSubTypes = List.of("PATRON_PHYSICAL",  "AcqWorkOrder", "PreConServ", "MOVE_TO_TEMPORARY", "PRESERV", "PHYSICAL_TO_DIGITIZATION", "GENERAL_HOLD", "RESOURCE_SHARING_PHYSICAL_SHIPMENT");
    private final TaskProblemRepository taskProblemRepository;
    private final String sruMarcXmlUrlPrefix;
    private final BibApi bibApi;
    private final UserApi userApi;
    private final RestTemplate restTemplate;
    private final TaskLogRepository taskLogRepository;
    private final BarcodeRepository barcodeRepository;
    private final Map<String, JsonNode> patronCache = new HashMap<>();

    public TaskLoaderImpl(TaskRepository taskRepository, LocationRepository locationRepository,
                          CirculationDeskRepository circulationDeskRepository,
                          TaskProblemRepository taskProblemRepository,
                          TaskLogRepository taskLogRepository,
                          BarcodeRepository barcodeRepository,
                          @Value("${alma.sru.bib.marcxml:https://yale.alma.exlibrisgroup.com/view/sru/01YALE_INST?version=1.2&operation=searchRetrieve&recordSchema=marcxml&query=mms_id==}") String sruMarcXmlUrlPrefix) {
        this.taskRepository = taskRepository;
        this.locationRepository = locationRepository;
        this.circulationDeskRepository = circulationDeskRepository;
        this.taskProblemRepository = taskProblemRepository;
        this.sruMarcXmlUrlPrefix = sruMarcXmlUrlPrefix;
        this.bibApi = ApiClient.getInstance().getBibApi();
        this.userApi = ApiClient.getInstance().getUserApi();
        this.restTemplate = new RestTemplate();
        this.taskLogRepository = taskLogRepository;
        this.barcodeRepository = barcodeRepository;
        if (StringUtils.hasText(sruMarcXmlUrlPrefix)) {
            log.info("Using SRU for MARC Records");
        } else {
            log.info("Using API for MARC Records");
        }
    }

    @Override
    @Transactional
    public void loadTasks(String libraryCode, String circDeskCode, boolean reloadDetails) throws ApiClientException {
        log.info("Loading tasks for " + libraryCode + " " + circDeskCode);
        TaskApi taskApi = ApiClient.getInstance().getTaskApi();
        List<JsonNode> requestedResourceList = new ArrayList<>();
        int offset = 0;
            int limit = 50;
        while (true) {
            JsonNode requestedResources = taskApi.loadTasks(libraryCode, circDeskCode, limit, offset);
            List<JsonNode> requestedResourceElements = IteratorUtils.toList(requestedResources.path("requested_resource").elements());
            if (!requestedResourceElements.isEmpty()) {
                requestedResourceList.addAll(requestedResourceElements);
                offset += limit;
                if (requestedResources.path("total_record_count").asInt() < offset) {
                    break;
                }
            } else {
                break;
            }
        }
        closeTasksNoLongerOpenInAlma(libraryCode, circDeskCode, requestedResourceList);
        createNewTasks(libraryCode, circDeskCode, requestedResourceList, reloadDetails);
    }

    private void createNewTasks(String libraryCode, String circDeskCode, List<JsonNode> requestedResourceList, boolean reloadDetails) {
        for (JsonNode requestedResource: requestedResourceList) {
            JsonNode location = requestedResource.path("location");
            JsonNode metadata = requestedResource.path("resource_metadata");
            JsonNode requests = requestedResource.path("request");
            String md5Hash = DigestUtils.md5Hex(requestedResource.toString());
            JsonNode copies = location.path("copy");
            if (copies.isEmpty()) {
                log.info("Adding dummy copy since there are none");
                copies = JsonNodeFactory.instance.arrayNode().add(JsonNodeFactory.instance.objectNode());
            }
            if (requests.isEmpty()) {
                log.error("No requests found for requested resources");
                continue;
            }
            JsonNode request = requests.get(0);
            if (requests.size() > 1) {
                List<String> requestIds = new ArrayList<>();
                for (JsonNode r : requests) {
                    requestIds.add(r.path("id").asText());
                }
                log.info("Multiple requests for one item: " + String.join(", ", requestIds) + " Request type: " + request.path("request_sub_type").path("value").asText());
            }
            for (JsonNode copy: copies) {
                String barcode = copy.path("barcode").asText("no-barcode");
                String requestId = request.path("id").asText();
                String requestType = request.path("request_type").asText();
                String requestSubType = request.path("request_sub_type").path("value").asText();
                if (taskRequestTypes.contains(requestType) && taskRequestSubTypes.contains(requestSubType)) {
                    boolean reset = false;
                    boolean load = reloadDetails; // is it a force reload
                    if (!load) {
                        load = !taskRepository.existsByAlmaRequestIdAndItemBarcode(requestId, barcode); // does it not exist
                    }
                    if (!load) {
                        Optional<Task> task = taskRepository.findByAlmaRequestIdAndItemBarcode(requestId, barcode);
                        if (task.isPresent() && !md5Hash.equals(task.get().getRequestedResourceMd5())) { // has it changed in Alma changed
                            load = true;
                            reset = true;
                        }
                    }
                    if (load) {
                        // create a new call slip
                        Task task = taskRepository.findByAlmaRequestIdAndItemBarcode(requestId, barcode).orElseGet(() -> {
                            Task task1 = new Task();
                            task1.setStatus(TaskStatus.New);
                            task1.setOpen(true);
                            return task1;
                        });

                        if (reset) {
                            taskLogRepository.log(task, null, "The task was updated in Alma.");
                            task.setOpen(true);
                            if (!circDeskCode.equals(task.getCircDeskCode()) || !libraryCode.equals(task.getLibraryCode())) {
                                task.setStatus(TaskStatus.New);
                                taskLogRepository.log(task, null, "The task circulation desk has changed. The status was reset to 'New'.");
                            }
                        }
                        task.setRequestedResourceMd5(md5Hash);
                        task.setAlmaRequestId(requestId);
                        task.setRequestType(requestType);
                        task.setRequestSubType(requestSubType);
                        task.setLibraryCode(libraryCode);
                        task.setCircDeskCode(circDeskCode);
                        task.setAlmaItemPid(copy.path("pid").asText());
                        task.setAlmaHoldingId(location.path("holding_id").path("value").asText());
                        task.setAlmaBibMmsId(metadata.path("mms_id").path("value").asText());
                        String callNumber = location.path("call_number").asText();
                        String description = copy.path("description").asText();
                        if (StringUtils.hasText(description)) {
                            callNumber = String.format("%s (%s)", callNumber, description);
                        }
                        task.setCallNumber(callNumber);
                        task.setCallNumberDisplay(callNumber);
                        task.setCallNumberNormalized(normalizeCallNumber(location.path("call_number").asText()));
                        task.setCallNumberType("");
                        String locationCode = "";
                        String itemLibraryCode = libraryCode;
                        boolean itemIsSet = StringUtils.hasText(task.getAlmaItemPid());
                        if (itemIsSet) {
                            try {
                                JsonNode itemData = bibApi.getItem(task.getAlmaBibMmsId(), task.getAlmaHoldingId(), task.getAlmaItemPid());
                                task.setItemPermLocation(itemData.path("item_data").path("location").path("value").asText());
                                task.setLastDischargeDateTime(DateParser.parseDate(itemData.path("item_data").path("modification_date").asText()));
                                task.setHoldingLocation(task.getItemPermLocation()); // these will always be equal for these
                                locationCode = task.getItemPermLocation();
                                itemLibraryCode = itemData.path("item_data").path("library").path("value").asText();
                                if (itemData.path("holding_data").path("in_temp_location").asBoolean()) {
                                    task.setItemTempLocation(itemData.path("holding_data").path("temp_location").path("value").asText());
                                    locationCode = task.getItemTempLocation();
                                    itemLibraryCode = itemData.path("holding_data").path("temp_library").path("value").asText();
                                } else {
                                    task.setHoldingLocation(task.getItemPermLocation());
                                    task.setItemTempLocation(task.getItemPermLocation());
                                }
                            } catch (ApiClientException e) {
                                itemIsSet = false;
                            }
                        }
                        if (!itemIsSet) {
                            task.setHoldingLocation(location.path("shelving_location").asText());
                            task.setItemPermLocation(location.path("shelving_location").asText());
                            task.setItemTempLocation(location.path("shelving_location").asText());
                            locationCode = location.path("shelving_location").asText();
                        }
                        lookupMarc300(task);


                        locationRepository.findByLibrary_CodeAndCode(itemLibraryCode, locationCode).ifPresentOrElse(location2 -> {
                            task.setLocationDisplay(location2.getName());
                        }, () -> log.error("There was a request ("+task.getAlmaRequestId()+") with a library/location that was not found: " + libraryCode + " :: " + location.path("shelving_location").asText()));
                        circulationDeskRepository.findByLibrary_CodeAndCode(libraryCode, circDeskCode).ifPresentOrElse(circulationDesk -> {
                            task.setPickupLocation(libraryCode + ":" + circDeskCode);
                            task.setPickupLocationDisplay(circulationDesk.getName());
                        }, ()-> {
                            log.error("There is a request ("+task.getAlmaRequestId()+") without a circulation desk: " + libraryCode + " :: " + circDeskCode);
                        });
                        task.setItemBarcode(barcode);
                        task.setTitle(metadata.path("title").asText());
                        task.setAuthor(metadata.path("author").asText());
                        task.setPublisher(metadata.path("publisher").asText());
                        task.setPublicationYear(metadata.path("publication_year").asText());
                        task.setDestination(request.path("destination").path("desc").asText());
                        task.setEnumeration(copy.path("enumeration_a").asText());
                        task.setPatronRequestDate(request.path("request_time").asText());
                        task.setRequester(request.path("requester").path("desc").asText("n/a"));
                        JsonNode patron = loadPatronInfo(request.path("requester").path("link").asText());
                        if (patron != null) {
                            task.setPatronBarcode(extractPatronBarcode(patron));
                            task.setPatronEmail(extractPatronEmail(patron));
                        }
                        task.setPatronComment(request.path("comment").asText());
                        task.setTaskLocation(task.getPickupLocationDisplay());
                        String problemCode = TaskProblemIdentifier.identifyProblemCode(task);
                        if (StringUtils.hasText(problemCode)) {
                            task.setTaskProblem(taskProblemRepository.findByValue(problemCode));
                        }
                        taskRepository.save(task);
                        if (StringUtils.hasText(barcode)) {
                            List<Barcode> barcodes = barcodeRepository.findByTask(task);
                            List<String> barcodeList = barcodes.stream().map(Barcode::getBarcode).toList();
                            if (!barcodeList.contains(barcode)) {
                                Barcode b = new Barcode();
                                b.setTask(task);
                                b.setBarcode(barcode);
                                barcodeRepository.save(b);
                            }
                        }
                    }
                } else {
                    log.info("Skipping a request of type: " + requestType + " and subtype: " + requestSubType + " for " + requestedResource);
                }
            }
        }
    }

    private String extractPatronBarcode(JsonNode patron) {
        for (JsonNode id : patron.path("user_identifier")) {
            if (id.path("id_type").path("value").asText().equals("BARCODE")) {
                return id.path("value").asText();
            }
        }
        return "n/a";
    }

    private String extractPatronEmail(JsonNode patron) {
        for (JsonNode email : patron.path("contact_info").path("email")) {
            return email.path("email_address").asText("n/a");
        }
        return "n/a";
    }

    private void lookupMarc300(Task task) {
        try {
            String physicalDescription = null;
            if (StringUtils.hasText(sruMarcXmlUrlPrefix)) {
                String sruUri = sruMarcXmlUrlPrefix + task.getAlmaBibMmsId();
                org.marc4j.marc.Record record = retrieveSruMarcRecord(sruUri);
                if (record != null) {
                    physicalDescription = record.getVariableFields("300").stream().map(f-> ((DataField)f).getSubfields().stream().map(Subfield::getData).collect(Collectors.joining())).collect(Collectors.joining(", "));
                }
            } else {
                ObjectNodeAndMarc bibData = bibApi.getBib(task.getAlmaBibMmsId());
                physicalDescription = bibData.marc().getVariableFields("300").stream().map(f-> ((DataField)f).getSubfields().stream().map(Subfield::getData).collect(Collectors.joining())).collect(Collectors.joining(", "));
            }
            task.setPhysicalDescription(physicalDescription);
        } catch (Exception e) {
            log.error("Error getting marc 300", e);
        }
    }

    private org.marc4j.marc.Record retrieveSruMarcRecord(String sruUri) {
        String xml = restTemplate.getForObject(sruUri, String.class);
        if (xml == null) return null;
        Matcher m = almaSruMarcPatter.matcher(xml);
        if (m.find()) {
            String marcXml = String.format("<marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\"><marc:record>%s</marc:record></marc:collection>", m.group(1));
            StringInputStream inputStream = new StringInputStream(marcXml);
            MarcXmlReader reader = new MarcXmlReader(inputStream);
            if (reader.hasNext()) {
                return reader.next();
            }
        }
        return null;
    }

    private void closeTasksNoLongerOpenInAlma(String libraryCode, String circDeskCode, List<JsonNode> requestedResourceList) {
        List<String> openRequestIds = new ArrayList<>();
        for (JsonNode requestedResource: requestedResourceList) {
            JsonNode requests = requestedResource.path("request");
            JsonNode copies = requestedResource.path("location").path("copy");
            if (copies.isEmpty()) {
                copies = JsonNodeFactory.instance.arrayNode().add(JsonNodeFactory.instance.objectNode());
            }
            for (JsonNode request : requests) {
                for (JsonNode copy: copies) {
                    String barcode = copy.path("barcode").asText("no-barcode");
                    openRequestIds.add(request.path("id").asText() + "::" + barcode);
                }
            }
        }
        List<Task> updatedTasks = new ArrayList<>();
        List<Task> openTasks = taskRepository.findAllByOpenTrueAndLibraryCodeAndCircDeskCodeOrderByCallNumber(libraryCode, circDeskCode);
        for (Task openTask : openTasks) {
            if (!openRequestIds.contains(openTask.getAlmaRequestId() + "::" + openTask.getItemBarcode())) {
                taskLogRepository.log(openTask, null, "Closing task because it is no longer in Alma for this circulation desk: " + libraryCode + " " + circDeskCode);
                openTask.setOpen(false);
                updatedTasks.add(openTask);
            }
        }
        if (!updatedTasks.isEmpty()) {
            taskRepository.saveAll(updatedTasks);
        }
    }

    @Override
    @Transactional
    public void loadAllTasks(boolean reloadDetails) throws ApiClientException {
        for (CirculationDesk circulationDesk : circulationDeskRepository.findAll()) {
            try {
                loadTasks(circulationDesk.getLibrary().getCode(), circulationDesk.getCode(), reloadDetails);
            } catch (Exception e) {
                log.error("Unable to load tasks for circ %s, %s".formatted(circulationDesk.getLibrary().getCode(), circulationDesk.getCode()), e);
            }
        }
        clearOldClosedTasks();
    }

    @Override
    @Transactional
    public void clearOldClosedTasks() {
        List<Task> clearedTasks = new ArrayList<>();
        for (Task task: taskRepository.findAllByPatronClearedFalseAndOpenFalseAndUpdateDateTimeBefore(Timestamp.from(Instant.now().minus(Duration.ofDays(5))))) {
            task.setPatronEmail("------");
            task.setPatronBarcode("------");
            task.setPatronCleared(true);
            clearedTasks.add(task);
        }
        if (!clearedTasks.isEmpty()) {
            taskRepository.saveAll(clearedTasks);
        }
    }

    private JsonNode loadPatronInfo(String uri) {
        if (StringUtils.hasText(uri)) {
            JsonNode value = patronCache.get(uri);
            if (value == null) {
                String[] uriParts = uri.split("/");
                try {
                    value = this.userApi.user(uriParts[uriParts.length-1]);
                } catch (ApiClientException e) {
                    log.error("Unable to load patron: " + uriParts[uriParts.length-1]);
                    value = NullNode.getInstance();
                }
                patronCache.put(uri, value);
            }
            return value;
        }
        return NullNode.getInstance();
    }

    private String normalizeCallNumber(String callNumber) {
        return callNumber;
    }
}
