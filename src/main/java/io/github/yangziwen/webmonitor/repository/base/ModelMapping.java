package io.github.yangziwen.webmonitor.repository.base;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

public class ModelMapping<E> {

	protected Class<E> clazz;

	protected final String table;

	protected final List<Field> fields;

	protected final Field idField;

	protected final Map<String, String> fieldColumnMapping;

	protected final E[] emptyArray;

	@SuppressWarnings("unchecked")
	public ModelMapping(Class<E> clazz) {
		this.clazz = clazz;
		this.table = getTable(clazz);
		this.fields = getAnnotatedFields(clazz);
		this.fieldColumnMapping = createFieldColumnMapping(fields);
		this.emptyArray = (E[]) Array.newInstance(clazz, 0);
		Field idField = null;
		for (Field field : fields) {
			if (field.isAnnotationPresent(Id.class)) {
				idField = field;
				break;
			}
		}
		this.idField = idField;
	}

	public String getColumnByField(String field) {
		return fieldColumnMapping.get(field);
	}

	public String getTable(Map<String, Object> params) {
		return table;
	}

	public List<Field> getFields() {
		return Collections.unmodifiableList(fields);
	}

	public Map<String, String> getFieldColumnMapping() {
		return new HashMap<String, String>(fieldColumnMapping);
	}

	public Map<String, String> getFieldColumnMappingWithoutIdField() {
		Map<String, String> mapping = getFieldColumnMapping();
		String idFieldName = getIdFieldName();
		if (StringUtils.isNotBlank(idFieldName)) {
			mapping.remove(idFieldName);
		}
		return mapping;
	}

	public E[] emptyArray() {
		return emptyArray;
	}

	public Field getIdField() {
		return idField;
	}

	public String getIdFieldName() {
		if (getIdField() == null) {
			return null;
		}
		return getIdField().getName();
	}

	public String getIdColumn() {
		return fieldColumnMapping.get(getIdFieldName());
	}

	public List<String> getSelectStmts() {
		List<String> list = new ArrayList<String>();
		for (Entry<String, String> entry : fieldColumnMapping.entrySet()) {
			list.add(entry.getValue() + " AS " + entry.getKey());
		}
		return list;
	}

	public static String getTable(Class<?> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null && StringUtils.isNotBlank(table.name())) {
			return table.name();
		}
		return camelToUnderscore(clazz.getSimpleName());
	}

	public static List<Field> getAnnotatedFields(Class<?> clazz) {
		List<Field> list = new ArrayList<Field>();
		if (clazz.getSuperclass() != Object.class) {
			list.addAll(getAnnotatedFields(clazz.getSuperclass()));
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(Column.class) == null) {
				continue;
			}
			list.add(field);
		}
		return Collections.unmodifiableList(list);
	}

	public static Map<String, String> createFieldColumnMapping(List<Field> fields) {
		Map<String, String> mapping = new LinkedHashMap<String, String>();
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}
			String columnName = StringUtils.isNotBlank(column.name())
					? column.name()
					: camelToUnderscore(field.getName());
			mapping.put(field.getName(), columnName);
		}
		return Collections.unmodifiableMap(mapping);
	}

	public static String camelToUnderscore(String str) {
		return StringUtils.isBlank(str) ? "" : str.replaceAll("([^\\sA-Z])([A-Z])", "$1_$2").toLowerCase();
	}

	public static <T> ModelMapping<T> newInstance(Class<T> clazz) {
		return new ModelMapping<T>(clazz);
	}

}
