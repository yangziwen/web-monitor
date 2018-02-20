package io.github.yangziwen.webmonitor.command.converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.beust.jcommander.IStringConverter;

public class TimeIntervalConverter implements IStringConverter<Long> {

    @Override
    public Long convert(String value) {

        if (value.chars().allMatch(Character::isDigit)) {
            return NumberUtils.createLong(value);
        }

        @SuppressWarnings("serial")
        Map<String, Long> timeUnitMap = new HashMap<String, Long>() {{
            put("s", DateUtils.MILLIS_PER_SECOND);
            put("m", DateUtils.MILLIS_PER_MINUTE);
            put("h", DateUtils.MILLIS_PER_HOUR);
            put("d", DateUtils.MILLIS_PER_DAY);
        }};

        return Arrays.stream(value.split("\\s"))
                .mapToLong(v -> {
                    if (v.chars().allMatch(Character::isDigit)) {
                        return NumberUtils.createLong(v);
                    }
                    String timeUnit = v.substring(v.length() - 1);
                    Long num = NumberUtils.createLong(v.substring(0, v.length() - 1));
                    return num * timeUnitMap.get(timeUnit);
                })
                .reduce(0L, (v1, v2) -> v1 + v2);
    }

}
