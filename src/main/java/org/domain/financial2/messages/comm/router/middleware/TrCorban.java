package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.commom.Utils;
import org.domain.financial2.messages.comm.router.Atalla;
import org.domain.financial2.messages.comm.router.Tags;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class TrCorban implements Module {
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

	private String convertYYYY_MM_DD_To_DD_MM_YYYY(String date) {
		date = date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
		return date;
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

	private long buildMessage(Message transaction, String msgType, String codeProcess, String codeResponseBank, Tags tlvCaptureData) {
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
			String rut = tlvCaptureData.getLeftAlign("0201", '0', 11);//a11
			String text = String.format(fmt, providerData, rut, value);
			transaction.setData( text);
		} else if (codeProcess.equals("832000")) {
			String fmt =
					"96%s" +
					"</Venc: %s" +
					"</$%s" +
					"</CONF            CANC</§";
			String codeProcessBank = transaction.getCodeProcess();
			String dtExpiration;

			if (codeProcessBank.startsWith("70")) {
				dtExpiration = providerData.substring(10, 10+10);
				dtExpiration = convertYYYY_MM_DD_To_DD_MM_YYYY(dtExpiration);
			} else {
				dtExpiration = providerData.substring(0, 10);
			}

			String dsConvenio = transaction.getFieldData("dsConvenio");
			String text = String.format(fmt, dsConvenio, dtExpiration, value);
			transaction.setData( text);
		} else if (codeProcess.equals("833002")) {
			String fmt =
					"98%s" +
					"</Ingresse monto entre" +
					"</$%s y $%s§";
			String personName = providerData.substring(0, 30);
			value = providerData.substring(30, 30+12);
			value = getFormatedValue(value);
			String valueMin = providerData.substring(30+12, 30+12+12);
			valueMin = getFormatedValue(valueMin);
			String text = String.format(fmt, personName, valueMin, value);
			transaction.setData( text);
		} else if (codeProcess.equals("833000") || (codeProcess.equals("833001"))) {
			String fmt =
					"96Cuota: %s" +
					"</Interes: $%s" +
					"</Total: $%s" +
					"</CONF            CANC</§";
			String cuota = providerData.substring(0+10, 0+10+3);
			cuota = removeLeftZeros(cuota, 1);
			String valueInteres = providerData.substring(0+10+3, 0+10+3+12);
			valueInteres = getFormatedValue(valueInteres);
			String text = String.format(fmt, cuota, valueInteres, value);
			transaction.setData( text);
		}

		return rc;
	}

	private long buildReceipt(Message transaction, String msgType, String codeProcess, String codeResponseBank, Tags tlvCaptureData) {
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
			String rut = tlvCaptureData.getLeftAlign("0201", '0', 11);//a11
			rut = rut.substring(7);
			String account = tlvCaptureData.getLeftAlign("0202", '0', 12);//a12
			account = account.substring(8);
			String textClient = String.format(fmtClient, providerNSU, account, providerData, rut, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, value, financialDate);
			rc = setReceipt(transaction, null, title, textComerce, textClient, true);
		} else if (codeProcess.equals("012000")) {
			String title = "GIRO EN CUENTA";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</Cuenta No: ********%s" +
					"</Nombre Titular: %s" +
					"</<C<B<HMonto Giro<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</<C<B<HMonto Giro<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String account = providerData.substring(8, 12);
			String personName = providerData.substring(12);
			String textClient = String.format(fmtClient, providerNSU, account, personName, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, value, financialDate);
			rc = setReceipt(transaction, null, title, textComerce, textClient, true);
		} else if (codeProcess.equals("302400")) {
			String title = "TRANSFERENCIA<h<b<c</<C<B<HDE FONDOS";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</Nombre Titular: %s" +
					"</Cuenta Cargo N: ********%s" +
					"</Cuenta Abono N: ********%s" +
					"</<C<B<HMonto Transferencia<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</<C<B<HMonto Transferencia<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String accountSource = providerData.substring(0+8, 0+12);
			String accountDest = providerData.substring(12+8, 12+12);
			String personName = providerData.substring(12+12);
			String textClient = String.format(fmtClient, providerNSU, personName, accountSource, accountDest, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, value, financialDate);
			rc = setReceipt(transaction, null, title, textComerce, textClient, true);
		} else if (codeProcess.equals("302100")) {
			String title = "CONSULTA DE SALDO";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</No Cuenta: ********%s" +
					"</<C<B<HSaldo Disponible<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String account = providerData.substring(0+8, 0+12);
			value = providerData.substring(12);
			value = getFormatedValue(value);
			String textClient = String.format(fmtClient, providerNSU, account, value, financialDate);
			rc = setReceipt(transaction, null, title, null, textClient, false);
		} else if (codeProcess.equals("832000")) {
			String title = "PAGO DE SERVICIOS";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</Servicio: %s" +
					"</No Cliente: ********%s" +
					"%s" +
					"</Fecha Vencimiento: %s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</Servicio: %s" +
					"</Fecha Vencimiento: %s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String nrClienteServico = tlvCaptureData.getLeftAlign("0995", '0', 12);

			if (nrClienteServico == null) {
				nrClienteServico = tlvCaptureData.getLeftAlign("0100", '0', 12);
			}

			nrClienteServico = nrClienteServico.substring(8);
			String account;

			if (providerData.length() == 10+10+12+12) {
				account = "</No Cuenta: ********" + providerData.substring(10+10+12+8);
			} else if (providerData.length() == 10+12) {
				account = "</No Cuenta: ********" + providerData.substring(10+8);
			} else {
				account = "";
			}

			String dsConvenio = transaction.getFieldData("dsConvenio");

			String codeProcessBank = transaction.getCodeProcess();
			String dtExpiration;

			if (codeProcessBank.startsWith("70")) {
				dtExpiration = providerData.substring(10, 10+10);
				dtExpiration = convertYYYY_MM_DD_To_DD_MM_YYYY(dtExpiration);
			} else {
				dtExpiration = providerData.substring(0, 10);
			}

			String textClient = String.format(fmtClient, providerNSU, dsConvenio, nrClienteServico, account, dtExpiration, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, dsConvenio, dtExpiration, value, financialDate);
			rc = setReceipt(transaction, null, title, textComerce, textClient, true);
		} else if (codeProcess.equals("833002")) {
			String title = "PAGO TARJETA CREDITO";
			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</No Tarjeta Credito: ************%s" +
					"</Nombre Titular: %s" +
					"%s" +
					"</Fecha Vencimiento: %s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</Fecha Vencimiento: %s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String tarjetaCredito = tlvCaptureData.getLeftAlign("0393", '0', 16);
			tarjetaCredito = tarjetaCredito.substring(12);
			String personName;
			String account;

			if (providerData.length() > 40) {
				personName = providerData.substring(10, 10+30);
				account = providerData.substring(10+30);
			} else {
				personName = providerData.substring(10);
				account = "";
			}

			if (account.length() > 4) {
				account = "</Cuenta Cargo No: ********" + account.substring(account.length()-4);
			}

			String dtExpiration = providerData.substring(0, 10);
			dtExpiration = convertYYYY_MM_DD_To_DD_MM_YYYY(dtExpiration);
			String textClient = String.format(fmtClient, providerNSU, tarjetaCredito, personName, account, dtExpiration, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, dtExpiration, value, financialDate);
			rc = setReceipt(transaction, "PAGO DE PRODUCTOS BANCO", title, textComerce, textClient, true);
		} else if (codeProcess.equals("833000") || (codeProcess.equals("833001"))) {
			String title;

			if (codeProcess.equals("833000")) {
				title = "CREDITO HIPOTECARIO";
			} else {
				title = "CREDITO CONSUMO";
			}

			String fmtClient =
					"</Codigo Autorizacion: %s" +
					"</Llave Operacion: ********%s" +
					"</Nombre Titular: %s" +
					"%s" +
					"</Fecha Vencimiento: %s" +
					"</Cuota: %s" +
					"</Monto Interes: $%s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String fmtComerce =
					"</Codigo Autorizacion: %s" +
					"</Fecha Vencimiento: %s" +
					"</Monto Interes: $%s" +
					"</<C<B<HMonto Pago<c<b<h" +
					"</<C<B<H$%s<c<b<h" +
					"</Fecha Contable: %s";
			String personName = providerData.substring(10+3+12+12+12);
			String account = tlvCaptureData.getLeftAlign("0100", '0', 12);//a12
			account = account.substring(8);
			String accountSource = providerData.substring(10+3+12+12+8, 10+3+12+12+12);

			if (accountSource.equals("    ")) {
				accountSource = "";
			} else {
				accountSource = "</Cuenta Cargo No: ********" + accountSource;
			}

			String cuota = providerData.substring(0+10, 0+10+3);
			cuota = removeLeftZeros(cuota, 1);
			String valueInteres = providerData.substring(0+10+3, 0+10+3+12);
			valueInteres = getFormatedValue(valueInteres);
			String dtExpiration = providerData.substring(0, 10);
			dtExpiration = convertYYYY_MM_DD_To_DD_MM_YYYY(dtExpiration);
			String textClient = String.format(fmtClient, providerNSU, account, personName, accountSource, dtExpiration, cuota, valueInteres, value, financialDate);
			String textComerce = String.format(fmtComerce, providerNSU, dtExpiration, valueInteres, value, financialDate);
			rc = setReceipt(transaction, "PAGO DE PRODUCTOS BANCO", title, textComerce, textClient, true);
		}

		return rc;
	}

	private long setReceiptAdm(Message transaction, String title, String text, boolean enableReprinter, boolean printFooter, boolean bugWayId) {
		long rc = 0;

		StringBuilder builder = new StringBuilder(1024);
		builder.append("92");
		builder.append("<?");
		builder.append("<+");

		if (bugWayId) {
			builder.append("0001");
		} else {
			builder.append("0100");
		}

		if (enableReprinter) {
			builder.append("<R");
		}

		builder.append("<-");
		builder.append("<C<BBANCO SANTANDER<b<c");

		if (text != null) {
			if (bugWayId) {
				builder.append("<~");
			} else {
				builder.append("<=");
			}
			
			if (title != null) {
				builder.append("</<C<B<H");
				builder.append(title);
				builder.append("<h<b<c");
			}

			builder.append(text);
			
			if (printFooter) {
				builder.append("</");
				builder.append("</<C<HORIGINAL COMERCIO<c<h");
				builder.append("</");
			}
		}

		builder.append("<!§");
		transaction.setData( builder.toString());
		return rc;
	}

	private long buildMessageAdm(Message transaction, String msgType, String codeProcess) {
		long rc = 0;
		String codeResponse = transaction.getCodeResponse();
		String msg = transaction.getData();

		if (codeResponse.equals("0") == false) {
		} else if (codeProcess.equals("831000")) {
			String textComerce = 
					"</----------------------------------------" +
					"</<CCOMPROBANTE INICIO DE DIA<c" +
					"</----------------------------------------";
			rc = setReceiptAdm(transaction, null, textComerce, true, false, false);
		} else if (codeProcess.equals("871000")) {
			String title = "CERRAR CAJA";
			String fmtComerce =
					"</Op.: %s" +
					"</" +
					"</En Efectivo" +
					"</Trx              Cant    Totales" +
					"</Pago Servicios:  %s     $%s" +
					"</Pago Productos:  %s     $%s" +
					"</Depositos     :  %s     $%s" +
					"</Giros         :  %s     $%s" +
					"</Totales       :  %s     $%s" +
					"</" +
					"</Con Tarjeta Debito" +
					"</Trx              Cant    Totales" +
					"</Pago Servicios:  %s     $%s" +
					"</Pago Productos:  %s     $%s" +
					"</Transferencias:  %s     $%s" +
					"</Totales       :  %s     $%s" +
					"</" +
					"</Otros" +
					"</Trx              Cant    Totales" +
					"</Consulta Saldo:  %s";
			String[] fields = msg.split("#");
			String textComerce = String.format(fmtComerce,
					fields[19],
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4],
                    fields[5],
                    fields[6],
                    fields[7],
                    fields[8],
                    fields[9],
                    fields[10],
                    fields[11],
                    fields[12],
                    fields[13],
                    fields[14],
                    fields[15],
                    fields[16],
                    fields[17],
                    fields[18]);
			rc = setReceiptAdm(transaction, title, textComerce, true, true, true);
		} else if (codeProcess.equals("901000")) {
			String[] fields = msg.split("#");
			String message = String.format("91%s</$%s§", fields[0], fields[1]);
			transaction.setData( message);
		} else if (codeProcess.equals("910000")) {
			String title = "RESUMEN DEL DIA";
			String fmtComerce =
					"</" +
					"</En Efectivo" +
					"</Trx              Cant    Totales" +
					"</Pago Servicios:  %s     $%s" +
					"</Pago Productos:  %s     $%s" +
					"</Depositos     :  %s     $%s" +
					"</Giros         :  %s     $%s" +
					"</Totales       :  %s     $%s" +
					"</" +
					"</Con Tarjeta Debito" +
					"</Trx              Cant    Totales" +
					"</Pago Servicios:  %s     $%s" +
					"</Pago Productos:  %s     $%s" +
					"</Transferencias:  %s     $%s" +
					"</Totales       :  %s     $%s" +
					"</" +
					"</Otros" +
					"</Trx              Cant    Totales" +
					"</Consulta Saldo:  %s";
			String[] fields = msg.split("#");
			String textComerce = String.format(fmtComerce,
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4],
                    fields[5],
                    fields[6],
                    fields[7],
                    fields[8],
                    fields[9],
                    fields[10],
                    fields[11],
                    fields[12],
                    fields[13],
                    fields[14],
                    fields[15],
                    fields[16],
                    fields[17],
                    fields[18]);
			rc = setReceiptAdm(transaction, title, textComerce, true, true, true);
		}

		return rc;
	}

	private static boolean checkSendLastOkNSU(String msgType, String codeProcess) {
		boolean ret = false;

		if (msgType.equals("0100")) {
			ret = true;
		} else if (msgType.equals("0200")) {
			if (codeProcess.equals("012000")) {
				ret = true;
			} else if (codeProcess.equals("302400")) {
				ret = true;
			} else if (codeProcess.equals("302100")) {
				ret = true;
			}
		}

		return ret;
	}

	private long authComm(Message transaction, String codeProcessOriginal, Tags tlvCaptureData, StringBuilder bufferAux) throws Exception {
		long rc = 0;
		String msgType = transaction.getMsgType();
		String codeProcess = transaction.getCodeProcess();
		String codeResponse = transaction.getCodeResponse();
		String providerData = transaction.getFieldData("providerData");
		String value = transaction.getTransactionValue();
		value = Utils.getLeftAlign(bufferAux, value, '0', 12);

		if (providerData != null) {
			if (codeProcess.equals("0")) {
			} else if (codeProcess.startsWith("701011")) {
				value = providerData.substring(10+10);
				transaction.setTransactionValue(value);
			} else if (codeProcess.startsWith("901011")) {
				value = providerData.substring(10);
				transaction.setTransactionValue(value);
			} else if (codeProcess.startsWith("401011")) {
				value = providerData.substring(10+3+12);
				transaction.setTransactionValue(value);
			} else if (codeProcess.startsWith("501011")) {
				value = providerData.substring(10+3+12);
				transaction.setTransactionValue(value);
			}
		}
		
		if (msgType.equals("0100") || msgType.equals("0200") || msgType.equals("0202") || msgType.equals("0420")) {
			transaction.setFieldData("cdTipoMensagem", msgType);
			transaction.setFieldData("cdFormaCaptura", "2");
			transaction.setFieldData("cdUsuario", "terminal_pos");
			String captureDate = transaction.getDateLocal();
			String captureHour = transaction.getHourLocal();
			String dtTransacao = captureDate + captureHour;
			transaction.setFieldData("dtTransacao", dtTransacao);
		} else {
			if (codeResponse != null && codeResponse.equals("000")) {
				transaction.setData( null);
			}
		}

		String captureType = transaction.getCaptureType();

		if (captureType != null) {
			String cdFormaPagamento;

			if (captureType.equals("0")) {
				cdFormaPagamento = "1";
			} else if (captureType.equals("1")) {
				cdFormaPagamento = "2";
			} else if (captureType.equals("2")) {
				cdFormaPagamento = "3";
			} else if (captureType.equals("3")) {
				cdFormaPagamento = "3";
			} else {
				cdFormaPagamento = null;
			}

			transaction.setFieldData("cdFormaPagamento", cdFormaPagamento);
		} else if (codeProcessOriginal.equals("832000") || codeProcessOriginal.startsWith("83300")) {
			transaction.setFieldData("cdFormaPagamento", "1");
		}

		if (checkSendLastOkNSU(msgType, codeProcessOriginal)) {
			String authNSU = transaction.getAuthNsu();
			String lastOkNSU = transaction.getLastOkNsu();

			if (authNSU == null && lastOkNSU != null) {
				transaction.setAuthNsu(lastOkNSU);
			}
		}

		if (codeProcessOriginal.equals("000000")) {
		} else if (codeProcessOriginal.equals("012000")) {
			// SAQUE
			transaction.setFieldData("vlTransacao", value);
		} else if (codeProcessOriginal.equals("302300")) {
			// DEPOSITO
			transaction.setFieldData("vlDeposito", value);
		} else if (codeProcessOriginal.equals("302400")) {
			// TRANSFERENCIA
			transaction.setFieldData("vlTransacao", value);
		} else if (codeProcessOriginal.equals("832000")) {
			// PAGAMENTO
//			capture_0100_062 [captureData]          = 0269 004 0269 0995 008 63769770
//			capture_0100_062 [captureData]          = 0288 004 0288 0100 007 3594872
			String nrConvenio = tlvCaptureData.get(0);
			nrConvenio = Utils.removeLeftAlign(nrConvenio, '0');
			transaction.setFieldData("nrConvenio", nrConvenio);
			transaction.setFieldData("vlPagamento", value);
			transaction.setFieldData("vlDocumento", value);
			String nrClienteServico = tlvCaptureData.get(1);
			transaction.setFieldData("nrClienteServico", nrClienteServico);
		} else if (codeProcessOriginal.equals("833002")) {
//			capture_0100_062 [captureData] = 0392 004 0392 0393 016 5432001000760814
			transaction.setFieldData("vlDocumento", value);
		} else if (codeProcessOriginal.startsWith("83300")) {
//			capture_0200_062 [captureData]          = 0390 004 0390 0100 012 500003551116 -> 833000
//			capture_0100_062 [captureData]          = 0391 004 0391 0100 012 650014230704 -> 833001
			String nrConvenio = tlvCaptureData.get(0);
			nrConvenio = Utils.removeLeftAlign(nrConvenio, '0');
			transaction.setFieldData("nrConvenio", nrConvenio);
			transaction.setFieldData("vlPagamento", value);
			String nrContratoBanco = tlvCaptureData.get(1);
			transaction.setFieldData("nrContratoBanco", nrContratoBanco);
		} else if (msgType.equals("0500") || msgType.equals("0800")) {
			transaction.setCodeProcess(null);
			// dsCaixaDiscagem
			transaction.setFieldData("dsCaixaDiscagem", "SDLC");
			// dtLocal
			String date = transaction.getDateLocal();
			String hour = transaction.getHourLocal();
			transaction.setFieldData("dtLocal", date + hour);
			// nrNSUCaptura
			String captureNSU = transaction.getCaptureNsu();
			transaction.setFieldData("nrNSUCaptura", captureNSU);
			// nrNii
			String nii = transaction.getFieldData("nii");
			transaction.setFieldData("nrNii", nii);
			// nrTransAnterior
			String nrTransAnterior = transaction.getLastOkNsu();

			if (nrTransAnterior == null) {
				nrTransAnterior = "0";
			}

			transaction.setFieldData("nrTransAnterior", nrTransAnterior);
			// nrVersaoMsg
			transaction.setFieldData("nrVersaoMsg", "000");
			
			if (codeProcess.equals("NULL")) {
			} else if (codeProcess.equals("831000")) {
				// cdOperadorPos
				String captureData = transaction.getFieldData("captureData");
				String cdOperadorPos = captureData.substring(6);
				// remove espaços à direita
				int pos = cdOperadorPos.indexOf(' ');

				if (pos > 0) {
					cdOperadorPos = cdOperadorPos.substring(0, pos);
				}

				transaction.setFieldData("cdOperadorPos", cdOperadorPos);
				// dsCargaTabelas
				transaction.setFieldData("dsCargaTabelas", captureData);
			} else if (codeProcess.equals("871000")) {
			}
		}
		// parâmetros a serem aplicados APÓS O RETORNO DO BANCO
		// na 0202 o auth_0210_060 [providerData] não existe
		if (providerData != null) {
			if (codeProcess.equals("0")) {
			} else if (codeProcess.startsWith("40")) {
//				auth_0210_060 [providerData] = 2010-09-06 007 000004945500 000040510600
//				dtVencimento                 = 2010-09-06
//				nrNrParcela                  = 7
//				vlDocumento                  = 000040510600
//				vlMoraMulta                  = 000004945500
//				vlPagTotal                   = 000040510600
//				vlPagamento                  = 000040510600
				String dtVencimento = providerData.substring(0, 10);
				transaction.setFieldData("dtVencimento", dtVencimento);
				String nrNrParcela = providerData.substring(10, 10+3);
				nrNrParcela = Utils.removeLeftAlign(nrNrParcela, '0');
				// TODO : verificar se realmente precisa remover os zeros à esquerda
				transaction.setFieldData("nrNrParcela", nrNrParcela);
				String vlMoraMulta = providerData.substring(10+3, 10+3+12);
				transaction.setFieldData("vlMoraMulta", vlMoraMulta);
				String vlDocumento = providerData.substring(10+3+12, 10+3+12+12);
				transaction.setFieldData("vlDocumento", vlDocumento);
				transaction.setFieldData("vlPagTotal", vlDocumento);
				transaction.setFieldData("vlPagamento", vlDocumento);
			} else if (codeProcess.startsWith("50")) {
//				auth_0210_060 [providerData] = 2013-02-01 012 000000000618 000027900500            ALEX ESTEBAN CARRASCO ABARCA -> 501010
//				dtVencimento                 = 2013-02-01
//				nrNrParcela                  = 12
//				vlDocumento                  = 000027900500
//				vlMoraMulta                  = 000000000618
//				vlPagTotal                   = 000027900500
				String dtVencimento = providerData.substring(0, 10);
				transaction.setFieldData("dtVencimento", dtVencimento);
				String nrNrParcela = providerData.substring(10, 10+3);
				nrNrParcela = Utils.removeLeftAlign(nrNrParcela, '0');
				// TODO : verificar se realmente precisa remover os zeros à esquerda
				transaction.setFieldData("nrNrParcela", nrNrParcela);
				String vlMoraMulta = providerData.substring(10+3, 10+3+12);
				transaction.setFieldData("vlMoraMulta", vlMoraMulta);
				String vlDocumento = providerData.substring(10+3+12, 10+3+12+12);
				transaction.setFieldData("vlDocumento", vlDocumento);
				transaction.setFieldData("vlPagTotal", vlDocumento);
			} else if (codeProcess.equals("601011")) {
//				auth_0210_060 [providerData]      = JUAN CARDENAS ALVAREZ          000195387000000000000002012-10-05
//				dtVencimento                      = 2012-10-05
//				vlDocumento                       =  00019538700
//				vlPagMinimo                       = 000000000000
//				vlPagTotal                        =  00019538700
				String vlDocumento = providerData.substring(30, 30+12);
				String vlPagMinimo = providerData.substring(30+12, 30+12+12);
				String dtVencimento = providerData.substring(30+12+12);
				transaction.setFieldData("vlDocumento", vlDocumento);
				transaction.setFieldData("vlPagTotal", vlDocumento);
				transaction.setFieldData("vlPagMinimo", vlPagMinimo);
				transaction.setFieldData("dtVencimento", dtVencimento);
			} else if (codeProcess.startsWith("60")) {
//				auth_0210_060 [providerData]      = 2012-10-05JUAN CARDENAS ALVAREZ -> 601010
//				auth_0210_060 [providerData]      = 2012-10-05JUAN CARDENAS ALVAREZ         000006044905 -> 601110
//				dtVencimento                      = 2012-10-05
//				vlDocumento                       = 000019538700
//				vlPagMinimo                       = 000000020000
//				vlPagTotal                        = 000019538700
//				vlPagamento                       = 000000020000
				String dtVencimento = providerData.substring(0, 10);
				transaction.setFieldData("dtVencimento", dtVencimento);
				transaction.setFieldData("vlPagMinimo", value);
				transaction.setFieldData("vlPagamento", value);
				transaction.setFieldData("vlDocumento", null);
			} else if (codeProcess.startsWith("70")) {
//				auth_0210_060 [providerData] = 0930062012 2012-07-04 000013902800
//				auth_0210_060 [providerData] = 0930072012 2012-07-30 000013854700
//				auth_0210_060 [providerData] = 0930062012 2012-07-04 000013902800 000006044905
				String nrNrParcela = providerData.substring(0, 10);
				// TODO : verificar se realmente precisa remover os zeros à esquerda
				nrNrParcela = Utils.removeLeftAlign(nrNrParcela, '0');
				transaction.setFieldData("nrNrParcela", nrNrParcela);
				String dtVencimento = providerData.substring(10, 10+10);
				transaction.setFieldData("dtVencimento", dtVencimento);
				String vlDocumento = providerData.substring(20);

				if (vlDocumento.length() > 12) {
					vlDocumento = vlDocumento.substring(0, 12);
					transaction.setFieldData("vlDocumento", vlDocumento);
					transaction.setFieldData("vlPagamento", vlDocumento);
				}
			} else if (codeProcess.startsWith("90")) {
//				auth_0210_060 [providerData] = 20110630  000000560000
				String dtVencimento = providerData.substring(0, 10);
				transaction.setFieldData("dtVencimento", dtVencimento);
				String vlDocumento = providerData.substring(10);

				if (vlDocumento.length() > 12) {
					vlDocumento = vlDocumento.substring(0, 12);
					transaction.setFieldData("vlDocumento", vlDocumento);
					transaction.setFieldData("vlPagamento", vlDocumento);
				}
			}
		}

		if (codeProcess.startsWith("83300")) {
			transaction.setCodeProcess("833000");
		}

		transaction.setReplyEspected("1");
		transaction.setMsgType("root");
		return rc;
	}

	private long providerComm(Message transaction, String module, String msgType, String codeProcess, Tags tlvCaptureData, StringBuilder bufferAux) throws Exception {
		long rc = 0;
		String captureData = null;
		String trackII = transaction.getTrackIi();
		String panDigitado = transaction.getPan();
		String value = transaction.getTransactionValue();

		// parâmetros dependentes da transação
		if (codeProcess.equals("302300")) {
			codeProcess = "10";
		} else if (codeProcess.equals("012000")) {
			codeProcess = "20";
		} else if (codeProcess.equals("302400")) {
			codeProcess = "30";
		} else if (codeProcess.equals("833001")) {
			codeProcess = "40";
		} else if (codeProcess.equals("833000")) {
			codeProcess = "50";
		} else if (codeProcess.equals("833002")) {
			codeProcess = "60";
		} else if (codeProcess.equals("302100")) {
			codeProcess = "80";
		} else if (codeProcess.equals("832000")) {
//			capture_0100_062 [captureData] = 0288 004 0288 0100 007 3594872 -> 901011
			String cdPartner = tlvCaptureData.get(0);//a11

			if (cdPartner.equals("0269")) {
				codeProcess = "70";
			} else if (cdPartner.equals("0288")) {
				codeProcess = "90";
			} else {
				codeProcess = "90";
			}
		}

		if (trackII == null && panDigitado == null) {
			codeProcess = codeProcess + "10";
		} else {
			codeProcess = codeProcess + "11";
		}

		if (msgType.equals("0100")) {
			codeProcess = codeProcess + "11";
		} else {
			codeProcess = codeProcess + "10";
		}
		// ajusta o tipo da mensagem
		if (msgType.equals("0100")) {
			msgType = "0200";
		} else if (msgType.equals("0202")) {
			msgType = "0220";
		}

		if (msgType.equals("0420")) {
			transaction.setCodeResponse("001");
		}

		if (codeProcess.startsWith("10101")) {
//			capture_0100_062 [captureData]          = 0201011001248097030202012000000268933
			String rut = tlvCaptureData.getLeftAlign("0201", '0', 11);//a11
			String account = tlvCaptureData.getLeftAlign("0202", '0', 12);//a12
			captureData = rut + account;
		} else if (codeProcess.equals("301110")) {
			captureData = tlvCaptureData.getLeftAlign("0051", '0', 12);//a12
		} else if (codeProcess.startsWith("40")) {
//			capture_0100_062 [captureData]          = 0391 004 0391 0100 012 650014230704
//			auth_0200_054 [captureData]              = 650014230704
			captureData = tlvCaptureData.get(1);//a20
		} else if (codeProcess.startsWith("50")) {
//			capture_0200_062 [captureData]          = 0390 004 0390 0100 012 500003551116
//			auth_0200_054 [captureData]              = 500003551116
			captureData = tlvCaptureData.get(1);//a20
		} else if (codeProcess.startsWith("60")) {
//			capture_0100_062 [captureData] = 0392 004 0392 0393 016 5432001000760814
//			auth_0200_054 [captureData] = 5432001000760814
			captureData = tlvCaptureData.get(1);//a19
		} else if (codeProcess.startsWith("70") || codeProcess.startsWith("90")) {
//			capture_0200_062 [captureData]          = 0269 004 0269 0995 008 63769770
//			cdConvenio                          = 94
//			auth_0200_054 [captureData]              = 94   63769770
			String cdConvenio = transaction.getFieldData("cdConvenio");
			cdConvenio = Utils.getRightAlign(bufferAux, cdConvenio, ' ', 5);
			String nrClienteServico = tlvCaptureData.getRightAlign(1, ' ', 24);
			captureData = cdConvenio + nrClienteServico;
		}


		if (value != null && value.equals("0")) {
			value = null;
			transaction.setTransactionValue(value);
		}

		if (codeProcess.charAt(3) == '1') {
			panDigitado = "0000000000000000000";
		} else {
			panDigitado = null;
		}

		transaction.setMsgType(msgType);
		transaction.setCodeProcess(codeProcess);
		transaction.setPan(panDigitado);
		transaction.setData( captureData);
		rc = Atalla.getInstance().process(this.manager, transaction, true, true, true, null, null);
		// como o Santander não oferece transações que permita digitar o cartão, força zeros,
		// conforme especificação do Santander.
		transaction.setFieldData("getLabel", "GET");
//		transaction.setData("auth_0200_061", "0001");
		transaction.setProviderName(module);
		transaction.setReplyEspected("1");
		// a 0202 do POS não sobe o bit 49
		String codeCountry = transaction.getCountryCode();

		if (codeCountry == null) {
			transaction.setCountryCode("152");
		}

		return rc;
	}

	private void setTrName(Message transaction, String msgType, String codeProcess) throws Exception {
		String trName = "TR";

		if (msgType.equals("0100")) {
			trName = trName + "_CONSULTA";
		} else if (msgType.equals("0202")) {
			trName = trName + "_CONF";
		} else if (msgType.equals("0420")) {
			trName = trName + "_DESFAZ";
		}

		if (codeProcess.equals("302300")) {
			trName = trName + "_DEPOSITO";
		} else if (codeProcess.equals("012000")) {
			trName = trName + "_SAQUE";
		} else if (codeProcess.equals("302400")) {
			if (msgType.equals("0202") || msgType.equals("0420")) {
				trName = trName + "_TRANSF";
			} else {
				trName = trName + "_TRANSFERENCIA";
			}
		} else if (codeProcess.equals("302100")) {
			trName = trName + "_CONS_SALDO_CC";
		} else if (codeProcess.equals("832000")) {
			if (msgType.equals("0100")) {
				trName = trName + "_PGTO";
			} else {
				trName = trName + "_PGTO_CONTA";
			}
		} else if (codeProcess.startsWith("83300")) {
			if (msgType.equals("0100")) {
				trName = trName + "_PGTO";
			} else {
				trName = trName + "_PGTO_CONTA";
			}
		} else if (codeProcess.equals("831000")) {
			trName = "ABERTURA_TERMINAL";
		} else if (codeProcess.equals("871000")) {
			trName = "FECHA_CAIXA_OP";
		} else if (codeProcess.equals("901000")) {
			trName = "CONSULTA_SALDO_CAIXA";
		} else if (codeProcess.equals("910000")) {
			trName = "RESUMO_CAIXA";
		}

		transaction.setFieldData("nmTransacao", trName);
	}

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}

	public long execute(Object data) {
		long rc = 0;
		Message transaction = (Message) data;
		String msgType = transaction.getMsgType();
		String codeProcessOriginal = transaction.getCodeProcess();
		String date = transaction.getDateLocal();
		String hour = transaction.getHourLocal();
		String captureNSU = transaction.getCaptureNsu();
		String dateTime = transaction.getDateTimeGmt();
		String route = transaction.getFieldData("route");
		String[] modules = route.split(",");
		String value = transaction.getTransactionValue();
		String captureData = transaction.getFieldData("captureData");
		Tags tlvCaptureData;
		String codeResponseBank = null;
		String codeResponseAuth = null;
		StringBuilder bufferAux = new StringBuilder(1024);

		if ((msgType.startsWith("01") || msgType.startsWith("02") || msgType.startsWith("04")) && captureData != null) {
			tlvCaptureData = Tags.parseTLV(captureData, 10, 4, 3);
		} else {
			tlvCaptureData = null;
		}

		try {
			setTrName(transaction, msgType, codeProcessOriginal);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (value != null) {
			value = value + "00";
			transaction.setTransactionValue(value);
		}

		for (int i = 0; i < modules.length && rc >= 0; i++) {
			String module = modules[i];

			try {
				if (module.equals("SANTANDER_CHILE")) {
					rc = providerComm(transaction, module, msgType, codeProcessOriginal, tlvCaptureData, bufferAux);
					
					if (rc >= 0) {
						transaction.setModuleOut(module);
						rc = this.manager.commOut(transaction, null);
						
						if (rc >= 0) {
							codeResponseBank = transaction.getCodeResponse();
						}
					}
				} else if (module.equals("CORBAN")) {
					rc = authComm(transaction, codeProcessOriginal, tlvCaptureData, bufferAux);
					
					if (rc >= 0) {
						transaction.setModuleOut(module);
						rc = this.manager.commOut(transaction, null);
						
						if (rc >= 0) {
							codeResponseAuth = transaction.getCodeResponse();
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				System.out.printf("Error in module %s\n", module);
				break;
			}
		}

		transaction.setSendResponse(true);

		if (msgType.equals("0100")) {
			buildMessage(transaction, msgType, codeProcessOriginal, codeResponseBank, tlvCaptureData);
		} else if (msgType.equals("0200")) {
			buildReceipt(transaction, msgType, codeProcessOriginal, codeResponseBank, tlvCaptureData);
		} else if (msgType.equals("0420")) {
		} else if (msgType.equals("0800")) {
			buildMessageAdm(transaction, msgType, codeProcessOriginal);
		} else if (msgType.equals("0500")) {
			buildMessageAdm(transaction, msgType, codeProcessOriginal);
		} else {
			transaction.setSendResponse(false);
		}

		String providerNSU = transaction.getProviderNsu();

		if (providerNSU != null && providerNSU.length() > 6) {
			int diff = providerNSU.length() - 6;
			providerNSU = providerNSU.substring(diff);
			transaction.setProviderNsu(providerNSU);
		}

		String retValue = transaction.getTransactionValue();

		if (retValue != null && retValue.length() > 2) {
			retValue = retValue.substring(0, retValue.length()-2);
			transaction.setTransactionValue(retValue);
		} else if (retValue == null && value != null) {
			transaction.setTransactionValue(value);
		}

		transaction.setCodeProcess(codeProcessOriginal);
		transaction.setDateLocal(date);
		transaction.setHourLocal(hour);
		transaction.setDateTimeGmt(dateTime);
		transaction.setCaptureNsu(captureNSU);
		
		if (codeResponseBank != null && codeResponseBank.equals("000") == false) {
			transaction.setCodeResponse("83");
		}
		
		if (codeResponseAuth != null && codeResponseAuth.equals("0") == false) {
			transaction.setCodeResponse("82");
		}
		// códigos de resposta para o POS :
		// 12
		// 81
		// cdRetorno
		// codigoResposta
		// cdRetornoOperacao
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
		info.name = "TrCorban";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}

/*
 * Códigos de processamento com o SANTANDER :
 * ABCDEF
 *
 * AB - TIPO TRANSACCION
 *
 * B sempre é 0
 *
 * AB :
 *
 * 10 DEPOSITO
 * 20 GIRO
 * 30 TRANSFERENCIA
 * 40 CREDITO CONSUMO
 * 50 CREDITO HIPOTECARIO
 * 60 PAGAMENTO DE TARJETA DE CREDITO (PAGAMENTO DA FATURA DO CARTÃO DE CREDITO)
 * 70 SERVICIOS CARTERA CONOCIDA
 * 80 CONSULTA DE SALDO
 * 90 SERVICIOS CARTERA DESCONOCIDA
 *
 * CD - MODALIDAD CAPTURA Y TIPO PAGO
 *
 * C :
 * 1 Manual (digitado)
 * 2 Código de Barras
 *
 * D :
 * 0 pagamento em efetivo (dinheiro)
 * 1 pagamento com tarjeta debito (cartão de débito)
 *
 * EF - TIPO OPERACIÓN
 *
 * 10 : PAGO (operaciones con comision cierran una Transaccion)
 * 11 : CONSULTA (operaciones Intermedias necesarias para un TX)
 *
 *
 */

}
