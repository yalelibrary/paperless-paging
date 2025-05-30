package edu.yale.library.paperless.report;

import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.UserTaskBatch;
import edu.yale.library.paperless.entities.UserTaskBatchInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserBatchEmailGenerator {

    @Value("${email.from:noreply@library.yale.edu}")
    private String fromEmail;
    @Value("${email.netid.suffix:@connect.yale.edu}")
    private String netIdSuffix;

    private UserBatchExcelGenerator userBatchExcelGenerator = new UserBatchExcelGenerator();

    private JavaMailSender emailSender;

    public UserBatchEmailGenerator(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSubmitEmails(UserTaskBatch batch) throws MessagingException {
        DateTimeFormatter simpleDateFormatFilename = DateTimeFormatter.ofPattern("MM-dd-yyyy_hh-mm-ss").withZone(ZoneId.of("US/Eastern"));
        DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss").withZone(ZoneId.of("US/Eastern"));
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        UserTaskBatchInfo info = UserTaskBatchInfo.fromTaskBatch(batch);
        helper.setFrom(fromEmail);
        helper.setTo(batch.getUser().getNetId() + netIdSuffix);
        StringBuilder subject = new StringBuilder("Task Batch Submit: " + simpleDateFormat.format(batch.getEndTime().toInstant()));
        subject.append(" / ");
        if (info.isFirstPass()) subject.append("1x");
        if (info.isFirstPass() && info.isSecondPass()) subject.append(" & ");
        if (info.isSecondPass()) subject.append("2x");
        subject.append(" / ");
        subject.append(info.getTaskLocations().stream().collect(Collectors.joining(", ")));
        subject.append(" / ");
        subject.append(info.getMinCallNumber());

        helper.setSubject(subject.toString());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Your batch report is attached.\n\n");
        stringBuilder.append("The batch was submitted at: " + simpleDateFormat.format(batch.getEndTime().toInstant()) + " by " + batch.getUser().getNetId() + "\n\n");
        stringBuilder.append("Start Time: " + simpleDateFormat.format(batch.getStartTime().toInstant())+ "\n");
        stringBuilder.append("End Time: " + simpleDateFormat.format(batch.getEndTime().toInstant())+ "\n\n");
        stringBuilder.append("Call number range: " + info.getMinCallNumber() + " - " + info.getMaxCallNumber() + "\n\n");

        Map<TaskStatus, Integer> statusCounts = info.getStatusCounts();
        stringBuilder.append("Status Counts:\n" +
                statusCounts.keySet().stream().map((status) -> status.toString() + ": " + statusCounts.get(status)).collect(Collectors.joining(",\n"))
                + "\n\n\n\n");



        helper.setText(stringBuilder.toString());
        InputStreamSource in = new InputStreamSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return userBatchExcelGenerator.userBatchToExcel(batch);
            }
        };
        helper.addAttachment("task-report-" +
                batch.getUser().getNetId() + "-" +
                simpleDateFormatFilename.format(batch.getEndTime().toInstant()) +
                ".xlsx", in, "application/vnd.ms-excel");
        emailSender.send(message);
    }

}
