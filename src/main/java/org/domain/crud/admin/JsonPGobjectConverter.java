package org.domain.crud.admin;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.persistence.Converter;

import java.io.StringReader;

import org.postgresql.util.PGobject;

@Converter(autoApply = false)
public class JsonPGobjectConverter implements javax.persistence.AttributeConverter<JsonStructure, PGobject> {

	@Override
	public PGobject convertToDatabaseColumn(JsonStructure objectValue) {
		PGobject obj = null;

		if (objectValue != null) {
	    	try {
		    	String str = objectValue.toString();

		    	obj = new PGobject();
		    	obj.setType("jsonb");
		    	obj.setValue(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        return obj;
	}

	@Override
	public JsonStructure convertToEntityAttribute(PGobject dataValue) {
		JsonStructure object = null;

		if (dataValue != null) {
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
