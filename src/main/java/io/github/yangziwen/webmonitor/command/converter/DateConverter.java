package io.github.yangziwen.webmonitor.command.converter;

import java.util.Date;

import com.beust.jcommander.IStringConverter;

import io.github.yangziwen.webmonitor.util.DateUtil;

public class DateConverter implements IStringConverter<Date> {

    @Override
    public Date convert(String value) {
        return DateUtil.parseDateQuietly(value, DateUtil.DATE_PATTERN);
    }

}
