package edu.yale.library.paperless.controllers;


import edu.yale.library.paperless.entities.*;
import edu.yale.library.paperless.repositories.*;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskLogRepository taskLogRepository;
    private final BarcodeRepository barcodeRepository;

    public record ReportInfo(Task task, List<TaskAssignmentDto> assignments, List<TaskLogDto> logs) {};

    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/barcode")
    public List<ReportInfo> barcodeReport(String barcode) {
        List<Barcode> barcodes = barcodeRepository.findByBarcode(barcode);
        Set<Long> taskIds = new HashSet<>();
        List<Task> taskList = new ArrayList<>();
        for (Barcode barcodeLink : barcodes) {
            if (!taskIds.contains(barcodeLink.getTask().getId())) {
                taskList.add(barcodeLink.getTask());
                taskIds.add(barcodeLink.getTask().getId());
            }
        }
        return getReportInfos(taskList);
    }

    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/mmsid")
    public List<ReportInfo> mmsidReport(String mmsid) {
        List<Task> taskList = taskRepository.findByAlmaBibMmsId(mmsid);
        return getReportInfos(taskList);
    }

    private List<ReportInfo> getReportInfos(List<Task> taskList) {
        List<ReportInfo> response = new ArrayList<>();
        for (Task task : taskList) {
            List<TaskAssignment> taskAssignments = taskAssignmentRepository.findByTaskOrderByUpdateDateTime(task);
            List<TaskLogDto> logs = taskLogRepository.findByTaskOrderByUpdateDateTime(task).stream().map(taskLog -> new TaskLogDto(taskLog.getUpdateDateTime(), taskLog.getLogInformation(),
                    newId(taskLog.getUser()), fullName(taskLog.getUser()), taskLog.getItemBarcode())).collect(Collectors.toList());
            List<TaskAssignmentDto> assignments =  taskAssignments.stream().map(taskAssignment -> new TaskAssignmentDto(taskAssignment.getUpdateDateTime(), taskAssignment.getTaskStatus(), fullName(taskAssignment.getAssigner()), fullName(taskAssignment.getRetriever()), taskAssignment.getItemBarcode())).collect(Collectors.toList());
            response.add(new ReportInfo(task, assignments, logs));
        }
        return response;
    }

    private String fullName(User user){
        if (user != null) return user.getFirstName() + " " + user.getLastName(); else return "System";
    }

    private String newId(User user){
        if (user != null) return user.getNetId(); else return "System";
    }

    @AllArgsConstructor
    @Getter
    public static class TaskLogDto {
        private Timestamp timestamp;
        private String message;
        private String userNetId;
        private String userName;
        private String itemBarcode;
    }

    @AllArgsConstructor
    @Getter
    public static class TaskAssignmentDto {
        private Timestamp timestamp;
        private TaskStatus status;
        private String assigner;
        private String retriever;
        private String itemBarcode;
    }

}
