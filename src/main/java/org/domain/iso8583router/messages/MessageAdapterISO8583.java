package org.domain.iso8583router.messages;

import java.security.InvalidParameterException;
import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapter;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapterItem;

public class MessageAdapterISO8583 implements MessageAdapter {
	private boolean isAsciiHexExpanded = false;
	
	public MessageAdapterISO8583(boolean isAsciiHexExpanded) {
		this.isAsciiHexExpanded = isAsciiHexExpanded;
	}

	private static String getRoot(Message message) throws Exception {
		String msgType = message.getMsgType();
		
		if (msgType == null) {
			throw new Exception("getRoot() : missing msgType");
		}
		
		String codeProcess = message.getCodeProcess();
		String root = getRootName(msgType, codeProcess);
		message.setRoot(root);
		return root;
	}
	
	private static String getRootName(String msgType, String codeProcess) {
		String ret = null;

		if (codeProcess == null) {
			ret = msgType + "_000000";
		} else if (codeProcess.length() == 6 && Utils.isUnsignedInteger(codeProcess.substring(0, 5)) == true) {
			ret = msgType + "_" + codeProcess;
		}
		
		return ret;
	}
	
	private static boolean isPanCandidate(String str) {
		boolean ret = false;
		
		if (Utils.isHex(str, 0, str.length(), true, false)) {
			ret = true;
		} else if (str.length() == 16 && (Utils.isDecimal(str, 0, 6) || str.startsWith("******")) && str.startsWith("******", 6) && Utils.isDecimal(str, 12, 4)) {
			ret = true;
		}
		
		return ret;
	}

	public static String getRootISO8583(Message message, String str) {
//		StringBuilder auxParse = new StringBuilder(str.length() * 2);
/*		
		if (compress) {
			offet = Utils.unCompress(str, 0, auxParse, 4, true);
		}
*/
		int offet = 0;
		int posEnd = str.length();
		String msgType = str.substring(offet, offet+4);
		offet += 4;
		
		int mapSize = 16;
		// verifica se tem segundo mapa de bits
		char ch = str.charAt(offet);
		int firstByteFromBitMap = ByteArrayUtils.hexAsciiToInt(ch, true, false, '0', '0');
		
		if (firstByteFromBitMap < 0) {
			return null;
		}
		
		if ((firstByteFromBitMap & 0x08) != 0) {
			mapSize = 32;
		}
		
		if (offet > posEnd - (mapSize + 6)) {
			// TODO : nem tenta mais outros offsets
			return null;
		}
		// verifica se o mapa de bits esta todo em hexa
		String map = str.substring(offet, offet+mapSize);
		offet += mapSize;
		
		if (Utils.isHex(map, 0, map.length(), true, false) == false) {
			return null;
		}
		// verifica se tem bit 2
		if ((firstByteFromBitMap & 0x04) != 0) {
			String strPanLen = str.substring(offet, offet+2);
			offet += 2;
			
			if (Utils.isUnsignedInteger(strPanLen) == false) {
				return null;
			}
			
			try {
				int panLen = Integer.parseInt(strPanLen);
				// O Banrisul foi o primeiro a utilizar pan criptografado, antes eu testava isDecimal
				String pan = str.substring(offet, offet+panLen);
				offet += panLen;
				
				if (isPanCandidate(pan) == false) {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}
		
		String codeProcess = null;
		// verificar se o mapa de bits tem o bit 3, caso contrário retorna null
		if ((firstByteFromBitMap & 0x02) != 0) {
			codeProcess = str.substring(offet, offet+6);
			offet += 6;
		}
		
		String ret = getRootName(msgType, codeProcess);
		
		if (ret != null && message != null) {
			message.setMsgType(msgType);
			message.setCodeProcess(codeProcess);
		}
		
		return ret;
	}
	
	// se enableCompress == true, esta funcao deve rececer 'src' jah convertida com Utils.convertToHexAscii
	private int parseData(Message message, Iso8583RouterMessageAdapterItem conf, String src, int offset, int sizeToRead) throws Exception {
		boolean oddRightAlign = false;
		String tag = conf.getTag();
		// TODO : verificar o motivo desta excessão
		if ("22".equals(tag) || "32".equals(tag)) {
			oddRightAlign = true;
		}

		boolean isAsciiHexExpandedValue = this.isAsciiHexExpanded;
		boolean mayBeAlpha = (conf.getDataType() & Utils.DATA_TYPE_ALPHA) > 0;
		boolean mayBeSpecial = (conf.getDataType() & Utils.DATA_TYPE_SPECIAL) > 0;

		if ((mayBeAlpha == false) && (mayBeSpecial == false)) {
			isAsciiHexExpandedValue = false;
		}
	
		int sizeSkip = 0;
		
		if (mayBeSpecial == true && message.enableBinarySkip == true) {
			int srcSize = src.length();
			src = ByteArrayUtils.replaceBinarySkipes(src, offset, sizeToRead);
			sizeSkip = srcSize - src.length();
		}
		
		String value;
		
		try {
			if (isAsciiHexExpandedValue == true) {
				sizeToRead *= 2;
				message.bufferAuxLocal.setLength(0);
				ByteArrayUtils.appendAsciiHexToBinary(message.bufferAuxLocal, src, offset, sizeToRead, 2, '*', '0');
				value = message.bufferAuxLocal.toString();
			} else if (this.isAsciiHexExpanded == false || (sizeToRead % 2) == 0) {
				value = src.substring(offset, offset + sizeToRead);
			} else if (oddRightAlign) {
				offset++;
				value = src.substring(offset, offset + sizeToRead);
			} else {
				value = src.substring(offset, offset + sizeToRead);
				sizeToRead++;
			}
		} catch (Exception e) {
			throw new Exception(String.format("MessageAdapterISO8583.parse - error in src.substring(%d, %d) - src = %s : %s", offset, offset + sizeToRead, src, e.getMessage()));
		}
		
		MessageAdapter.setFieldData(conf, message, value);
		offset += sizeToRead + sizeSkip;
		return offset;
	}
	
	// se enableCompress == true, esta funcao deve rececer 'src' jah convertida com Utils.convertToHexAscii
	private int parse(Message message, Iso8583RouterMessageAdapterItem conf, String src, int offset) throws Exception {
		int srcSize = src.length();

		if (offset > srcSize) {
			throw new InvalidParameterException();
		}

		int maxDataLength = conf.getMaxLength();
		int sizeHeader = conf.getSizeHeader();
		int sizeToRead;
		// extrai o tamanho dos dados
		if (sizeHeader > 0) {
			if (this.isAsciiHexExpanded && ((sizeHeader % 2) != 0)) {
				sizeHeader++;
			}

			sizeToRead = ByteArrayUtils.parseInt(src, offset, sizeHeader);
			offset += sizeHeader;
		} else if (sizeHeader == 0) {
			sizeToRead = maxDataLength;
		} else {
			sizeToRead = srcSize - offset;

			if (sizeToRead > maxDataLength) {
				sizeToRead = maxDataLength;
			}
		}

		if (sizeToRead == 0) {
			MessageAdapter.setFieldData(conf, message, null);
		} else {
			offset = parseData(message, conf, src, offset, sizeToRead);
		}

		return offset;
	}

	// se enableCompress == true, esta funcao deve rececer 'data' jah convertida com Utils.convertToHexAscii
	public void parse(Message message, Iso8583RouterMessageAdapter adapterConf, String root, String data, String directionSuffix) throws Exception {
		int dataLength = data.length();

		if (dataLength < (4+16)) {
			throw new Exception("Short message lenght");
		}
		// pega o tipo da mensagem e desloca o offset
		// gambi até arrumar o gtw
		if (data.endsWith("]")) {
			data = data.substring(0, data.length()-1) + "00]";
		}
		
		root = getRootISO8583(message, data);
		List<Iso8583RouterMessageAdapterItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		message.setRoot(root);
		int offset = 4;
		int mapSize = 16;
		int bits = ByteArrayUtils.hexToInt(data, 4, 2, '0', '0');

		if ((bits & 0x80) != 0) {
			mapSize *= 2;
		}

		offset += mapSize;
		boolean[] bitMask = new boolean[128+1]; 
		message.bufferParseGenerateDebug.append("\nbits : ");
		
		for (int i = 0, bit = 1; i < mapSize; i++) {
			bits = ByteArrayUtils.hexToInt(data, 4+i, 2, '0', '0');
			i++;

			for (int c = 0; c < 8; c++, bit++, bits <<= 1) {
				if ((bits & 0x80) != 0) {
					bitMask[bit] = true;
					message.bufferParseGenerateDebug.append(bit);
					message.bufferParseGenerateDebug.append(' ');
				}
			}
		}
		
		message.bufferParseGenerateDebug.append('\n');
		message.bufferParseGenerateDebug.append('\n');
		parse(message, confs.get(0), data, 0);
		
		for (int bit = 2, numBits = mapSize*4; bit <= numBits; bit++) {
			if (bitMask[bit]) {
				try {
					Iso8583RouterMessageAdapterItem conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, Integer.toString(bit));
					offset = parse(message, conf, data, offset);
				} catch (Exception e) {
					throw new Exception(String.format("MessageAdapterISO8583.parse - error in bit %d : %s", bit, e.getMessage()));
				}
			}
		}
	}

	// apenas para debug, sua funcao eh tentar resolver as mensagens com BAD_FORMAT devido a bits setados mas nao enviados
	public static String setBit(String data, int bit, boolean state) {
		if (bit < 2) {
			return data;
		}
		
		int offset = 4;
		int mapSizeAsciiHex = 16;
		char ch = data.charAt(offset);
		int value = ByteArrayUtils.hexAsciiToInt(ch, true, false, '0', '0');

		if ((value & 0x08) != 0) {
			mapSizeAsciiHex *= 2;
		}
		
		if (bit > (mapSizeAsciiHex * 4)) {
			return data;
		}

		int byteOffset = (bit-1) / 4;
		int bitOffset = (bit-1) % 4;
		offset += byteOffset;
		int bitMask = 0x08 >> bitOffset;
		ch = data.charAt(offset);
		value = ByteArrayUtils.hexAsciiToInt(ch, true, false, '0', '0');
		
		if (state == true) {
			value |= bitMask;
		} else {
			bitMask = ~ bitMask;
			value &= bitMask;
		}
		
		ch = ByteArrayUtils.intToHexAsciiChar(value);
		StringBuilder buffer = new StringBuilder(data);
		buffer.setCharAt(offset, ch);
		return buffer.toString();
	}
	// Message
	// Copia o valor inteiro para um "char*" e preenche com zeros a esquerda
	private static void intToStr(int value, StringBuilder buffer, int size) {
		String fmt = String.format("%%0%dd", size);
		buffer.append(String.format(fmt, value));
	}
	
	private void addField(StringBuilder buffer, Message message, Iso8583RouterMessageAdapterItem conf, String value) {
		boolean mayBeAlpha = (conf.getDataType() & Utils.DATA_TYPE_ALPHA) > 0;
		boolean mayBeSpecial = (conf.getDataType() & Utils.DATA_TYPE_SPECIAL) > 0;
		boolean expandedData = this.isAsciiHexExpanded;

		if ((mayBeAlpha == false) && (mayBeSpecial == false)) {
			expandedData = false;
		}

		int sizeHeader = conf.getSizeHeader();
		int strLen = value.length();

		if (sizeHeader > 0) {
			if (this.isAsciiHexExpanded == true && (sizeHeader % 2) != 0) {
				MessageAdapterISO8583.intToStr(strLen, buffer, sizeHeader + 1);
			} else {
				MessageAdapterISO8583.intToStr(strLen, buffer, sizeHeader);
			}
		}

		boolean oddRightAlign = false;
		String tag = conf.getTag();
		// TODO : verificar o motivo desta excessão
		if ("22".equals(tag) || "32".equals(tag)) {
			oddRightAlign = true;
		}

		if (expandedData == true) {
			ByteArrayUtils.unCompress(buffer, value);
		} else if (this.isAsciiHexExpanded == false || (strLen % 2) == 0) {
			buffer.append(value);
		} else if (oddRightAlign) {
			buffer.append('0');
			buffer.append(value);
		} else {
			buffer.append(value);
			buffer.append('1');
		}
	}

	public String generate(Message message, Iso8583RouterMessageAdapter adapterConf, String root) throws Exception {
		root = getRoot(message);
		List<Iso8583RouterMessageAdapterItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		StringBuilder buffer = new StringBuilder(2048);
		String msgType = message.getMsgType();
		buffer.append(msgType);
		int sizeMap = 16;
		String[] values = new String[128+1];
		Iso8583RouterMessageAdapterItem[] usedConfs = new Iso8583RouterMessageAdapterItem[128];
		int lastField = 0;
		//verifica se usa o segundo mapa de Bits
		for (int field = 0; field < 128; field++) {
			Iso8583RouterMessageAdapterItem conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, Integer.toString(field));
			
			if (conf != null) {
				String value = MessageAdapter.getFieldDataWithAlign(conf, message);
			
				if (value != null) {
					values[field] = value;
						usedConfs[field] = conf;
					lastField = field;
				}
			}
		}
		
		if (lastField >= 65) {
			sizeMap = 32;
			values[1] = "";
		}
		// Faz o Zoneamento e monta a string
		for (int i = 0, field = 1; i < sizeMap; i++) {
			char bitZone = 0;

			for (int j = 0; j < 4; j++) {
				bitZone <<= 1;

				if (values[field++] != null) {
					bitZone |= 1;
				}
			}

			buffer.append(ByteArrayUtils.intToHexAsciiChar(bitZone));
		}

		for (int field = 2; field < 128; field++) {
			String value = values[field];

			if (value != null && value.length() > 0) {
				addField(buffer, message, usedConfs[field], value);
			}
		}

		String ret = buffer.toString();
		
		if (ret == null) {
			throw new Exception("MessageAdapterISO8583 : fail in generate String for message : " + message.toString());
		}
		
		return ret;
	}

	public String getTagName(String root, String tagPrefix, String tagName) {
		if (tagPrefix != null && tagName.startsWith(tagPrefix) == false) {
			tagName = tagPrefix + "_" + root + "_" + tagName; 
		}
		
		return tagName;
	}

}
