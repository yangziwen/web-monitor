package io.github.yangziwen.webmonitor.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {

	private ReflectionUtil() {}

	@SuppressWarnings("rawtypes")
	public static Type[] getSuperClassGenericTypes(Class clazz) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			throw new IllegalStateException("Seems no valid ParameterizedType exist!");
		}
		return ((ParameterizedType) genType).getActualTypeArguments();
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getSuperClassGenericType(Class<?> clazz, int index) {
		Type[] params = getSuperClassGenericTypes(clazz);
		if (index < 0 || index >= params.length) {
			throw new IllegalStateException("Seems no valid ParameterizedType exist at the position of " + index + "!");
		}
		return (Class<T>) params[index];
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object entity, Field field) {
		try {
			field.setAccessible(true);
			return (T) field.get(entity);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> void setFieldValue(Object entity, Field field, T value) {
		try {
			field.setAccessible(true);
			field.set(entity, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
