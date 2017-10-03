package io.github.yangziwen.webmonitor.repository.base;

import org.apache.commons.lang3.StringUtils;

public class Condition {

    private String key;

    private String oper;

    private String placeholder;

    private String fullKey;

    public Condition(String key, String oper, String placeholder, String fullKey) {
        this.key = key;
        this.oper = oper;
        this.placeholder = placeholder;
        this.fullKey = fullKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getFullKey() {
        return fullKey;
    }

    public void setFullKey(String fullKey) {
        this.fullKey = fullKey;
    }

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
