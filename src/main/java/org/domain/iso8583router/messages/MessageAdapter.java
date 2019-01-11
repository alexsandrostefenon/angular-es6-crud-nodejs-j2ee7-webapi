package org.domain.iso8583router.messages;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.domain.commom.Utils;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapter;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapterItem;

public interface MessageAdapter {
	public void parse(Message message, Iso8583RouterMessageAdapter adapterConf, String root, String data, String directionSuffix) throws Exception;

	public String generate(Message message, Iso8583RouterMessageAdapter adapterConf, String root) throws Exception;

	public String getTagName(String root, String tagPrefix, String tagName);

	static String getFieldDataWithAlign(Iso8583RouterMessageAdapterItem conf, Message message) {
		String fieldName = conf.getFieldName();
		Integer minLength = conf.getMinLength();
		Integer maxLength = conf.getMaxLength();
		Utils.DataAlign alignment = conf.getAlignment();
		String tag = conf.getTag();
		
		Supplier<String> getFieldData = () -> {
			String data = null;
			
			if (fieldName != null) {
				data = message.getFieldData(fieldName);
			} else if (tag != null) {
				data = message.getFieldData(tag);
			}
			// first check for forced manipulation of ISO8583 fields
			if (data != null && Utils.year != null && fieldName != null) {
				if (fieldName.equals("dateTimeGmt") || fieldName.equals("systemDateTime")) {
					if (data.length() == 10 && maxLength == 14) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year + data;
					} else if (data.length() == 14 && maxLength == 10) {
						data = data.substring(4);
					}
				} else if (fieldName.equals("captureDate")) {
					if (data.length() == 4 && maxLength == 8) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year + data;
					} else if (data.length() == 8 && maxLength == 4) {
						data = data.substring(4);
					}
				} else if (fieldName.equals("lastOkDate")) {
					if (data.length() == 6 && maxLength == 8) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year.substring(0, 2) + data;
					} else if (data.length() == 8 && maxLength == 6) {
						data = data.substring(2);
					}
				}
			}
			
			return data;
		};
	
		BiFunction<Character, Integer, String> getFieldDataLeftAlign = (Character ch, Integer lenght) -> {
			String value = getFieldData.get();
			value = Utils.getLeftAlign(message.auxData, value, ch, lenght);
			return value;
		};
	
		BiFunction<Character, Integer, String> getFieldDataRightAlign = (Character ch, Integer lenght) -> {
			String value = getFieldData.get();
			value = Utils.getRightAlign(message.auxData, value, ch, lenght);
			return value;
		};
	
		String data;
		int lenght = minLength;
		// adiciona os caracteres de alinhamento
		if (alignment == Utils.DataAlign.SPACE_RIGHT) {
			data = getFieldDataRightAlign.apply(' ', lenght);
		} else if (alignment == Utils.DataAlign.SPACE_LEFT) {
			data = getFieldDataLeftAlign.apply(' ', lenght);
		} else if (alignment == Utils.DataAlign.ZERO_LEFT) {
			data = getFieldDataLeftAlign.apply('0', lenght);
		} else if (alignment == Utils.DataAlign.ZERO_RIGHT) {
			data = getFieldDataRightAlign.apply('0', lenght);
		} else {
			data = getFieldData.get();
		}
	
		return data;
	}

	static void setFieldData(Iso8583RouterMessageAdapterItem conf, Message message, String data) throws Exception {
			if (data == null || data.length() == 0) {
				return;
			}
			
			String fieldName = conf.getFieldName();
			Integer maxLength = conf.getMaxLength();
			Utils.DataAlign alignment = conf.getAlignment();
			Integer dataType = conf.getDataType();
			Integer sizeHeader = conf.getSizeHeader();
			String tag = conf.getTag();
	
			// first check for forced manipulation of ISO8583 fields
			if (Utils.year != null && fieldName != null) {
				if (fieldName.equals("dateTimeGmt") || fieldName.equals("systemDateTime")) {
					if (data.length() == 10 && maxLength == 14) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year + data;
					} else if (data.length() == 14 && maxLength == 10) {
						data = data.substring(4);
					}
				} else if (fieldName.equals("captureDate")) {
					if (data.length() == 4 && maxLength == 8) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year + data;
					} else if (data.length() == 8 && maxLength == 4) {
						data = data.substring(4);
					}
				} else if (fieldName.equals("lastOkDate")) {
					if (data.length() == 6 && maxLength == 8) {
						// TODO : trocar o valor chumbado do ano por um valor dinÃ¢mico
						data = Utils.year.substring(0, 2) + data;
					} else if (data.length() == 8 && maxLength == 6) {
						data = data.substring(2);
					}
				}
			}
	
			int length = data.length();
	
			if (length > maxLength) {
				String str = String.format("length[%d] > maxLength [%d] - field [%s] - str = %s", length, maxLength, fieldName, data);
	//			debug(str);
				InvalidParameterException exception = new InvalidParameterException(str);
				throw exception;
			}
	
			int pos = Utils.checkContentType(dataType, data); 
			
			if (pos >= 0) {
				String str = String.format("field [%s] - pos [%d], dataType (%s) : str = %s", fieldName, pos, dataType, data);
				throw new InvalidParameterException(str);
			}
			// Apply data align in fixed size case 
			if (sizeHeader == 0) {
				if (length <= maxLength && alignment != Utils.DataAlign.NONE) {
					// por padrÃ£o Ã© ISO8583Conf.ZERO_LEFT_ALIGNMENT
					char ch = '0';
					int side = 0;
					int posIni = 0;
					int posEnd = length-1;
	
					if (alignment == Utils.DataAlign.SPACE_RIGHT) {
						ch = ' ';
						side = 1;
					} else if (alignment == Utils.DataAlign.SPACE_LEFT) {
						ch = ' ';
					}
	
					if (side == 0) {
						while (posIni < posEnd && data.charAt(posIni) == ch) {
							posIni++;
						}
					} else {
						while (posIni < posEnd && data.charAt(posEnd) == ch) {
							posEnd--;
						}
					}
	
					data = data.substring(posIni, posEnd+1);
				} else if (length != maxLength) {
					String str = String.format("length [%d] < fizedLength [%d] - field [%s] - str = %s", length, maxLength, fieldName, data);
	//				debug(str);
					throw new InvalidParameterException(str);
				}
			}
	
			if (fieldName != null) {
				message.setFieldData(fieldName, data);
			} else {
				message.setFieldData(tag, data);
			}
		}

	static List<Iso8583RouterMessageAdapterItem> getMessageAdapterConfItems(Iso8583RouterMessageAdapter adapterConf, String root) throws Exception {
		ArrayList<Iso8583RouterMessageAdapterItem> ret = new ArrayList<Iso8583RouterMessageAdapterItem>();
		
		for (Iso8583RouterMessageAdapterItem item : adapterConf.getItems()) {
			String regex = item.getRootPattern();
			
			if (regex == null || Pattern.matches(regex, root)) {
				ret.add(item);
			}
		}
	
		return ret;
	}

	/**
	 * 
	 */
	static Iso8583RouterMessageAdapterItem getMessageAdapterConfItemFromTag(List<Iso8583RouterMessageAdapterItem> confs, String tagName) {
		Iso8583RouterMessageAdapterItem ret = null;
		
		for (Iso8583RouterMessageAdapterItem conf : confs) {
			if (tagName.equals(conf.getTag())) {
				ret = conf;
			}
		}
		
		return ret;
	}
}
