package org.domain.iso8583router.messages;

import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConfItem;

public class MessageAdapterTag implements MessageAdapter {
	
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
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		// capture_0910_032=00000000007|capture_0910_060=0@@|capture_0910_100=00000000001
		String[] nodes = data.split("\\|");

		for (int i = 0; i < nodes.length; i++) {
			String node = nodes[i];
			int pos = node.indexOf('=');

			if (pos > 0) {
				String name = node.substring(0, pos);
				String value = node.substring(pos+1);
				ISO8583RouterMessageAdapterConfItem conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, name);

				if (conf == null) {
					conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, name);

					if (conf == null) {
						throw new Exception(String.format("Message.parseMessage : fail to add new field [%s]", name));
					}
				}

				parse(message, conf, value);
			}
		}
	}

	public String generate(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root) throws Exception {
		StringBuilder buffer = new StringBuilder(2048);
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);
		buffer.append("root=");
		buffer.append(root);
		buffer.append("|");
		
		for (ISO8583RouterMessageAdapterConfItem conf : confs) {
			String str = MessageAdapter.getFieldDataWithAlign(conf, message);
			
			if (str != null) {
				String fieldName = conf.getFieldName();

				if (fieldName == null || fieldName.length() == 0) {
					fieldName = conf.getTag();
				}
				
				buffer.append(fieldName);
				buffer.append("=");
				buffer.append(str);
				buffer.append("|");
			}
		}

		buffer.setLength(buffer.length()-1);
		return buffer.toString();
	}

	public String getTagName(String root, String tagPrefix, String tagName) {
		return tagName;
	}
}
