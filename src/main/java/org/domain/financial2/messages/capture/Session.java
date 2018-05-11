package org.domain.financial2.messages.capture;

import javax.persistence.EntityManager;

import org.domain.commom.Utils;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Session {
	private CaptureTables tefTables;
// Flags TefBin
//00 80 00 00 -> pan manual
//00 40 00 00 -> service code chip
//00 20 00 00 -> service code pass
//00 08 00 00 -> pass
//00 00 40 00 -> parcelamento ADM co, juros
//00 00 20 00 -> parcelamento lojista sem juros
//00 00 10 00 -> pre-date
//00 00 01 00 -> PBM
//Flags TefProvider
//00 00 80 00 -> chip
//00 00 20 00 -> fallback
//00 00 10 00 -> cancel
//00 00 10 00 -> crypt card
//ECOFROTAS = 10010001 00000000 - chip, cancel, crypt card
//OUTRAS    = 00010000 00000000 - cancel
	private Connector manager;
	private BinDinamicTables binDinamicTables;
	
	private static String captureTablesVersionsCard = "000001";
	private static String captureTablesVersionsRecharge = "000001";

	public Session(Connector manager, EntityManager entityManager, boolean isJTA) {
		this.manager = manager;
		this.tefTables = new CaptureTables(entityManager, isJTA);
		this.binDinamicTables = new BinDinamicTables(entityManager, isJTA);
	}

	private static Message buildMessage(Message message) throws Exception {
		// bit 048
		message.setCaptureTablesVersionsIn(Session.captureTablesVersionsCard);
//		String root = MessageAdapterISO8583.getRoot(message);
//		message.setRoot(root);
		return message;
	}

	public static Message execute(Connector manager, Message message) {
		Message messageIn = new Message();
		
		try {
			message = buildMessage(message);
			message.setModuleOut("TEF_IP");
			long rc = manager.commOut(message, messageIn);
			
			if (rc != 0) {
				// todo
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return messageIn;
	}
	
	public void logon() {
		try {
			// Primeiro faz o Logon
			Message message = new Message();
			message.setMsgType("0800");
			message.setCodeProcess("910000");
			Session.buildMessage(message);
			message.setFieldData("capture_0800_910000_070", "001");
			long rc = manager.commOut(message, null);
			
			if (rc == 0) {
				String codeResponse = message.getCodeResponse();
				
				if (codeResponse != null && codeResponse.equals("0")) {
					String captureTablesVersions = message.getCaptureTablesVersionsIn();
					
					if (captureTablesVersions != null && captureTablesVersions.equals(Session.captureTablesVersionsCard) == false) {
						loadTefTables(false);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public String loadTefTables(boolean useDirectConn) {
		StringBuilder builder = new StringBuilder(100*1024);
		
		try {
			boolean stop = false;
			int sequenceIndex = 1;
			
			while (stop == false && sequenceIndex < 99) {
				stop = true;
				Message messageOut = new Message();
				messageOut.setMsgType("0800");
				messageOut.setCodeProcess("920000");
				buildMessage(messageOut);
				messageOut.setSequenceIndex(String.format("%02d", sequenceIndex++));
				messageOut.setReplyEspected("1");
				
				if (useDirectConn) {
					messageOut.setModuleOut("HCT");
				}
				
				Message messageIn = new Message();
				
				long rc = manager.commOut(messageOut, messageIn);
				
				if (rc == 0) {
					String codeResponse = messageIn.getCodeResponse();
					
					if (codeResponse != null && codeResponse.equals("0")) {
						builder.append(messageIn.getData());
						builder.append(messageIn.getDataComplement());
						String strSequenceIndex = messageIn.getSequenceIndex();
						
						if (Utils.isUnsignedInteger(strSequenceIndex) == true && strSequenceIndex.equals("00") == false) {
							stop = false;
						}
					}
				}
			}
			
			if (useDirectConn && builder.length() > 0) {
				String tables = builder.toString();
				String logs = this.tefTables.parse(tables, "920000", true);
				builder.append("\n\n");
				builder.append(logs);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return builder.toString();
	}
		
	public void loadPosBinDinamicTables() {
		try {
			Message message = new Message();
			message.setModuleOut("SYSCAP_RequisicaoTefBinDinamico");
			message.rawData = "<RequisicaoTefBinDinamico><FEP>GN-FEP09-M3</FEP></RequisicaoTefBinDinamico>\n";
			
			long rc = manager.commOut(message, null);
			
			if (rc == 0) {
				String rawData = message.rawData;
				
				if (rawData.length() > 0) {
					String strIni = "SDLC-560</MODELO><DADOS>";
					int posIni = rawData.indexOf(strIni);
					
					if (posIni > 0) {
						posIni += strIni.length();
						int posEnd = rawData.indexOf("</DADOS>", posIni);
						
						if (posEnd > 0) {
							String data = rawData.substring(posIni, posEnd);
							this.binDinamicTables.parse(data);
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
}
