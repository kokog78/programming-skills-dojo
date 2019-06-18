package hu.ibello.utils;

import java.lang.reflect.Field;

public class ClassUtils {

	public static void setValue(Object object, Field field, Object value) throws Exception {
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			field.set(object, value);
		} finally {
			if (!accessible) {
				field.setAccessible(accessible);
			}
		}
	}
	
	public static void setValue(Object object, String fieldName, Object value) throws Exception {
		setValue(object, getField(object.getClass(), fieldName), value);
	}
	
	public static Object getValue(Object object, Field field) throws Exception {
		Object result = null;
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			result = field.get(object);
		} finally {
			if (!accessible) {
				field.setAccessible(accessible);
			}
		}
		return result;
	}
	
	public static Object getValue(Object object, String fieldName) throws Exception {
		return getValue(object, getField(object.getClass(), fieldName));
	}
	
	private static Field getField(Class<?> type, String name) throws Exception {
		try {
			return type.getDeclaredField(name);
		} catch (NoSuchFieldException ex) {
			type = type.getSuperclass();
			if (type!=null) {
				return getField(type, name);
			} else {
				throw ex;
			}
		}
	}
}
