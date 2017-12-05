package io.github.yangziwen.webmonitor.repository.base;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Condition {

    private String key;

    private String oper;

    private String placeholder;

    private String fullKey;

    public String toSql() {
        return key + oper + placeholder;
    }

    public static <T> Condition parse(String fullKey, ModelMapping<T> mapping) {
        if (StringUtils.isBlank(fullKey)) {
            return null;
        }
        int index = fullKey.lastIndexOf(Operator.__);
        if (index == -1) {
            String key = fullKey;
            String stmt = mapping.getColumnByField(key);
            if (StringUtils.isBlank(stmt)) {
                stmt = key;
            }
            return Operator.eq.buildCondition(stmt, fullKey);
        }
        String key = fullKey.substring(0, index);
        String oper = fullKey.substring(index + 2);
        String stmt = mapping.getColumnByField(key);
        if (StringUtils.isBlank(stmt)) {
            stmt = key;
        }
        return Operator.valueOf(oper).buildCondition(stmt, fullKey);
    }

}
