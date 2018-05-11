package org.domain.financial2.messages.comm.router.middleware;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class DinamicMenu {
	private ArrayList<String> names;
	private ArrayList<String> values;
	// auxilia na manipulação de strings no parse e setData
//	StringBuilder auxParse;
//	StringBuilder auxSetData;

	public void dispose() {
//		this.auxParse = null;
//		this.auxSetData = null;
		this.names.clear();
		this.names = null;
		this.values.clear();
		this.values = null;
	}

	public DinamicMenu() {
		this.names = new ArrayList<String>(128);
		this.values = new ArrayList<String>(128);
//		this.auxParse = new StringBuilder(512);
//		this.auxSetData = new StringBuilder(512);
	}
	
	public DinamicMenu(DinamicMenu c) {
		this.names = new ArrayList<String>(128);
		this.values = new ArrayList<String>(128);
//		this.auxParse = new StringBuilder(512);
//		this.auxSetData = new StringBuilder(512);
		this.names.addAll(c.names);
		this.values.addAll(c.values);
	}

	private int fieldIndex(String field) {
		int ret = -1;
		
		for (int i = 0; i < this.names.size(); i++) {
			String name = this.names.get(i);
			
			if (name.equals(field)) {
				ret = i;
				break;
			}
		}
		
		return ret;
	}
	
	public String getData(String field) {
		int index = fieldIndex(field);
		
		if (index < 0) {
			throw new InvalidParameterException();
		}
		
		return this.values.get(index);
	}

	public void setData(String field, String str) {
		if (field == null || field.length() == 0) {
			return;
		}
		
		int index = fieldIndex(field);
		
		if (index < 0) {
			this.names.add(field);
			this.values.add(str);
		} else {
			this.names.set(index, field);
			this.values.set(index, str);
		}
	}

	public void clear() {
		this.names.clear();
		this.values.clear();
	}

	public void parse(String str) {
		if (str == null) {
			throw new InvalidParameterException();
		}
		
		clear();
		// @0019@0035@0166@00971000@00412788827698@00412788827698@
		String[] params = str.split("@");
		
		for (int i = 0; i < params.length; i++) {
			str = params[i];
			
			if (str.length() > 3) {
				String field = str.substring(0, 4);
				str = str.substring(4);
				setData(field, str);
			} else if (str.length() != 0) {
				throw new InvalidParameterException();
			}
		}
	}

	public void generateMessage(StringBuilder buffer) {
		buffer.setLength(0);
		
		for (int i = 0; i < this.names.size(); i++) {
			String name = this.names.get(i);
			String value = this.values.get(i);
			buffer.append("@");
			buffer.append(name);
			
			if (value != null) {
				buffer.append(value);
			}
		}
	}
	
	public String getMessage() {
		StringBuilder buffer = new StringBuilder(256);
		generateMessage(buffer);
		return buffer.toString();
	}
}
