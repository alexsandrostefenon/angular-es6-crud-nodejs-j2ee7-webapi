package org.domain.financial2.messages.log.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.domain.iso8583router.entity.ISO8583RouterTransaction;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.MessageAdapterISO8583;
import org.domain.iso8583router.messages.MessageAdapterTTLV;
import org.domain.iso8583router.messages.comm.Comm;
import org.domain.iso8583router.messages.comm.Connector;
import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Logger;
import org.domain.commom.Utils;

// apenas um converter por fep, o converter de um dia pode processar o do dia seguinte
public class Converter {
	private ArrayList<Message> unresolvedMessages;
	private String filename;
	private LineNumberReader lineNumberReader;
	// Parser
	private long processedFilesSize = 0;
	private long totalFileSize = 0;
	private String lineOriginal;
	private String lineRemind;
	private String activeDateTime;
	private long activeTimeStamp;
	private long activeNsuInternal;
	// 0 (Unknow), 1 (IN), 2 (OUT)
	private String activeDirection;
	private String activeModule;
	private String activeMessageRoot;
	private Pattern patternHeaderFep_03_05_09_10 = Pattern.compile("\\d{6}\\-\\d{6}\\.\\d{3} ");
	private Pattern patternHeaderFep_04 = Pattern.compile("\\d\\d\\d\\d-\\d\\d\\-\\d\\d ;");
	private Pattern patternHeaderPTG = Pattern.compile("\\d{2}/\\d{2}/\\d{2}\\-\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3} \\[TRANS\\] ");
	private Pattern patternIpAndSocket = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5} ");
	private Pattern patternISO8583 = Pattern.compile("^[09][1-9][0123][02][0-7][0-9A-F]{15}| [09][1-9][0123][02][0-7][0-9A-F]{15}|[^\\w][09][1-9][0123][02][2367][0-9A-F]{15}|[^\\w][09][1-9][0123][02][8-9A-F][0-9A-F]{31}|^[09][1-9][0123][02][2367][0-9A-F]{15}|^[09][1-9][0123][02][8-9A-F][0-9A-F]{31}");
	private Pattern patternModuleName = Pattern.compile("^\\[[a-zA-Z _]{4,40}[0-9]{0,10}\\]");
	private StringBuilder bufferCrack = new StringBuilder(100*1024);
	
	private static boolean breakAfterEndOfFile = true;
	private static EntityManager entityManagerMessage = null;
	private static EntityManager entityManagerRequest = null;
	private static UserTransaction userTransaction = null;
	private static boolean isJtaMessage = false;
	private static boolean isJtaRequest = false;
	private static ConverterLogger logger = ConverterLogger.getInstance();
	
	public Converter() throws Exception {
		// convert os logs do gateway antigo, caso existam
		// vou considerar a entrada de 10 transações por segundo (ex.: 10 solicitações de compra por segundo)
		// 60 segundos * 10 tps * 10 registros por transação
		this.unresolvedMessages = new ArrayList<Message>(60*10*10);
	}

	private void getLastRoot() {
		if (this.activeNsuInternal >= 0) {
			for (int i = this.unresolvedMessages.size()-1; i >= 0; i--) {
				Message message = this.unresolvedMessages.get(i);
				String module = message.getModule();
				Long nsuInternal = message.getTransactionId();
				
				if (nsuInternal != null && nsuInternal == this.activeNsuInternal && this.activeModule.equals(module)) {
					this.activeMessageRoot = message.getRoot();
					break;
				}
			}
		}
	}

	private void getLastModule(String connId, long nsuInternal) {
		if (connId == null || connId.length() == 0) {
			return;
		}
		
		for (int i = this.unresolvedMessages.size()-1; i >= 0; i--) {
			Message message = this.unresolvedMessages.get(i);
			String moduleIn = message.getModuleIn();
			String _connId = message.getConnId();
			
			if (moduleIn != null && _connId != null && _connId.equals(connId)) {
				this.activeModule = moduleIn;
				
				if (message.getTransactionId() == null) {
					message.setTransactionId(nsuInternal);
				}
				
				break;
			}
		}
	}
	
	private String getLastModule(long nsuInternal) {
		String module = null;
		
		for (int i = this.unresolvedMessages.size()-1; i >= 0; i--) {
			Message message = this.unresolvedMessages.get(i);
			
			if (message.getTransactionId() == nsuInternal) {
				module = message.getModule();
				break;
			}
		}
		
		return module;
	}
	
	private Long getNsuInternal(String connId) {
		if (connId == null || connId.length() == 0) {
			return null;
		}
		
		Long nsuInternal = null;
		
		for (int i = this.unresolvedMessages.size()-1; i >= 0; i--) {
			Message message = this.unresolvedMessages.get(i);
			String _connId = message.getConnId();
			Long _nsuInternal = message.getTransactionId();
			
			if (_nsuInternal != null && _nsuInternal != 0 && _connId != null && _connId.equals(connId)) {
				nsuInternal = _nsuInternal;
				break;
			}
		}
		
		return nsuInternal;
	}

/*
	private Integer getLastNsuInternal(String root, int activeIndex) {
		if (root == null || root.length() == 0) {
			return null;
		}
		
		Integer nsuInternal = null;
		
		for (int i = activeIndex-1; i >= 0; i--) {
			Message message = this.unresolvedMessages.get(i);
			String _root = message.getRoot();
			Integer _nsuInternal = message.getTransactionId();
			
			if (_nsuInternal != null && _nsuInternal != 0 && _root != null && _root.equals(root)) {
				nsuInternal = _nsuInternal;
				break;
			}
		}
		
		return nsuInternal;
	}
*/

	private String extractColumn(int offset, int minSize, int maxSize, char splitter, boolean enableAlpha, boolean enableNumericDecimal, boolean enableNumericHexadecimal, boolean enableSpecial) {
		String ret = null;
		
        int pos = this.lineRemind.indexOf(splitter, offset);
        
        if (pos >= offset + minSize) {
        	int size = pos - offset;
        	
            if (size >= minSize && size <= maxSize) {
            	ret = this.lineRemind.substring(offset, pos);
        		int pos_fail = ByteArrayUtils.checkContentType(ret, enableAlpha, enableNumericDecimal, enableNumericHexadecimal, enableSpecial, false); 
        		
        		if (pos_fail < 0) {
                    this.lineRemind = this.lineRemind.substring(pos+1);
        		} else {
        			ret = null;
        		}
        		
            }
        }

		return ret;		
	}
	
	private void parseIpPort(Message message) {
		if (this.lineRemind.startsWith("[IP:")) {
			// min : 1.1.1.1 max : 111.111.111.111:123456
			String str = extractColumn(4, 8, 22, ']', true, true, true, true);
			
			if (str != null) {
				message.setConnId(str);
			}
		} else if (this.lineRemind.startsWith("IP: ")) {
			// min : 1.1.1.1 max : 111.111.111.111:123456
			String str = extractColumn(4, 8, 22, ' ', true, true, true, true);
			
			if (str != null) {
				message.setConnId(str);
			}
		}
		
		if (this.lineRemind.startsWith("sfd: ")) {
			// sfd: 4160
			String str = extractColumn(5, 1, 5, ' ', false, true, false, false);
			
			if (str != null) {
				message.setConnId(str);
			}
		}
	}

	private static int getTimeStampGtwProcess(String str) {
		try {
			int hour = Integer.parseInt(str.substring(7, 9));
			int minute = Integer.parseInt(str.substring(9, 11));
			int second = Integer.parseInt(str.substring(11, 13));
			int milis = Integer.parseInt(str.substring(14, 17));
			int timeStamp = (hour * 3600 + minute * 60 + second) * 1000 + milis;
			return timeStamp;
		} catch (Exception e) {
			System.out.println("Linha sem timestamp : " + str);
			return 0;
		}
	}
	
	private static int getTimeStampGtwOut(String str) {
		try {
	        int hour = Integer.parseInt(str.substring(12, 14));
	        int minute = Integer.parseInt(str.substring(15, 17));
	        int second = Integer.parseInt(str.substring(18, 20));
	        int milis = Integer.parseInt(str.substring(21, 24));
			int timeStamp = (hour * 3600 + minute * 60 + second) * 1000 + milis;
			return timeStamp;
		} catch (Exception e) {
			System.out.println("Linha sem timestamp : " + str);
			return 0;
		}
	}
	
	private static int getTimeStampPTG(String str) {
		try {
			// 06/06/16-13:52:44.841 [TRANS] [0.E2I.IN2] [ID=15925190]  Mensagem
			int hour = Integer.parseInt(str.substring(9, 11));
			int minute = Integer.parseInt(str.substring(12, 14));
			int second = Integer.parseInt(str.substring(15, 17));
			int milis = Integer.parseInt(str.substring(18, str.indexOf(' ', 18)));
			int timeStamp = (hour * 3600 + minute * 60 + second) * 1000 + milis;
			return timeStamp;
		} catch (Exception e) {
			System.out.println("Linha sem timestamp : " + str);
			return 0;
		}
	}
	
	private void parseHeaderFeps_03_05_09_10(Message message, java.util.regex.Matcher matcher) {
		String str = this.lineRemind.substring(matcher.start());
		this.activeDateTime = "20" + str.substring(4, 6) + str.substring(2, 4) + str.substring(0, 2) + str.substring(7, 13);
		this.activeTimeStamp = getTimeStampGtwProcess(str);
		message.setTimeStamp(this.activeTimeStamp);
		this.lineRemind = this.lineRemind.substring(matcher.end());
		// coluna thread id ou lixo
		if (extractColumn(0, 2, 5, ' ', true, true, true, true) == null) {
			// logar a falta do campo
			log(Logger.LOG_LEVEL_ERROR, "missing thread id", "", message);
		}
		// coluna x25 chanel ou socked id
		if (extractColumn(0, 3, 6, ' ', true, true, true, true) == null) {
			log(Logger.LOG_LEVEL_ERROR, "missing x25 chanel or socket id", "", message);
		}
		// em alguns casos vem o IP neste ponto
		matcher = this.patternIpAndSocket.matcher(lineRemind);
		
		if (matcher.find() && matcher.start() == 0) {
			message.setConnId(this.lineRemind.substring(matcher.start(), matcher.end()-1));
			this.lineRemind = this.lineRemind.substring(matcher.end());
		}
		// em alguns vem o DTE neste ponto, logo antes do nsuInternal
		matcher = Pattern.compile("\\d{1,15} \\d{1,8} ").matcher(lineRemind);
		
		if (matcher.find() && matcher.start() == 0) {
			if ((str = extractColumn(0, 1, 15, ' ', false, true, false, false)) != null) {
				message.setConnId(str);
			} else {
				log(Logger.LOG_LEVEL_ERROR, "missing dte", "", message);
			}
		}
		// coluna nsuInternal
		if ((str = extractColumn(0, 1, 8, ' ', false, true, false, false)) == null) {
			// aceita os '------' que ficam no lugar do nsuInternal
			if ((str = extractColumn(0, 1, 8, ' ', false, false, false, true)) == null) {
				log(Logger.LOG_LEVEL_WARNING, "missing nsuInternal", "", message);
			}
		} else {
			if (Utils.isUnsignedInteger(str)) {
				this.activeNsuInternal = Integer.parseInt(str);
				message.setTransactionId(this.activeNsuInternal);
			} else {
				log(Logger.LOG_LEVEL_ERROR, "wrong nsuInternal", str, message);
			}
		}
		// dte
		if (this.lineRemind.startsWith("'")) {
			if ((str = extractColumn(1, 3, 16, '\'', false, true, false, false)) != null) {
				this.activeModule = "TEF";
			} else {
				log(Logger.LOG_LEVEL_ERROR, "missing dte", "", message);
			}
		}
		// TEF extra 1 byte
		if ((str = extractColumn(0, 1, 3, '-', false, true, false, false)) != null) {
			this.lineRemind = "-" + this.lineRemind;
			this.activeModule = "TEF";
		}
//		if (this.lineRemind.startsWith("1->")) {
//			this.activeModule = "TEF";
//		}
		
		if (this.lineRemind.startsWith("ETH ")) {
			this.lineRemind = lineRemind.substring(4);
		} else if (this.lineRemind.startsWith("[Requisicao EMBRATEC]")) {
			this.activeModule = "EMBRATEC";
			this.lineRemind = lineRemind.substring(21);
		} else if (this.lineRemind.startsWith("[Retorno EMBRATEC] ->")) {
			this.activeModule = "EMBRATEC";
			this.lineRemind = "<- " + this.lineRemind.substring(22);
			getLastRoot();
		}
		
		int pos = this.lineRemind.indexOf("<-");
		
		if (pos < 0) {
			pos = this.lineRemind.indexOf("->");
		}
		
		if (pos > 0 && pos < 50) {
			this.lineRemind = this.lineRemind.substring(pos);
		}
		
		if (parseDirection() == false) {
			log(Logger.LOG_LEVEL_WARNING, "missing direction", "", message);
		}
		
		parsePosDirection(message);
		parseIpPort(message);
		parseModuleId(message);
		parseIpPort(message);
		
		if (this.lineRemind.startsWith("[") && this.lineRemind.endsWith("]")) {
			this.lineRemind = this.lineRemind.substring(1, this.lineRemind.length()-1);
		}
	}	
	
	private void parseHeaderFeps_04(Message message, java.util.regex.Matcher matcher) {
		String str;
		this.lineRemind = this.lineRemind.substring(matcher.start());
		this.activeTimeStamp = getTimeStampGtwOut(this.lineRemind);
		message.setTimeStamp(this.activeTimeStamp);
		this.lineRemind = this.lineRemind.substring(26);
		// coluna ip
		if (extractColumn(0, 18, 20, ';', true, true, true, true) == null) {
			// logar a falta do campo
			log(Logger.LOG_LEVEL_ERROR, "missing gtw out ip", "", message);
		}
        // coluna provider
		if ((str = extractColumn(0, 21, 30, ';', true, true, true, true)) == null) {
			// logar a falta do campo
			log(Logger.LOG_LEVEL_ERROR, "missing gtw out provider", "", message);
		} else {
			int pos = str.indexOf(" ", 5);

			if (pos > 5) {
				this.activeModule = str.substring(5, pos);
				pos = this.activeModule.indexOf('[');
				
				if (pos >=0) {
					this.activeModule = this.activeModule.substring(0, pos);
				}
				
				message.setProviderName(this.activeModule);
			}
		}
        // coluna direction
		if ((str = extractColumn(0, 19, 21, ';', true, true, true, true)) == null) {
			// logar a falta do campo
			log(Logger.LOG_LEVEL_ERROR, "missing gtw out direction", "", message);
		} else {
			// <- [039282] [0411] 
			if (str.startsWith("->")) {
				this.activeDirection = Message.DIRECTION_NAME_C2S;
			} else if (str.startsWith("<-")) {
				this.activeDirection = Message.DIRECTION_NAME_S2C;
			} else {
				log(Logger.LOG_LEVEL_ERROR, "wrong gtw out direction", "", message);
			}
			
			int pos = str.indexOf(']', 3);
			
			if (pos > 3) {
				str =  str.substring(4, pos);
				
				if (Utils.isUnsignedInteger(str)) {
					this.activeNsuInternal = Integer.parseInt(str);
					message.setTransactionId(this.activeNsuInternal);
				} else {
					log(Logger.LOG_LEVEL_ERROR, "wrong nsuInternal", str, message);
				}
			}
		}
		
		if (this.lineRemind.startsWith("Processa os dados da operadora REPOWER")) {
			this.activeModule = "REPOWER";
			this.lineRemind = this.lineRemind.substring(this.lineRemind.indexOf("]= ") + 3);
		}
		// o restante é a ISO8583
	}
	
	private void parseHeaderPTG(Message message, java.util.regex.Matcher matcher) {
		// 06/06/16-13:52:44.841 [TRANS] [0.E2I.IN2] [ID=15925190]  Mensagem
		String str = this.lineRemind.substring(matcher.start());
		this.activeDateTime = "20" + str.substring(6, 8) + str.substring(3, 5) + str.substring(0, 2) + str.substring(9, 11) + str.substring(12, 14) + str.substring(15, 17);
		this.activeTimeStamp = getTimeStampPTG(str);
		message.setTimeStamp(this.activeTimeStamp);
		// [0.E2I.IN2] [ID=15925190]  Mensagem
		this.lineRemind = this.lineRemind.substring(matcher.end());
		
		if (this.lineRemind.startsWith("[0.E2I.") || this.lineRemind.startsWith("[0.I2E.")) {
			int pos = this.lineRemind.indexOf("]", 7);
			
			if (pos > 7) {
				this.activeModule = this.lineRemind.substring(7, pos);
				
				if (this.activeModule.equals("IN")) {
					this.activeModule = "ETH_V2";
				}
				
				this.lineRemind = this.lineRemind.substring(pos+2);
			}
		} else if (this.lineRemind.startsWith("[0.SISCAP] ")) {
			this.activeModule = "SYSCAP";
			int posEnd = this.lineRemind.length();
			
			while (this.lineRemind.charAt(posEnd-1) == ' ') {
				posEnd--;
			}
			
			if (this.lineRemind.startsWith("(0A)", posEnd-4)) {
				posEnd -= 4;
			}
			
			this.lineRemind = this.lineRemind.substring(11, posEnd);
		} else if (this.lineRemind.startsWith("[0.GTWOUTOLD] ")) {
			this.activeModule = "OPERADORA_CAPTURA_PADRAO";
			this.lineRemind = this.lineRemind.substring(14);
		} else {
			log(Logger.LOG_LEVEL_WARNING, "missing module name", "", message);
		}
		// coluna nsuInternal
		if (this.lineRemind.startsWith("[ID=")) {
			int pos = this.lineRemind.indexOf("] ", 4);
			
			if (pos > 5) {
				str = this.lineRemind.substring(4, pos);
				
				if (Utils.isUnsignedInteger(str)) {
					this.activeNsuInternal = Long.parseLong(str);
					message.setTransactionId(this.activeNsuInternal);
				} else {
					log(Logger.LOG_LEVEL_ERROR, "wrong nsuInternal", str, message);
				}
				
				this.lineRemind = this.lineRemind.substring(pos+2);
				skipSpaces();
			}
		} else {
			log(Logger.LOG_LEVEL_WARNING, "missing nsuInternal", "", message);
		}
		// direction
		if (this.lineRemind.startsWith("Mensagem convertida para ISO PCI:")) {
			this.activeDirection = Message.DIRECTION_NAME_C2S;
			this.lineRemind = this.lineRemind.substring(33);
		} else if (this.lineRemind.startsWith("Mensagem enviada ao host:")) {
			this.activeDirection = Message.DIRECTION_NAME_C2S;
			this.lineRemind = this.lineRemind.substring(25);
		} else if (this.lineRemind.startsWith("Mensagem recebida do host:")) {
			this.activeDirection = Message.DIRECTION_NAME_S2C;
			this.lineRemind = this.lineRemind.substring(26);
		} else if (this.lineRemind.startsWith("Mensagem ISO:")) {
			this.activeDirection = Message.DIRECTION_NAME_S2C;
			this.lineRemind = this.lineRemind.substring(13);
		} else {
			log(Logger.LOG_LEVEL_WARNING, "missing direction", "", message);
		}
		// parseia a ISO da operadora
		if (this.activeModule.equals("OPERADORA_CAPTURA_PADRAO")) {
			try {
				if (this.lineRemind.endsWith("          ")) {
					this.lineRemind = this.lineRemind.substring(0, this.lineRemind.length()-10);
				}
				
				Message messageTTLV = new Message();
				MessageAdapterTTLV messageAdapterTTLV = new MessageAdapterTTLV();
				
				if (this.activeDirection == Message.DIRECTION_NAME_C2S) {
					Comm.parseMessage(messageTTLV, "messageAdapterTTLV", "root_c2s", this.lineRemind, null);
				} else {
					Comm.parseMessage(messageTTLV, "GtwOut", "root_s2c", this.lineRemind, null);
				}
				
				this.lineRemind = messageTTLV.getData();
				String providerName = messageTTLV.getProviderName();
				
				if (providerName != null) {
					this.activeModule = providerName; 
				} else {
					this.activeModule = this.getLastModule(this.activeNsuInternal);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	}	
	
	private boolean parseHeader(Message message) {
		boolean found = false;

		try {
			java.util.regex.Matcher matcher = this.patternHeaderFep_03_05_09_10.matcher(lineRemind);
			// log dos feps 03,05,09 e 10
			if (matcher.find()) {
				found = true;
				this.parseHeaderFeps_03_05_09_10(message, matcher);
			} else {
				// 2015-11-18 ;09:17:52:390; Processa os dados da operadora REPOWER (sfd=452). Itens na fila = 0. Buffer recebido[213]= 
				matcher = this.patternHeaderFep_04.matcher(lineRemind);

				if (matcher.find()) {
					// log dos feps 04
					found = true;
					this.parseHeaderFeps_04(message, matcher);
				} else {
					// 2015-11-18 ;09:17:52:390; Processa os dados da operadora REPOWER (sfd=452). Itens na fila = 0. Buffer recebido[213]= 
					matcher = this.patternHeaderPTG.matcher(lineRemind);

					if (matcher.find()) {
						// log dos feps 04
						found = true;
						this.parseHeaderPTG(message, matcher);
					}
				}
			}
		} catch (Exception e) {
			log(Logger.LOG_LEVEL_ERROR, "Header Exception", e.getMessage(), message);
		}
		
		return found;
	}

	private boolean skipLine() {
		java.util.regex.Matcher matcher = this.patternISO8583.matcher(lineRemind);
		
		// antes de fazer qualquer escape, testa para ver se tem uma ISO8583
		if (matcher.find() == true) {
			return false;
		}
		
		boolean found = false;

		for (String str : ConverterConf.getInstance().skipLines) {
			if (this.lineRemind.startsWith(str)) {
				found = true;
				break;
			}
		}
		// verifica ocorrências especiais
		if (found == false) {
			found = true;
			// verifica os skips
			if (this.lineRemind.contains("[(5)(0)(0)(1)(4) I")) {
				// ACK do protocolo RENPAC
			} else {
				found = false;
			}
		}
		// TODO : agora que estou utilizando o getRootISO8583 nao precisa mais se preocupar de perder as mensagens da SPTRANS
		// também verifica se é uma transação criptografada
		if (found == false && this.activeModule.equals("SPTRANS") == false) {
			int countOpen = 0;
			int countClose = 0;
			// o SPTRANS é repleto de caracteres binários, mas aparentemente não ocorrem após a posição 100 e antes da 140
			for (int i = 0; i < this.lineRemind.length() && i < 100; i++) {
				char ch = this.lineRemind.charAt(i);

				if (ch == '(') {
					countOpen++;
				} else if (ch == ')') {
					countClose++;
				}
				// (120/4)/2
				if (countOpen > 10 && countClose > 10) {
					found = true;
					break;
				}
			}
		}

		return found;
	}

	private static String getPayloadLogFromProcedureReturn(String line) {
		// this.lineRemind = this.lineRemind.replaceAll("; \\[\\d\\d\\] ", "|");
		// status = 10; [00] 10; [01] 20111103 12:54:41; [02] 464816430; [03] 848703251500; [04] Oi - RN;
    	String[] params = line.split("; \\[");
		ArrayList<Integer> listIndex = new ArrayList<Integer>(params.length);
		ArrayList<String> listValues = new ArrayList<String>(params.length);
		int maxIndex = -1;

		if (params.length > 0) {
			String str = params[0];
			// carrega o status
			listIndex.add(0);
			listValues.add(str);

			for (int i = 1; i < params.length; i++) {
				str = params[i];
				int index = Integer.parseInt(str.substring(0, 2)) + 1;
				listIndex.add(index);

				if (index > maxIndex) {
					maxIndex = index;
				}

				listValues.add(str.substring(4));
			}
		}

		String[] values = new String[maxIndex+1];

		for (int i = 0; i < listIndex.size(); i++) {
			int index = listIndex.get(i);
			values[index] = listValues.get(i);
		}

		StringBuilder buffer = new StringBuilder(maxIndex*10);

		for (int i = 0; i <= maxIndex; i++) {
			buffer.append(values[i]);
			buffer.append('|');
		}

		return buffer.toString();
	}

	private void parsePosDirection(Message message) {
		skipSpaces();
		
		if (this.lineRemind.startsWith("[Requisicao EMBRATEC]")) {
			this.lineRemind = this.lineRemind.substring(22);
		} else if (this.lineRemind.startsWith("[Retorno EMBRATEC]")) {
			this.lineRemind = this.lineRemind.substring(19);
			this.activeModule = "procedure_embratec";
		} else if (this.lineRemind.startsWith("gout= ")) {
			int pos = this.lineRemind.indexOf(' ', 6);
			int timeExec = Integer.parseInt(this.lineRemind.substring(6, pos));
			this.lineRemind = this.lineRemind.substring(pos+1);

			if (timeExec >= 0) {
				message.setTimeExec(timeExec);
			} else {
				message.setTimeExec(null);
			}
			// método para verificar se é retorno do SisCap
		} else if (this.lineRemind.startsWith("Delay = ")) {
			int pos = this.lineRemind.indexOf(' ', 8);
			int timeExec = Integer.parseInt(this.lineRemind.substring(8, pos));
			this.lineRemind = this.lineRemind.substring(pos+3);

			if (timeExec >= 0) {
				message.setTimeExec(timeExec);
			} else {
				message.setTimeExec(null);
			}
		} else if (this.lineRemind.startsWith("Tsp= ")) {
			if (this.lineRemind.indexOf('[') < 0) {
				this.activeDirection = Message.DIRECTION_NAME_C2S;
			}

			this.lineRemind = this.lineRemind.substring(5);
			int pos = this.lineRemind.indexOf(' ');
			int timeOut = Integer.parseInt(this.lineRemind.substring(0, pos));
			this.lineRemind = this.lineRemind.substring(pos+1);
			message.setTimeout(timeOut * 1000);
			pos = this.lineRemind.indexOf(' ');
			int timeExec = Integer.parseInt(this.lineRemind.substring(0, pos));
			this.lineRemind = this.lineRemind.substring(pos+1);

			if (timeExec >= 0) {
				message.setTimeExec(timeExec);
			} else {
				message.setTimeExec(null);
			}
		}
	}
	
	private boolean parseDirection() {
		if (this.lineRemind.startsWith("sp_")) {
			return false;
		}
		
		skipSpaces();
		boolean ret = true;
		int size = 0;
		boolean isIn = false;
		boolean isOut = false;
		
		if (this.lineRemind.startsWith("<-")) {
			size = 2;
			isIn = true;
		} else if (this.lineRemind.startsWith("->")) {
			size = 2;
			isOut = true;
		} else if (this.lineRemind.startsWith("<==")) {
			size = 3;
			isIn = true;
		} else if (this.lineRemind.startsWith("==>")) {
			size = 3;
			isOut = true;
		} else if (this.lineRemind.startsWith("--")) {
			size = 2;
		} else {
			ret = false;
		}
		
		if (isIn) {
			this.activeDirection = Message.DIRECTION_NAME_S2C;
		} else if (isOut) {
			this.activeDirection = Message.DIRECTION_NAME_C2S;
		}
		
		if (size > 0) {
			this.lineRemind = this.lineRemind.substring(size);
		}

		return ret;
	}

	private void skipSpaces() {
		while (this.lineRemind.length() > 0 && this.lineRemind.charAt(0) == ' ') {
			this.lineRemind = this.lineRemind.substring(1);
		}
	}
	
	private boolean parseModuleId(Message message) {
		skipSpaces();
		java.util.regex.Matcher matcher = this.patternModuleName.matcher(this.lineRemind);
		boolean ret = false;

		if (matcher.find() == true) {
			this.activeModule = this.lineRemind.substring(matcher.start()+1, matcher.end()-1);
			this.lineRemind = this.lineRemind.substring(matcher.end()+1);
			ret = true;
			
			if (this.activeModule.startsWith("Resposta ")) {
				this.activeModule = this.activeModule.substring(9);
			} else if (this.activeModule.startsWith("Requisicao ")) {
				this.activeModule = this.activeModule.substring(11);
			} else if (this.activeModule.startsWith("Confirmacao ")) {
				this.activeModule = this.activeModule.substring(12);
			}
			
			// tmp
			if (Utils.checkValue(this.activeModule, "GETCONFIG")) {
				if (this.lineRemind.length() == 8 || this.lineRemind.contains("|") == true) {
					this.activeModule = "GETCONFIG_PSV";
				}
			}
		}

		return ret;
	}

	private boolean checkPayloadProcedure(Message message) {
		skipSpaces();
		// verifica informacoes de linhas e colunas e remove
		if (this.lineRemind.startsWith("L(")) {
			int pos = this.lineRemind.indexOf(") ", 8);
			
			if (pos >= 8 && pos <= 10) {
				this.lineRemind = this.lineRemind.substring(pos+2);
			}
		}
		// verifica se comeca com 'exec ' e remove
		if (this.lineRemind.startsWith("exec ")) {
			this.lineRemind = this.lineRemind.substring(5);
		}
		
		skipSpaces();
		boolean ret = false;
		
		if (this.lineRemind.startsWith("sp_")) {
			int pos = this.lineRemind.indexOf(' ');

			if (pos > 0) {
				ret = true;
				String procName = this.lineRemind.substring(0, pos);
				
				if (procName != null && procName.endsWith(";")) {
					procName = procName.substring(0, procName.length()-1);
				}
				
				if (this.activeModule == null || this.activeModule.length() == 0) {
					this.activeModule = "SQL_PROC";
				}
				
				if (this.activeMessageRoot == null || this.activeMessageRoot.length() == 0) {
					this.activeMessageRoot = procName + this.activeDirection;
				}
				
//				this.lineRemind = this.lineRemind.substring(pos+1);
				// se for retorno de procedure, ignora os dados de entrada
				pos = this.lineRemind.indexOf("status = ");

				if (pos >= 0) {
					if (this.activeDirection == null) {
						this.activeDirection = Message.DIRECTION_NAME_S2C;
						this.activeMessageRoot = procName + this.activeDirection;
					}
					// procedure return values
					this.lineRemind = this.lineRemind.substring(pos+9, this.lineRemind.length()-1);
					this.lineRemind = getPayloadLogFromProcedureReturn(this.lineRemind);
					// se nao tem nsu interno, pega da perna _c2s
//					if (this.activeNsuInternal == 0) {
//						message.setTransactionId(getLastNsuInternal(procName + Message.DIRECTION_NAME_C2S, this.unresolvedMessages.size()));
//					}
				} else {
					// procedure out values
					// remove o ponto final
					if (this.lineRemind.endsWith(".")) {
						this.lineRemind = this.lineRemind.substring(0, this.lineRemind.length()-1);
					}

					if (this.activeDirection == null) {
						this.activeDirection = Message.DIRECTION_NAME_C2S;
					}
				}
			}
		} else if (this.activeModule.equals("EMBRATEC")) {
			if (this.activeMessageRoot != null && this.activeMessageRoot.length() > 4) {
				this.activeMessageRoot = this.activeMessageRoot.substring(0, this.activeMessageRoot.length() - 4) + this.activeDirection;
				ret = true;
			}
		}
		
		if (ret == true) {
			message.setRoot(this.activeMessageRoot);
		}
		
		return ret;
	}
	
	private boolean checkPayloadXml(String rootTag) {
		boolean ret = false;
		int posIni = this.lineRemind.indexOf("<" + rootTag + ">");
		
		if (posIni > 0) {
			String tagEnd = "</" + rootTag + ">";
			int posEnd = this.lineRemind.indexOf(tagEnd, posIni);
			
			if (posEnd > posIni) {
				this.lineRemind = this.lineRemind.substring(posIni, posEnd + rootTag.length() + 3);
				ret = true;
			}
		}
		
		return ret;
	}
	
	private boolean checkPayloadXml(Message message) {
		skipSpaces();
		boolean ret = false;
		
		if (this.lineRemind.startsWith("XML: ")) {
			this.lineRemind = this.lineRemind.substring(5);
		}
		
		String skipHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"  standalone=\"no\" ?>";
		
		if (this.lineRemind.startsWith(skipHeader)) {
			this.lineRemind = this.lineRemind.substring(skipHeader.length());
		}
		
		if (this.lineRemind.startsWith("<") || checkPayloadXml("SondaEco") || checkPayloadXml("ConsultaValores")) {
			int pos = this.lineRemind.indexOf('>', 1);
			
			if (pos > 3) {
				ret = true;
				String root = this.lineRemind.substring(1, pos);
				this.activeMessageRoot = root;
				message.setRoot(root);
				int index = Utils.findInList(ConverterConf.getInstance().rootsXmlNonSiscap, root); 
				
				if (this.activeModule == null || this.activeModule.length() == 0) {
					if (index >= 0) {
						this.activeModule = root;
						
						if (root.equals("root")) {
							this.activeMessageRoot = "root" + this.activeDirection;
						}
					} else {
						this.activeModule = "SYSCAP";
					}
				}
			}
		}		
		
		return ret;
	}
	
	private boolean checkPayloadAtalla() {
		skipSpaces();
		boolean ret = false;
		
		if (this.lineRemind.startsWith("ATALLA")) {
			String module = extractColumn(0, 3, 30, ' ', true, true, true, true);
			this.activeModule = module;
			ret = true;
		}
		
		return ret;
	}
	
	private String adjustBadFormat(String str) {
		if (this.activeMessageRoot.equals("0700_007004")) {
			str = MessageAdapterISO8583.setBit(str, 61, false);
			// tenta se livrar do 008 (LLL) do bit 61
			str = MessageAdapterISO8583.setBit(str, 49, true);
		} else if (this.activeMessageRoot.equals("0700_007005")) {
			str = MessageAdapterISO8583.setBit(str, 42, false);
		} else if (this.activeMessageRoot.equals("0700_007007")) {
			str = MessageAdapterISO8583.setBit(str, 42, false);
		} else if (this.activeMessageRoot.equals("0800_001020")) {
			str = MessageAdapterISO8583.setBit(str, 42, false);
		} else if (this.activeMessageRoot.startsWith("0200")) {
			str = MessageAdapterISO8583.setBit(str, 4, false);
			str = MessageAdapterISO8583.setBit(str, 67, false);
		} else if (this.activeMessageRoot.startsWith("0420")) {
			str = MessageAdapterISO8583.setBit(str, 4, false);
		}
		
		return str;
	}
	// extrai (se existir) o primeiro candidado a ISO8553 (verificado até o bit 3) de uma linha de texto
	private String extractISO8583(String str) {
		java.util.regex.Matcher matcher = this.patternISO8583.matcher(str);
		
		if (matcher.find() == false) {
			return null;
		}
		
		int offset = matcher.start();
		char ch = str.charAt(offset);
		
		if (ch < '0' || ch > '9') {
			offset++;
		}
		
		String ret = str.substring(offset);
		this.activeMessageRoot = MessageAdapterISO8583.getRootISO8583(null, ret);
		
		if (this.activeMessageRoot != null) {
			if (str.contains("BAD_FORMAT")) {
				ret = adjustBadFormat(ret);
			}
		} else {
			ret = null;
		}
		
		return ret;
	}
	
	private boolean checkPayloadISO8583(Message message) {
		skipSpaces();
		boolean ret = false;
		String isoStr = extractISO8583(this.lineRemind);

		if (isoStr != null) {
			ret = true;
			this.lineRemind = isoStr;

			if (this.activeModule == null || this.activeModule.length() == 0) {
				getLastModule(message.getConnId(), this.activeNsuInternal);
				
				if (this.activeModule == null || this.activeModule.length() == 0) {
					this.activeModule = "ETH_V2";
				}
				
				if (this.activeModule.equals("ETH_V2") == false &&
						this.activeModule.equals("POS_SDLC") == false &&
						this.activeModule.equals("POS_Ethernet") == false &&
						this.activeModule.equals("POS_GPRS") == false &&
						this.activeModule.equals("POS_RENPAC") == false &&
						this.activeModule.equals("pos_csd") == false &&
						this.activeModule.equals("B24PERMANENTE") == false) {
					System.err.printf("Unknow module : %s\n", this.activeModule);
					this.activeModule = "ETH_V2";
				}
			}
		}
		
		return ret;
	}
	
	private String buildUniqueCaptureNSU(Message message) {
		String equipamentId = message.getEquipamentId();
		
		if (equipamentId == null) {
			equipamentId = "00000000";
		}
		
		String dateTime = message.getDateTimeGmt();
		
		if (dateTime == null) {
			String date = message.getDateLocal();
			String hour = message.getHourLocal();
			
			if (date != null && hour != null) {
				dateTime = date + hour;
			} else {
				dateTime = this.activeDateTime;
			}
		}

		String nsu = message.getCaptureNsu();

		if (nsu == null) {
			nsu = message.getAuthNsu();
		}
		
		if (nsu == null) {
			nsu = "0";
		}
		
		Long internalNSU = message.getTransactionId();
		String uniqueCaptureNSU = String.format("%s_%s_%06d_%06d", equipamentId, dateTime, Integer.parseInt(nsu), internalNSU);
		message.setUniqueCaptureNsu(uniqueCaptureNSU);
		return uniqueCaptureNSU;
	}
	
	public String popLogs() {
		String str = null;
		
		if (Converter.logger != null) {
			str =  Converter.logger.popLogs();
		}

		return str;
	}
	
	private void log(int logLevel, String header, String text, Message message) {
		if (Converter.logger != null) {
			Converter.logger.log(logLevel, header, text, message);
		}
	}
	
	private static String convertEbcdicToAscii(String ebcdic) {
		try {
	    String ebcdic_encoding = "CP1047";
	    byte[] buffer = ebcdic.getBytes();
	    String ascii = new String(buffer, ebcdic_encoding);
			return ascii;
		} catch (Exception e) {
			return "";
		}
	}
	
	static private String convertRepowerISO8583(String raw) {
		// ascii : <0><D3>0210<FA><BB><0><1><86><81><C0><2><0><0><0><0><0><0><0><6>165258240000972398280000000000000800000000000800111811164461000000000065091644111811181117099986183551090000018357790620038673806008P7703C03986986012MS2744089613015006P4G004HISFNX009334045862
		// ebcdic: <0><E6><F0><F2><F0><F0><F2>8D<1><80><E1><80><8><0><0><0><0><0><0><0><2><F1><F6><F5><F0><F3><F3><F9><F7><F2><F0><F0><F0><F0><F0><F0><F0><F0><F0><F2><F8><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F1><F0><F0><F0><F0><F9><F2><F5><F1><F3><F1><F2><F5><F3><F0><F0><F0><F1><F1><F1><F1><F0><F1><F2><F5><F3><F0><F9><F2><F5><F6><F5><F3><F2><F0><F1><F1><F0><F9><F9><F9><F8><F6><F8><F9><F6><F0><F1><F1><F0><F9><F0><F0><F0><F0><F0><F0><F8><F9><F6><F3><F9><F5><F4><F0><F8><F8><F2><F0><F0><F0><F0><F0><F0><F0><F0><F3><F6><F2><F4><F5><F1><F5><C8><E3><C9>@<C1><C4><D8>@<D3><E3><C4><C1>a<C3><C1><D9><D3><D6><E2>@<C7><D6>@<D7><D6><D9><E3><D6>@<C1><D3><C5><C7><D9><C5>@@<C2><D9><C1><F0><F0><F8><D7><F7><F7><F0><F3><C3><F0><F3><F9><F8><F6><F0><F2><F6><F0><F0><F0><F1><F1><F0><F0><F0><F0><F0><F8><F0><F1><F0><F7><F6>@@@@@@@@@@<F0><F0><F9><F3><F6><F7><F7><F3><F4><F1><F3><F6>
		boolean isEbcdic = false;
		
		if (raw.lastIndexOf("<F0>") > 77) {
			isEbcdic = true;
		}
		
		String str = ByteArrayUtils.unEscapeBinaryData(raw, "<", ">", 2);
		int offset = 0;
		String strSize = str.substring(offset, offset+2);
		offset += 2;
		int size = strSize.charAt(0);
		size <<= 8;
		size += strSize.charAt(1);
		size += 8; // dobra o tamanho do primeiro mapa de bits
		String strMsgType = str.substring(offset, offset+4);
		offset += 4;
		String strBitMap1 = str.substring(offset, offset+8);
		offset += 8;
		char firstByte = strBitMap1.charAt(0);
		String strBitMap2;
		
		if (firstByte > 0x7F) {
			strBitMap2 = str.substring(offset, offset+8);
			offset += 8;
			size += 8; // dobra o tamanho do segundo mapa de bits
		} else {
			strBitMap2 = "";
		}
		
		String strBody = str.substring(offset);
		StringBuffer buffer = new StringBuffer(str.length());
		
		if (isEbcdic) {
			buffer.append(convertEbcdicToAscii(strMsgType));
		} else {
			buffer.append(strMsgType);
		}
		
		buffer.append(ByteArrayUtils.getHexStr(strBitMap1));
		buffer.append(ByteArrayUtils.getHexStr(strBitMap2));
		
		if (isEbcdic) {
			buffer.append(convertEbcdicToAscii(strBody));
		} else {
			buffer.append(strBody);
		}
		
		if (buffer.length() != size) {
			System.err.println("convertRepowerISO8583(...) fail");
		}
		
		return buffer.toString();
	}
	
	private void parse(Message message) throws Exception {
		this.lineRemind = this.lineOriginal;
		int lineNumber = this.lineNumberReader.getLineNumber();
		this.activeNsuInternal = -1;
		this.activeModule = "";
		this.activeMessageRoot = "";
		message.rawData = String.format("%s - %08d - %s", this.filename, lineNumber, "");
		this.activeDirection = null;
		
		if (parseHeader(message) == false) {
			log(Logger.LOG_LEVEL_ERROR, "Wrong Header", "", message);
		}
		
		if (skipLine() == true) {
			log(Logger.LOG_LEVEL_DEBUG, "Skiped", "", message);
			return;
		}
		// estou colocando a lineoriginal neste ponto. pois os skips podem ser muito grandes
		// para caber no campo RAW_DATA (ex. isos criptografadas - (00)(0A), etc...)
		message.rawData = String.format("%s - %08d - %s", this.filename, lineNumber, this.lineOriginal);
		message.setEnableBinarySkip(true);
		
		if (checkPayloadProcedure(message) == true) {
		} else if (checkPayloadXml(message) == true) {
		} else if (checkPayloadAtalla() == true) {
		} else if (this.activeModule.equals("GETCONFIG") || this.activeModule.equals("GETCONFIG_PSV")) {
			this.activeMessageRoot = "root" + this.activeDirection;
		} else if (this.activeModule.equals("ON")) {
			this.activeMessageRoot = "root";
			message.setTimeStampOn(this.activeTimeStamp);
		} else if (this.activeModule.equals("OFF")) {
			this.activeMessageRoot = "root";
			message.setTimeStampOff(this.activeTimeStamp);
			message.setTransactionId(getNsuInternal(message.getConnId()));
		} else if (this.activeModule.equals("REPOWER") && this.lineRemind.contains("<0>")) {
			this.lineRemind = convertRepowerISO8583(this.lineRemind);
		// last try is ISO 8583
		} else if (checkPayloadISO8583(message) == true) {
			if (this.lineRemind.startsWith("080082220000000000000400000000000000") || this.lineRemind.startsWith("081082220000020000000400000000000000")) {
				this.activeModule = "B24PERMANENTE";
			}
		} else {
			log(Logger.LOG_LEVEL_ERROR, "Unknow Payload", "", message);
			return;
		}
		
		if (Utils.findInList(ConverterConf.getInstance().modulesIn, this.activeModule) >= 0) {
			if (this.activeDirection == Message.DIRECTION_NAME_S2C) {
				this.activeDirection = Message.DIRECTION_NAME_C2S;
			} else if (this.activeDirection == Message.DIRECTION_NAME_C2S) {
				this.activeDirection = Message.DIRECTION_NAME_S2C;
			}
		}

		message.setConnDirection(this.activeDirection);
		message.setModule(this.activeModule);
		
		try {
			Comm.parseMessage(message, this.activeModule, this.activeMessageRoot, this.lineRemind, this.activeDirection);
		} catch (Exception e) {
			log(Logger.LOG_LEVEL_ERROR, "MessageAdapterConfManager.parseMessage", e.getMessage(), message);
			e.printStackTrace();
			throw e;
		}
		
		if (this.bufferCrack.length() < 1024*1024) {
			this.bufferCrack.append(this.lineOriginal);
			this.bufferCrack.append('\n');
			this.bufferCrack.append('\n');
			this.bufferCrack.append(message.bufferParseGenerateDebug);
			this.bufferCrack.append('\n');
		}
		
		String uniqueCaptureNSU = buildUniqueCaptureNSU(message);
		String connId = message.getConnId();

		if (uniqueCaptureNSU != null || this.activeNsuInternal > 0 || connId != null) {
			message.setId(null);
			this.unresolvedMessages.add(message);
			processUnresolvedMessages(false);
			log(Logger.LOG_LEVEL_DEBUG, "OK", "", message);
		} else {
			log(Logger.LOG_LEVEL_ERROR, "Missing InternalNSU and UniqueCaptureNSU and ConnId", "", message);
		}
	}

	private boolean checkMatchEquipamentAndCaptureNsu(Message message, Message other) {
		boolean match = false;
		
		// se chegou aqui é pq não bate o nsuInterno ou o uniqueCaptureNSU
		String refEquipamentId = message.getEquipamentId();
		String refCaptureNSU = message.getCaptureNsu();

		String equipamentId = other.getEquipamentId();
		String captureNSU = other.getCaptureNsu();

		if (refEquipamentId != null && refCaptureNSU != null && equipamentId != null && captureNSU != null) {
			match = refEquipamentId.equals(equipamentId) && refCaptureNSU.equals(captureNSU);
		}
		
		return match;
	}
	
	// este método também acerta o internalNsu = 0 quando bate o uniqueCaptureNSU,
	// também seta o uniqueCaptureNSU das pernas sem ele
	private boolean resolverIsCompatible(Long internalNSU, Long _internalNSU, String uniqueCaptureNSU, String _uniqueCaptureNSU, Message message, Message other) throws Exception {
		boolean ret = false;
		
		if (_uniqueCaptureNSU != null && _uniqueCaptureNSU.equals(uniqueCaptureNSU)) {
			other.setTransactionId(internalNSU);
			ret = true;
		} else if (internalNSU != null && internalNSU.intValue() != 0 && internalNSU.equals(_internalNSU)) {
			other.setUniqueCaptureNsu(uniqueCaptureNSU);
			ret = true;
		} else {
			if (checkMatchEquipamentAndCaptureNsu(message, other)) {
				other.setUniqueCaptureNsu(uniqueCaptureNSU);
				other.setTransactionId(internalNSU);
				ret = true;
			}
		}

		return ret;
	}

	private void resolveFields(ArrayList<Message> messages) {
		for (int i = messages.size()-1; i > 0; i--) {
			Message message = messages.get(i);
			String direction = message.getConnDirection();

			if (direction != null && direction.equals(Message.DIRECTION_NAME_C2S)) {
				boolean haveChanges = false;

				for (int j = i-1; j >= 0; j--) {
					Message parent = messages.get(j);
					String _direction = parent.getConnDirection();

					if (_direction != null && _direction.equals(Message.DIRECTION_NAME_S2C)) {
//						if (message.resolveFields(parent) == true) {
//							haveChanges = true;
//						}
					}
				}

				if (haveChanges) {
//					Message.conf.save();
				}
			}
		}
	}

	// integra as pernas 0200 e 0202 da mesma transação
	private Long resolverAdjustInternalNSU(Long internalNSU, Long _internalNSU, String uniqueCaptureNSU, String _uniqueCaptureNSU, Message message, Message other) {
		if (_internalNSU != null && _internalNSU > 0) {
			if (_uniqueCaptureNSU != null && uniqueCaptureNSU.equals(_uniqueCaptureNSU)) {
				internalNSU = _internalNSU;
			} else if (checkMatchEquipamentAndCaptureNsu(message, other)) {
				internalNSU = _internalNSU;
			}
		}
		
		return internalNSU;
	}

	private boolean resolverCheckTimeout(long refTimeStamp, long timeStamp) {
		boolean isTimeOut = refTimeStamp > timeStamp + ConverterConf.getInstance().flowTimeout;
		return isTimeOut;
	}

	// varre a lista a procura de transacoes expiradas
	// para cada transacao expirada monta uma lista com o mesmo uniqueCaptureNSU
	// resolve os alias dos campos destes registros
	// grava no log
	private int resolve(ArrayList<Message> messages, int activeIndex, boolean forceTimeout) {
		// permite unir as pernas 0200 e 0202, ambas vao ficar com o mesmo uniqueCaptureNSU, uma terceira 0200
		// com o mesmo uniqueCaptureNSU nao vai ficar com o uniqueCaptureNSU das outras pois o metodo store
		// vai removelo da lista na primeira ocorrencia, ou seja, terao uniqueCaptureNSU distintos pois nao vai
		// herdar o internalNSU.
		// varre a lista a procura de transacoes expiradas
		Message message = this.unresolvedMessages.get(activeIndex);
		log(Logger.LOG_LEVEL_DEBUG, "Resolve", "check header", message);
		// antes de agrupar, verifica se deu tempo para todas as pernas terem sido parseadas
		if (forceTimeout == false && resolverCheckTimeout(this.activeTimeStamp, message.getTimeStamp()) == false) {
			return this.unresolvedMessages.size();
		}

		Long internalNSU = message.getTransactionId();

		if (internalNSU != null) {
			String uniqueCaptureNSU = message.getUniqueCaptureNsu();
			// se nao tem uniqueCaptureNSU e tem internalNSU, pega o unique captureNSU de outra perna da mesma transacao
			if (uniqueCaptureNSU == null) {
				for (int j = activeIndex+1; j < this.unresolvedMessages.size(); j++) {
					Message _message = this.unresolvedMessages.get(j);
					Long _internalNSU = _message.getTransactionId();
					String _uniqueCaptureNSU = _message.getUniqueCaptureNsu();
					
					if (_internalNSU != null && _internalNSU.equals(internalNSU) && _uniqueCaptureNSU != null) {
						uniqueCaptureNSU = _uniqueCaptureNSU;
						message.setUniqueCaptureNsu(uniqueCaptureNSU);
						break;
					}
				}
			}
			// sai a cata de todas as suas pernas
			if (uniqueCaptureNSU != null) {
				log(Logger.LOG_LEVEL_DEBUG, "Resolve", "header", message);
				messages.add(message);
				this.unresolvedMessages.remove(activeIndex);
				int j = activeIndex;

				while (j < this.unresolvedMessages.size()) {
					Message _message = this.unresolvedMessages.get(j);
					log(Logger.LOG_LEVEL_DEBUG, "Resolve", "query", _message);
					
					// TODO : fazer break se esta mensagem estiver em timeout em relacao a mensagem referencia

					try {
						Long _internalNSU = _message.getTransactionId();
						String _uniqueCaptureNSU = _message.getUniqueCaptureNsu();
						// integra as pernas 0200 e 0202 da mesma transação, isto é, o internalNSU da 0200,
						// vai passar a ser o internalNSU da 0202
						internalNSU = resolverAdjustInternalNSU(internalNSU, _internalNSU, uniqueCaptureNSU, _uniqueCaptureNSU, message, _message);
						
						if (resolverIsCompatible(internalNSU, _internalNSU, uniqueCaptureNSU, _uniqueCaptureNSU, message, _message)) {
							log(Logger.LOG_LEVEL_DEBUG, "Resolve", "child", _message);
							messages.add(_message);
							this.unresolvedMessages.remove(j);
//							message.setTimeExec((int) (_message.getTimeStamp() - message.getTimeStamp()));
						} else {
							log(Logger.LOG_LEVEL_DEBUG, "Resolve", "not child", _message);
							j++;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
				// resolve os alias dos campos destes registros
				if (ConverterConf.getInstance().verifyConfigChanges) {
					resolveFields(messages);
				}
			} else {
				this.unresolvedMessages.remove(activeIndex);
				log(Logger.LOG_LEVEL_ERROR, "Timeout and Mismatch Unique Capture NSU", "", message);
			}
		} else {
			this.unresolvedMessages.remove(activeIndex);
			
			if (message.getModule().equals("ON") || message.getModule().equals("OFF")) {
				log(Logger.LOG_LEVEL_WARNING, "Converter.resolve", "missing internalNSU", message);
			} else {
				log(Logger.LOG_LEVEL_ERROR, "Converter.resolve", "missing internalNSU", message);
			}
		}
		
		return activeIndex;
	}

	private void escapeBinaryData(Message message) {
		String data;
		// bit 62
		data = message.getData();
		data = ByteArrayUtils.escapeBinaryData(data, false, '?');
		message.setData(data);
		// bit 63
		data = message.getDataComplement();
		data = ByteArrayUtils.escapeBinaryData(data, false, '?');
		message.setDataComplement(data);
		// bit 48 -- em algumas mensagens 0X10 o primeiro CRC desce zerado (0x00)(0x00)(0x00)(0x00)
		data = message.getCaptureTablesVersionsOut();
		data = ByteArrayUtils.escapeBinaryData(data, false, '?');
		message.setCaptureTablesVersionsOut(data);
		// bit 39 -- em algumas mensagens 0X10 esta (erroneamente) descendo zerado (0x00)(0x00)
		data = message.getCodeResponse();
		data = ByteArrayUtils.escapeBinaryData(data, false, '?');
		message.setCodeResponse(data);
		// campos din�micos
		data = message.getDinamicFields();
		data = ByteArrayUtils.escapeBinaryData(data, false, '?');
		message.setDinamicFields(data);
		//
	}
	
	private void store(Message message, Object obj, boolean isJTA, EntityManager entityManager) {
		try {
			synchronized (entityManager) {
				if (isJTA == true) {
					if (Converter.userTransaction != null) {
						Converter.userTransaction.begin();
					}
				} else {
					entityManager.getTransaction().begin();
				}
				
				try {
					entityManager.persist(obj);
				} catch (Exception e) {
					log(Logger.LOG_LEVEL_ERROR, "Fail in entityManager.persist(message)", e.getMessage(), message);
				}
				
				if (isJTA == true) {
					if (Converter.userTransaction != null) {
						try {
							Converter.userTransaction.commit();
						} catch (Throwable e) {
							log(Logger.LOG_LEVEL_ERROR, "Converter.userTransaction.commit()", e.getMessage(), message);
							
							try {
								Converter.userTransaction.rollback();
							} catch (Exception e2) {
								log(Logger.LOG_LEVEL_ERROR, "Converter.userTransaction.rollback()", e2.getMessage(), message);
							}
						}
					}
				} else {
					try {
						entityManager.getTransaction().commit();
					} catch (Throwable e) {
						log(Logger.LOG_LEVEL_ERROR, "this.entityManager.getTransaction().commit()", e.getMessage(), message);
						
						try {
							entityManager.getTransaction().rollback();
						} catch (Exception e2) {
							log(Logger.LOG_LEVEL_ERROR, "entityManager.getTransaction().rollback()", e2.getMessage(), message);
						}
					}
					
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			log(Logger.LOG_LEVEL_ERROR, "store", e.getMessage(), message);
		}
	}
	
	private void store(ArrayList<Message> messages, HashMap<String, Long> mapUniqueCaptureNsu) {
		if (Converter.entityManagerMessage == null) {
			return;
		}
		
		if (messages.size() < 1) {
			return;
		}

		Message messageRef = messages.get(0);
		// TODO : revisar a necessidade de ter o internalNSU
		Long internalNSU = messageRef.getTransactionId();
		
		if (internalNSU == null) {
			log(Logger.LOG_LEVEL_ERROR, "store : don't find internalNSU", "null", messageRef);
			return;
		}
		
		String uniqueCaptureNSU = messageRef.getUniqueCaptureNsu();

		if (uniqueCaptureNSU == null) {
			log(Logger.LOG_LEVEL_ERROR, "store : don't find uniqueCaptureNSU", "", messageRef);
			return;
		}

		if (mapUniqueCaptureNsu != null) {
			Long storedInternalNSU = mapUniqueCaptureNsu.remove(uniqueCaptureNSU);
	
			if (storedInternalNSU == null) {
				mapUniqueCaptureNsu.put(uniqueCaptureNSU, internalNSU);
			} else {
				internalNSU = storedInternalNSU;
			}
		}
		
		for (Message message : messages) {
			message.setUniqueCaptureNsu(uniqueCaptureNSU);
			escapeBinaryData(message);
			org.domain.iso8583router.entity.ISO8583RouterTransaction store = new org.domain.iso8583router.entity.ISO8583RouterTransaction(message);
			store(message, store, Converter.isJtaMessage, Converter.entityManagerMessage);
		}
	}

	private void storageRequest(ArrayList<Message> messages) {
		Message messageRequest = null;
		int requestIndex;
		
		for (requestIndex = 0; requestIndex < messages.size(); requestIndex++) {
			Message message = messages.get(requestIndex);
			
			try {
				Message route = Message.getFirstCompatible(Connector.getInstance().getMessagesRouteRef(), message, Connector.fieldsRouteMask, false, Connector.getInstance(), "Connector.route");
				
				if (route != null) {
					// TODO : não precisa do new
					// request = message;
					messageRequest = message;
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
		if (messageRequest != null) {
			try {
				String requestModuleId = messageRequest.getModule();
				String requestDirection = messageRequest.getConnDirection();
				log(Logger.LOG_LEVEL_DEBUG, "storageRequest", String.format("requestModuleId = %s - requestDirection = %s", requestModuleId, requestDirection), messageRequest);
				Message response = null;
				Message confirmation = null;
				
				for (int i = requestIndex+1; i < messages.size(); i++) {
					Message message = messages.get(i);
					String moduleId = message.getModule();
					String direction = message.getConnDirection();
					String msgType = message.getMsgType();
					// verifica retorno e reposta do meio de captura
					if (moduleId == null) {
						continue;
					} else if (moduleId.equals(requestModuleId)) {
						if (response == null) {
							if (direction != null && direction.equals(requestDirection) == false) {
								messageRequest = response = message;
							}
						} else if (confirmation == null) {
							if (direction != null && direction.equals(requestDirection) == true) {
								messageRequest = confirmation = message;
							}
						}
					} else if (confirmation != null && msgType != null) {
						messageRequest = message;
					}
				}
				
				log(Logger.LOG_LEVEL_DEBUG, "storageRequest", "response", response);
				log(Logger.LOG_LEVEL_DEBUG, "storageRequest", "confirmation", confirmation);
				log(Logger.LOG_LEVEL_DEBUG, "storageRequest", "request", messageRequest);
				Message messageMerge = new Message(messageRequest);
//				messageMerge.setLogger(Converter.logger);
				
				for (Message message : messages) {
					messageMerge.copyFrom(message, false);
				}
				
				ISO8583RouterTransaction request = new ISO8583RouterTransaction(messageMerge);
				escapeBinaryData(messageMerge);
				request.setId(null);
				store(messageMerge, request, Converter.isJtaRequest, Converter.entityManagerRequest);
			} catch (Exception e) {
				log(Logger.LOG_LEVEL_ERROR, "Fail Storage Request Exception", e.getMessage(), messageRequest);
			}
		} else {
			Message message = messages.get(0);
			log(Logger.LOG_LEVEL_ERROR, "Fail Storage Mismatched Request", "", message);
		}
	}

	private void processUnresolvedMessages(boolean forceTimeout) {
		if (Converter.entityManagerRequest == null) {
			this.store(this.unresolvedMessages, null);
//			this.unresolvedMessages.clear();
			return;
		}
		
		HashMap<String, Long> mapUniqueCaptureNsu = new HashMap<String, Long>(1024);
		ArrayList<Message> groupedMessages = new ArrayList<Message>(100);
		int activeIndex = 0;
		
		while (activeIndex < this.unresolvedMessages.size()) {
			activeIndex = this.resolve(groupedMessages, activeIndex, forceTimeout);
			
			if (groupedMessages.size() > 0) {
				this.storageRequest(groupedMessages);
				this.store(groupedMessages, mapUniqueCaptureNsu);
				groupedMessages.clear();
			}
		}
	}
	
	private boolean isFileNameToday() {
		boolean ret = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
		String today = sdf.format(new Date());
		
		if (this.filename.contains(today)) {
			ret = true;
		}
		
		return ret;
	}
	
	private boolean checkNextFile() throws Exception {
		boolean ret = false;
		File file = new File(this.filename);
		String name = file.getName();
		File dir = file.getParentFile();
		
		if (dir.isDirectory()) {
			String[] files = dir.list();
			java.util.Arrays.sort(files);
			
			for (int i = 0; i < files.length - 1; i++) {
				String filename = files[i];
				
				if (name.equals(filename)) {
					String _name = files[i+1];
					filename = dir.getAbsolutePath() + File.separator + _name;
					java.util.regex.Matcher matcher = Pattern.compile("\\d\\d\\d\\d_\\d\\d_\\d\\d").matcher(_name);
					
					if (matcher.find()) {
						ret = setFile(filename);
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	// Troca a chamada a readLine() por uma que cheque se o arquivo do outro dia foi criado,
	// neste caso, troca o handle e nome do arquivo e reseta o activeLineNumber.
	private String getNextLine() throws Exception {
		String line = this.lineNumberReader.readLine();
		
		while (line == null) {
			if (this.filename == null) {
				break;
			}
			// primeiro checa se existe novo arquivo, e em caso positivo faz a troca
			if (checkNextFile() == false) {
				// se o arquivo em processamento está com a data corrente, aguarda um tempo antes de fazer nova tentativa
				if (isFileNameToday() == false) {
					if (Converter.breakAfterEndOfFile == true) {
						break;
					}
				}
			}
			
			Thread.sleep(ConverterConf.getInstance().sleepAfterEndOfFile);
			line = this.lineNumberReader.readLine();
			
			if (line == null) {
//				System.out.printf("-> %s - não conseguiu ler nova linha %d\n", this.filename, this.lineNumberReader.getLineNumber());
			} else {
//				System.out.printf("%s - conseguiu ler nova linha %d\n", this.filename, this.lineNumberReader.getLineNumber());
			}
		}
		
		this.lineOriginal = line;
		return line;
	}
	
	private boolean setFile(String filename) throws Exception {
		boolean found = false;
		File file = new File(filename);
		
		if (file.isDirectory()) {
			// se for um diretório, pega o último arquivo
			String[] files = file.list();
			java.util.Arrays.sort(files);

			if (files != null && files.length > 0) {
				for (int i = files.length-1; found == false && i >= 0; i--) {
					String _filename = filename + File.separator + files[i];
					file = new File(_filename);
					
					if (file.isFile() && (_filename.endsWith(".txt") || _filename.endsWith(".zip") || _filename.endsWith(".gz"))) {
						found = true;
						filename = _filename;
					}
				}
			}
		} else {
			found = true;
		}
		
		if (found == true) {
			this.processedFilesSize = 0;
			this.filename = filename;
			
			if (this.lineNumberReader != null) {
				this.lineNumberReader.close();
			}
			
			if (filename.endsWith(".txt")) {
				this.lineNumberReader = new LineNumberReader(new FileReader(filename));
				file = new File(filename);
				this.totalFileSize += file.length(); 
			} else if (filename.endsWith(".zip")) {
				ZipInputStream zis = new ZipInputStream(new FileInputStream(filename));
				ZipEntry entry  = zis.getNextEntry();
				this.lineNumberReader = new LineNumberReader(new InputStreamReader(zis));
				this.totalFileSize += entry.getSize(); 
			} else if (filename.endsWith(".gz")) {
				GZIPInputStream zis = new GZIPInputStream(new FileInputStream(filename));
				this.lineNumberReader = new LineNumberReader(new InputStreamReader(zis));
				// determina o tammanho do arquivo descompactado, desde que não seja maior que 4GB
				{
					RandomAccessFile raf = new RandomAccessFile(filename, "r");
					raf.seek(raf.length() - 4);
					byte[] bytes = new byte[4];
					raf.read(bytes);
					long fileSize = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
					
					if (fileSize < 0) {
					  fileSize += (1L << 32);
					}
					
					raf.close();
					this.totalFileSize += fileSize; 
				}
			} else {
				return false;
			}
			
			System.out.println(String.format("Parseando arquivo %s...", this.filename));
		}
		
		return found;
	}

	public long execute(LineNumberReader lineNumberReader) throws Exception {
		ConverterConf.getInstance().reset();
		this.bufferCrack.setLength(0);
		this.lineNumberReader = lineNumberReader;
		long tIni = System.currentTimeMillis();

		while (getNextLine() != null) {
			Message message = new Message();
//			message.setLogger(Converter.logger);
			
			try {
				parse(message);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(this.lineOriginal);
				System.out.println(message.bufferParseGenerateDebug.toString());
				log(Logger.LOG_LEVEL_ERROR, "Fail Parse", e.getMessage(), message);
			}
			
			processedFilesSize += this.lineOriginal.length()+1;
			dumpPerformanceStatistics(tIni, 20000);
		}
		// cada vez que finaliza um arquivo, força salvar em disco
		// não posso colocar 30000 pois existem logs (ex. fep10) que utiliza mais que um gerador de horário
		this.processUnresolvedMessages(true);
		dumpPerformanceStatistics(tIni, 1);
		return 0;
	}
	
	public long execute(File file) throws Exception {
		if (setFile(file.getAbsolutePath()) == false) {
			return 0;
		}
		
		long rc = execute(this.lineNumberReader); 
		this.lineNumberReader.close();
		this.lineNumberReader = null;
		return rc;
	}

	private void dumpPerformanceStatistics(long tIni, int rateSample) {
		int lineNumber = this.lineNumberReader.getLineNumber(); 
		// atualiza a cada 20000 linhas ~ 2000 transações ~ 10 segundos
		if (this.processedFilesSize > 0 && lineNumber % rateSample == 0) {
			long processedTime = System.currentTimeMillis();
			processedTime -= tIni;
			double processTime = ((processedTime*this.totalFileSize)/processedFilesSize);

			System.out.print(String.format("%s - size : %d ", filename, totalFileSize));

			if (processTime < 1000.0) {
				System.out.printf("%s - %s/%s bytes - total execution is %s milis\n", lineNumber, processedFilesSize, this.totalFileSize, processTime);
			} else {
				processTime /= 1000.0;

				if (processTime < 60.0) {
					System.out.printf("%s - %s/%s bytes - total execution is %s secounds\n", lineNumber, processedFilesSize, this.totalFileSize, processTime);
				} else {
					processTime /= 60.0;

					if (processTime < 60.0) {
						System.out.printf("%s - %s/%s bytes - total execution is %s minutes\n", lineNumber, processedFilesSize, this.totalFileSize, processTime);
					} else {
						processTime /= 60.0;

						if (processTime < 24.0) {
							System.out.printf("%s - %s/%s bytes - total execution is %s hours\n", lineNumber, processedFilesSize, this.totalFileSize, processTime);
						} else {
							processTime /= 24.0;
							System.out.printf("%s - %s/%s bytes - total execution is %s days\n", lineNumber, processedFilesSize, this.totalFileSize, processTime);
						}
					}
				}
				
				System.out.println("\n");
			}
		}
	}

	private static class FepConverter implements Runnable {
		private File dirFep;
		
		public void run() {
			try {
				Converter converter = new Converter();
				converter.execute(dirFep);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public FepConverter(File dir) {
			this.dirFep = dir;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ebcdic = "<80><E6><F0><F2><F0><F0><F2>8D<1><80><E1><80><8><0><0><0><0><0><0><0><2><F1><F6><F5><F0><F3><F3><F9><F7><F2><F0><F0><F0><F0><F0><F0><F0><F0><F0><F2><F8><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F0><F1><F0><F0><F0><F0><F9><F2><F5><F1><F3><F1><F2><F5><F3><F0><F0><F0><F1><F1><F1><F1><F0><F1><F2><F5><F3><F0><F9><F2><F5><F6><F5><F3><F2><F0><F1><F1><F0><F9><F9><F9><F8><F6><F8><F9><F6><F0><F1><F1><F0><F9><F0><F0><F0><F0><F0><F0><F8><F9><F6><F3><F9><F5><F4><F0><F8><F8><F2><F0><F0><F0><F0><F0><F0><F0><F0><F3><F6><F2><F4><F5><F1><F5><C8><E3><C9>@<C1><C4><D8>@<D3><E3><C4><C1>a<C3><C1><D9><D3><D6><E2>@<C7><D6>@<D7><D6><D9><E3><D6>@<C1><D3><C5><C7><D9><C5>@@<C2><D9><C1><F0><F0><F8><D7><F7><F7><F0><F3><C3><F0><F3><F9><F8><F6><F0><F2><F6><F0><F0><F0><F1><F1><F0><F0><F0><F0><F0><F8><F0><F1><F0><F7><F6>@@@@@@@@@@<F0><F0><F9><F3><F6><F7><F7><F3><F4><F1><F3><F6>";
		String ascii = convertRepowerISO8583(ebcdic);
		System.out.println(ascii);
		
		
		try {
			ConverterConf.getInstance().reset();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		String persistenceUnitName = ConverterConf.getInstance().persistenceUnitName;
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		Converter.setEntityManagerLog(entityManagerFactory.createEntityManager());
		Converter.setEntityManagerMessage(entityManagerFactory.createEntityManager());
		Converter.setEntityManagerRequest(entityManagerFactory.createEntityManager());
		
		try {
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();
			entityManager.createQuery("delete from CrackerLog").executeUpdate();
			entityManager.createQuery("delete from Message").executeUpdate();
			entityManager.createQuery("delete from Request").executeUpdate();
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
//		try {
//			String textDebug =
//					"130515-001148.783 00 -0001 6360790 ETH <- [ 0200A238040120E1100C020000000000000000100105130011470023760011470513022110000000016126603574170029713404000405332185369900000000126102911521-853-6990040014558CAFFB1423F15B003001058#100@#019@#700@17#400@379385#500@40533124#871@0#902@27061410213012(0)(0) ]"+
//					"\n"+
//					"130515-001148.971 00 -0004 000000 Tsp= 11 16 L(1) C(1) sp_terminal_ultima_transacao '000000001261029','21853699','','0513001147',1,1."+
//					"\n"+
//					"130515-001148.971 00 00000 000000 sp_terminal_ultima_transacao '000000001261029','21853699','','0513001147',1,1; status = 0; [00] OK;"+
//					"";
//			
//			java.io.StringReader reader = new java.io.StringReader(textDebug);
//			LineNumberReader lineNumberReader = new LineNumberReader(reader);
//			Converter converter = new Converter();
//			converter.execute(lineNumberReader);
//			System.out.println(converter.getBufferCrack());
//			return;
//		} catch (Exception e) {
//			// TODO: handle exception
//			return;
//		}
		
		String dataIn = ConverterConf.getInstance().dataIn;
		File root = new File(dataIn);
		
		if (root.exists() && root.isDirectory()) {
			// previsão de até 20 feps
			List<Thread> threads = new ArrayList<Thread>(20);
			String str = ConverterConf.getInstance().feps;
			String[] feps = str.split(",");
			File[] dirs = root.listFiles();
			
			for (File dir : dirs) {
				if (dir.isDirectory()) {
					boolean found = false;
					
					for (String fep : feps) {
						if (dir.getName().toUpperCase().contains(fep)) {
							found = true;
							break;
						}
					}
					
					if (found == true) {
						Converter.FepConverter logsConverter = new Converter.FepConverter(dir);
						Thread thread = new Thread(logsConverter);
						thread.start();
						threads.add(thread);
					}
				}
			}
			// aguarda a finalização do processamento
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void setBreakAfterEndOfFile(boolean breakAfterEndOfFile) {
		Converter.breakAfterEndOfFile = breakAfterEndOfFile;
	}
	
	public static void setFlagsLogLevel(int flagsLogLevels) {
		Converter.logger.setFlagsLogLevels(flagsLogLevels);
	}

	public static void setUserTransaction(UserTransaction userTransaction) {
		Converter.userTransaction = userTransaction;
		Converter.logger.setUserTransaction(userTransaction);
	}

	public static void setEntityManagerLog(EntityManager entityManager) {
		Converter.logger.setEntityManager(entityManager);
	}

	public static void setEntityManagerMessage(EntityManager entityManager) {
		Converter.entityManagerMessage = entityManager;
		Converter.isJtaMessage = false;
		
		try {
			entityManager.getTransaction().begin();
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			Converter.isJtaMessage = true;
		}
	}

	public static void setEntityManagerRequest(EntityManager entityManager) {
		Converter.entityManagerRequest = entityManager;
		Converter.isJtaRequest = false;
		
		try {
			entityManager.getTransaction().begin();
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			Converter.isJtaRequest = true;
		}
	}

	public StringBuilder getBufferCrack() {
		return bufferCrack;
	}

}
