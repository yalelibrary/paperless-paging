package edu.yale.library.paperless.utilities;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.util.StringUtils;

import javax.swing.plaf.TextUI;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateParser {

    // e.g. 2023-11-25 10:08:42 US/Eastern
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d[ H:m:s][ z]");
    private static final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
    private static final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'Z'");

    public static Timestamp parseDate(String date) {
        return parseDate(date, null);
    }

    public static Timestamp parseDate(String date, Long defaultTime) {
        if (StringUtils.hasText(date)) {
            try {
                try {
                    return Timestamp.from(ZonedDateTime.parse(date, dateTimeFormatter).toInstant());
                } catch (DateTimeParseException e) {
                    try {
                        return Timestamp.from(LocalDateTime.parse(date, dateTimeFormatter2).atZone(ZoneId.systemDefault()).toInstant());
                    } catch (DateTimeParseException e2) {
                        return Timestamp.from(LocalDate.parse(date, dateTimeFormatter3).atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant());
                    }
                }
            } catch (Exception e) {
                log.error("Unable to parse date", e);
            }
        }
        if (defaultTime == null) return null;
        return new Timestamp(defaultTime);
    }
}
