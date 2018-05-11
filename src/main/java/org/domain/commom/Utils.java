package org.domain.commom;

import java.util.ArrayList;
import java.util.Calendar;

public class Utils {
	public static enum DataAlign {
		NONE, ZERO_LEFT, ZERO_RIGHT, SPACE_LEFT, SPACE_RIGHT
	}
	public static enum CommRequestDirection {
		CLIENT_TO_SERVER, SERVER_TO_CLIENT, BIDIRECIONAL
	}
	//
	private static boolean isHex(char ch, boolean enableUpper, boolean enableLower) {
		boolean ret = false;

		if (ch >= '0' && ch <= '9') {
			ret = true;
		} else if (enableUpper && ch >= 'A' && ch <= 'F') {
			ret = true;
		} else if (enableLower && ch >= 'a' && ch <= 'f') {
			ret = true;
		}

		return ret;
	}
	// unused
	public static boolean isHex(CharSequence str, int posIni, int posEnd, boolean enableUpper, boolean enableLower) {
		if (str == null || str.length() == 0) {
			return false;
		}

		boolean ret = true;

		for (int i = posIni; i < posEnd; i++) {
			char ch = str.charAt(i);
			
			if (isHex(ch, enableUpper, enableLower) == false) {
				ret = false;
				break;
			}
		}

		return ret;
	}
	// Message, Converter
	public static boolean isUnsignedInteger(String str) {
		if (str == null || str.length() == 0 || str.length() > 18) {
			return false;
		}

		boolean ret = false;

		int i = 0;

		if (str.charAt(i) == '-') {
			i++;
		}

		while (i < str.length()) {
			char ch = str.charAt(i++);

			if (ch >= '0' && ch <= '9') {
				ret = true;
				continue;
			}

			ret = false;
			break;
		}

		return ret;
	}
	// Connector, Emulator, EmulatorSystems
	public static String getYYYYMMDDhhmmss() {
		Calendar rightNow = Calendar.getInstance();
		int yyyy = rightNow.get(Calendar.YEAR);
		int MM = rightNow.get(Calendar.MONTH);
		int dd = rightNow.get(Calendar.DAY_OF_MONTH);
		int hh = rightNow.get(Calendar.HOUR_OF_DAY);
		int mm = rightNow.get(Calendar.MINUTE);
		int ss = rightNow.get(Calendar.SECOND);
		String ret = String.format("%04d%02d%02d%02d%02d%02d", yyyy, MM+1, dd, hh, mm, ss);
		return ret;
	}
	// Converter, ConverterLogger
	public static int findInList(String[] list, String item) {
		int index = -1;
		
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(item)) {
				index = i;
				break;
			}
		}
		
		return index;
	}

	public static int findInList(ArrayList<String> list, String item) {
		int index = -1;
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(item)) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	static boolean isPrintable(char ch) {
		boolean ret = false;

		if (ch >= 32 && ch <= 122) {
			ret = true;
		}
		
		if (ch == '\'') {
			ret = false;
		}

		if (ch == '|') {
			ret = true;
		}

		return ret;
	}

	public static boolean checkValue(String data, String value) {
		boolean ret = data != null && data.equals(value);
		return ret;
	}
	// unused
	public static boolean isDecimal(String str, int offset, int size) {
		boolean ret = true;
	
		for (int i = 0; i < size; i++) {
			char ch = str.charAt(offset+i);
	
			if (ch >= '0' && ch <= '9') {
				continue;
			}
	
			ret = false;
			break;
		}
	
		return ret;
	}
	// Message, TrCorban, Tags
	/**
	 * Insert or remove alignment characters
	 * 
	 * @param bufferAux auxiliar buffer for performance convenience
	 * @param value  object for action
	 * @param ch     character of aligment escope
	 * @param lenght size of alignment
	 * @return       new algigmented valued
	 */
	public static String getLeftAlign(StringBuilder bufferAux, String value, char ch, int lenght) {
		if (value != null) {
			int len = value.length();
			int diff = lenght - len;
	
			if (diff > 0) {
				bufferAux.setLength(0);
	
				for (int i = 0; i < diff; i++) {
					bufferAux.append(ch);
				}
	
				bufferAux.append(value);
				value = bufferAux.toString();
			} else if (diff < 0) {
				diff *= -1;
				int i = 0;
				
				while (i < diff && value.charAt(i) == ch) {
					i++;
				}
				
				value = value.substring(i);
			}
		}
	
		return value;
	}
	// Message, TrCorban, Tags
	public static String getRightAlign(StringBuilder bufferAux, String value, char ch, int lenght) {
		if (value != null) {
			bufferAux.setLength(0);
			bufferAux.append(value);
			int len = value.length();
			int diff = lenght - len;
	
			if (diff > 0) {
				for (int i = 0; i < diff; i++) {
					bufferAux.append(ch);
				}
	
				value = bufferAux.toString();
			}
		}
	
		return value;
	}
	// TrCorban
	public static String removeLeftAlign(String str, char ch) {
		if (str.charAt(0) == ch) {
			int len = str.length();
			int pos = 0;
	
			while (++pos < len) {
				char _ch = str.charAt(pos);
	
				if (_ch != ch) {
					str = str.substring(pos, len);
					break;
				}
			}
		}
	
		return str;
	}
	public static int DATA_TYPE_DECIMAL = 1;
	public static int DATA_TYPE_HEX = 2 | 1;
	public static int DATA_TYPE_ALPHA = 4 | 2;
	public static int DATA_TYPE_SPECIAL = 8;
	public static int DATA_TYPE_MASK = 16;
	public static boolean isAlpha(char ch) {
		boolean ret = false;
	
		if (ch >= 'A' && ch <= 'Z') {
			ret = true;
		} else if (ch >= 'a' && ch <= 'z') {
			ret = true;
		}
	
		return ret;
	}
	public static int checkContentType(int dataType, String data) {
		int pos = -1;
		boolean mayBeNumberDecimal = (dataType & DATA_TYPE_DECIMAL) > 0;
		boolean mayBeNumberHex = (dataType & DATA_TYPE_HEX) > 0;
		boolean mayBeAlpha = (dataType & DATA_TYPE_ALPHA) > 0;
		boolean mayBeSpecial = (dataType & DATA_TYPE_SPECIAL) > 0;
		boolean mayBeMask = (dataType & DATA_TYPE_MASK) > 0;
		// varre cada caracter da string, cada letra tem que encaixar em um grupo válido
		for (int i = 0; i < data.length(); i++) {
			char ch = data.charAt(i);
	
			if (mayBeNumberDecimal && ch >= '0' && ch <= '9') {
				continue;
			}
	
			if (mayBeNumberHex && ch >= '0' && ch <= '9') {
				continue;
			}
	
			if (mayBeNumberHex && ch >= 'A' && ch <= 'F') {
				continue;
			}
	
			if (mayBeNumberHex && ch >= 'a' && ch <= 'f') {
				continue;
			}
	
			if (mayBeAlpha && Utils.isAlpha(ch)) {
				continue;
			}
	
			if (mayBeSpecial && (ch >= '0' && ch <= '9') == false && Utils.isAlpha(ch) == false) {
				continue;
			}
	
			if (mayBeMask && ch == '*') {
				continue;
			}
	
			pos = i;
			break;
		}
		
		return pos;
	}
	public static String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
}
