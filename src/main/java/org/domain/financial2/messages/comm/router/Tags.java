package org.domain.financial2.messages.comm.router;

import java.util.ArrayList;

import org.domain.commom.Utils;

public class Tags {
	StringBuilder auxAlign;
	ArrayList<String> keys;
	ArrayList<String> values;

	public static String getValue(String tag, String splitter, String data) {
		String ret = null;
		
		int posIni = data.indexOf(tag);
		
		if (posIni >= 0) {
			posIni += tag.length();
			int posEnd = posIni;
			
			if (posEnd < data.length()) {
				posEnd = data.indexOf(splitter, posEnd);
			}
			
			if (posEnd >= 0) {
				ret = data.substring(posIni, posEnd);
			} else {
				ret = data.substring(posIni);
			}
		}
		
		return ret;
	}
	
	
	public static Tags parseTLV(String buffer, int maxNumElements, int tagSize, int lengthSize) {
		Tags tlv = new Tags();
		tlv.keys = new ArrayList<String>(maxNumElements);
		tlv.values = new ArrayList<String>(maxNumElements);
		tlv.auxAlign = new StringBuilder(1024);
		int offset = 0;

		while (offset < buffer.length()) {
			String tag = buffer.substring(offset, offset + tagSize);
			offset += tagSize;
			String strSize = buffer.substring(offset, offset + lengthSize);
			offset += lengthSize;
			int valueSize = Integer.parseInt(strSize);
			String value = buffer.substring(offset, offset + valueSize);
			offset += valueSize;
			tlv.keys.add(tag);
			tlv.values.add(value);
		}

		return tlv;
	}

	public String get(String tag) {
		String value = null;

		for (int i = 0; i < this.keys.size(); i++) {
			String key = this.keys.get(i);

			if (key.equals(tag)) {
				value = this.values.get(i);
				break;
			}
		}

		return value;
	}

	public String get(int index) {
		String value = this.values.get(index);
		return value;
	}

	public String getLeftAlign(String tag, char ch, int lenght) {
		String value = get(tag);
		value = Utils.getLeftAlign(this.auxAlign, value, ch, lenght);
		return value;
	}

	public String getRightAlign(String tag, char ch, int lenght) {
		String value = get(tag);
		value = Utils.getRightAlign(this.auxAlign, value, ch, lenght);
		return value;
	}

	public String getLeftAlign(int index, char ch, int lenght) {
		String value = get(index);
		value = Utils.getLeftAlign(this.auxAlign, value, ch, lenght);
		return value;
	}

	public String getRightAlign(int index, char ch, int lenght) {
		String value = get(index);
		value = Utils.getRightAlign(this.auxAlign, value, ch, lenght);
		return value;
	}
}
