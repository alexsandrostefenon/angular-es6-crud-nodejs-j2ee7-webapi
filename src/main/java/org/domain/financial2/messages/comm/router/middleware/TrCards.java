package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Logger;
import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.commom.Utils;
import org.domain.financial2.entity.CardConf;
import org.domain.financial2.entity.ISO8583TefProvider;
import org.domain.financial2.messages.comm.router.Atalla;
import org.domain.financial2.messages.comm.router.Tags;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class TrCards implements Module {
	Connector manager;

	private String removeLeftZeros(String value, int minDigits) {
		if (value != null && value.length() > minDigits) {
			int posEnd = value.length()-minDigits;
			int posIni = 0;

			while (posIni < posEnd && (value.charAt(posIni) == '0' || value.charAt(posIni) == ' ')) {
				posIni++;
			}

			value = value.substring(posIni, posEnd+1);
		}

		return value;
	}

	private String getFormatedValue(String value) {
		if (value == null) {
			return null;
		}

		value = removeLeftZeros(value, 3);
		int len = value.length();

		if (len > 9) {
			value = value.substring(0, len-9) + "." + value.substring(len-9, len-6) + "." + value.substring(len-6, len-3) + "." + value.substring(len-3, len-0);
		} else if (len > 6) {
			value = value.substring(0, len-6) + "." + value.substring(len-6, len-3) + "." + value.substring(len-3, len-0);
		} else if (len > 3) {
			value = value.substring(0, len-3) + "." + value.substring(len-3, len-0);
		}

		return value;
	}

	private String getFormatedValue(Message transaction) {
		String value = transaction.getTransactionValue();

		return getFormatedValue(value);
	}

	private long setReceipt(Message transaction, String preTitle, String title, String textComerce, String textClient, boolean enableReprinter) {
		long rc = 0;
		String textFixed =
				"</Informese sobre la garantia estatal" +
				"</de los depositos en su banco o en" +
				"</www.sbif.cl." +
				"</Toda transaccion queda" +
				"</sujeta a verificacion.";

		StringBuilder builder = new StringBuilder(1024);
		builder.append("92");
		builder.append("<?");
		builder.append("<+");

		if (textComerce != null) {
			builder.append("01");
		} else {
			builder.append("00");
		}

		if (textClient != null) {
			builder.append("01");
		} else {
			builder.append("00");
		}

		if (enableReprinter) {
			builder.append("<R");
		}

		builder.append("<-");
		builder.append("<C<BBANCO SANTANDER<b<c");

		if (textClient != null) {
			builder.append("<~");
			
			if (preTitle != null) {
				builder.append("</<C");
				builder.append(preTitle);
				builder.append("<c");
			}
			
			builder.append("</<C<B<H");
			builder.append(title);
			builder.append("<h<b<c");
			builder.append(textClient);
			builder.append("</");
			builder.append("</<C<HORIGINAL CLIENTE<c<h");
			builder.append("</");
			builder.append(textFixed);
		}

		if (textComerce != null) {
			builder.append("<=");
			
			if (preTitle != null) {
				builder.append("</<C");
				builder.append(preTitle);
				builder.append("<c");
			}
			
			builder.append("</<C<B<H");
			builder.append(title);
			builder.append("<h<b<c");
			builder.append(textComerce);

			if (textClient != null) {
				builder.append("</");
				builder.append("</<C<HCOPIA COMERCIO<c<h");
				builder.append("</");
			}

			builder.append(textFixed);
		}

		builder.append("<!§");
		transaction.setData( builder.toString());
		return rc;
	}

	private long buildMessage(Message transaction, String msgType, String codeProcess, String codeResponseBank) {
		long rc = 0;
		String providerData = transaction.getFieldData("providerData");
		String value = getFormatedValue(transaction);

		if (codeResponseBank == null || codeResponseBank.equals("000") == false) {

		} else if (codeProcess.equals("302300")) {
			String fmt =
					"96%s" +
					"</Rut: %s" +
					"</$%s" +
					"</CONF            CANC</§";
			String rut = "";
			String text = String.format(fmt, providerData, rut, value);
			transaction.setData( text);
		}

		return rc;
	}

	private long buildReceipt(Message transaction, String msgType, String codeProcess, String codeResponseBank) {
		long rc = 0;
		String providerData = transaction.getFieldData("providerData");
		String value = getFormatedValue(transaction);
		String financialDate = TransactionsCommom.getFormatedDate(transaction.getFieldData("financialDate"));
		String providerNSU = transaction.getProviderNsu();
//		String dtVencimento = transaction.getData("dtVencimento");

		if (codeResponseBank == null || codeResponseBank.equals("000") == false) {
		} else if (codeProcess.equals("302300")) {
			String title = "DEPOSITO EN CUENTA";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</Cuenta Deposito: ********%s" +
					"</Nombre Titular: %s" +
					"</RUT Titular: *******%s" +
					"</<C<B<HMonto Deposito<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</<C<B<HMonto Deposito<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String rut = "";
			rut = rut.substring(7);
			String account = "";
			account = account.substring(8);
			String textClient = String.format(fmtClient, providerNSU, account, providerData, rut, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, value, financialDate);
			rc = setReceipt(transaction, null, title, textComerce, textClient, true);
		}

		return rc;
	}

	private long authCommSYSCAP(Message messageOut, ISO8583TefProvider conf, String root) throws Exception {
		long rc = -1;
		
		Message messageIn = new Message();
//		String msgType = messageOut.getMsgType();
//		String providerData = messageOut.getFieldData("providerData");
		
		if (root != null && conf.getAuthCallName() != null) {
			root = root + conf.getAuthCallName() + "Entrada";
			messageOut.setRoot(root);
			messageOut.setReplyEspected("1");
			String moduleIn = messageOut.getModuleIn();
			
			if (moduleIn != null && moduleIn.startsWith("TEF")) {
				messageOut.setFieldData("TEFDedicado", "true");
			} else {
				messageOut.setFieldData("TEFDedicado", "false");
			}
			
//			messageOut.setProviderName(conf.authProviderName);
			rc = this.manager.commOut(messageOut, messageIn);
			
			if (rc >= 0) {
				String codeResponse = messageIn.getCodeResponse();
				
				if (Utils.checkValue(codeResponse, "00")) {
					messageOut.copyFrom(messageIn, false);
//					messageOut.setProviderName(conf.name);
					rc = 0;
				}
			}
		} else {
			rc = -1;
		}
		
		return rc;
	}

	private long cryptProviderDes(Message message, ISO8583TefProvider conf) {
		long rc = -1;
		
		try {
			String pinOpenedHex = message.getPassword();
			String workingKey = message.getFieldData("workingKey");
			String keyDesToProvider;
			
			if (workingKey != null && workingKey.length() == 16) {
				keyDesToProvider = TransactionsCommom.cryptDes(conf.getCryptKeyReference(), workingKey);
			} else {
				keyDesToProvider = conf.getCryptKeyReference();
			}
			
			String pinProviderCrypted = TransactionsCommom.cryptDes(keyDesToProvider, pinOpenedHex);
			
			if (pinProviderCrypted != null) {
				message.setPassword(pinProviderCrypted);
				rc = 0;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return rc;
	}

	private long translatePassword3DesToDes(Message message, ISO8583TefProvider conf) {
		long rc = -1;

		if (rc == 0) {
			try {
				CardConf.openPassword(message, TransactionsCommom.masterKeyKnownDes);
				rc = cryptProviderDes(message, conf);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				rc = -1;
			}
		}
		
		return rc;
	}

	private long translatePassword3DesTo3Des(Message message, ISO8583TefProvider conf, int captureCryptType) {
		long rc = -1;
//	String msgType = message.getMsgType();
//	String codeProcess = message.getCodeProcess();
//	String captureData = null;
//	String trackI = message.getTrackI();
//	String panDigitado = message.getPan();
//	String value = message.getTransactionValue();
//	String providerEC = message.getProviderEc();
//	String equipamentId = message.getEquipamentId();
		String atallaAkbKeyRef = TransactionsCommom.atallaAkbKeyRefEquipament;
		
		if (captureCryptType == 3) {
			String transportData = message.getTransportData();

			if (transportData != null) {
				Tags tags = Tags.parseTLV(transportData, 100, 4, 3);
				String softwareProvider =  tags.get("0067");

				if (softwareProvider != null) {
					if (softwareProvider.equals("VBI")) {
						atallaAkbKeyRef = "1PUNE000,1EFD07468101DA4F12A1F8F5D8EDEFAB57CBFDBFBCDDBF6B,766B7437C36BF00F";
					} else if (softwareProvider.equals("SWE")) {
						atallaAkbKeyRef = "1PUNE000,C9882882D180D9A35C0E9867ADB318A68998685F4A8E1DFB,2825B52E30F17DD5";
					}
				}
			}
		}

		rc = Atalla.getInstance().translatePIN(this.manager, message, atallaAkbKeyRef, conf.getCryptKeyReference());
		return rc;
	}
	
	private int getCaptureCryptType(Message message) {
		int type = 0;
		String transportData = message.getTransportData();

		if (transportData != null) {
			// vai apenas a subtag (0069) que vem dentro do bit 59
			Tags tags = Tags.parseTLV(transportData, 100, 4, 3);
			String tefCryptType =  tags.get("0069");

			if (tefCryptType != null) {
				type = Integer.parseInt(tefCryptType);
			}
		}

		return type;
	}

	private long cryptTranslate(Message message, ISO8583TefProvider conf) {
		long rc = -1;
		int captureCryptType = getCaptureCryptType(message);
		
		if (captureCryptType >= 0) {
			if (captureCryptType == 0 || captureCryptType == 4) {
				rc = cryptProviderDes(message, conf);
			} else {
//			rc = translatePassword3DesToDes(message, conf);
				rc = translatePassword3DesTo3Des(message, conf, captureCryptType);
			}
		}
		
		return rc;
	}

	private long providerComm(Message message, ISO8583TefProvider conf, String root) {
		long rc = -1;
		Message messageIn = new Message();
		rc = cryptTranslate(message, conf);
		
		if (rc == 0) {
			String msgType = message.getMsgType();
			String providerName = message.getProviderName();
			//		String codeProcess = transaction.getCodeProcess();
			//		String captureData = null;
			boolean espectResponse = (msgType.endsWith("02") == false);
			
			if (espectResponse) {
				message.setReplyEspected("1");
			}
			
			if (providerName == null || providerName.length() == 0) {
				message.setProviderName("OPERADORA_CAPTURA_PADRAO");
			}
/*
	// bit 061, nao vai nas 0202 e nas 0800
	if (strcmp(tr->route->msgTypeCapture, "0202") != 0 && strcmp(tr->route->msgTypeCapture, "0420") != 0 && strcmp(tr->route->msgTypeCapture, "0800") != 0) {
		if (tr->inputType == POS) {
				tr->providerOut->Set_BIT_061("001", 3);
		} else if (tr->inputType == TEF_NOVO || tr->inputType == TEF_ANTIGO) {
				tr->providerOut->Set_BIT_061("002", 3);
		} else if (tr->inputType == GET) {
				tr->providerOut->Set_BIT_061("003", 3);
		}
	}
 */
			message.setModuleOut(providerName);
			rc = this.manager.commOut(message, messageIn);
			
			if (rc == 0) {
				// processa resposta
				if (espectResponse) {
					message.setData(messageIn.getData());
					message.setCodeResponse(messageIn.getCodeResponse());
//					ret = transaction_provider_comm_process_response_ISO8583(tr);
				}
			} else {
				// processa erro de comunica��o
			}
		}
		
		return rc;
	}

	private void adjustCaptureEquipamentType(Message message) {
		String[] listTef = {"SITEF", "TEF", "CSI", "SITEF_02", "SIXNET", "HCT", "SIX_02"};
		
		if (Utils.findInList(listTef, message.getModuleIn()) >= 0) {
			message.setCaptureEquipamentType("002");
		}
	}

	public long execute(Object obj) {
		Message message = (Message) obj;
		CardConf tefConf = CardConf.getTefConf(this.manager.getEntityManager(), message);

		if (tefConf == null) {
			this.manager.log(Logger.LOG_LEVEL_ERROR, "TrCards.execute", "Don't found card, provider and flow configuration", message);
			return -1;
		}
		
		ISO8583TefProvider conf = tefConf.getProvider();
		
		if (conf == null) {
			this.manager.log(Logger.LOG_LEVEL_ERROR, "TrCards.execute", "Don't found card configuration", message);
			return -1;
		}
		
		adjustCaptureEquipamentType(message);
		long rc = 0;
		String msgTypeOriginal = message.getMsgType();
		String codeProcessOriginal = message.getCodeProcess();
		String route = message.getFieldData("route");
		String[] modules = route.split(",");
		String codeResponseProvider = null;
		String codeResponseAuth = null;
		message.setProviderName(conf.getText());

		for (int i = 0; i < modules.length && rc >= 0; i++) {
			String moduleParms = modules[i];
			String[] params = moduleParms.split(":");
			
			if (params.length > 0) {
				String module = params[0];
				String root = null;
				
				if (params.length > 1) {
					root = params[1];
				}
				
				try {
					message.setModuleOut(module);
					
					if (module.toUpperCase().equals("PROVIDER")) {
						rc = providerComm(message, conf, root);
						
						if (rc >= 0) {
							codeResponseProvider = message.getCodeResponse();
						}
					} else if (module.toUpperCase().equals("SYSCAP")) {
						rc = authCommSYSCAP(message, conf, root);
						
						if (rc >= 0) {
							codeResponseAuth = message.getCodeResponse();
						}
					}
				} catch (Exception e) {
					this.manager.log(Logger.LOG_LEVEL_ERROR, "TrCards.execute", String.format("exception raised in module %s : %s", module, e.getMessage()), message);
					rc = -1;
					break;
				}
			}
		}

		message.setSendResponse(true);

		if (msgTypeOriginal.equals("0100")) {
			buildMessage(message, msgTypeOriginal, codeProcessOriginal, codeResponseProvider);
		} else if (msgTypeOriginal.equals("0200")) {
			buildReceipt(message, msgTypeOriginal, codeProcessOriginal, codeResponseProvider);
		} else if (msgTypeOriginal.equals("0420")) {
		} else {
			message.setSendResponse(false);
		}

		String providerNSU = message.getProviderNsu();

		if (providerNSU != null && providerNSU.length() > 6) {
			int diff = providerNSU.length() - 6;
			providerNSU = providerNSU.substring(diff);
			message.setProviderNsu(providerNSU);
		}

		message.setCodeProcess(codeProcessOriginal);
		
		if (codeResponseProvider != null && codeResponseProvider.equals("00") == false) {
			message.setCodeResponse("83");
		}
		
		if (codeResponseAuth != null && codeResponseAuth.equals("0") == false) {
			message.setCodeResponse("82");
		}

		return rc;
	}

	public void start() {
		// TODO Auto-generated method stub
	}

	public void stop() {
		// TODO Auto-generated method stub
	}

	public void getInfo(ModuleInfo info) {
		info.family = "Transactions";
		info.name = "TrCards";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO reload cards.ini, use semaphore
	}

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
}
	
/*
COD TEXTO_POS MASCARA MIN MAX CAMPO AGRUPADOR QTD_PERG COD_PERG PARAMETROS
0100 QUILOMETRAGEM 02 01 06 062 0000 00
0101 LITRAGEM 15 00 09 062 0000 00
0102 VALOR DO COMBUSTIVEL 15 01 07 062 0000 00 #9028@1
0103 LITROS DE OLEO 15 00 06 062 0000 00
0104 VALOR DO OLEO 15 00 07 062 0000 00 #9028@1
0105 CODIGO MANUTENCAO 02 00 02 062 0000 01 #9030@20 0106
0106 VALOR MANUTENCAO 15 01 06 062 0000 00 #9028@1 
0107 MATRICULA 02 01 11 062 0000 00 
0108 NR. FROTA 02 01 08 062 0000 00 
0109 PLACA DO VEICULO 02 01 04 062 0000 00
0110 LETRAS DA PLACA 04 01 03 062 0000 00
0111 TIPO DE COMBUSTIVEL 02 01 02 062 0000 00
0112 SENHA 06 04 08 052 0000 00 #9016@2
0113 VALOR TOTAL 17 00 01 004 0000 00 #9029@1
0114 REG EMPREGADO 02 01 08 062 0000 00
0115 ORDEM DE SERVICO 02 01 08 062 0000 00

M�scara Descri��o 
01 Menu (n�o h� digita��o) 
02 N�mero sem m�scara de formata��o 
04 Texto alfanum�rico sem m�scara de formata��o 
06 Senha do portador do cart�o (PIN) 
14 Item de Menu 
16 Pergunta com resposta SIM ou N�O 
17 Texto para display (sem digita��o) 

COD Descri��o
01 Menu (n�o h� digita��o) 
02 N�mero sem m�scara de formata��o 
03 N�mero com m�scara de valor monet�rio 
04 Texto alfanum�rico sem m�scara de formata��o 
05 Senha simples (exibir apenas asteriscos no display) 
06 Senha do portador do cart�o (PIN) 
09 N�mero de cart�o � obtido de trilha 
10 N�mero de cart�o � obtido de trilha ou digitado 
11 Data no formato DD/MM/AA 
12 Data no formato DD/MM 
13 Data no formato MM/AA 
14 Item de Menu (op��es de Menu) 
15 N�mero com formata��o 2 casas decimais (Ex.: 0,00) 
16 Pergunta com resposta SIM ou N�O 
17 Texto para display (n�o h� digita��o) 
18 C�digo de barras (leitora) 
19 C�digo de barras (leitora/digitado) 
20 Hora no formato HH:MM 
21 Hora no formato HH:MM:SS 
 */

