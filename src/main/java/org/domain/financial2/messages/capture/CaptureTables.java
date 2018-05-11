package org.domain.financial2.messages.capture;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.financial2.entity.ISO8583Bin;
import org.domain.financial2.entity.ISO8583PosFreteMoney;
import org.domain.financial2.entity.ISO8583TefFlow;
import org.domain.financial2.entity.ISO8583TefProduct;
import org.domain.financial2.entity.ISO8583TefProductWithoutCard;
import org.domain.financial2.entity.ISO8583TefProvider;
import org.domain.financial2.entity.ISO8583TefQuestion;
import org.domain.financial2.entity.ISO8583Terminal;
import org.domain.iso8583router.entity.ISO8583RouterChipApplicationIdentifier;
import org.domain.iso8583router.entity.ISO8583RouterChipPublicKey;

public class CaptureTables {
	private EntityManager entityManager;
	private int tableHeaderLengthSize;
	private int tableHeaderIdSize;
	StringBuilder auxBuffer = new StringBuilder(1024);
	StringBuilder bufferDebug = new StringBuilder(10*1024);
	private int tableSizeFactor;
	private boolean isJTA;
	private UserTransaction userTransaction = null;
	// 950032
	// contents : 0 -> n, 2 -> b, 3 -> ans
	// 1
	private String namesPosFreteTerminal[] = { "gaps1", "freteCountry", "gaps2" };
	private int[] sizesPosFreteTerminal = { 184, 4, 26 };
	private int[] typesPosFreteTerminal = { 0, 0, 0 };
	private int[] contentsPosFreteTerminal = { 2, 0, 2 };
	// 2
	private String namesPosFreteMoney[] = { "code", "flags", "symbol", "symbolSide", "centsSeparator", "exponentTag_5f36" };
	private int[] sizesPosFreteMoney = { 4, 2, 8, 2, 2, 2 };
	private int[] typesPosFreteMoney = { 0, 0, 0, 0, 0, 0 };
	private int[] contentsPosFreteMoney = { 0, 2, 3, 0, 0, 0 };
	// 8
	/*
 String applicationIdentifierCodeTag9f06
 Integer versionITag9f09
 Integer versionIITag9f09
 Integer versionIIITag9f09
 String tags
 String terminalCapabilitiesTag9f33
 String terminalCapabilitiesAditionalTag9f40
 String terminalActionCodeDefault
 String terminalActionCodeDenial
 String terminalActionCodeOnline
 Integer terminalTypeTag9f35;
 Integer targetPercentage
 Integer maxTargetPercentage;
 String thresholdAmount
 String terminalFloorLimitTag9f1b;
 Integer merchantCategoryCodeTag9f15
 String transactionCategoryCodeTag9f53
 Integer transactionCurrencyCodeTag5f2a
 Integer transactionCurrencyExponentTag5f36
 String transactionCertificateDataObjectList
 String dynamicDataAuthenticationDataObjectList
 Integer product
 Integer tefId
 String tefLabel
 Integer terminalCountryCodeTag9f1A
 Integer terminalReferenceCurrencyCodeTag9f3C
 String responseCodeOfflineAproved
 String responseCodeOfflineDeclined;
 String responseCodeOnlineApproved
 String responseCodeOnlineDeclined;
	 */
	// TEF
	private String namesTefAid[] = {
			"id", "applicationIdentifierCodeTag9f06", "tefLabel", "versionITag9f09", "versionIITag9f09",
			"versionIIITag9f09", "terminalCapabilitiesTag9f33", "terminalCapabilitiesAditionalTag9f40",
			"terminalTypeTag9f35",
			"terminalActionCodeDenial", "terminalActionCodeOnline", "terminalActionCodeDefault",
			"targetPercentage",	"maxTargetPercentage", "thresholdAmount",
			
			
			"terminalFloorLimitTag9f1b",
			"merchantCategoryCodeTag9f15",
			
			"transactionCategoryCodeTag9f53", "transactionCertificateDataObjectList",
			"dynamicDataAuthenticationDataObjectList", "product", "tags" };
	private int[] sizesTefAid =    { 3, 2, 24, 4, 4, 4, 6, 10, 2, 10, 10, 10, 4, 4, 8,   8, 4, 2, 40, 40, 2, 2 };
	private int[] typesTefAid =    { 0, 2,  0, 0, 0, 0, 0,  0, 0,  0,  0,  0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 2 };
	private int[] contentsTefAid = { 0, 2,  3, 0, 0, 2, 2,  2, 0,  2,  2,  2, 0, 0, 0,   0, 2, 2, 2, 0, 2 };
	// POS
	private String namesPosFreteAid[] = { "applicationIdentifierCodeTag9f06", "versionITag9f09", "versionIITag9f09",
			"versionIIITag9f09", "tags", "terminalCapabilitiesTag9f33", "terminalCapabilitiesAditionalTag9f40",
			"terminalActionCodeDefault", "terminalActionCodeDenial", "terminalActionCodeOnline", "terminalTypeTag9f35",
			"targetPercentage", "maxTargetPercentage", "thresholdAmount", "terminalFloorLimitTag9f1b",
			"merchantCategoryCodeTag9f15", "transactionCategoryCodeTag9f53", "transactionCertificateDataObjectList",
			"dynamicDataAuthenticationDataObjectList", "product" };
	private int[] sizesPosFreteAid = { 2, 4, 4, 4, 2, 6, 10, 10, 10, 10, 2, 2, 2, 8, 8, 4, 2, 40, 40, 2 };
	private int[] typesPosFreteAid = { 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] contentsPosFreteAid = { 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 0, 0, 0, 2, 2, 0, 2, 2, 2, 0 };
	// 9
	/*
	 * String registeredApplicationProviderIdentifier; String
	 * publicKeyIndexTag9F22; Integer expSize; String publicKeyExponentTag9F2E;
	 * String publicKeyModulusTag9F2D; String publicKeyCheckSum
 Integer
	 * tefId
 Integer hashStatus;
	 */
	private String namesPosFreteKey[] = { "registeredApplicationProviderIdentifier", "publicKeyIndexTag9F22", "expSize",
			"publicKeyExponentTag9F2E", "publicKeyModulusTag9F2D", "publicKeyCheckSum" };
	private int[] sizesPosFreteKey = { 10, 2, 2, 6, 4, 40 };
	private int[] typesPosFreteKey = { 0, 0, 0, 0, 2, 0 };
	private int[] contentsPosFreteKey = { 2, 2, 0, 2, 2, 2 };
	// 16
	private String namesPosFreteBin[] = { "bin", "range", "emitter", "flagsPosFrete", "chipQuestions", "offsetYear",
			"offsetMonth", "offsetServiceCode", "offsetCVV", "offsetYearChip", "offsetMonthChip", "offsetServiceCodeChip",
			"offsetCvvChip", "serviceCodeChip" };
	private int[] sizesPosFreteBin = { 6, 4, 2, 8, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
	private int[] typesPosFreteBin = { 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] contentsPosFreteBin = { 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public CaptureTables(EntityManager entityManager, boolean isJTA) {
		this.entityManager = entityManager;
		this.isJTA = isJTA;
	}
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private String getData(Object obj, String name, int type, int sizeType, int size) {
		String prefix = "";
		String data = "";

		try {
			if (obj != null) {
				Field field = obj.getClass().getDeclaredField(name);
				field.setAccessible(true);
				Object value = field.get(obj);
				data = value.toString();
				// type : 0 -> n, 2 -> b, 3 -> ans
				if (type == 0) {
					int diff = size - data.length();

					for (int i = 0; i < diff; i++) {
						data = "0" + data;
					}
				} else if (type == 3) {
					data = ByteArrayUtils.getHexStr(data);
				}
				// sizeType : 0, 2, 6
				if (sizeType > 0) {
					StringBuilder buffer = new StringBuilder(size);
					Integer len = data.length() / sizeType;
					prefix = Utils.getLeftAlign(buffer, len.toString(), '0', size);
					/*
					 * int cnt = Utils.parseInt(data, offset, size);
					 * System.out.printf("prefix [%04d] = %s\n", size,
					 * data.substring(offset, offset+size)); offset += size; posEnd =
					 * offset + (cnt * sizeType);
					 */
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (prefix.length() > 0) {
			this.bufferDebug.append(String.format("%40s [%04d] : %s [size header]\n", name, prefix.length(), prefix));
		}

		this.bufferDebug.append(String.format("%40s [%04d] : %s\n", name, data.length(), data));
		return prefix + data;
	}

	private void setData(Object obj, String name, int type, String str) {
		try {
			if (name != null && str != null && str.length() > 0) {
				String className = obj != null ? obj.getClass().toString() : "";
				Object value = str;

				if (type == 0) {
					if (Utils.isDecimal(str, 0, str.length())) {
						value = new Integer(str);
					} else {
						this.bufferDebug.append(String.format("Error in setData : setData(%s, %s, %s, %s)\n\n", className, name, type, str));
						this.bufferDebug.append(String.format("Ivalid decimal value - class : %s - field = %s - value = %s\n\n", className, name, str));
						value = null;
					}
				} else if (type == 1) {
					if (Utils.isHex(str, 0, str.length(), true, false)) {
						int hex = ByteArrayUtils.hexToInt(str, 0, str.length(), '0', '0');
						value = hex;
					} else {
						this.bufferDebug.append(String.format("Error in setData : setData(%s, %s, %s, %s)\n\n", className, name, type, str));
						this.bufferDebug.append(String.format("Ivalid hex value - class : %s - field = %s - value = %s\n\n", className, name, str));
						value = null;
					}
				} else if (type == 2) {
					// string
				} else if (type == 3) {
					// ASCII Hex to ASCII
					auxBuffer.setLength(0);
					ByteArrayUtils.appendAsciiHexToBinary(auxBuffer, str, 0, str.length(), 2, '0', '0');
					value = auxBuffer.toString();
				} else {
					this.bufferDebug.append(String.format("Unknow type : %s\n", type));
				}

				this.bufferDebug.append(String.format("%40s [%04d] : %s\n", name, value.toString().length(), value));

				if (obj != null) {
					Field field = obj.getClass().getDeclaredField(name);
					field.setAccessible(true);
					field.set(obj, value);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			this.bufferDebug.append(String.format("Error in setData : setData(%s, %s, %s, %s)\n\n", obj.getClass(), name, type, str));
			this.bufferDebug.append(String.format("%s\n", e.getMessage()));
		}
	}

	private void persist(Object obj, String data, String[] names, int[] sizes, int[] types, int[] contents) {
		this.bufferDebug.append(String.format("%47s : %s\n", "table name", obj.getClass().getSimpleName()));
		int offset = 0;
		
		for (int i = 0; i < names.length; i++) {
			try {
				int size = sizes[i];
				int posEnd = offset + size;
				int sizeType = types[i];

				if (posEnd <= data.length()) {
					if (sizeType > 0) {
						if (Utils.isDecimal(data, offset, size)) {
							int cnt = ByteArrayUtils.parseInt(data, offset, size);
							this.bufferDebug.append(String.format("%33s prefix [%04d] : %s\n", "", size, data.substring(offset, offset + size)));
							offset += size;
							posEnd = offset + (cnt * sizeType);

							if (posEnd > data.length()) {
								this.bufferDebug.append(String.format("Ivalid field end : table = %s - field = %s - offset = %s\n", obj, names[i], sizes[i], data.substring(offset)));
								break;
							}
						} else {
							this.bufferDebug.append(String.format("Ivalid field size : table = %s - field = %s - offset = %s\n", obj, names[i], sizes[i],	data.substring(offset)));
							break;
						}
					}

					String str = data.substring(offset, posEnd);
					setData(obj, names[i], contents[i], str);
					offset = posEnd;
					// System.out.println(str);
				} else {
					this.bufferDebug.append(String.format("Ivalid field end : table = %s - field = %s - offset = %s\n", obj, names[i], sizes[i], data.substring(offset)));
					break;
				}
			} catch (Exception e) {
				this.bufferDebug.append(String.format("Error : table = %s - field = %s - offset = %s\n", obj, names[i], sizes[i],	data.substring(offset)));
				this.bufferDebug.append(String.format("%s\n", e.getMessage()));
			}
		}

		if (obj != null & this.entityManager != null) {
			if (isJTA == true) {
				if (this.userTransaction != null) {
					try {
						this.userTransaction.begin();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				entityManager.getTransaction().begin();
			}

			try {
				this.entityManager.persist(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (isJTA == true) {
				if (this.userTransaction != null) {
					try {
						this.userTransaction.commit();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				try {
					entityManager.getTransaction().commit();
				} catch (Exception e) {
					e.printStackTrace();
				}

				entityManager.clear();
			}
		}
		// DEBUG
		this.bufferDebug.append(String.format("\n       %40s :\n", "Campos gerados"));
		genarateTable(0, obj, null, names, sizes, types, contents);
	}

	private boolean parseTable_tefHct_920000(int tableId, String data) {
		boolean ret = true;
		// System.out.println("\t\"" + data.substring(offset, offset + 4) + "\" +");
		// System.out.println("\t\"" + data.substring(offset + 4, posEnd) + "\" +");

		if (tableId == 1) {
			String namesTefTerminal[] = { "version", "helpDesk", "crypt", "workingkey", "indexMasterkey", "flags1", "flags2", "flags3",
					"receiptLine1", "receiptLine2", "receiptLine3", "cnpj", "limitRemind", "adquirent", "dte1", "dte2",
					"timeout", "transactionCategory", "merchantType", "terminalCategory", "countryCode", "codeMoney", };
			int[] sizesTefTerminal = { 7, 12, 2, 32, 2, 2, 2, 2, 40, 40, 40, 20, 12, 20, 16, 16, 2, 1, 4, 3, 4, 4 };
			int[] typesTefTerminal = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			int[] contentsTefTerminal = { 2, 2, 2, 2, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2 };
			ISO8583Terminal tefTerminal = new ISO8583Terminal();
			tefTerminal.setId(1);
			persist(tefTerminal, data, namesTefTerminal, sizesTefTerminal, typesTefTerminal, contentsTefTerminal);
		} else if (tableId == 2) {
			String namesTefBin[] = { "id", "bin", "range", "emitter", "provider", "product", "panSize", /*"cryptType",*/ "flags", "flows" };
			int[] sizesTefBin =    { 4, 6, 6, 2, 4, 4, 2, /*2,*/ 6, 2 };
			int[] typesTefBin =    { 0, 0, 0, 0, 0, 0, 0, /*0,*/ 0, 3 };
			int[] contentsTefBin = { 0, 0, 0, 2, 0, 0, 0, /*0,*/ 1, 2 };
			persist(new ISO8583Bin(), data, namesTefBin, sizesTefBin, typesTefBin, contentsTefBin);
		} else if (tableId == 3) {
			String namesTefProduct[] = { "id", "text", "msgType", "codeProcess", "flags", "flows" };
			int[] sizesTefProduct = { 4, 24, 4, 6, 4, 2 };
			int[] typesTefProduct = { 0, 0, 0, 0, 0, 3 };
			int[] contentsTefProduct = { 0, 2, 0, 0, 1, 2 };
			persist(new ISO8583TefProduct(), data, namesTefProduct, sizesTefProduct, typesTefProduct, contentsTefProduct);
		} else if (tableId == 4) {
			String namesTefProvider[] = { "id", "text", "flags" };
			int[] sizesTefProvider = { 4, 24, 4 };
			int[] typesTefProvider = { 0, 0, 0 };
			int[] contentsTefProvider = { 0, 2, 1 };
			persist(new ISO8583TefProvider(), data, namesTefProvider, sizesTefProvider, typesTefProvider, contentsTefProvider);
		} else if (tableId == 5) {
			String namesTefProductWithOutCard[] = { "id", "provider", "flows" };
			int[] sizesTefProductWithOutCard = { 4, 4, 2 };
			int[] typesTefProductWithOutCard = { 0, 0, 3 };
			int[] contentsTefProductWithOutCard = { 0, 0, 2 };
			persist(new ISO8583TefProductWithoutCard(), data, namesTefProductWithOutCard, sizesTefProductWithOutCard, typesTefProductWithOutCard, contentsTefProductWithOutCard);
		} else if (tableId == 6) {
			String namesTefFlow[] = { "id", "text", "flags", "questions" };
			int[] sizesTefFlow = { 3, 2, 2, 2 };
			int[] typesTefFlow = { 0, 1, 0, 4 };
			int[] contentsTefFlow = { 0, 2, 1, 2 };
			persist(new ISO8583TefFlow(), data, namesTefFlow, sizesTefFlow, typesTefFlow, contentsTefFlow);
		} else if (tableId == 7) {
			String names[] = { "id", "text", "mask", "min", "max", "flags" };
			int[] sizes = { 4, 3, 2, 2, 2, 2 };
			int[] types = { 0, 1, 0, 0, 0, 0 };
			int[] contents = { 0, 2, 0, 0, 0, 1 };
			persist(new ISO8583TefQuestion(), data, names, sizes, types, contents);
		} else if (tableId == 8) {
//			persist(new ChipApplicationIdentifier(), data, namesTefAid, sizesTefAid, typesTefAid, contentsTefAid);
		} else if (tableId == 9) {
			String names[] = { "id", "text", "mask" };
			int[] sizes = { 4, 3, 2 };
			int[] types = { 0, 1, 0 };
			int[] contents = { 0, 2, 0 };
//			persist(new ChipPublicKey(), data, names, sizes, types, contents);
		} else if (tableId == 10) {
			String names[] = { "id", "text", "mask" };
			int[] sizes = { 4, 3, 2 };
			int[] types = { 0, 1, 0 };
			int[] contents = { 0, 2, 0 };
//			persist(new TefCrypt(), data, names, sizes, types, contents);
		} else {
			this.bufferDebug.append(String.format("Invalid table id value : %s\n", tableId));
			ret = false;
		}

		return ret;
	}

	private boolean parseTable_posFrete_950032(int tableId, String data) {
		boolean ret = true;

		if (tableId == 1) {
			ISO8583Terminal obj = new ISO8583Terminal();
			persist(obj, data, namesPosFreteTerminal, sizesPosFreteTerminal, typesPosFreteTerminal,	contentsPosFreteTerminal);
		} else if (tableId == 2) {
			ISO8583PosFreteMoney obj = new ISO8583PosFreteMoney();
			persist(obj, data, namesPosFreteMoney, sizesPosFreteMoney, typesPosFreteMoney, contentsPosFreteMoney);
		} else if (tableId == 8) {
			ISO8583RouterChipApplicationIdentifier obj = new ISO8583RouterChipApplicationIdentifier();
			persist(obj, data, namesPosFreteAid, sizesPosFreteAid, typesPosFreteAid, contentsPosFreteAid);
		} else if (tableId == 9) {
			ISO8583RouterChipPublicKey obj = new ISO8583RouterChipPublicKey();
			persist(obj, data, namesPosFreteKey, sizesPosFreteKey, typesPosFreteKey, contentsPosFreteKey);
		} else if (tableId == 16) {
			ISO8583Bin obj = new ISO8583Bin();
			persist(obj, data, namesPosFreteBin, sizesPosFreteBin, typesPosFreteBin, contentsPosFreteBin);
		} else {
			this.bufferDebug.append(String.format("Invalid table id value : %s\n", tableId));
			ret = false;
		}

		return ret;
	}

	private void genarateTable(int tableId, Object object, StringBuilder buffer, String[] names, int[] sizes,	int[] sizeTypes, int[] contents) {
		StringBuilder aux = new StringBuilder(10 * 1024);

		for (int i = 0; i < names.length; i++) {
			try {
				String str = getData(object, names[i], contents[i], sizeTypes[i], sizes[i]);
				aux.append(str);
			} catch (Exception e) {
				this.bufferDebug.append(String.format("%s\n", e.getMessage()));
			}
		}

		if (buffer != null) {
			buffer.append(String.format("%02d", tableId));
			buffer.append(String.format("%04d", aux.length() / 2));
			buffer.append(aux);
		}

		this.bufferDebug.append(String.format("       %40s : %s\n\n", "genarated table sizeTable", aux.length() / 2));
	}

	private void genarateTable(int tableId, String className, StringBuilder buffer, String[] names, int[] sizes, int[] sizeTypes, int[] contents) {
		this.bufferDebug.append(String.format("\n       %40s : %s\n", "table id", tableId));
		List<?> list = entityManager.createQuery("from " + className + " o").getResultList();

		for (Object object : list) {
			genarateTable(tableId, object, buffer, names, sizes, sizeTypes, contents);
		}
	}

	public String generateTable_posFrete_950032() {
		StringBuilder buffer = new StringBuilder(10 * 1024);
		this.tableHeaderIdSize = 2;
		this.tableHeaderLengthSize = 4;
		this.tableSizeFactor = 2;
		genarateTable(1, "Terminal", buffer, namesPosFreteTerminal, sizesPosFreteTerminal,
				typesPosFreteTerminal, contentsPosFreteTerminal);
		genarateTable(2, "PosFreteMoney", buffer, namesPosFreteMoney, sizesPosFreteMoney, typesPosFreteMoney,
				contentsPosFreteMoney);
		genarateTable(8, "ChipApplicationIdentifier", buffer, namesPosFreteAid, sizesPosFreteAid, typesPosFreteAid,
				contentsPosFreteAid);
		genarateTable(9, "ChipPublicKey", buffer, namesPosFreteKey, sizesPosFreteKey, typesPosFreteKey,
				contentsPosFreteKey);
		genarateTable(16, "Bin", buffer, namesPosFreteBin, sizesPosFreteBin, typesPosFreteBin,
				contentsPosFreteBin);
		return buffer.toString();
	}

	public String generateTable_tefHct_920000() {
		StringBuilder buffer = new StringBuilder(10 * 1024);
		this.tableHeaderIdSize = 2;
		this.tableHeaderLengthSize = 3;
		this.tableSizeFactor = 1;
		genarateTable(1, "Terminal", buffer, namesPosFreteTerminal, sizesPosFreteTerminal,
				typesPosFreteTerminal, contentsPosFreteTerminal);
		genarateTable(2, "PosFreteMoney", buffer, namesPosFreteMoney, sizesPosFreteMoney, typesPosFreteMoney,
				contentsPosFreteMoney);
		genarateTable(8, "ChipApplicationIdentifier", buffer, namesPosFreteAid, sizesPosFreteAid, typesPosFreteAid,
				contentsPosFreteAid);
		genarateTable(9, "ChipPublicKey", buffer, namesPosFreteKey, sizesPosFreteKey, typesPosFreteKey,
				contentsPosFreteKey);
		genarateTable(16, "Bin", buffer, namesPosFreteBin, sizesPosFreteBin, typesPosFreteBin,
				contentsPosFreteBin);
		return buffer.toString();
	}

	public String parse(String data, String codeProcess, boolean clearDb) {
		this.bufferDebug.setLength(0);
		
		if (codeProcess.equals("920000")) {
			this.tableHeaderIdSize = 2;
			this.tableHeaderLengthSize = 3;
			this.tableSizeFactor = 1;
		} else if (codeProcess.equals("950032")) {
			this.tableHeaderIdSize = 2;
			this.tableHeaderLengthSize = 4;
			this.tableSizeFactor = 2;
		}

		if (this.entityManager != null && clearDb) {
			// this.entityManager.getTransaction().begin();
			this.entityManager.createQuery("DELETE FROM Bin a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM Terminal a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM ChipApplicationIdentifier a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM ChipPublicKey a").executeUpdate();
			
			this.entityManager.createQuery("DELETE FROM PosFreteMoney a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM TefProduct a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM TefProvider a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM TefProductWithoutCard a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM TefFlow a").executeUpdate();
			this.entityManager.createQuery("DELETE FROM TefQuestion a").executeUpdate();
			// this.entityManager.getTransaction().commit();
		}

		int offset = 0;

		while (offset < data.length()) {
			if (Utils.isDecimal(data, offset, 2)) {
				int tableId = ByteArrayUtils.parseInt(data, offset, this.tableHeaderIdSize);
				// this.bufferDebug.append();
				this.bufferDebug.append(String.format("\n       %40s : %s\n", "table id", tableId));
				// System.out.println("\n\t\"" + data.substring(offset, offset + 2) +
				// "\" +");
				offset += this.tableHeaderIdSize;

				if (Utils.isDecimal(data, offset, this.tableHeaderLengthSize)) {
					int sizeTable = ByteArrayUtils.parseInt(data, offset, this.tableHeaderLengthSize);
					this.bufferDebug.append(String.format("       %40s : %s\n", "parsed table sizeTable", sizeTable));
					int size = sizeTable * this.tableSizeFactor;
					// System.out.println("\t\"" + data.substring(offset, offset +
					// this.tableHeaderLengthSize) + "\" +");
					offset += this.tableHeaderLengthSize;
					int posEnd = offset + size;

					if (posEnd <= data.length()) {
						String table = data.substring(offset, posEnd);
						
						if (codeProcess.equals("920000")) {
							if (parseTable_tefHct_920000(tableId, table) == false) {
								break;
							}
						} else if (codeProcess.equals("950032")) {
							if (parseTable_posFrete_950032(tableId, table) == false) {
								break;
							}
						}
					} else {
						this.bufferDebug.append(String.format("Invalid table end : %s\n", tableId));
						break;
					}

					offset = posEnd;
				} else {
					this.bufferDebug.append("Invalid table size\n");
					break;
				}
			} else {
				this.bufferDebug.append(String.format("Invalid table id content : %s\n", data.substring(offset)));
				break;
			}
		}

		if (this.entityManager != null && this.isJTA == false) {
			try {
				FileOutputStream fileOutputStream = new FileOutputStream("BinHct.txt");
				String str = ISO8583Bin.report(this.entityManager);
				fileOutputStream.write(str.getBytes());
				fileOutputStream.close();
			} catch (Exception e) {
				this.bufferDebug.append(String.format("%s\n", e.getMessage()));
				e.printStackTrace();
			}
		}
		
		return this.bufferDebug.toString();
	}

	public static void main(String[] args) {
		// POS FRETE
		// 01010700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000076000000000000000000000000000200100986805224202000000208011907A0000005081214000100010001265F349F029F039F1A955F2A9A9C9F37829F369F279F109F269F33E0F8C8F000F0A001FC78BC88000000000000FC78BC880022000000000000000000000000520000000000000000000000000000000000000000000000000000000000000000000000000000000002090224A000000508F0010300000192A118B34F9E910DA92ABFDBE6CD320D87689263F78CC340D1FFD3B868E6569FD96452E8391ED22B533663FF7208C3AB94E97514FF0EBD339C5FA4574B460D22DAD2A6C50BF832EBFDC19AC338C38EE87AC71795650494541D77DA11E41A9E0A85AF5E6601872B991F9FDB19CDC1A2DF4E26E67F0858207543E0B4EA7D799A09FBEA0370DE47A6E4247A8416F4CBC4A35AC6D24ABCABD853A4796902485FD92A4A37E883076EBB69A107688F24A43DE84E555ABBB7620465456F586BD37E7AB6A52A245EF56C7007ED85ABAEF50EAD06AA532E174D090280A00000050801030100010248B5E59F04D5CE312C3EA0A6A30221685376DA3E098BC9B09AFC4049B60E093A523F24E102B0593F5EF2B70B43FE8FF18E6AF426FFF9FEE36568256D1113288264337C1F581AF848DB0444C8605910756DD34788B7852E550E4A3F04134C8CDFD7E6E9B9B450507C8B4F3E7D3593FC4EF625239DC992DE3D09F6F26C3E0E0D394B6D65D82DFB278FA5E4A373F3E4F9BF9F1E16EEEA469D804388CA0BA8D602BE1DC3C8AE3BA4E6F7B974C5C2E3C62D64D172ADA412B60B1C70F59A7A6372244179F9D2521C93D524F464FE18CF9A50459E02FC5F4D4F5F0101B45B559EE1B006E77842B7E202A89529F126B6A4F56C9BB0ED7FBD658C439281E646D59F030D25553BB6D1203FBEDD35665647BB1600236056630000FF030F0F01016080001719212317192123621600116035740000170000000000
		// 01, 0107, [184]
		// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
		// [4] 0076, [26] 00000000000000000000000000
		// 02, 0010, [4] 0986, [2] 80, [8] 52242020, [2] 00, [2] 00, [2] 02
		// 08, 0119, [07] A0000005081214 [4] 0001 [4] 0001 [4] 0001 [26] 5F34 9F02
		// 9F03 9F1A 95 5F2A 9A 9C 9F37 82 9F36 9F27 9F10 9F26 9F33 [6] E0F8C8 [10]
		// F000F0A001 [10] FC78BC8800 [10] 0000000000 [10] FC78BC8800 [2] 22 [2] 00
		// [2] 00 [8] 00000000 [8] 00000000 [4] 0000 [2] 52 [40]
		// 0000000000000000000000000000000000000000 [40]
		// 0000000000000000000000000000000000000000 [2] 02
		// 09, 0224, [10] A000000508 [2] F0 [2] 01 [6] 030000 [4] 0192
		// A118B34F9E910DA92ABFDBE6CD320D87689263F78CC340D1FFD3B868E6569FD96452E8391ED22B533663FF7208C3AB94E97514FF0EBD339C5FA4574B460D22DAD2A6C50BF832EBFDC19AC338C38EE87AC71795650494541D77DA11E41A9E0A85AF5E6601872B991F9FDB19CDC1A2DF4E26E67F0858207543E0B4EA7D799A09FBEA0370DE47A6E4247A8416F4CBC4A35AC6D24ABCABD853A4796902485FD92A4A37E883076EBB69A107688F24A43DE84E555ABBB7620465456F586BD37E7AB6A5
		// [40] 2A245EF56C7007ED85ABAEF50EAD06AA532E174D
		// 09, 0280, [10] A000000508 [2] 01 [2] 03 [6] 010001 [4] 0248
		// B5E59F04D5CE312C3EA0A6A30221685376DA3E098BC9B09AFC4049B60E093A523F24E102B0593F5EF2B70B43FE8FF18E6AF426FFF9FEE36568256D1113288264337C1F581AF848DB0444C8605910756DD34788B7852E550E4A3F04134C8CDFD7E6E9B9B450507C8B4F3E7D3593FC4EF625239DC992DE3D09F6F26C3E0E0D394B6D65D82DFB278FA5E4A373F3E4F9BF9F1E16EEEA469D804388CA0BA8D602BE1DC3C8AE3BA4E6F7B974C5C2E3C62D64D172ADA412B60B1C70F59A7A6372244179F9D2521C93D524F464FE18CF9A50459E02FC5F4D4F5F0101B45B559EE1B006E77842B7E202A89529F126B6A4F56C9BB0ED7FBD658C439281E646D59F030D25553BB6D1203FBEDD35665647BB
		// 16, 0023, [6] 605663 [4] 0000 [2] FF [8] 030F0F01 [2] 01 [6] 608000 [2]
		// 17 [2] 19 [2] 21 [2] 23 [2] 17 [2] 19 [2] 21 [2] 23 [2] 62
		// 16, 0011, [6] 603574 [4] 0000 [2] 17 [8] 00000000 [2] 00

		// 08, 0119, [07] A0000005081010 [4] 0001 [4] 0001 [4] 0001 [26] 5F34 9F02
		// 9F03 9F1A 95 5F2A 9A 9C
		// 9F37829F369F279F109F269F33E0F8C8F000F0A001FC78BC88000000000000FC78BC880022000000000000000000000000520000000000000000000000000000000000000000000000000000000000000000000000000000000001
		// 16, 0017, [6] 604868 [4] 0000 [2] 12 [8] 03040401 [2] 01 [6] 828018 [2]
		// 21 [2] 21 [2] 62

		// TEF

		CaptureTables tables = new CaptureTables(null, false);
		String data =

		"01"
				+ "283"
				+ "0000"
				+ "00100004003502500AE68442157E37893000000000000000003200000VERTICAL REESTRUTURACAO                 RUA PERNAMBUCO                          PORTO ALEGRE                            91662967000194      000000001000GETNET              00000000        00000000        30R322000008560986"
				+

				"02"
				+ "039"
				+ "0001"
				+ "504169504169##000500010060600001007"
				+

				"02"
				+ "039"
				+ "0002"
				+ "604997604998##0005000100E0000001007"
				+

				"02"
				+ "039"
				+ "0003"
				+ "610599610599##0005001300E0000001007"
				+

				"02"
				+ "039"
				+ "0004"
				+ "639269639269##000500010060600001007"
				+

				"02"
				+ "039"
				+ "0005"
				+ "589657589657##0006000100E0000001002"
				+

				"02"
				+ "039"
				+ "0006"
				+ "603522"+"603522"+"##"+"0006"+"0001"+"00"+""+"E80000"+"01"+"002"
				+

				"02"
				+ "039"
				+ "0007"
				+ "604201604201##000600010060600001007"
				+

				"02"
				+ "039"
				+ "0008"
				+ "604201604201##000600020068000001001"
				+

				"02"
				+ "039"
				+ "0009"
				+ "604202604202##0006000100E8000001002"
				+

				"02"
				+ "039"
				+ "0010"
				+ "604203604203##000600010060600001007"
				+

				"02"
				+ "039"
				+ "0011"
				+ "604203604203##000600020068000001001"
				+

				"02"
				+ "039"
				+ "0012"
				+ "604205604213##0006000100E8000001002"
				+

				"02"
				+ "039"
				+ "0013"
				+ "604215604217##0006000100E8000001002"
				+

				"02"
				+ "039"
				+ "0014"
				+ "604219604219##0006000100E8000001002"
				+

				"02"
				+ "039"
				+ "0015"
				+ "604220604220##000600020068000001001"
				+

				"02"
				+ "039"
				+ "0016"
				+ "604237604237##000600010060000001002"
				+

				"02"
				+ "039"
				+ "0017"
				+ "507860507860##0023000100E0000001007"
				+

				"02"
				+ "039"
				+ "0018"
				+ "505783505783##0030000100E0000001007"
				+

				"02"
				+ "039"
				+ "0019"
				+ "627892627892##0030000100E8000001007"
				+

				"02"
				+ "039"
				+ "0020"
				+ "636414636414##0030000100E0000001007"
				+

				"02"
				+ "039"
				+ "0021"
				+ "955500955559##0030000100E0000001009"
				+

				"02"
				+ "039"
				+ "0022"
				+ "955500955559##003000170060000001008"
				+

				"02"
				+ "039"
				+ "0023"
				+ "627777627777##0046001300E0000001007"
				+

				"02"
				+ "039"
				+ "0024"
				+ "628028628028##004900010060600001007"
				+

				"02"
				+ "039"
				+ "0025"
				+ "960331960346##004900010060600001007"
				+

				"02"
				+ "036"
				+ "0026"
				+ "639401639401##005200020068000000"
				+

				"02"
				+ "039"
				+ "0027"
				+ "001900001999##0054000100E0600001006"
				+

				"02"
				+ "039"
				+ "0028"
				+ "001900001999##0054001400E0000001007"
				+

				"02"
				+ "039"
				+ "0029"
				+ "001900001999##0054001800E0000001007"
				+

				"02"
				+ "036"
				+ "0030"
				+ "001900001999##0054001900E0000000"
				+

				"02"
				+ "036"
				+ "0031"
				+ "001900001999##0054002000E0000000"
				+

				"02"
				+ "036"
				+ "0032"
				+ "001900001999##0054002200E0000000"
				+

				"02"
				+ "039"
				+ "0033"
				+ "604792604792##0054000100E0600001006"
				+

				"02"
				+ "039"
				+ "0034"
				+ "604792604792##0054001400E0000001007"
				+

				"02"
				+ "039"
				+ "0035"
				+ "604792604792##0054001800E0000001007"
				+

				"02"
				+ "036"
				+ "0036"
				+ "604792604792##0054001900E0000000"
				+

				"02"
				+ "036"
				+ "0037"
				+ "604792604792##0054002000E0000000"
				+

				"02"
				+ "036"
				+ "0038"
				+ "604792604792##0054002200E0000000"
				+

				"02"
				+ "039"
				+ "0039"
				+ "627894627894##0054000100E0600001006"
				+

				"02"
				+ "039"
				+ "0040"
				+ "627894627894##0054001400E0000001007"
				+

				"02"
				+ "039"
				+ "0041"
				+ "627894627894##0054001800E0000001007"
				+

				"02"
				+ "036"
				+ "0042"
				+ "627894627894##0054001900E0000000"
				+

				"02"
				+ "036"
				+ "0043"
				+ "627894627894##0054002000E0000000"
				+

				"02"
				+ "036"
				+ "0044"
				+ "627894627894##0054002200E0000000"
				+

				"02"
				+ "039"
				+ "0045"
				+ "980010980019##007600010060000001007"
				+

				"02"
				+ "039"
				+ "0046"
				+ "627517627517##008800130060000001007"
				+

				"02"
				+ "039"
				+ "0047"
				+ "60496260496200010200010060600001007"
				+

				"02"
				+ "039"
				+ "0048"
				+ "60496260496201010200010060600001007"
				+

				"02"
				+ "039"
				+ "0049"
				+ "62784062784000010200010060600001007"
				+

				"02"
				+ "039"
				+ "0050"
				+ "62784062784001010200010060600001007"
				+

				"02"
				+ "036"
				+ "0051"
				+ "62784062784070010200020068000000"
				+

				"02"
				+ "039"
				+ "0052"
				+ "604650604650##011500011968600001007"
				+

				"02"
				+ "039"
				+ "0053"
				+ "605089605089##011500011968600001007"
				+

				"02"
				+ "039"
				+ "0054"
				+ "606291606291##011500011968600001007"
				+

				"02"
				+ "039"
				+ "0055"
				+ "606421606421##011500011968000001007"
				+

				"02"
				+ "039"
				+ "0056"
				+ "628159628159##011500011968000001007"
				+

				"02"
				+ "039"
				+ "0057"
				+ "636700636700##011500011968000001007"
				+

				"02"
				+ "039"
				+ "0058"
				+ "639445639445##011500011968600001007"
				+

				"02"
				+ "039"
				+ "0059"
				+ "606427606427##011800010068600001007"
				+

				"02"
				+ "039"
				+ "0060"
				+ "636450636450##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0061"
				+ "902501902506##012200010060600001007"
				+

				"02"
				+ "039"
				+ "0062"
				+ "902601902606##012200010060600001007"
				+

				"02"
				+ "039"
				+ "0063"
				+ "904501904502##012200010060600001007"
				+

				"02"
				+ "039"
				+ "0064"
				+ "904601904630##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0065"
				+ "904621904621##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0066"
				+ "904658904658##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0067"
				+ "904659904659##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0068"
				+ "904684904684##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0069"
				+ "904686904686##012200010060000001007"
				+

				"02"
				+ "039"
				+ "0070"
				+ "606387606387##0123000100E0600001004"
				+

				"02"
				+ "039"
				+ "0071"
				+ "606266606266##013000010068600001007"
				+

				"02"
				+ "039"
				+ "0072"
				+ "606266606266##013000150060000001007"
				+

				"02"
				+ "036"
				+ "0073"
				+ "502978502978##013400010060000000"
				+

				"02"
				+ "039"
				+ "0074"
				+ "639564639564##0143000100E8600001007"
				+

				"02"
				+ "039"
				+ "0075"
				+ "000002001899##0145000100E0600001007"
				+

				"02"
				+ "039"
				+ "0076"
				+ "002000011999##0145000100E0600001007"
				+

				"02"
				+ "039"
				+ "0077"
				+ "100000100099##0145000100E0600001007"
				+

				"02"
				+ "039"
				+ "0078"
				+ "629898629898##0145000100E0600001007"
				+

				"02"
				+ "036"
				+ "0079"
				+ "60357460357405015200010060000000"
				+

				"02"
				+ "039"
				+ "0080"
				+ "604868604868##0164000100E8600001007"
				+

				"02"
				+ "036"
				+ "0081"
				+ "60357460357406016500010060000000"
				+

				"02"
				+ "039"
				+ "0082"
				+ "605674605674##017000010068010001007"
				+

				"02"
				+ "036"
				+ "0084"
				+ "606014606014##017100010068000000"
				+

				"02"
				+ "036"
				+ "0085"
				+ "636623636623##0173000200E8600000"
				+

				"02"
				+ "039"
				+ "0086"
				+ "636786636786##017300010068000001007"
				+

				"02"
				+ "039"
				+ "0087"
				+ "604685604685##0174000100E0600001007"
				+

				"02"
				+ "039"
				+ "0088"
				+ "636769636769##017500010068600001007"
				+

				"02"
				+ "036"
				+ "0089"
				+ "636552636552##017900020068000000"
				+

				"02"
				+ "039"
				+ "0090"
				+ "60504160504140018200130068600001007"
				+

				"02"
				+ "039"
				+ "0091"
				+ "60504160504140018200170060000001007"
				+

				"02"
				+ "039"
				+ "0092"
				+ "60504160504143018200130068600001007"
				+

				"02"
				+ "039"
				+ "0093"
				+ "60504160504143018200170060000001007"
				+

				"02"
				+ "039"
				+ "0094"
				+ "60504160504148018200130068600001007"
				+

				"02"
				+ "039"
				+ "0095"
				+ "60504160504148018200170060000001007"
				+

				"02"
				+ "039"
				+ "0096"
				+ "60504160504149018200130068600001007"
				+

				"02"
				+ "039"
				+ "0097"
				+ "60504160504149018200170060000001007"
				+

				"02"
				+ "039"
				+ "0098"
				+ "61514161514140018200130068600001007"
				+

				"02"
				+ "039"
				+ "0099"
				+ "61514161514140018200170060000001007"
				+

				"02"
				+ "039"
				+ "0100"
				+ "62524162524140018200130068600001007"
				+

				"02"
				+ "039"
				+ "0101"
				+ "62524162524140018200170060000001007"
				+

				"02"
				+ "039"
				+ "0102"
				+ "65554165554140018200130068600001007"
				+

				"02"
				+ "039"
				+ "0103"
				+ "65554165554140018200170060000001007"
				+

				"02"
				+ "036"
				+ "0104"
				+ "627110627110##0183000200E8000000"
				+

				"02"
				+ "039"
				+ "0105"
				+ "627112627112##0183001300E8600001007"
				+

				"02"
				+ "036"
				+ "0106"
				+ "636909636909##0183000200E8000000"
				+

				"02"
				+ "039"
				+ "0107"
				+ "639447639447##0183000100E8600001004"
				+

				"02"
				+ "039"
				+ "0108"
				+ "628167628167##0184000100E0600001006"
				+

				"02"
				+ "039"
				+ "0109"
				+ "628167628167##0184001900E0000001005"
				+

				"02"
				+ "036"
				+ "0110"
				+ "63940163940103018600020068000000"
				+

				"02"
				+ "036"
				+ "0111"
				+ "63655763655700019100020068000000"
				+

				"02"
				+ "036"
				+ "0112"
				+ "63655763655701019100020068000000"
				+

				"02"
				+ "039"
				+ "0113"
				+ "63655763655702019100010068600001007"
				+

				"02"
				+ "036"
				+ "0114"
				+ "63655763655703019100020068000000"
				+

				"02"
				+ "036"
				+ "0115"
				+ "63655763655704019100020068000000"
				+

				"02"
				+ "036"
				+ "0116"
				+ "605663605663##019500020068000000"
				+

				"02"
				+ "039"
				+ "0118"
				+ "605673605673##019800010068600001007"
				+

				"02"
				+ "039"
				+ "0119"
				+ "637089637089##020100010068600001004"
				+

				"02"
				+ "036"
				+ "0120"
				+ "637089637089##020100190060000000"
				+

				"02"
				+ "039"
				+ "0121"
				+ "627995627995##0202000100E8000001007"
				+

				"02"
				+ "036"
				+ "0122"
				+ "627995627995##0202001900E0000000"
				+

				"02"
				+ "036"
				+ "0123"
				+ "627995627995##0202002000E0000000"
				+

				"02"
				+ "039"
				+ "0124"
				+ "636908636908##0202000100E8000001007"
				+

				"02"
				+ "036"
				+ "0125"
				+ "636908636908##0202001900E0000000"
				+

				"02"
				+ "036"
				+ "0126"
				+ "636908636908##0202002000E0000000"
				+

				"02"
				+ "039"
				+ "0127"
				+ "636910636910##0202000100E8000001007"
				+

				"02"
				+ "036"
				+ "0128"
				+ "636910636910##0202001900E0000000"
				+

				"02"
				+ "036"
				+ "0129"
				+ "636910636910##0202002000E0000000"
				+

				"02"
				+ "039"
				+ "0130"
				+ "637197637197##020300130068000001003"
				+

				"02"
				+ "036"
				+ "0131"
				+ "606444606444##020400020068000000"
				+

				"02"
				+ "039"
				+ "0132"
				+ "606482606482##020400010068600001007"
				+

				"02"
				+ "039"
				+ "0133"
				+ "637257637257##020600130068000001003"
				+

				"02"
				+ "039"
				+ "0134"
				+ "636578636578##0207000119E8600001007"
				+

				"02"
				+ "039"
				+ "0135"
				+ "636578636578##0207001319E8600001007"
				+

				"02"
				+ "036"
				+ "0136"
				+ "605676605676##020900010068000000"
				+

				"02"
				+ "036"
				+ "0137"
				+ "627892627892##003000240000000000"
				+

				"02"
				+ "036"
				+ "0138"
				+ "955500955559##003000240000000000"
				+

				"02"
				+ "036"
				+ "0139"
				+ "639401639401##005200240000000000"
				+

				"02"
				+ "036"
				+ "0140"
				+ "980010980019##007600240000000000"
				+

				"02"
				+ "036"
				+ "0141"
				+ "627517627517##008800240000000000"
				+

				"02"
				+ "036"
				+ "0142"
				+ "60496260496200010200240000000000"
				+

				"02"
				+ "036"
				+ "0143"
				+ "60496260496201010200240000000000"
				+

				"02"
				+ "036"
				+ "0144"
				+ "62784062784000010200240000000000"
				+

				"02"
				+ "036"
				+ "0145"
				+ "62784062784001010200240000000000"
				+

				"02"
				+ "036"
				+ "0146"
				+ "62784062784070010200240000000000"
				+

				"02"
				+ "036"
				+ "0147"
				+ "606427606427##011800240000000000"
				+

				"02"
				+ "036"
				+ "0148"
				+ "606266606266##013000240000000000"
				+

				"02"
				+ "036"
				+ "0149"
				+ "502978502978##013400240000000000"
				+

				"02"
				+ "036"
				+ "0150"
				+ "639564639564##014300240000000000"
				+

				"02"
				+ "036"
				+ "0151"
				+ "60357460357405015200240000000000"
				+

				"02"
				+ "036"
				+ "0152"
				+ "60357460357406016500240000000000"
				+

				"02"
				+ "036"
				+ "0153"
				+ "605674605674##017000240008000000"
				+

				"02"
				+ "036"
				+ "0154"
				+ "636623636623##017300240008000000"
				+

				"02"
				+ "036"
				+ "0155"
				+ "636786636786##017300240000000000"
				+

				"02"
				+ "036"
				+ "0156"
				+ "636769636769##017500240008000000"
				+

				"02"
				+ "036"
				+ "0157"
				+ "636552636552##017900240000000000"
				+

				"02"
				+ "036"
				+ "0158"
				+ "60504160504140018200240000000000"
				+

				"02"
				+ "036"
				+ "0159"
				+ "60504160504143018200240000000000"
				+

				"02"
				+ "036"
				+ "0160"
				+ "60504160504148018200240000000000"
				+

				"02"
				+ "036"
				+ "0161"
				+ "60504160504149018200240000000000"
				+

				"02"
				+ "036"
				+ "0162"
				+ "61514161514140018200240000000000"
				+

				"02"
				+ "036"
				+ "0163"
				+ "62524162524140018200240000000000"
				+

				"02"
				+ "036"
				+ "0164"
				+ "65554165554140018200240000000000"
				+

				"02"
				+ "036"
				+ "0165"
				+ "627110627110##018300240000000000"
				+

				"02"
				+ "036"
				+ "0166"
				+ "627112627112##018300240000000000"
				+

				"02"
				+ "036"
				+ "0167"
				+ "636909636909##018300240000000000"
				+

				"02"
				+ "036"
				+ "0168"
				+ "639447639447##018300240000000000"
				+

				"02"
				+ "036"
				+ "0169"
				+ "628167628167##018400240000000000"
				+

				"02"
				+ "036"
				+ "0170"
				+ "63940163940103018600240000000000"
				+

				"02"
				+ "036"
				+ "0171"
				+ "63655763655700019100240008000000"
				+

				"02"
				+ "036"
				+ "0172"
				+ "63655763655701019100240008000000"
				+

				"02"
				+ "036"
				+ "0173"
				+ "63655763655702019100240008000000"
				+

				"02"
				+ "036"
				+ "0174"
				+ "63655763655703019100240008000000"
				+

				"02"
				+ "036"
				+ "0175"
				+ "63655763655704019100240008000000"
				+

				"02"
				+ "036"
				+ "0176"
				+ "605663605663##019500240008000000"
				+

				"02"
				+ "036"
				+ "0177"
				+ "605673605673##019800240000000000"
				+

				"02"
				+ "036"
				+ "0178"
				+ "627995627995##020200240000000000"
				+

				"02"
				+ "036"
				+ "0179"
				+ "636908636908##020200240000000000"
				+

				"02"
				+ "036"
				+ "0180"
				+ "636910636910##020200240000000000"
				+

				"02"
				+ "036"
				+ "0181"
				+ "606444606444##020400240008000000"
				+

				"02"
				+ "036"
				+ "0182"
				+ "606482606482##020400240008000000"
				+

				"02"
				+ "036"
				+ "0183"
				+ "636578636578##020700241908000000"
				+

				"02"
				+ "036"
				+ "0184"
				+ "636578636578##020700241908000000"
				+

				"02"
				+ "036"
				+ "0185"
				+ "605676605676##020900240008000000"
				+

				"03"
				+ "044"
				+ "0001"
				+ "CREDITO                 0200003000000000"
				+

				"03"
				+ "044"
				+ "0002"
				+ "DEBITO                  0200002000000000"
				+

				"03"
				+ "044"
				+ "0003"
				+ "PRE AUTORIZACAO         0200009000000000"
				+

				"03"
				+ "044"
				+ "0004"
				+ "VOUCHER                 0200002000000000"
				+

				"03"
				+ "044"
				+ "0007"
				+ "CONF PRE AUTORIZACAO    0200009800000000"
				+

				"03"
				+ "044"
				+ "0013"
				+ "CONVENIO                0200004000000000"
				+

				"03"
				+ "044"
				+ "0014"
				+ "CDC                     0200006000000000"
				+

				"03"
				+ "044"
				+ "0015"
				+ "CONSULTA CREDITO        0100950004000000"
				+

				"03"
				+ "044"
				+ "0016"
				+ "CONSULTA DEBITO         0100950005000000"
				+

				"03"
				+ "044"
				+ "0017"
				+ "CONSULTA CONVENIO       0100004010000000"
				+

				"03"
				+ "044"
				+ "0018"
				+ "CONSULTA CDC            0100006010000000"
				+

				"03"
				+ "044"
				+ "0019"
				+ "FATURA                  0200007000000000"
				+

				"03"
				+ "044"
				+ "0020"
				+ "CONSULTA FATURA         0100007010000000"
				+

				"03"
				+ "044"
				+ "0022"
				+ "ANTECIPACAO             0200008000000000"
				+

				"03"
				+ "044"
				+ "0024"
				+ "CONSULTA SALDO          0100001000000000"
				+

				"03"
				+ "044"
				+ "0027"
				+ "CONSULTA ANTECIPACAO    0100008010000000"
				+

				"03"
				+ "044"
				+ "0028"
				+ "SAQUE CREDITO           0200013000000000"
				+

				"04"
				+ "032"
				+ "0005"
				+ "UNIK                    1000"
				+

				"04"
				+ "032"
				+ "0006"
				+ "CABAL                   1000"
				+

				"04"
				+ "032"
				+ "0023"
				+ "CETELEM                 1000"
				+

				"04"
				+ "032"
				+ "0030"
				+ "SOROCRED                1000"
				+

				"04"
				+ "032"
				+ "0046"
				+ "PRATICARD               1000"
				+

				"04"
				+ "032"
				+ "0049"
				+ "CREDSYSTEM              1000"
				+

				"04"
				+ "032"
				+ "0052"
				+ "GOOD VALE               1000"
				+

				"04"
				+ "032"
				+ "0054"
				+ "VERDECARD               1000"
				+

				"04"
				+ "032"
				+ "0076"
				+ "SYSPRODATA              1000"
				+

				"04"
				+ "032"
				+ "0088"
				+ "VIVO AP                 1000"
				+

				"04"
				+ "032"
				+ "0102"
				+ "MINASCRED               1000"
				+

				"04"
				+ "032"
				+ "0115"
				+ "SOFTNEX                 1000"
				+

				"04"
				+ "032"
				+ "0118"
				+ "AUTO EXPRESSO           1000"
				+

				"04"
				+ "032"
				+ "0122"
				+ "CALCARD                 1000"
				+

				"04"
				+ "032"
				+ "0123"
				+ "HS FINANCEIRA           1000"
				+

				"04"
				+ "032"
				+ "0130"
				+ "UNIMAIS                 1000"
				+

				"04"
				+ "032"
				+ "0134"
				+ "ACEITO                  1000"
				+

				"04"
				+ "032"
				+ "0143"
				+ "ZAFFARI                 1000"
				+

				"04"
				+ "032"
				+ "0145"
				+ "PRATICARD II            1000"
				+

				"04"
				+ "032"
				+ "0152"
				+ "POWERCARD CP            1000"
				+

				"04"
				+ "032"
				+ "0164"
				+ "TOPAZIO HPS             1000"
				+

				"04"
				+ "032"
				+ "0165"
				+ "DIRECAO PLUS            1000"
				+

				"04"
				+ "032"
				+ "0170"
				+ "GOODCARD                1000"
				+

				"04"
				+ "032"
				+ "0171"
				+ "SOROVALE                1000"
				+

				"04"
				+ "032"
				+ "0173"
				+ "ATUAL GROUP             1000"
				+

				"04"
				+ "032"
				+ "0174"
				+ "LECCA COND              1000"
				+

				"04"
				+ "032"
				+ "0175"
				+ "ZEMA                    1000"
				+

				"04"
				+ "032"
				+ "0179"
				+ "NACIONAL BENEFICIOS     1000"
				+

				"04"
				+ "032"
				+ "0182"
				+ "BANCRED                 1000"
				+

				"04"
				+ "032"
				+ "0183"
				+ "BIG CARD                1000"
				+

				"04"
				+ "032"
				+ "0184"
				+ "FORTBRASIL              1000"
				+

				"04"
				+ "032"
				+ "0186"
				+ "GOODVALE PADRAO         1000"
				+

				"04"
				+ "032"
				+ "0191"
				+ "MAXXCARD                1000"
				+

				"04"
				+ "032"
				+ "0195"
				+ "ECOFROTAS               9100"
				+

				"04"
				+ "032"
				+ "0198"
				+ "ITS                     1000"
				+

				"04"
				+ "032"
				+ "0201"
				+ "NOVOCARD                1000"
				+

				"04"
				+ "032"
				+ "0202"
				+ "AVISTA                  1000"
				+

				"04"
				+ "032"
				+ "0203"
				+ "CONSIGNUM               1000"
				+

				"04"
				+ "032"
				+ "0204"
				+ "VALECARD                1000"
				+

				"04"
				+ "032"
				+ "0206"
				+ "METTACARD               1000"
				+

				"04"
				+ "032"
				+ "0207"
				+ "ASCARD                  1000"
				+

				"04"
				+ "032"
				+ "0209"
				+ "FLEETCOR                1000"
				+

				"05"
				+ "010"
				+ "0022"
				+ "005400"
				+

				"06"
				+ "017"
				+ "0010"
				+ "0000200040006"
				+

				"06"
				+ "021"
				+ "0020"
				+ "00003000400060007"
				+

				"06"
				+ "017"
				+ "0030"
				+ "0000200040007"
				+

				"06"
				+ "017"
				+ "0040"
				+ "0000200050007"
				+

				"06"
				+ "013"
				+ "0050"
				+ "000010006"
				+

				"06"
				+ "017"
				+ "0060"
				+ "0000200060007"
				+

				"06"
				+ "013"
				+ "0070"
				+ "000010007"
				+

				"06"
				+ "021"
				+ "0080"
				+ "00003000700100018"
				+

				"06"
				+ "017"
				+ "0090"
				+ "0000200070018"
				+

				"07"
				+ "041"
				+ "0004"
				+ "026SOLICITA ULTIMOS 4 DIGITOS02040480"
				+

				"07"
				+ "043"
				+ "0005"
				+ "028SOLICITA CODIGO DE SEGURANCA020303C0"
				+

				"07"
				+ "040"
				+ "0006"
				+ "025SOLICITA DATA DE VALIDADE13060680"
				+

				"07"
				+ "042"
				+ "0007"
				+ "027SOLICITA NUMERO DE PARCELAS020202E0"
				+

				"07"
				+ "027"
				+ "0010"
				+ "012SOLICITA CPF021111F0"
				+

				"07"
				+ "028"
				+ "0011"
				+ "013SOLICITA CNPJ021414F0"
				+

				"07"
				+ "026"
				+ "0012"
				+ "011SOLICITA RG021010F0"
				+

				"07"
				+ "041"
				+ "0014"
				+ "026SOLICITA DIA DO NASCIMENTO020202F0"
				+

				"07"
				+ "041"
				+ "0015"
				+ "026SOLICITA MES DO NASCIMENTO020202F0"
				+

				"07"
				+ "041"
				+ "0016"
				+ "026SOLICITA ANO DO NASCIMENTO020404F0"
				+

				"07"
				+ "039"
				+ "0017"
				+ "024SOLICITA PLANO DE COMPRA020010F0"
				+

				"07"
				+ "040"
				+ "0018"
				+ "025SOLICITA VALOR DE ENTRADA150012F0"
				+

				"07"
				+ "037"
				+ "0019"
				+ "022SOLICITA IDENTIFICACAO040030F0"
				+

				"07"
				+ "052"
				+ "0022"
				+ "037SOLICTA DATA DE PGTO PRIMEIRA PARCELA110606F0"
				+

				"07"
				+ "036"
				+ "0023"
				+ "021SOLICITA DDD TELEFONE021011F0"
				+

				"07"
				+ "032"
				+ "0024"
				+ "017DIGITE INFORMACAO020011F0"
				+

				"08"
				+ "284"
				+ "0011"
				+ "4A0000005081214FRETES                  000100010001E0F8C8F000F0A001220000000000FC78BC8800FC78BC88000000000000000000R986100000760000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000Y1Z1Y3Z30485F349F029F039F1A955F2A9A9C9F37829F369F109F269F33"
				+

				"09"
				+ "450"
				+ "001F"
				+ "0A000000508103000012A245EF56C7007ED85ABAEF50EAD06AA532E174D192A118B34F9E910DA92ABFDBE6CD320D87689263F78CC340D1FFD3B868E6569FD96452E8391ED22B533663FF7208C3AB94E97514FF0EBD339C5FA4574B460D22DAD2A6C50BF832EBFDC19AC338C38EE87AC71795650494541D77DA11E41A9E0A85AF5E6601872B991F9FDB19CDC1A2DF4E26E67F0858207543E0B4EA7D799A09FBEA0370DE47A6E4247A8416F4CBC4A35AC6D24ABCABD853A4796902485FD92A4A37E883076EBB69A107688F24A43DE84E555ABBB7620465456F586BD37E7AB6A5"
				+

				"10"
				+ "558"
				+ "0020"
				+ "1A0000005080100011E646D59F030D25553BB6D1203FBEDD35665647BBB5E59F04D5CE312C3EA0A6A30221685376DA3E098BC9B09AFC4049B60E093A523F24E102B0593F5EF2B70B43FE8FF18E6AF426FFF9FEE36568256D1113288264337C1F581AF848DB0444C8605910756DD34788B7852E550E4A3F04134C8CDFD7E6E9B9B450507C8B4F3E7D3593FC4EF625239DC992DE3D09F6F26C3E0E0D394B6D65D82DFB278FA5E4A373F3E4F9BF9F1E16EEEA469D804388CA0BA8D602BE1DC3C8AE3BA4E6F7B974C5C2E3C62D64D172ADA412B60B1C70F59A7A6372244179F9D2521C93D524F464FE18CF9A50459E02FC5F4D4F5F0101B45B559EE1B006E77842B7E202A89529F126B6A4F56C9BB0ED7FBD658C439281"
				+ "";
		tables.parse(data, "920000", true);
	}

}
