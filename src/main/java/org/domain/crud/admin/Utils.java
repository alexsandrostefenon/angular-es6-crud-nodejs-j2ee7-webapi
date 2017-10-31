package org.domain.crud.admin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.fasterxml.jackson.databind.ObjectMapper;

//import com.google.gson.Gson;

public class Utils {
	// Message
	// Copia o valor inteiro para um "char*" e preenche com zeros a esquerda
	public static void intToStr(int value, StringBuilder buffer, int size) {
		String fmt = String.format("%%0%dd", size);
		buffer.append(String.format(fmt, value));
	}
	// Message
	public static int parseInt(String data, int offset, int length) {
		int sum = 0;
		int pos = offset;

		for (int i = 0; i < length; i++) {
			char ch = data.charAt(pos++);
			int val = ch;

			if (val < '0' || val > '9') {
				throw new NumberFormatException();
			}

			sum *= 10;
			sum += val - '0';
		}

		return sum;
	}
	// Message
	public static void unCompress(StringBuilder buffer, String src) {
		for (int i = 0; i < src.length(); i++) {
			int c;
			c = src.charAt(i);
			c = (c & 0xF0) >> 4;
			c += c >= 0 && c <= 9 ? 0x30 : 0x37;
			buffer.append((char)c);
			c = (src.charAt(i) & 0x0F);
			c += c >= 0 && c <= 9 ? 0x30 : 0x37;
			buffer.append((char)c);
		}
	}
	// unused
	public static void unCompress(byte[] bytes, int offset, int length) {
		for (int i = length-1; i >= 0; i--) {
    	int unsignedByte = convertToUnsignedValue(bytes[offset + i]);
			int low = unsignedByte & 0x0000000f;
			int hight = unsignedByte >> 4;
			char chHight = IntToHexAsciiChar(hight);
			char chLow = IntToHexAsciiChar(low);
			bytes[offset + 2*i] = (byte)chHight;
			bytes[offset + 2*i+1] = (byte)chLow;
		}
	}

	public static String xorHexAscii(String strUpperHex, String strLowerHex) {
		if (strUpperHex == null || strLowerHex == null) {
			return null;
		}

		if (strUpperHex.length() != strLowerHex.length()) {
			return null;
		}

		if (strUpperHex.length() == 0) {
			return null;
		}

		int len = strUpperHex.length();

		if (isHex(strUpperHex, 0, len, true, true) == false) {
			return null;
		}

		if (isHex(strLowerHex, 0, len, true, true) == false) {
			return null;
		}

		StringBuilder buffer = new StringBuilder(len);

		for (int i = 0; i < len; i++) {
			char chUpper = strUpperHex.charAt(i);
			char chLower = strLowerHex.charAt(i);
			int valUpper = Utils.hexAsciiToInt(chUpper, true, true, '0', '0');
			int valLower = Utils.hexAsciiToInt(chLower, true, true, '0', '0');
			int valXor = valUpper ^ valLower;
			char chXor = Utils.IntToHexAsciiChar(valXor);
			buffer.append(chXor);
		}

		return buffer.toString();
	}
	// Message, hexToInt
	// Converte uma letra hexadecimal em seu valor inteiro
 	public static int hexAsciiToInt(char ch, boolean enableUpper, boolean enableLower, char escapeCharIn, char escapeCharOut) {
		int ret = ch;

		if (ch >= '0' && ch <= '9') {
			ret -= 0x30;
		} else if (enableUpper && ch >= 'A' && ch <= 'F') {
			ret -= 0x37;
		} else if (enableLower && ch >= 'a' && ch <= 'f') {
			ret -= 0x37;
		} else if (ch == escapeCharIn) {
			ch = escapeCharOut;
			ret = hexAsciiToInt(ch, enableUpper, enableLower, '0', '0');
		} else {
			ret = -1;
		}

		return ret;
	}
	// Message, appendAsciiHexToBinary, AsciiHexToBinary
	// Converte uma sequencia de até quatro letras hexadecimais em um valor inteiro
	public static int hexToInt(CharSequence data, int offset, int length, char escapeCharIn, char escapeCharOut) {
		if (length <= 0 || length > 2*4) {
			throw new InvalidParameterException();
		}

		int bits = 0;
		int pos = offset;
		int posEnd = offset + length;

		while (pos < posEnd && pos < data.length()) {
			char ch = data.charAt(pos);
			int val = hexAsciiToInt(ch, true, true, escapeCharIn, escapeCharOut);

			if (val >= 0) {
				bits <<= 4;
				bits |= val;
				pos++;
			} else {
				throw new InvalidParameterException(String.format("hexToInt : must by val >= 0 (val : %d), ch = %s, pos = %d, data = %s", val, ch, pos, data));
			}
		}

		while (pos < posEnd) {
			bits <<= 4;
			pos++;
		}

		return bits;
	}
	// Message
	public static void appendAsciiHexToBinary(StringBuilder buffer, CharSequence src, int offset, int srcLength, int bytesByValue, char escapeCharIn, char escapeCharOut) {
		int posEnd = offset + srcLength;

		for (int i = offset; i < posEnd; i += bytesByValue) {
			char ch;

			if (bytesByValue == 2) {
				ch = (char) hexToInt(src, i, 2, escapeCharIn, escapeCharOut);
				buffer.append(ch);
			} else if (bytesByValue == 4) {
				ch = (char) hexToInt(src, i, 2, escapeCharIn, escapeCharOut);
				buffer.append(ch);
				ch = (char) hexToInt(src, i+2, 2, escapeCharIn, escapeCharOut);
				buffer.append(ch);
			} else {
				throw new InvalidParameterException();
			}
		}
	}
	// Message
	public static void appendAsciiHexToBinary(StringBuilder buffer, CharSequence src, int srcLength, int bytesByValue, boolean oddRightAlign, char escapeCharIn, char escapeCharOut) {
		if ((srcLength % 2 != 0) && oddRightAlign) {
			StringBuilder aux = new StringBuilder(srcLength + 1);
			aux.append('0');
			aux.append(src);
			src = aux;
			srcLength++;
		}

		appendAsciiHexToBinary(buffer, src, 0, srcLength, bytesByValue, escapeCharIn, escapeCharOut);
	}
	// Message, AddAsciiHexFromUnsignedByte
	public static char IntToHexAsciiChar(int value) {
		if (value < 0 || value > 15) {
			throw new InvalidParameterException(String.format("IntToHexChar : only alowed 0 -> 15 (value = %s)", value));
		}

		char ch = 0x30;

		if (value > 9) {
			ch = 0x37;
		}

		return (char)(ch + value);
	}
	// checkContentType
	public static boolean isAlpha(char ch) {
		boolean ret = false;

		if (ch >= 'A' && ch <= 'Z') {
			ret = true;
		} else if (ch >= 'a' && ch <= 'z') {
			ret = true;
		}

		return ret;
	}
	// unused
	public static boolean isPrintable(char ch) {
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
	//
	public static boolean isHex(char ch, boolean enableUpper, boolean enableLower) {
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
	// MessageConf
	public static ArrayList<String> getFileLines(String filename) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String str = input.readLine();

		while (str != null) {
			list.add(str);
			str = input.readLine();
		}

		input.close();
		return list;
	}
	// Message, MessageConf
	public static int parseUnsignedInt(String str, int maxDigits, int failValue) {
		int ret = 0;
		int len = str.length();
		int i = 0;

		while (i < len && i < maxDigits) {
			char ch = str.charAt(i);

			if (ch < '0' || ch > '9') {
				break;
			}

			ret *= 10;
			ch -= '0';
			ret += ch;
			i++;
		}

		if (i == 0) {
			ret = failValue;
		}

		return ret;
	}
	// Message, TrCorban, Tags
	/**
	 * Insert or remove alignment characters
	 *
	 * @param buffer auxiliar buffer for performance convenience
	 * @param value  object for action
	 * @param ch     character of aligment escope
	 * @param lenght size of alignment
	 * @return       new algigmented valued
	 */
	public static String getLeftAlign(StringBuilder buffer, String value, char ch, int lenght) {
		if (value != null) {
			int len = value.length();
			int diff = lenght - len;

			if (diff > 0) {
				buffer.setLength(0);

				for (int i = 0; i < diff; i++) {
					buffer.append(ch);
				}

				buffer.append(value);
				value = buffer.toString();
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
	public static String getRightAlign(StringBuilder buffer, String value, char ch, int lenght) {
		if (value != null) {
			buffer.setLength(0);
			buffer.append(value);
			int len = value.length();
			int diff = lenght - len;

			if (diff > 0) {
				for (int i = 0; i < diff; i++) {
					buffer.append(ch);
				}

				value = buffer.toString();
			}
		}

		return value;
	}
	// unused
	public static String removeRightAlign(String str, char ch) {
		int pos = str.length() - 1;

		if (str.charAt(pos) == ch) {
			while (--pos > 0) {
				char _ch = str.charAt(pos);

				if (_ch != ch) {
					str = str.substring(0, pos+1);
					break;
				}
			}
		}

		return str;
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
	// Message, Converter
	public static int checkContentType(String data, boolean mayBeAlpha, boolean mayBeNumberDecimal, boolean mayBeNumberHex, boolean mayBeSpecial, boolean mayBeMask) {
		int pos = -1;

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
	// Image, AddAsciiHex
	public static int convertToUnsignedValue(byte byteVal) {
		int ret = byteVal;

		if (ret < 0) {
			ret += 256;
		}

		return ret;
	}
	// Image, AddAsciiHex
	public static int convertToUnsignedValue(byte byteVal, int numBits) {
		int ret = convertToUnsignedValue(byteVal);

		while (numBits++ < 8) {
			ret >>= 1;
		}

		return ret;
	}
	// Image
	public static byte convertToUnsignedByte(int val, int numBits) {
		if (val > 127) {
			val -= 256;
		}

		if (numBits < 8) {
			while (numBits++ < 8) {
				val <<= 1;
			}

			if (val == 0x000000FE) {
//				val = 0x000000FF;
			}
		}

		return (byte) val;
	}
	// Image, AddAsciiHex
	public static void AddAsciiHexFromUnsignedByte(StringBuffer buffer, int unsignedByte) {
		if (unsignedByte < 0 || unsignedByte > 255) {
			throw new InvalidParameterException(String.format(
					"AddAsciiHexFromUnsignedByte : only ISO8589-1 alowed (ch = %s)",
					unsignedByte));
		}

		int low = unsignedByte & 0x0000000f;
		unsignedByte >>= 4;
		int hight = unsignedByte;
		buffer.append(IntToHexAsciiChar(hight));
		buffer.append(IntToHexAsciiChar(low));
	}
	// unused
	public static void AddAsciiHex(StringBuffer buffer, byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
        	int unsignedByte = convertToUnsignedValue(bytes[i], 8);
        	AddAsciiHexFromUnsignedByte(buffer, unsignedByte);
        }
	}
	// unused
	public static void asciiToHex(CharSequence bufferIn, StringBuilder bufferOut) {
		bufferOut.setLength(0);

		for (int i = 0; i < bufferIn.length(); i++) {
			char ch = bufferIn.charAt(i);

			if (ch < 127) {
				bufferOut.append(String.format("%02X", (int)ch));
			} else {
				bufferOut.append("??");
			}
		}
	}
	// ImageClassificator
	public static void AsciiHexToBinary(byte[] data, CharSequence src) {
		int numChars = src.length();

		for (int i = 0, j = 0; i < numChars; i += 2, j++) {
			char ch;
			ch = (char) hexToInt(src, i, 2, '0', '0');
			data[j] = (byte) ch;
		}
	}
	// Converter
	public static String replaceSpliter(String str, String regionLimiter, char oldSpliter, char newSpliter) {
		StringBuilder buffer = new StringBuilder(str);
		boolean found;
		int posIni = 0;
		int posEnd;

		do {
			int posRegionIni = buffer.indexOf(regionLimiter, posIni);

			if (posRegionIni > 0) {
				posEnd = posRegionIni;
			} else {
				posEnd = buffer.length();
				posRegionIni = buffer.length();
			}

			for (int pos = posIni; pos < posEnd; pos++) {
				if (buffer.charAt(pos) == oldSpliter) {
					buffer.setCharAt(pos, newSpliter);
				}
			}

			int posRegionEnd = buffer.indexOf(regionLimiter, posRegionIni+1);

			if (posRegionEnd > 0) {
				found = true;
				posIni = posRegionEnd+1;
			} else {
				found = false;
			}
		} while (found);

		return buffer.toString();
	}

	public static String getStringContent(InputStream inputStream) throws IOException {
		StringBuffer buffer = new StringBuffer(10*1024);
		BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
		String str = input.readLine();

		while (str != null) {
			buffer.append(str);
			buffer.append('\n');
			str = input.readLine();
		}

		input.close();
		return buffer.toString();
	}
	// GatewayLogCarcker, ConnectorConf
	public static String getFileContent(String filename) throws IOException {
		StringBuffer buffer = new StringBuffer(10*1024);
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String str = input.readLine();

		while (str != null) {
			buffer.append(str);
			buffer.append('\n');
			str = input.readLine();
		}

		input.close();
		return buffer.toString();
	}
	// test : CommPosEthernet
	public static String getHexStr(byte[] bytes, int length) {
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < length; i++) {
			int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
			int parteBaixa = bytes[i] & 0xf;

			if (parteAlta == 0) {
				s.append('0');
			}

			s.append(Integer.toHexString(parteAlta | parteBaixa));
		}

		return s.toString().toUpperCase();
	}

	public static String getHexStr(String data) {
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < data.length(); i++) {
			char value = data.charAt(i);
			int parteAlta = ((value >> 4) & 0xf) << 4;
			int parteBaixa = value & 0xf;

			if (parteAlta == 0) {
				s.append('0');
			}

			s.append(Integer.toHexString(parteAlta | parteBaixa));
		}

		return s.toString().toUpperCase();
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

	public static boolean findInSet(Set<String> set, String item) {
		boolean found = false;

		for (String key : set) {
			if (key.equals(item)) {
				found = true;
				break;
			}
		}

		return found;
	}
	// Converter
	public static int getNumOcorrencies(String text, String value) {
		int count = 0;
		int pos = text.indexOf(value);

		while (pos >= 0) {
			count++;
			pos = text.indexOf(value, pos + value.length());
		}

		return count;
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

	public static String escapeBinaryData(String data, boolean replaceNonAscii, char replaceChar) {
		if (data == null) {
			return null;
		}

		if (data.length() == 0) {
			return data;
		}

		StringBuffer buffer = new StringBuffer(data.length() * 2);

		for (int i = 0; i < data.length(); i ++) {
			char ch = data.charAt(i);

			if (ch < 0 || ch > 255) {
				if (replaceNonAscii) {
					ch = replaceChar;
				} else {
					throw new InvalidParameterException(String.format("escapeBinaryData : only ISO8589-1 alowed (ch = %s = %d)", ch, (int) ch));
				}
			}

			if (Utils.isPrintable(ch)) {
				buffer.append(ch);
			} else {
				buffer.append("(0x");
				Utils.AddAsciiHexFromUnsignedByte(buffer, ch);
				buffer.append(')');
			}
		}

		return buffer.toString();
	}

	public static String unEscapeBinaryData(String data, String escapeLeft, String escapeRight, int maxEscapeSize) {
		if (data == null) {
			return null;
		}

		if (data.length() == 0 || escapeLeft == null || escapeRight == null) {
			return data;
		}

		int firstPos = data.indexOf(escapeLeft);

		if (firstPos < 0) {
			return data;
		}

		StringBuilder buffer = new StringBuilder(data.length());
		int offset = 0;
		int srcLen = data.length();

		while (offset < srcLen) {
			if (offset < srcLen - (escapeLeft.length() + 1 + escapeRight.length())) {
				if (data.startsWith(escapeLeft, offset)) {
					int posIni = offset + escapeLeft.length();
					int posEnd = data.indexOf(escapeRight, posIni);

					if (posEnd > posIni && posEnd <= posIni + maxEscapeSize) {
						if (Utils.isHex(data, posIni, posEnd, true, false)) {
							String hex = data.substring(posIni, posEnd);
							int val = Utils.hexToInt(hex, 0, posEnd-posIni, '0', '0');
							char ch = (char) val;
							buffer.append(ch);
//							System.out.printf("value = %d - %d - %d - %d - %d - %d\n", (int) ch, (int) b[0], val, (int) buffer.toString().getBytes()[0], (int) buffer.toString().charAt(0), (int) buffer.toString().charAt(1));
							offset = posEnd + escapeRight.length();
							continue;
						}
					}
				}
			}

			buffer.append(data.charAt(offset++));
		}

		return buffer.toString();
	}

	// transforma as cadeias de (hh) e (h) em ?
	public static String replaceBinarySkipes(String str, int offset, int size) {
//		isoStr = isoStr.replaceAll("\\(\\w\\w\\)", "?");
//		isoStr = isoStr.replaceAll("\\(\\w\\)", "?");
//		str = str.replace('\\', '?');
		StringBuilder buffer = new StringBuilder(str);
		int pos = offset;
		int posMax = offset + size;

		while (pos < buffer.length() && pos < posMax + 3) {
			pos  = buffer.indexOf("(", pos);

			if (pos >= offset && pos < posMax) {
				int posEnd = buffer.indexOf(")", pos);

				if (posEnd >= pos+2 && posEnd <= pos+3) {
					if (Utils.isHex(buffer, pos+1, posEnd, true, true)) {
						String content = buffer.substring(pos+1, posEnd);
						char ch = (char) Utils.hexToInt(content, 0, content.length(), '0', '0');
//						ch = '?';
						buffer.replace(pos, posEnd+1, Character.toString(ch));
					} else {
/*
						if (forceReplace) {
							buffer.insert(pos+1, replace);
						}
*/
//						System.out.printf("not hex : %s\n", content);
					}
				} else {
//					System.out.printf("missing valid close : %s\n", buffer.substring(pos+1, posEnd));
				}

				pos++;
			} else {
				break;
			}
		}

		return buffer.toString();
	}

	public static boolean checkValue(String data, String value) {
		boolean ret = data != null && data.equals(value);
		return ret;
	}

	public static String extractText(StringBuffer buffer, String strIni, String strEnd, boolean isIncludeStrIni, boolean isIncludeStrEnd, boolean isEraseStrIni, boolean isEraseStrEnd) {
		if (buffer == null || strIni == null || strEnd == null) {
			return null;
		}

		int posIniSel = buffer.indexOf(strIni);
		String str = null;

		if (posIniSel >= 0) {
			int posIniErase = posIniSel;
			int posEndSel = buffer.indexOf(strEnd, posIniSel + strIni.length());
			int posEndErase = posEndSel;

			if (posEndSel <= 0) {
				posEndErase = posEndSel = buffer.length();
				isEraseStrEnd = false;
			} else {
				if (isIncludeStrEnd) {
					posEndSel += strEnd.length();
				}
			}

			if (isIncludeStrIni == false) {
				posIniSel += strIni.length();
			}

			str = buffer.substring(posIniSel, posEndSel);

			if (isEraseStrIni == false) {
				posIniErase += strIni.length();
			}

			if (isEraseStrEnd) {
				posEndErase += strEnd.length();
			}

			buffer.delete(posIniErase, posEndErase);
		}

		return str;
	}

	public static File getFileFromResource(String destDir, String dirName, String name) throws Exception {
		String resourcePath = null;

		try {
			File file = new File(destDir  + File.separator + name);

			if (file.exists() == false) {
		    File dir = new File(destDir);

		    if (dir.exists() == false) {
		    	dir.mkdirs();
		    }

		    FileOutputStream fos = new FileOutputStream(file);

		    if (dirName != null && dirName.length() > 0) {
			    resourcePath = dirName + "/" + name;
		    } else {
			    resourcePath = name;
		    }

		    InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourcePath);
		    InputStreamReader inputStreamReader = new InputStreamReader(is);
				BufferedReader input = new BufferedReader(inputStreamReader);
				String str = input.readLine();

				while (str != null) {
					fos.write(str.getBytes());
					fos.write('\n');
					str = input.readLine();
				}

				input.close();
				fos.close();
			}

			return file;
		} catch (Exception e) {
			System.err.println(String.format("getFileFromResource(String destDir = %s, String dirName = %s, String name = %s), resourcePath = %s", destDir, dirName, name, resourcePath));
			throw e;
		}
	}

	public static File getDirFromResource(String destDir, String dirName) {
		File dirOut = new File(destDir  + File.separator + dirName);

		try {
			URL url = Utils.class.getClassLoader().getResource(dirName);

			if (url == null && dirOut.exists()) {
				return dirOut;
			}

	    URLConnection urlConnection = url.openConnection();
	    Object[] names = null;

	    if (urlConnection instanceof JarURLConnection) {
	    	int offset = dirName.length() + 1;
	    	ArrayList<String> list = new ArrayList<String>(255);
	    	JarURLConnection jarConnection = (JarURLConnection) urlConnection;
	      JarFile jarFile = jarConnection.getJarFile();

	      for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
	      	JarEntry entry = e.nextElement();

          if (!entry.isDirectory()) {
  	      	String name = entry.getName();

            if (name.startsWith(dirName)) {
            	list.add(name.substring(offset));
            }
          }
	      }

	      names = list.toArray();
	    } else if (urlConnection.toString().startsWith("org.jboss.vfs.protocol.VirtualFileURLConnection")) {
	    	// TODO : arrumar um jeito de carregar a lista de arquivos
	    } else {
				URI uri = url.toURI();
		    File dirIn = new File(uri);

		    if (dirIn.exists() && dirIn.isDirectory()) {
		    	names = dirIn.list();
		    }
	    }

	    if (names != null) {
		    for (Object name : names) {
		    	getFileFromResource(dirOut.getPath(), dirName, name.toString());
		    }
	    }
		} catch (Exception e) {
			// TODO: handle exception
			System.err.printf("getDirFromResource(destDir = %s, dirName = %s) : dirOut = %s\n", destDir, dirName, dirOut);
			e.printStackTrace();
		}

		return dirOut;
	}

	public static void main(String[] args) {
		String str = Utils.unEscapeBinaryData("<81>", "<", ">", 2);
		StringBuffer buffer = new StringBuffer(str.length());
		int val = str.charAt(0);
		System.out.println(val);
		String strHex = Utils.getHexStr(str);
		buffer.append(strHex);
		System.out.println(buffer);
	}

	public static String convertCaseUnderscoreToCamel(String str, boolean isFirstUpper) {
		String ret = "";
		boolean nextIsUpper = false;

		if (isFirstUpper == true) {
			nextIsUpper = true;
		} else if (str.length() > 1) {
			char ch = str.charAt(0);
			str = Character.toLowerCase(ch) + str.substring(1);
		}

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);

			if (nextIsUpper == true) {
				ch = Character.toUpperCase(ch);
				nextIsUpper = false;
			}

			if (ch == '_') {
				nextIsUpper = true;
			} else {
				ret = ret + ch;
			}
		}

		return ret;
	}

	public static boolean haveField(Class<?> objectClass, String name) {
		boolean ret = true;

		try {
			objectClass.getDeclaredField(name);
		} catch (NoSuchFieldException e1) {
			ret = false;
		}

		return ret;
	}

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

	public static void writeField(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object loadObjectFromJson(Class<?> objectClass, InputStream inputStream) throws Exception {
		// Gson gson = new Gson();
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
			// obj = gson.fromJson(inputStreamReader, objectClass);
			obj = mapper.readValue(inputStreamReader, objectClass);
		} catch (Exception e) {
			String string = new String(buffer, 0, totalRead);
			throw new Exception(String.format("loadObjectFromJson : %s : %s", e.getMessage(), string));
		}

		return obj;
	}

	public static String getClassFieldsNames(String className) {
		String ret;

		try {
			Class<?> restClass = Class.forName(className);
			Field[] fields = restClass.getDeclaredFields();
			StringBuilder stringBuilder = new StringBuilder(1024);

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];

				if (field.getName().equals("serialVersionUID") == false) {
					if (stringBuilder.length() > 0) {
						stringBuilder.append(',');
					}

					stringBuilder.append(field.getName());
				}
			}

			ret = stringBuilder.toString();
		} catch (ClassNotFoundException e) {
			ret = null;
		}

		return ret;
	}

	public static String getClassFieldsTypesJson(String className) {
		String ret;

		try {
			Class<?> restClass = Class.forName(className);
			StringBuilder stringBuilder = new StringBuilder(1024);
			stringBuilder.append("{");
			Field[] fields = restClass.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];

				if (field.getName().equals("serialVersionUID") == false) {
					if (stringBuilder.length() > 1) {
						stringBuilder.append(',');
					}

					String typeName = field.getType().getName();

					if (typeName.endsWith(".String")) {
						typeName = null;
					} else if (typeName.endsWith(".Integer")) {
						typeName = "i"; // inteiro, apenas com a separação de milhar
					} else if (typeName.endsWith(".BigDecimal")) {
						typeName = "n"; // numero com separação de milhar com casas decimais
					} else if (typeName.endsWith(".Date")) {
						typeName = "d"; // data e hora completa
					}

					String type = typeName == null ? "" : String.format("\"type\":\"%s\"", typeName);
					String obj = String.format("\"%s\":{%s}", field.getName(), type);
					stringBuilder.append(obj);
				}
			}

			stringBuilder.append("}");
			ret = stringBuilder.toString();
		} catch (ClassNotFoundException e) {
			ret = null;
		}

		return ret;
	}

	public static Integer parseInt(String str) {
		Integer ret = null;

		if (str != null) {
			ret = Integer.parseInt(str);
		}

		return ret;
	}

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

}
