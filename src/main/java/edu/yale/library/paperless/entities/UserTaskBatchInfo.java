package edu.yale.library.paperless.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class UserTaskBatchInfo {
    @JsonIgnore
    private User user;
    @JsonIgnore
    private User assigningUser;
    private Long taskBatchId;
    private int taskCount;
    private Map<TaskStatus, Integer> statusCounts;
    private String maxCallNumber;
    private String minCallNumber;
    private String maxSortTitle;
    private String minSortTitle;
    private String maxTitle;
    private String minTitle;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Timestamp startDateTime;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Timestamp mostRecentResponse;
    private Set<String> taskLocations;
    private Set<String> itemLocations;
    private boolean cancellable;
    private boolean firstPass;
    private boolean secondPass;
    private int responseCount;

    public String getAssigningUserFirstName() {
        return assigningUser.getFirstName();
    }

    public String getAssigningUserLastName() {
        return assigningUser.getLastName();
    }

    public String getAssigningUserNetId() {
        return assigningUser.getNetId();
    }

    public Long getAssigningUserId() {
        return assigningUser.getId();
    }

    public String getUserFirstName() {
        return user.getFirstName();
    }

    public String getUserLastName() {
        return user.getLastName();
    }

    public String getUserNetId() {
        return user.getNetId();
    }

    public boolean isFirstPass() {
        return firstPass;
    }

    public boolean isSecondPass() {
        return secondPass;
    }

    public String getUserSort() {
        return Stream.of(
            user.getLastName(), user.getFirstName(), user.getNetId()).collect(
                    Collectors.joining(" ")).toUpperCase();
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getTaskLocationSort() {
        if ( taskLocations == null ) return "";
        return taskLocations.stream().map(s->s.toUpperCase()).collect(Collectors.joining(", "));
    }

    public static UserTaskBatchInfo fromTaskBatch(UserTaskBatch userTaskBatch) {
        final UserTaskBatchInfo ret = new UserTaskBatchInfo();
        ret.user = userTaskBatch.getUser();
        ret.setTaskBatchId(userTaskBatch.getId());
        ret.assigningUser = userTaskBatch.getAssigningUser();
        ret.taskCount = userTaskBatch.getTasks().size();
        ret.setStatusCounts(new HashMap<>());
        ret.setStartDateTime(userTaskBatch.getStartTime());
        ret.setMostRecentResponse(userTaskBatch.getMostRecentResponse());
        ret.setTaskLocations(new HashSet<>());
        ret.setItemLocations(new HashSet<>());
        ret.firstPass = false;
        ret.secondPass = false;

        userTaskBatch.getTasks().stream().forEach(task->{
            if ( TaskStatus.New.equals(task.getIncomingStatus()) ) ret.firstPass = true;
            else if ( TaskStatus.NOS.equals(task.getIncomingStatus()) ) ret.secondPass = true;

            ret.statusCounts.compute( task.getStatus(), (taskStatus, cnt) -> {
               if ( cnt != null ) return cnt + 1;
               else return 1;
            });
            if (ret.getMaxSortTitle() ==  null || ret.getMaxSortTitle().compareTo(task.getSortTitle()) < 0 ) {
                ret.setMaxSortTitle(task.getSortTitle());
                ret.setMaxTitle(task.getTitle());
            }
            if (ret.getMinSortTitle() ==  null || ret.getMinSortTitle().compareTo(task.getSortTitle()) > 0 ) {
                ret.setMinSortTitle(task.getSortTitle());
                ret.setMinTitle(task.getTitle());
            }
            if (task.getCallNumber() !=  null && (ret.getMaxCallNumber() ==  null || ret.getMaxCallNumber().compareTo(task.getCallNumber()) < 0 )) {
                ret.setMaxCallNumber(task.getCallNumber());
            }
            if (task.getCallNumber() !=  null && ( ret.getMinCallNumber() ==  null || ret.getMinCallNumber().compareTo(task.getCallNumber()) > 0 )) {
                ret.setMinCallNumber(task.getCallNumber());
                ret.setMinCallNumber(task.getCallNumber());
            }
            if (StringUtils.hasText(task.getTaskLocation())) {
                ret.getTaskLocations().add(task.getTaskLocation());
            }
            if (StringUtils.hasText(task.getItemPermLocation())) {
                ret.getItemLocations().add(task.getItemPermLocation());
            }
        });
        return ret;
    }

}
