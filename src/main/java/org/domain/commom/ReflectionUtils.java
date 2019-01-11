package org.domain.commom;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {
	// Utils
	public static boolean haveMethodName(Class<?> objectClass, String name) {
		if (objectClass == null) {
			return false;
		}
	
		boolean found = false;
		Method[] methods = objectClass.getDeclaredMethods();
	
		for (Method method : methods) {
			if (method.getName().equals(name)) {
				found = true;
			}
		}
	
		return found;
	}

	// Utils
	public static Object readField(Object obj, String name) {
		if (obj == null) {
			return null;
		}
	
		Object data;
	
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			data = field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			data = null;
		}
	
		return data;
	}

	// Utils
	public static void writeField(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
