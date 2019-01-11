package org.domain.crud.admin;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.persistence.Converter;

import java.io.StringReader;
import java.lang.reflect.Method;

@Converter(autoApply = false)
public class JsonConverter implements javax.persistence.AttributeConverter<JsonStructure, Object> {
	static Class<? extends Object> dbObjectClass = null;

	@Override
	public Object convertToDatabaseColumn(JsonStructure objectValue) {
		Object obj = null;

		if (objectValue != null) {
	    	try {
		    	String str = objectValue.toString();

	    		if (JsonConverter.dbObjectClass == String.class) {
			    	obj = str;
	    		} else if (JsonConverter.dbObjectClass.getName().equals("org.postgresql.util.PGobject")) {
			    	obj = JsonConverter.dbObjectClass.getConstructor().newInstance();
			    	Method setType = JsonConverter.dbObjectClass.getDeclaredMethod("setType", String.class);
			    	Method setValue = JsonConverter.dbObjectClass.getDeclaredMethod("setValue", String.class);
			    	setType.invoke(obj, "jsonb");
			    	setValue.invoke(obj, str);
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        return obj;
	}

	@Override
	public JsonStructure convertToEntityAttribute(Object dataValue) {
		JsonStructure object = null;

		if (dataValue != null) {
	    	if (JsonConverter.dbObjectClass == null) {
	    		JsonConverter.dbObjectClass = dataValue.getClass();
	    	}

        	String str = dataValue.toString();

        	if (str != null) {
	        	JsonReader jsonReader = Json.createReader(new StringReader(str));
	        	object = jsonReader.read();
	        	jsonReader.close();
        	}
		}

    	return object;
	}
}
