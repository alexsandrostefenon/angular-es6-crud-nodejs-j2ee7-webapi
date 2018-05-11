package org.domain.iso8583router.messages;

import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConfItem;

public class MessageAdapterTTLV implements MessageAdapter {
	
	private void parse(Message message, ISO8583RouterMessageAdapterConfItem conf, String value) throws Exception {
		if (value != null && value.length() > 0) {
			boolean mayBeSpecial = (conf.getDataType() == Utils.DATA_TYPE_SPECIAL);
			
			if (mayBeSpecial == true && message.enableBinarySkip == true) {
				value = ByteArrayUtils.replaceBinarySkipes(value, 0, value.length());
			}
			
			MessageAdapter.setFieldData(conf, message, value);
		} else {
			MessageAdapter.setFieldData(conf, message, null);
		}
	}
	
	public void parse(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root, String data, String directionSuffix) throws Exception {
		// primeiro converte os escapes hexa 
		data = ByteArrayUtils.unEscapeBinaryData(data, "(", ")", 2);
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		int offset = 0;

		while (offset < data.length()) {
			int pos_ini = offset;
			
			while (offset < data.length() && data.charAt(offset) > 0x04) {
				offset++;
			}
			
			if (offset < data.length() - 2) {
				String name = data.substring(pos_ini, offset);
				ISO8583RouterMessageAdapterConfItem conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, name);

				if (conf == null) {
					throw new Exception(String.format("MessageAdapterTTLV.parseMessage : fail to add new field [%s]", name));
				}

				int contentType = data.charAt(offset++);
				int size = 0;
				// parseia o tamanho
				{
					char byteVal;

					do {
						byteVal = data.charAt(offset++);
						size <<= 7;
						size |= (byteVal & 0x7f);
					} while (byteVal > 0x80);
				}
				
				if (offset <= data.length() - size) {
					if (contentType == 0x04) {
						String value = data.substring(offset, offset + size);
						parse(message, conf, value);
						offset += size;
					} else {
						throw new Exception(String.format("MessageAdapterTTLV.parseMessage : contentType unsuported [%s]", contentType));
					}
				} else {
					throw new Exception(String.format("MessageAdapterTTLV.parseMessage : fail to parse data [size = %s]", size));
				}
			} else {
				throw new Exception(String.format("MessageAdapterTTLV.parseMessage : fail to parse data [data.length = %s]", data.length()));
			}
		}
	}
	
	public String generate(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root) throws Exception {
		StringBuilder buffer = new StringBuilder(2048);
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		
		for (ISO8583RouterMessageAdapterConfItem conf : confs) {
			String fieldName = conf.getFieldName();
			String str = MessageAdapter.getFieldDataWithAlign(conf, message);
			int size = str == null ? 0 : str.length();
			
			if (size > 0) {
				// 0042(4)(F)0000000008730110048(4)(4)00020061(4)(3)TEF0071(4)(4)0705
				buffer.append(fieldName);
				buffer.append((char) 0x04);
				insertDataLength(buffer, size);
				buffer.append(str);
			}
		}

		return buffer.toString();
	}

	public String getTagName(String root, String tagPrefix, String tagName) {
		return tagName;
	}

	// usado internamente em generate
	private void insertDataLength(StringBuilder buffer, int size) {
		char aux[] = new char[10];
		int numBytes = 0;

		while (size > 0) {
			char byteVal = (char) (size & 0x0000007f);
			size >>= 7;

			if (numBytes > 0) {
				byteVal |= 0x80;
			}

			aux[numBytes] = byteVal;
			numBytes++;
		}
		
		for (int i = numBytes-1; i >= 0; i--) {
			buffer.append(aux[i]);
		}
	}
}
