package org.domain.iso8583router.messages;

import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConfItem;

public class MessageAdapterPsv implements MessageAdapter {
	public String directionSuffix = "";
	
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
		String[] values = data.split("\\|");
		int i = 0;
		
		for (ISO8583RouterMessageAdapterConfItem conf : confs) {
			String value = values[i++];
			parse(message, conf, value);
		}
	}

	public String generate(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root) throws Exception {
		StringBuilder buffer = new StringBuilder(2048);
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);

		for (ISO8583RouterMessageAdapterConfItem conf : confs) {
			String str = MessageAdapter.getFieldDataWithAlign(conf, message);
			
			if (str != null) {
				buffer.append(str);
				buffer.append('|');
			}
		}
		
		return buffer.toString();
	}

	public String getTagName(String root, String tagPrefix, String tagName) {
		if (tagName.startsWith(root) == false) {
			tagName = root + "_" + tagName; 
		}
		
		return tagName;
	}

}
