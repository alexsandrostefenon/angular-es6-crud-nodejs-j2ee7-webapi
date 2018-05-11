package org.domain.financial2.messages.comm.router;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.domain.commom.Logger;
import org.domain.financial2.entity.CardConf;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Atalla {
	private static Atalla instance;
	private String atalla_mackey =     "1MDNE000,04B4A61451329507C741F10E3BB9A0DBDBFEBA44E553CB45,8576F6D2FD6100ED";
	private String atalla_dek =        "1DDNE000,33CB49FDC3AA85C8A72DE2521ED7497C1634F36C65637772,6FDD44ECA289BFBD";
/*
	private String atalla_source_pek = "1PUNE000,E1BC2AD559B9011CEDBFB52C23E74D84E112DFEDA0628170,210564E9EC428D80";
	private String atalla_target_pek = "1PUNE000,3DF3C5F36D2FC4E3102A6A443455E4FD892389665F3CF1F3,C85AF4C448E385AE";
*/
/*
"SOURCEPEK"="1PUNE000,A113AEC5CACF789FFC1C5652FAC521819DE57BF7C7752376,92FBE2FEE40C9A27"
"TARGETPEK"="1PUNE000,92002805D209DAFFCCE8A485B6C79895C197D9558127CA63,9D7CDC528936197A"
"DEK"="1DDNE000,A8789BEC78EFD1A0BAA3E78A21D0C6FA832355973403EE82,2B8757A5C685B39B"
"MACKEY"="1MDNE000,BA8DD4B32B6A01E85CC8DAA5EB03F2DBB49ED061E963E319,79EBE660A5A060FA"
 * 
 */
	private long atallaCryptTrackII(Connector manager, Message transaction, String trackII, String trackI) throws Exception {
		long rc = 0;
		StringBuilder buffer = new StringBuilder(256);
		atallaAddHex(buffer, trackII, 18, false);
		atallaAddHex(buffer, "=", 1, false);
		String[] fields = trackI.split("\\^");

		if (fields.length > 2) {
			atallaAddHex(buffer, fields[2], 40-18-1, false);
		}

		String send = String.format("<97#E#6#%s#D#U#80#%s#>", atalla_dek, buffer);
		transaction.setData(send);
		String module = transaction.getModule();
		transaction.setModuleOut("ATALLA");
		rc = manager.commOut(transaction, null);
		transaction.setModuleOut(module);

		if (rc >= 0) {
			String receive = transaction.getData();
			String[] params = receive.split("#");

			if (params.length > 8) {
				receive = params[8];
				transaction.setFieldData("trackII", receive);
			} else {
				rc = -1;
			}
		}

		return rc;
	}

	// criptografa trackI se existe
	private long atallaCryptTrackI(Connector manager, Message transaction, String trackI) throws Exception {
		long rc = 0;

		if (trackI != null) {
			StringBuilder buffer = new StringBuilder(256);
			int len = trackI.length();
			atallaAddHex(buffer, trackI, len, false);

			while (len % 8 != 0) {
				len++;
				buffer.append("00");
			}

			String send = String.format("<97#E#6#%s#D#U#%d#%s#>", atalla_dek, len*2, buffer);
			transaction.setData(send);
			String module = transaction.getModule();
			transaction.setModuleOut("ATALLA");
			rc = manager.commOut(transaction, null);
			transaction.setModuleOut(module);

			if (rc >= 0) {
				String receive = transaction.getData();
				String[] params = receive.split("#");

				if (params.length > 8) {
					receive = params[8];
					transaction.setFieldData("trackI", receive);
				} else {
					rc = -1;
				}
			}
		}

		return rc;
/*
B589710500100043456^MELO G./CRISTIAN E.^9912101152258
423538393731303530303130303034333435365E4D454C4F20472E2F435249535449414E20452E5E39393132313031313532323538000000
F03B9A9D5D01EEAB4D0806EFEAC23E7F2FDE5FAC5693AEE0644A0C83881C2F1545D2A9A8ECF69AF13E49CEDEADA0E597A9EC541D0015C134
*/
	}

	synchronized public long translatePIN(Connector manager, Message message, String sourcePek, String destPek) {
		long rc = -1;
		String bckModule = message.getModule();
		String bckRoot = message.getRoot();
		String bckData = message.getData();
		// bit 052
		String pin = message.getPassword();
		// bit 002, 045
		String pan = CardConf.extractPan(message);
		String lastDigitsPanBeforeDv = CardConf.getLastDigitsPanBeforeDv(pan, false);
		
		if (pan == null || pin == null || sourcePek == null || destPek == null) {
			manager.log(Logger.LOG_LEVEL_ERROR, "Atalla.translatePIN", String.format("missing fields [pan = %s]\n", pan), message);
			return rc;
		}

		String send = String.format("<31#1#%s#%s#%s#%s#>", sourcePek, destPek, pin, lastDigitsPanBeforeDv);
		message.setData(send);
		message.setModuleOut("ATALLA_AKB");
		message.setRoot(null);
		message.setReplyEspected("1");
		Message messageIn = new Message();
		rc = manager.commOut(message, messageIn);

		if (rc >= 0) {
			String receive = messageIn.getData();
			
			if (receive != null) {
				String[] params = receive.split("#");
				// <41#480B100DF35EEAF8#Y#>
				if (params.length > 1) {
					receive = params[1];
					message.setPassword(receive);
				} else {
					rc = -1;
				}
			} else {
				rc = -1;
			}
		}

		message.setModuleOut(bckModule);
		message.setRoot(bckRoot);
		message.setData(bckData);
		return rc;
	}

	private static void atallaAddHex(StringBuilder buffer, String data) {
		for (int i = 0; i < data.length(); i++) {
			char ch = data.charAt(i);

			if (ch < 127) {
				buffer.append(String.format("%02X", (int)ch));
			} else {
				buffer.append("??");
			}
		}
	}

	private static void atallaAddHex(StringBuilder buffer, String data, int size, boolean isNumeric) {
		String hexByteIfNull;

		if (isNumeric) {
			hexByteIfNull = "30";
		} else {
			hexByteIfNull = "20";
		}

		if (data != null) {
			int len = data.length();

			if (len <= size) {
				int diff = size - len;

				if (isNumeric) {
					for (int i = 0; i < diff; i++) {
						buffer.append(hexByteIfNull);
					}

					atallaAddHex(buffer, data);
				} else {
					atallaAddHex(buffer, data);

					for (int i = 0; i < diff; i++) {
						buffer.append(hexByteIfNull);
					}
				}
			} else {
				atallaAddHex(buffer, data.substring(0, size));
			}
		} else {
			for (int i = 0; i < size; i++) {
				buffer.append(hexByteIfNull);
			}
		}
	}

	private long atallaGenerateMAC(Connector manager, Message transaction, String codeProcess, String panDigitado, String value, String captureData, String providerEC, String equipamentId) throws Exception {
		long rc = 0;

		if (value == null) {
			value = "000000000000";
		}

		StringBuilder buffer = new StringBuilder(256);
		atallaAddHex(buffer, panDigitado, 19, false);
		atallaAddHex(buffer, codeProcess, 6, true);
		atallaAddHex(buffer, value, 12, true);
		atallaAddHex(buffer, equipamentId, 16, true);
		atallaAddHex(buffer, captureData, 12, false);
		atallaAddHex(buffer, providerEC, 11, true);
		String send = String.format("<98#%s#3#7##U#152#%s#18#>", atalla_mackey, buffer);
		transaction.setData( send);
		String module = transaction.getModule();
		transaction.setModuleOut("ATALLA");
		rc = manager.commOut(transaction, null);
		transaction.setModuleOut(module);
		// <98#1MDNE000,04B4A61451329507C741F10E3BB9A0DBDBFEBA44E553CB45,8576F6D2FD6100ED#3#7##U#152#30303030303030303030303030303030303030323031313130303030303030313030303030303030303030303035303137383031323030303030303030303030303030373632363838353036#18#>

		if (rc >= 0) {
			String receive = transaction.getData();
			String[] params = receive.split("#");

			if (params.length > 2) {
				receive = params[2];
				receive = receive.replace(" ", "");
				transaction.setFieldData("mac", receive);
			} else {
				rc = -1;
			}
		}

		return rc;
	}

	public long process(Connector manager, Message message, boolean isGenerateMAC, boolean isGenerateTrackII, boolean isGenerateTrackI, String sourcePek, String targetPek) throws Exception {
		long rc = 0;
		String msgType = message.getMsgType();
		String codeProcess = message.getCodeProcess();
		String panDigitado = message.getPan();
		String value = message.getTransactionValue();
		String trackI = message.getTrackI();
		String trackII = message.getTrackIi();
		String providerEC = message.getProviderEc();
		String equipamentId = message.getEquipamentId();
		String captureData = message.getData();

		if (isGenerateMAC == true) {
			rc = atallaGenerateMAC(manager, message, codeProcess, panDigitado, value, captureData, providerEC, equipamentId);
		}

		if (trackII != null && msgType.equals("0420") == false) {
			rc = atallaCryptTrackII(manager, message, trackII, trackI);

			if (rc >= 0) {
				rc = atallaCryptTrackI(manager, message, trackI);

				if (rc >= 0) {
					rc = translatePIN(manager, message, sourcePek, targetPek);
				}
			}
		}

		if (rc >= 0) {
			rc = atallaGenerateMAC(manager, message, codeProcess, panDigitado, value, captureData, providerEC, equipamentId);
		}

		return rc;
	}
	
	public static synchronized Atalla getInstance() {
		if (instance == null) {
			instance = new Atalla();
		}
		
		return instance;
	}

	public static void main(String[] args) {
    try {
       String pin = args[0]; // conte�do do bit 52
	     String trackII = args[1]; // conte�do do bit 35 ou bit 2 (se for venda digitada)
	     String sourcePek = args[2]; // 1PUNE000,...
	     String destPek = args[3]; // 1PUNE000,...
	
	     String lastDigitsPanBeforeDv;
	     int pos = trackII.indexOf('=');
	
	     if (pos >= 12) {
	        lastDigitsPanBeforeDv = trackII.substring(pos-12-1, pos-1);
	     } else if (trackII.length() > 12) {
	        lastDigitsPanBeforeDv = trackII.substring(trackII.length()-12-1, trackII.length()-1);
	     } else {
	        System.err.println("Invalid PAN !");
	        return;
	     }
	
	     Socket socket = new Socket("10.33.5.14", 7000);
	     OutputStream os = socket.getOutputStream();
	     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	     // comando para o Atalla AKB
	     String cmd = String.format("<31#1#%s#%s#%s#%s#>", sourcePek, destPek, pin, lastDigitsPanBeforeDv);
	     os.write(cmd.getBytes());
	     String str = in.readLine();
	     socket.close();
	     String pinOut = null;
	
	     if (str.length() == 24) {
	        String[] params = str.split("#");
	        // <41#XXXXXXXXXXXXXXXX#Y#>
	        if (params.length == 4) {
	           if (params[2].equals("Y")) {
	              pinOut = params[1];
	           }
	        }
	     }
	
	     if (pinOut != null) {
	        System.out.println("dest pin : " + pinOut);
	     } else {
	        System.err.println("Invalid Atalla response");
	     }
	  } catch (Exception e) {
	     // TODO: handle exception
	  }
	}
	
}
