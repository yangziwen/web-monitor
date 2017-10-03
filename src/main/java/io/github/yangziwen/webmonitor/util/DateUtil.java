package io.github.yangziwen.webmonitor.util;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public class DateUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private DateUtil() {}

    public static Date parseDateQuietly(String date, String pattern) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        try {
            return DateUtils.parseDate(date, pattern);
        } catch (ParseException e) {
            return null;
        }
    }

}
