package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Logger;
import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.commom.Utils;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class TrSpTrans implements Module {
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

		builder.append("<!�");
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
					"</CONF            CANC</�";
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

	private long authComm(Message transaction) {
		long rc = 0;
		String msgType = transaction.getMsgType();
		String codeProcess = transaction.getCodeProcess();
		String codeResponse = transaction.getCodeResponse();
		String providerData = transaction.getFieldData("providerData");
		

		transaction.setReplyEspected("1");
		return rc;
	}

	private long providerComm(Message transaction, String rootIn) {
		long rc = 0;
		String msgType = transaction.getMsgType();
		String codeProcess = transaction.getCodeProcess();
		String captureData = null;
		
		if (rootIn.startsWith("RequisicaoEcoRetorno_in")) {
			transaction.setMsgType("0800");
			String dateTime = transaction.getDateTimeGmt();
			String date = dateTime.substring(4, 8);
			String hour = dateTime.substring(8);
			transaction.setDateLocal(date);
			transaction.setHourLocal(hour);
		} else if (rootIn.startsWith("ConsultaParametrosReqRetorno_in")) {
			transaction.setMsgType("0205");
			String dateTime = transaction.getDateTimeGmt();
			String date = dateTime.substring(4, 8);
			String hour = dateTime.substring(8);
			transaction.setDateLocal(date);
			transaction.setHourLocal(hour);
		}
		
		transaction.setProviderName("SPTRANS");
		transaction.setReplyEspected("1");
		return rc;
	}

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}

	public void buildSptransRechargeData(Message transaction) {
		StringBuilder buffer = new StringBuilder((3 + 10 + 3 + 1 + 2) + (16 + 58 + 16 + 16 + 16 + 16 + 28));
		// 64 bytes por setor * 2 caracteres por byte
		// 01) N 003: Vers�o do formato do bit 63 (�002�):
		// -> pegamos da recarga
		String version = transaction.getFieldData("versao"); 
		buffer.append(version);
		// 02) N 010: N�mero do cart�o (n�mero l�gico � vide dados de emiss�o);
		// -> pegamos da recarga
		String pan = transaction.getPan();
		buffer.append(pan);
		// 03) N 003: Tipo do cart�o (vide dados de emiss�o);
		// -> pegamos da recarga
		String cardType = transaction.getFieldData("tipo_cartao"); 
		buffer.append(cardType);
		// 04) N 001: Grupo do cart�o (vide mensagem de consulta 0205/0215);
		// -> pegamos da recarga
		String cardGroup = transaction.getFieldData("grupo_cartao"); 
		buffer.append(cardGroup);
		// 05) N 002: Tipo de carga a ser feita no cart�o, podendo ser: 03-VT; 04-VE; 05-VC; 06-Recarga de 20 passagens � Cart�o Fidelidade; 07-Recarga de 10 passagens � Cart�o Lazer; 8-VE Lista;
		// -> pegamos da recarga
		String chargeType = "";//transaction.getFieldDataLeftAlign("tipo_carga", '0', 2);
		buffer.append(chargeType);
		// Dados Cart�o I;
		int[] fileds_blocos_I = { 8, 12, 20, 24, 28, 32,  9}; 
		int[] fields_size_I   = {16, 58, 16, 16, 16, 16, 28}; 
	    // Dados Cart�o II;
		int[] fileds_blocos_II = {36, 44, 48, 53, 60, 61}; 
		int[] fields_size_II   = {58, 16, 16, 58, 16, 16}; 
		String captureData = transaction.getData();
		// c�digo tempor�rio para pegar os dados do cart�o diretamente do POS
		{
			int posIni = captureData.indexOf("@C06*");

			if (posIni >= 0) {
				posIni += 5;
				int posEnd = captureData.indexOf("@");

				if (posEnd >= posIni) {
					captureData = captureData.substring(posIni, posEnd);
				} else {
					captureData = captureData.substring(posIni);
				}
			}
		}
		
		ByteArrayUtils.appendAsciiHexToBinary(buffer, captureData, captureData.length(), 2, false, '0', '0');
		// B 004: Valor da Carga, formato LITTLE-ENDIAN, (0 = indica solicita��o de recarga pr�-paga � por lista, ou recarga m�ltipla, casos em que o valor � obtido pelo HM);
		// -> pegamos da recarga
		String value = "";//transaction.getFieldDataLeftAlign("transactionValue", '0', 8);
		ByteArrayUtils.appendAsciiHexToBinary(buffer, value, value.length(), 2, false, '0', '0');
		// 15/00) N 05: Quantidade de Cota para recarga temporal;
		// -> pegamos da recarga
//		String cota = transaction.getDataLeftAlign("quantidadeCota", '0', 5);
//		buffer.append(cota);
		// 16/11) B 004: N�mero f�sico do cart�o;
		// -> pegamos da recarga
		String cardNumber = transaction.getFieldData("cardNumber"); 
		ByteArrayUtils.appendAsciiHexToBinary(buffer, cardNumber, cardNumber.length(), 2, false, '0', '0');
		// 17/12) B 006: Dados gerais (preencher com zeros).
		ByteArrayUtils.appendAsciiHexToBinary(buffer, "000000000000", 12, 2, false, '0', '0');
		// Imagem do cart�o em texto (768 bytes)
		// Tipo de cart�o (2 bytes)
		// N�mero serial do cart�o (8bytes)
		int[] fileds_blocos_in = {8,9,10,12,13,14,16,17,18,20,21,22,24,25,26,28,29,30,32,33,34,36,37,38}; 
		// 106143B01967A4A014D282405AC82C050D36FFFFF8000000CD0A0B1363AEFD560D36FFFFF8000000CD0A0B1363AEFD56B40C270402225A00FCCC5268456BE16D008000044012C00000045E1D9200F90B11787402A00000000000000000009616B40C270402225A00FCCC5268456BE16D008000044012C00000045E1D9200F90B11787402A000000000000000000096160CF90022397F3D3C4A0169A3C10D1B4200000000FFFFFFFF0000000015EA15EA00000000FFFFFFFF0000000015EA15EA0E1F001D4DCF8988760B60FF4C77C57B14000000EBFFFFFF1400000019E619E614000000EBFFFFFF1400000019E619E611480007D0002B40BEA1D4BC1F4C578700000000FFFFFFFF000000001DE21DE200000000FFFFFFFF000000001DE21DE211780001F4002D403D45C43D4EFBC051E30000001CFFFFFFE300000021DE21DEE30000001CFFFFFFE300000021DE21DE00000000000000000000000000000000000000000000000000000000000000000819A00000000000000000000000A950 05 7977A5E1
		
//		byte[] data = new byte[38 + (16 + 58 + 16 + 16 + 16 + 16 + 28)];
//		Utils.AsciiHexToBinary(data, captureData);
		transaction.setData( buffer.toString());
	}
	
	public long execute(Object obj) {
		Message transaction = (Message) obj;
		long rc = 0;
		String rootOriginal = transaction.getRoot();
		String msgTypeOriginal = transaction.getMsgType();
		String codeProcessOriginal = transaction.getCodeProcess();
		String date = transaction.getDateLocal();
		String hour = transaction.getHourLocal();
		String dateTime = transaction.getDateTimeGmt();
		String routes = transaction.getFieldData("route");
		String[] listRoutes = routes.split(",");
		String codeResponse = null;
		String codeResponseAuth = null;
		
		if (date == null || hour == null) {
			String systemDateTime = transaction.getSystemDateTime();
			date = systemDateTime.substring(4, 8);
			transaction.setDateLocal(date);
			hour = systemDateTime.substring(8);
			transaction.setHourLocal(hour);
		}

		for (int i = 0; i < listRoutes.length && rc >= 0; i++) {
			String route = listRoutes[i];
			
			if (route == null) {
				continue;
			}
			
			String[] routeParams = route.split(":");
			String module = routeParams[0];
			String root = null;
			
			if (routeParams.length > 1) {
				root = routeParams[1];
			}

			try {
				if (module.toUpperCase().equals("PROVIDER")) {
					rc = providerComm(transaction, rootOriginal);
					
					if (rc >= 0) {
						transaction.setModuleOut(module);
						rc = this.manager.commOut(transaction, null);
						
						if (rc >= 0) {
							codeResponse = transaction.getCodeResponse();
						}
					}
				} else if (module.toUpperCase().equals("AUTH")) {
					rc = authComm(transaction);
					
					if (rc >= 0) {
						transaction.setModuleOut(module);
						rc = this.manager.commOut(transaction, null);
						
						if (rc >= 0) {
							codeResponseAuth = transaction.getCodeResponse();
						}
					}
				} else {
					transaction.setModuleOut(module);
					transaction.setRoot(root);
					
					if (module.equals("SPTRANS")) {
						if (codeProcessOriginal != null && codeProcessOriginal.equals("904000")) {
							buildSptransRechargeData(transaction);
						}
					}
					
					if (msgTypeOriginal != null && msgTypeOriginal.equals("0900")) {
						transaction.setMsgType("0200");
					}
					
					rc = this.manager.commOut(transaction, null);
					
					if (rc >= 0) {
						codeResponse = transaction.getCodeResponse();
						
						if (codeResponse == null || codeResponse.equals("00") == false) {
							rc = -1;
							this.manager.log(Logger.LOG_LEVEL_ERROR, "TrSpTrans.execute", String.format("%s - %s - codeResponse wrong : %s", module, root, codeResponse), transaction);
						}
					} else {
						this.manager.log(Logger.LOG_LEVEL_ERROR, "TrSpTrans.execute", String.format("%s - %s - result commOut(...) wrong : %s", module, root, rc), transaction);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				System.out.printf("Error in module %s\n", module);
				break;
			}
		}

		transaction.setSendResponse(true);

		if (rootOriginal.startsWith("RequisicaoEcoRetorno_c2s")) {
			transaction.setRoot("RespostaEcoEntrada_s2c");
		} else if (rootOriginal.startsWith("RequisicaoEco2Retorno_c2s")) {
			transaction.setRoot("RespostaEco2Entrada_s2c");
		} else if (rootOriginal.startsWith("ConsultaParametrosReqRetorno_c2s")) {
			transaction.setRoot("ConsultaParametrosRespEntrada_s2c");
		} else if (msgTypeOriginal.equals("0200")) {
			buildMessage(transaction, msgTypeOriginal, codeProcessOriginal, codeResponse);
		} else if (msgTypeOriginal.equals("0300")) {
			buildReceipt(transaction, msgTypeOriginal, codeProcessOriginal, codeResponse);
		} else if (msgTypeOriginal.equals("0800")) {
			buildReceipt(transaction, msgTypeOriginal, codeProcessOriginal, codeResponse);
		} else if (msgTypeOriginal.equals("0420")) {
		} else {
			transaction.setSendResponse(false);
		}

		String providerNSU = transaction.getProviderNsu();

		if (providerNSU != null && providerNSU.length() > 6) {
			int diff = providerNSU.length() - 6;
			providerNSU = providerNSU.substring(diff);
			transaction.setProviderNsu(providerNSU);
		}

		transaction.setCodeProcess(codeProcessOriginal);
		transaction.setDateLocal(date);
		transaction.setHourLocal(hour);
		transaction.setDateTimeGmt(dateTime);
		
		if (codeResponse != null && codeResponse.equals("00") == false) {
			transaction.setCodeResponse("83");
		}
		
		if (codeResponseAuth != null && codeResponseAuth.equals("0") == false) {
			transaction.setCodeResponse("82");
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
		info.name = "TrSpTrans";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
