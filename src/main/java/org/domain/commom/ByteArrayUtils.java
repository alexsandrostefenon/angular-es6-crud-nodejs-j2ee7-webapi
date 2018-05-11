package org.domain.commom;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

public class ByteArrayUtils {
	// Comm
	public static final int STX = 0x02;
	// Comm
	public static final int ETX = 0x03;
	// Comm
	public static final int CR = 0x0d;
	// Comm
	public static boolean equals(byte[] s1, int s1Offset, byte[] s2, int s2Offset, int n) {
		boolean ret = true;

		for (int i = 0; i < n; i++) {
			if (s1[s1Offset++] != s2[s2Offset++]) {
				ret = false;
				break;
			}
		}

		return ret;
	}
	// crc
	//Raiz para o calculo do CRC
    static short PPPINITFCS16 = (short) 0xFFFF;
    // crc
    // FCS lookup table as calculated by the table generator.
    static int[] fcstabCRC = new int[] {
    	   0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
    	   0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
    	   0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
    	   0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
    	   0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
    	   0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
    	   0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
    	   0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
    	   0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
    	   0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
    	   0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
    	   0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
    	   0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
    	   0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
    	   0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
    	   0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
    	   0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
    	   0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
    	   0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
    	   0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,

    	   0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
    	   0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
    	   0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
    	   0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
    	   0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
    	   0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
    	   0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
    	   0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
    	   0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
    	   0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
    	   0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
    	   0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    /*
	 * Calculate a new fcs given the current fcs and the new data.
	 */
	// Comm
    static public int crc(byte[] data, int offset, int lenght) {
	    int fcs = PPPINITFCS16;
	    int pos = offset;

	    while (lenght-- > 0) {
	        fcs = (fcs >> 8) ^ fcstabCRC[(fcs ^ data[pos]) & 0xff];
	        pos++;
	    }

	    return fcs;
	}
    // Comm
	// retorna o novo offset
	public static int pack(byte[] buffer, int offset, int numBytes, BinaryEndian packageType, int val) {
		if (numBytes < 0 || numBytes > 4) {
			throw new InvalidParameterException();
		}

		if (packageType == BinaryEndian.LITTLE) {
			for (int i = 0; i < numBytes; i++) {
				buffer[offset++] = (byte) (val & 0x000000ff);
				val >>= 8;
			}
		} else if (packageType == BinaryEndian.BIG) {
			for (int i = numBytes-1; i >= 0; i--) {
				buffer[offset+i] = (byte) (val & 0x000000ff);
				val >>= 8;
			}

			offset += numBytes;
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}
	// Comm
	// retorna o novo offset
	public static int pack(byte[] buffer, int offset, int numBytes, byte[] val) {
		if (numBytes < 0 || numBytes > val.length) {
			throw new InvalidParameterException();
		}

		for (int i = 0; i < numBytes; i++) {
			buffer[offset++] = val[i];
		}

		return offset;
	}
	// unpack
	// retorna o novo offset
	public static int unpack(byte[] buffer, int offset, int numBytes, BinaryEndian packageType, RefInt val) {
		val.value = 0;

		if (numBytes < 0 || numBytes > 4) {
			throw new InvalidParameterException();
		}

		if (packageType == BinaryEndian.LITTLE) {
			for (int i = numBytes-1; i >= 0; i--) {
				val.value <<= 8;
				val.value |= (buffer[offset+i] & 0x000000ff);
			}

			offset += numBytes;
		} else if (packageType == BinaryEndian.BIG) {
			for (int i = 0; i < numBytes; i++) {
				val.value <<= 8;
				val.value |= (buffer[offset++] & 0x000000ff);
			}
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}
	// unpack
	// retorna o novo offset
	public static int unpack(byte[] buffer, int offset, int numBytes, byte[] val) {
		if (numBytes < 0) {
			String msg = String.format("numBytes [%d] < 0", numBytes);
			System.out.println(msg);
			throw new InvalidParameterException(msg);
		}

		if (numBytes > val.length) {
			throw new InvalidParameterException(String.format("numBytes [%d] > val.length [%d]", numBytes, val.length));
		}

		for (int i = numBytes-1, j = 0; i >= 0; i--, j++) {
			val[j] = buffer[offset+i];
		}

		offset += numBytes;
		return offset;
	}
	// unpack
	// retorna o novo offset
	public static int unpack(byte[] buffer, int offset, int numBytes, BinaryEndian endianType, byte[] val) {
		if (numBytes < 0 || numBytes > val.length) {
			throw new InvalidParameterException();
		}

		if (endianType == BinaryEndian.LITTLE) {
			for (int i = numBytes-1, j = 0; i >= 0; i--, j++) {
				val[j] = buffer[offset+i];
			}

			offset += numBytes;
		} else if (endianType == BinaryEndian.BIG) {
			for (int i = 0; i < numBytes; i++) {
				val[i] = buffer[offset++];
			}
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}
	// Comm
	// retorna o novo offset
	public static int unpack(InputStream is, byte[] buffer, int offset, int numBytes, BinaryEndian packageType, RefInt val) throws IOException {
		int readen = is.read(buffer, offset, numBytes);
		
		if (readen < 0) {
			return readen;
		}

		if (readen != numBytes) {
			throw new IOException();
		}

		return unpack(buffer, offset, numBytes, packageType, val);
	}
	// Comm
	// retorna o novo offset e o número de bytes lidos
	public static int unpack(InputStream is, byte[] buffer, int offset, int numBytes, byte[] val) throws IOException {
		if (numBytes > 0) {
			int readen = is.read(buffer, offset, numBytes);

			if (readen < 0) {
				return readen;
			}

			if (readen != numBytes) {
				throw new IOException();
			}
		} else {
			numBytes = is.read(buffer, offset, buffer.length - offset);
			
			if (numBytes < 0) {
				throw new IOException();
			}
		}

		return unpack(buffer, offset, numBytes, val);
	}

	// unused
	// retorna o novo offset
	public static int pack(byte[] buffer, int offset, int numBytes, BinaryEndian endianType, byte[] val) {
		if (numBytes < 0 || numBytes > val.length) {
			throw new InvalidParameterException();
		}

		if (endianType == BinaryEndian.LITTLE) {
			for (int i = 0; i < numBytes; i++) {
				buffer[offset++] = val[i];
			}
		} else if (endianType == BinaryEndian.BIG) {
			for (int i = numBytes-1, j = 0; i >= 0; i--, j++) {
				buffer[offset+i] = val[j];
			}

			offset += numBytes;
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}
	// unused
	// retorna o novo offset
	public static int unpack(InputStream is, byte[] buffer, int offset, int numBytes, BinaryEndian endianType, byte[] val) throws IOException {
		int readen = is.read(buffer, offset, numBytes);

		if (readen < 0) {
			return readen;
		}

		if (readen != numBytes) {
			throw new IOException();
		}

		return unpack(buffer, offset, numBytes, endianType, val);
	}
	// unused
	// retorna o novo offset
	public static int unpackString(InputStream is, byte[] buffer, int offset) throws IOException {
		int numBytes = 1;
		int readen;

		do {
			readen = is.read(buffer, offset, numBytes);
			offset += readen;
		} while (readen == numBytes || buffer[offset] != 0);

		return offset;
	}
	// unused
	// retorna o novo offset
	public static int unpackDebug(InputStream is, byte[] buffer, int offset) throws IOException {
		int val;
		int index = 0;

		do {
			val = is.read();
			char ch = (char) val;
			System.out.printf("%d - (%X) : %s\n", index++, val, ch);
			buffer[offset++] = (byte) val;
		} while (val >= 0);

		return offset;
	}
	// ImageClassificator
	public static void AsciiHexToBinary(byte[] data, CharSequence src) {
		int numChars = src.length();
		
		for (int i = 0, j = 0; i < numChars; i += 2, j++) {
			char ch;
			ch = (char) ByteArrayUtils.hexToInt(src, i, 2, '0', '0');
			data[j] = (byte) ch;
		}
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
	// unused
	public static void unCompress(byte[] bytes, int offset, int length) {
		for (int i = length-1; i >= 0; i--) {
		int unsignedByte = ByteArrayUtils.convertToUnsignedValue(bytes[offset + i]);
			int low = unsignedByte & 0x0000000f;
			int hight = unsignedByte >> 4;
			char chHight = ByteArrayUtils.intToHexAsciiChar(hight);
			char chLow = ByteArrayUtils.intToHexAsciiChar(low);
			bytes[offset + 2*i] = (byte)chHight;
			bytes[offset + 2*i+1] = (byte)chLow;
		}
	}
	
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
		
		if (Utils.isHex(strUpperHex, 0, len, true, true) == false) {
			return null;
		}
		
		if (Utils.isHex(strLowerHex, 0, len, true, true) == false) {
			return null;
		}
		
		StringBuilder buffer = new StringBuilder(len);
		
		for (int i = 0; i < len; i++) {
			char chUpper = strUpperHex.charAt(i);
			char chLower = strLowerHex.charAt(i);
			int valUpper = ByteArrayUtils.hexAsciiToInt(chUpper, true, true, '0', '0');
			int valLower = ByteArrayUtils.hexAsciiToInt(chLower, true, true, '0', '0');
			int valXor = valUpper ^ valLower;
			char chXor = ByteArrayUtils.intToHexAsciiChar(valXor);
			buffer.append(chXor);
		}
		
		return buffer.toString();
	}
	// Message
	public static void appendAsciiHexToBinary(StringBuilder buffer, CharSequence src, int offset, int srcLength, int bytesByValue, char escapeCharIn, char escapeCharOut) {
		int posEnd = offset + srcLength;
		
		for (int i = offset; i < posEnd; i += bytesByValue) {
			char ch;
	
			if (bytesByValue == 2) {
				ch = (char) ByteArrayUtils.hexToInt(src, i, 2, escapeCharIn, escapeCharOut);
				buffer.append(ch);
			} else if (bytesByValue == 4) {
				ch = (char) ByteArrayUtils.hexToInt(src, i, 2, escapeCharIn, escapeCharOut);
				buffer.append(ch);
				ch = (char) ByteArrayUtils.hexToInt(src, i+2, 2, escapeCharIn, escapeCharOut);
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
	
			if (mayBeAlpha && isAlpha(ch)) {
				continue;
			}
	
			if (mayBeSpecial && (ch >= '0' && ch <= '9') == false && isAlpha(ch) == false) {
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
	// Message, AddAsciiHexFromUnsignedByte
	public static char intToHexAsciiChar(int value) {
		if (value < 0 || value > 15) {
			throw new InvalidParameterException(String.format("IntToHexChar : only alowed 0 -> 15 (value = %s)", value));
		}
	
		char ch = 0x30;
	
		if (value > 9) {
			ch = 0x37;
		}
	
		return (char)(ch + value);
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
		buffer.append(intToHexAsciiChar(hight));
		buffer.append(intToHexAsciiChar(low));
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
				ByteArrayUtils.AddAsciiHexFromUnsignedByte(buffer, ch);
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
								int val = ByteArrayUtils.hexToInt(hex, 0, posEnd-posIni, '0', '0');
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
							char ch = (char) ByteArrayUtils.hexToInt(content, 0, content.length(), '0', '0');
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
	// TODO : reimplementar a lógica do parâmetro bits
	public static int convertToUnsignedValue(byte byteVal, int bits) {
		int ret = byteVal;
		
		if (ret < 0) {
			ret += 256;
		}
		
		return ret;
	}
	// TODO : reimplementar a lógica do parâmetro bits
	public static byte convertToUnsignedByte(int byteVal, int bits) {
		int ret = byteVal;
		
		if (ret < 0) {
			ret += 256;
		}
		
		return (byte) ret;
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
}
