package io.github.yangziwen.webmonitor.command.converter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.IStringConverter;

import io.github.yangziwen.webmonitor.util.DateUtil;

public class DateConverter implements IStringConverter<Date> {

    @Override
    public Date convert(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (value.length() == DateUtil.DATE_PATTERN.length()) {
            return DateUtil.parseDateQuietly(value, DateUtil.DATE_PATTERN);
        }
        return DateUtil.parseDateQuietly(value, DateUtil.DATE_TIME_PATTERN);
    }

}
