package org.domain.iso8583router.messages;

import java.io.StringReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConfItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MessageAdapterXml implements MessageAdapter {
	// TODO : pegar de message
	String directionSuffix = "";

	public void parse(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root, String data, String directionSuffix) throws Exception {
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(data));
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		NodeList nodes = doc.getChildNodes();
		root = "unknow";

		if (nodes.getLength() == 1) {
			Element _root = (Element) nodes.item(0);
			nodes = _root.getChildNodes();
			root = _root.getNodeName();

			if (directionSuffix != null) {
				root = root + directionSuffix;
			}

			try {
				message.setRoot(root);
			} catch (Exception e) {
				root = "unknow";
				message.setRoot(root);
			}
		}

		TreeMap<String, String> map = new TreeMap<String, String>();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			String tagName = element.getNodeName();
			// TODO : originalmente era 'String value = element.getTextContent();'
			String value = element.getNodeValue();
			value = value.replace("<", "&lt;");
			value = value.replace(">", "&gt;");
			map.put(tagName, value);
		}
		
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);

		for (Entry<String, String> entry : map.entrySet()) {
		    String tagName = entry.getKey();
		    String value = entry.getValue();
			ISO8583RouterMessageAdapterConfItem conf = MessageAdapter.getMessageAdapterConfItemFromTag(confs, tagName);

			if (conf == null) {
				throw new Exception(String.format("MessageAdapterXml.parseMessage : fail to add new field [%s]", tagName));
			}

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
	}

	public String generate(Message message, ISO8583RouterMessageAdapterConf adapterConf, String root) throws Exception {
		StringBuilder buffer = new StringBuilder(2048);
		List<ISO8583RouterMessageAdapterConfItem> confs = MessageAdapter.getMessageAdapterConfItems(adapterConf, root);

		if (root == null) {
		} else if (root.endsWith(Message.DIRECTION_NAME_S2C)) {
			root = root.substring(0, root.length()- Message.DIRECTION_NAME_S2C.length());
		} else if (root.endsWith(Message.DIRECTION_NAME_C2S)) {
			root = root.substring(0, root.length()- Message.DIRECTION_NAME_C2S.length());
		}

		buffer.append("<");
		buffer.append(root);
		buffer.append(">");

		for (ISO8583RouterMessageAdapterConfItem conf : confs) {
			String str = MessageAdapter.getFieldDataWithAlign(conf, message);
			
			if (str != null) {
				String tagName = conf.getTag();
				buffer.append("<");
				buffer.append(tagName);
				buffer.append(">");
				buffer.append(str);
				buffer.append("</");
				buffer.append(tagName);
				buffer.append(">");
			}
		}

		buffer.append("</");
		buffer.append(root);
		buffer.append(">\r\n\n");
		return buffer.toString();
	}

	public String getTagName(String root, String tagPrefix, String tagName) {
		// TODO Auto-generated method stub
		return null;
	}

}
