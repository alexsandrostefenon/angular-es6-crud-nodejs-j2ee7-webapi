package org.domain.iso8583router.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.domain.commom.Utils;
import org.domain.commom.Utils.DataAlign;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapter;
import org.domain.iso8583router.entity.Iso8583RouterMessageAdapterItem;

@Singleton
public class Iso8583RouterMessageAdapterParser {
	
	private static int parseSizeHeader(String adapterClass, String str) {
		int ret = -1; // UNDEFINED SIZE
		
		if (str.equals("FIXED")) {
			ret = 0;
		} else if (str.equals("LVAR")) {
			ret = 1;
		} else if (str.equals("LLVAR")) {
			ret = 2;
		} else if (str.equals("LLLVAR")) {
			ret = 3;
		}
		
		return ret;
	}

	private static int parseDataType(String str) {
		int ret = 0;
		
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			
			if (ch == 'n') {
				ret |= Utils.DATA_TYPE_DECIMAL;
			} else if (ch == 'h') {
				ret |= Utils.DATA_TYPE_HEX;
			} else if (ch == 'm') {
				ret |= Utils.DATA_TYPE_MASK;
			} else if (ch == 'a') {
				ret |= Utils.DATA_TYPE_ALPHA;
			} else if (ch == 's') {
				ret |= Utils.DATA_TYPE_SPECIAL;
			}
		}
		
		return ret;
	}
	
	private static DataAlign parseDataAlignment(int dataType, String str) {
		DataAlign ret = DataAlign.NONE;
		
		if (dataType == Utils.DATA_TYPE_DECIMAL) {
			ret = DataAlign.ZERO_LEFT;
		} else {
			ret = DataAlign.NONE;
		}
		
		if (str.equals("SPACE_LEFT_ALIGNMENT")) {
			ret = DataAlign.SPACE_LEFT;
		} else if (str.equals("SPACE_RIGHT_ALIGNMENT")) {
			ret = DataAlign.SPACE_RIGHT;
		} else if (str.equals("ZERO_LEFT_ALIGNMENT")) {
			ret = DataAlign.ZERO_LEFT;
		} else if (str.equals("ZERO_RIGHT_ALIGNMENT")) {
			ret = DataAlign.ZERO_RIGHT;
		}
		
		return ret;
	}
	
	private static int parseUnsignedInt(String str, int maxDigits, int failValue) {
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
	
	private static void parseFile(UserTransaction userTransaction, EntityManager entityManager, File file) throws Exception {
		BufferedReader input = new BufferedReader(new FileReader(file));
		
		try {
			String line = input.readLine();
			
			if (line == null) {
				throw new Exception("[Iso8583RouterMessageAdapterParser.parseFile] : Missing first line");
			}
			// remove comentário no final da linha
			int pos = line.indexOf(';');
			
			if (pos >= 0) {
				line = line.substring(0, pos);
			}
			// name,parent,class
			String[] params = line.split(",");
			Iso8583RouterMessageAdapter adapter;
			boolean isNewRec = false;
			
			try {
				adapter = entityManager.createQuery("FROM Iso8583RouterMessageAdapter WHERE name=:name", Iso8583RouterMessageAdapter.class).
										setParameter("name", params[0]).getSingleResult();
			} catch (Exception e) {
				adapter = new Iso8583RouterMessageAdapter();
				adapter.setName(params[0]);
				isNewRec = true;
			}

			if (params[1].equals("-") == false) {
				adapter.setParent(params[1]);
			} else {
				adapter.setParent(null);
			}
			// type : ISO8583, XML, CSV, TSV, etc...
			adapter.setAdapterClass(params[2]);
			userTransaction.begin();
			if (isNewRec == false) adapter = entityManager.merge(adapter); else entityManager.persist(adapter);
			userTransaction.commit();
			line = input.readLine();
			int order = 1;
			
			while (line != null) {
				// remove possível comentário no final da linha
				pos = line.indexOf(';');
				
				if (pos >= 0) {
					line = line.substring(0, pos);
				}
				
				if (line.length() > 0) {
					String[] fields = line.split(",");

					if (fields.length == 9) {
						Iso8583RouterMessageAdapterItem adapterItem;
						isNewRec = false;
						
						try {
							adapterItem = entityManager.createQuery("FROM Iso8583RouterMessageAdapterItem WHERE messageAdapter=:messageAdapter and rootPattern=:rootPattern and tag=:tag", Iso8583RouterMessageAdapterItem.class).
									setParameter("messageAdapter", adapter.getName()).
									setParameter("rootPattern", fields[0]).
									setParameter("tag", fields[1]).
									getSingleResult();
						} catch (Exception e) {
							adapterItem = new Iso8583RouterMessageAdapterItem();
							adapterItem.setMessageAdapter(adapter.getName());
							adapterItem.setRootPattern(fields[0]);
							adapterItem.setTag(fields[1]);
							isNewRec = true;
						}
						
						adapterItem.setMinLength(parseUnsignedInt(fields[2], 4, 0));
						adapterItem.setMaxLength(parseUnsignedInt(fields[3], 4, 0));
						adapterItem.setSizeHeader(parseSizeHeader(adapter.getAdapterClass(), fields[4]));
						adapterItem.setDataType(parseDataType(fields[5]));

						if (fields[6].equals("-") == false) {
							adapterItem.setFieldName(fields[6]);
						}
						
						adapterItem.setAlignment(parseDataAlignment(adapterItem.getDataType(), fields[8]));
						adapterItem.setOrderIndex(order++);
						userTransaction.begin();
						if (isNewRec == false) adapterItem = entityManager.merge(adapterItem); else entityManager.persist(adapterItem);
						userTransaction.commit();
					} else {
						throw new Exception("Quantidade inválida de campos : " + line);
					}
				}
				
				line = input.readLine();
			}
		} finally {
			input.close();
		}
	}

	public static void loadConfs(UserTransaction userTransaction, EntityManager entityManager, String dirName) {
		File dir = new File(dirName);
		
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			
			for (File file : files) {
				String filename = file.getName();
				
				if (file.exists() && file.isFile() && filename.startsWith("config_") && filename.endsWith(".txt")) {
					try {
						parseFile(userTransaction, entityManager, file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;
	
	@Resource
	private UserTransaction userTransaction;
	
	@PostConstruct
	void postConstruct() {
//		Iso8583RouterMessageAdapterParser parser = new Iso8583RouterMessageAdapterParser();
//		Iso8583RouterMessageAdapterParser.loadConfs(parser.userTransaction, parser.entityManager, "/tmp");
	}

}
