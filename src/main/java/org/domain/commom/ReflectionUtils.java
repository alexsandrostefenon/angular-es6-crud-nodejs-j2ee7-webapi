package org.domain.commom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	// Utils
	public static Object loadObjectFromJson(Class<?> objectClass, InputStream inputStream) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		byte[] buffer = new byte[100*1024];
		int partialRead = 0;
		int totalRead = 0;
	
		do {
			totalRead += partialRead;
			partialRead = inputStream.read(buffer, totalRead, buffer.length - partialRead);
		} while (partialRead > 0);
	
	
		if (totalRead >= buffer.length) {
			throw new Exception("loadObjectFromJson : invalid data size");
		}
	
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, totalRead);
		inputStream = byteArrayInputStream;
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		Object obj;
	
		try {
			obj = mapper.readValue(inputStreamReader, objectClass);
		} catch (Exception e) {
			String string = new String(buffer, 0, totalRead);
			throw new Exception(String.format("loadObjectFromJson : %s : %s", e.getMessage(), string));
		}
	
		return obj;
	}

}
