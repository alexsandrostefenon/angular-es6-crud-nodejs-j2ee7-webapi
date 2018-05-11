package org.domain.financial2.messages.comm.router;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.domain.commom.ByteArrayUtils;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;


public class TransactionsCommom {
	public static String atallaAkbKeyRefEquipament = "1PUNE000,6BA5DE3F9C4535D6A46CC41372A1ECBA712E0D8FCA9B139A,FE2DC20D21AD1BC5";
	public static String atallaAkbKeyRefKnown =      "1PUNE000,DE3079F83A11D5B6D86891868EB4B882AE05CDBB0AD33021,E15701C234F1B2D6";
	public static String masterKeyKnownDes =         "D29621EBFAD81E56";
	
	public static void setTables(Message transaction, String[] tables) {
		int numSequences = tables.length / 2;
		int sequenceIndex = Integer.parseInt(transaction.getSequenceIndex());
		String bit62 = tables[2*sequenceIndex];
		String bit63 = tables[2*sequenceIndex+1];
		boolean forceMoreData = false;
		sequenceIndex++;

		if (sequenceIndex < numSequences) {
			forceMoreData = true;
		}
		
		TransactionsCommom.setBits_057_062_063(transaction, bit62, bit63, false, forceMoreData);
	}
	
	public static void setBits_057_062_063(Message transaction, String data1, String data2, boolean isFullData, boolean forceMoreData) {
		String bit62 = data1;
		String bit63 = data2;
		// bit 57
		String sequenceIndexStr = transaction.getSequenceIndex();
		Integer sequenceIndex = 0;
		
		if (sequenceIndexStr != null) {
			sequenceIndex = Integer.parseInt(sequenceIndexStr);
			
			if (forceMoreData == true) {
				sequenceIndex++;
			}
		}
		
		int posIni = 0;
		
		if (isFullData == true) {
			posIni = sequenceIndex * 2 * 999;
		}

		int posEnd = posIni + 999;
		
		if (posEnd > data1.length()) {
			posEnd = data1.length();
			bit62 = data1.substring(posIni, posEnd);
			posIni = posEnd;
			posEnd += 999;
			
			if (posEnd > data1.length()) {
				posEnd = data1.length();
				// n�o tem mais dados para enviar
				sequenceIndex = 0;
			} else {
				// tem mais dados para enviar
				sequenceIndex++;				
			}
			
			bit63 = data1.substring(posIni, posEnd);
		} else {
			bit62 = data1.substring(posIni, posEnd);
		}
		
		transaction.setData( bit62);
		transaction.setDataComplement(bit63);
		
		if (sequenceIndexStr != null) {
			transaction.setSequenceIndex(sequenceIndex.toString());
		}
	}

	public static void setBits_057_062_063(Message transaction, String data) {
		setBits_057_062_063(transaction, data, null, true, false);
	}
	
	private static long requestParametros(Connector manager, Message transaction) throws Exception {
		long rc = -12;
//		sp_parametros_out_002 = 1
		transaction.setFieldData("sp_parametros_out_002", "1");
		transaction.setModuleOut("sp_parametros");
		rc = manager.commOut(transaction, null);
		
		if (rc >= 0) {
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	private static long requestAutocarga(Connector manager, Message transaction) throws Exception {
		long rc = -11;
//		sp_autocarga_out_002 = 2
		transaction.setFieldData("sp_autocarga_out_002", "2");
		transaction.setModuleOut("sp_autocarga");
		rc = manager.commOut(transaction, null);
		
		if (rc >= 0) {
			rc = requestParametros(manager, transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
		
	private static long requestConectividade(Connector manager, Message transaction) throws Exception {
		long rc = -10;
//		sp_conectividade_out_002 = 1
		transaction.setFieldData("sp_conectividade_out_002", "1");
		transaction.setModuleOut("sp_conectividade");
		rc = manager.commOut(transaction, null);
		
		if (rc >= 0) {
			rc = requestAutocarga(manager, transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	public static long execConectividadeAutocargaParametros(Connector manager, Message transaction) throws Exception {
		long rc = requestConectividade(manager, transaction);
		
		return rc;
	}

	// recebe em yyyymmdd e devolve em dd/mm/yyyy
	public static String getFormatedDate(String date) {
		if (date != null && date.length() == 8) {
			date = date.substring(6, 8) + "/" + date.substring(4, 6) + "/" + date.substring(0, 4);
		}
		
		return date;
	}
	
	// recebe em yyyymmdd e devolve em dd/mm/yyyy
	public static String getFormatedDate(Message transaction, int field) {
		String date = null;//transaction.getFieldData(field);
		return getFormatedDate(date);
	}
	
	public static String cryptDes(String masterKey, String dataInHex) {
		String ret = null;
		
		try {
			byte[] dataIn = new byte[dataInHex.length()/2];
			ByteArrayUtils.AsciiHexToBinary(dataIn, dataInHex);
			byte[] masterKeyBin = new byte[8];
			ByteArrayUtils.AsciiHexToBinary(masterKeyBin, masterKey);
			KeySpec ks = new DESKeySpec(masterKeyBin);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			SecretKey key = kf.generateSecret(ks);
			String algorithm = "DES/ECB/NoPadding";
			Cipher cryptDesProvider = Cipher.getInstance(algorithm);
			cryptDesProvider.init(Cipher.ENCRYPT_MODE, key);
			byte[] dataOut = cryptDesProvider.doFinal(dataIn);
			ret = ByteArrayUtils.getHexStr(dataOut, dataOut.length);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return ret;
	}
	
	
}
