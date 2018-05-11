package org.domain.financial2.messages.comm.router;

import org.domain.commom.Logger;
import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.financial2.entity.CardConf;
import org.domain.financial2.entity.ISO8583TefProvider;
import org.domain.financial2.messages.comm.router.Atalla;
import org.domain.financial2.messages.comm.router.Tags;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Provider implements Module {
	Connector manager;
	private Integer providerNSU = 0;
	
	String tables_900001[] = {
		//MENUS DISPONIVEIS
		"04401015CARDAPIO/FRETES0100000620000C001401501306501115SHELL/ECOFROTAS0100000620000C010010110210310410510610710810906801215SERVICOS/FRETES0100000620000S0120121122123124125126127128129130",
		"04101318RECARGA DE CELULAR0100000620000S000206501415SHELL/ECOFROTAS1400000620000C010010110210310410510610710810906801515SERVICOS/FRETES1400000620000S0120121122123124125126127128129130",
		//ITENS DE MENUS DISPONIVEIS
		"03810006DIESEL1400000620000C050050150450504810116DIESEL ADITIVADO1400000620000C050050150450504110209BIODIESEL1400000620000C050050150450504910317DIESEL S-50 COMUM1400000620000C050050150450505310421DIESEL S-50 ADITIVADO1400000620000C050050150450504010508GASOLINA1400000620000C050050150450505010618GASOLINA ADITIVADA1400000620000C050050150450503810706ETANOL1400000620000C050050150450504810816ETANOL ADITIVADO1400000620000C0500501504505",
		"03510903GNV1400000620000C050050150450503212006COMPRA1400000620003S052052104012117TRANSF DEPENDENTE1400000620003S052103812215TRANSF BANCARIA1400000620003S052103112305SAQUE1400000620003S052052104012417CONSULTA DE SALDO1400000620003S052104312517QUITAR TRANSPORTE1400000620003S051052103512612ATIVAR CONTA1400000620003S052104112718DESBLOQUEAR CARTAO1400000620003S052103812812TROCAR SENHA1400000620003S013413504912917CANCELAR QUITACAO1400000620004S051051551851904813013CANCELA SAQUE1400000620004S051451551651752003913413PEDE 2 SENHAS1400000620003S052151204213513PEDE 3 SENHAS1400000620003S0521512512",                 //trocar senha, com 2 e 3 senhas, por menu
		//PERGUNTAS DISPONIVEIS
		"02550005BOMBA0201030620000C002650106LITROS1500100620000C002850208ODOMETRO0201060620000C002950309MATRICULA0201100620000C002550405VALOR0301090620000C002550505SENHA0504040620000S002550605BANCO0201030620000S002750707AGENCIA0201040620000S002550805CONTA0201200620000S002350903CPF0211110620000S003851018NUMERO OPER TRANSP0202100620000S004051120DATA NASC (DDMMAAAA)0208080620000S003751217DIGITE NOVA SENHA0504040620000S003951319CONFIRMA NOVA SENHA0504040620000S0",
		"03651416NSU DA TRANSACAO0201080620000S003351513NUMERO DO DOC0201060620000S003751617DATA DA TRANSACAO1800000620000S003751717HORA DA TRANSACAO1700000620000S003851818NUMERO AUTORIZACAO0201090620000S003751917VALOR LIBERADO QT0301090620000S002552005VALOR3301090620000S002552105SENHA0504060620000S0",
		//PERGUNTAS ADICIONAIS
		"03260012PESO INICIAL1501100620000S003060110PESO FINAL1501090620000S003660216VALOR DE DIARIAS0300090620000S003460314PASSE O CARTAO0900200620000S003960419PASSE/DIGITE CARTAO1000200620000S0",
		"",
		//MENSAGENS INFORMATIVAS 
		"03400114NAO HABILITADO0400000620000S003700217SOLICITE ATIVACAO0400000620000S003600316PERGUNTA TAG_9980201010620000S003400414PASSE O CARTAO0900200620000S004000520CONFIRMA?1-SIM 2-NAO0201010620000S0",
		""
	};

//	String table_900010 = "#9034@600078000000000000605693000000010110001101000101010201030104010501060107011101120113";
//	String table_900010_version = "00001";
			
	String tables_950011[] = {
		"18#2FF9F0B9222721C8FBA196CA7D63417F",
		""
	};

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}

	private long setResponseData(Message transaction) {
/*
provider_0210_002000_062 [msgText]          = O0000000000@MARCELO CARDOSO DA SILVA@@
 */
		// bit 3
		String codeProcess = transaction.getCodeProcess();
		
		if (codeProcess == null) {
			codeProcess = "";
		}
		
		long rc = 0;
		
		if (codeProcess.equals("")) {
			
		} else if (codeProcess.equals("950032") == true) {
		} else if (codeProcess.equals("002000") == true) { // 001002
			TransactionsCommom.setBits_057_062_063(transaction, "O0000000000@MARCELO CARDOSO DA SILVA@@");
		} else if (codeProcess.equals("001001") == true) {
			TransactionsCommom.setBits_057_062_063(transaction, "#810@46953#805@   BOMBA  LIBERADA   BOMBA 05 -   469,53 L       DIESEL        #900@000012301");
		} else if (codeProcess.equals("001002") == true) {
			TransactionsCommom.setBits_057_062_063(transaction, "#807@1</</Cartao: 6035 **** **** 1067</Ltrs:   488,43    Preco/Litro:     2,179</                  Desconto/Litro:  0,000</Subtotal:     1.064,29</Desconto:         0,00</Valor Total:  1.064,29</</Bomba: 5     DIESEL</</Cli: 60729 - B J TRANSP DE ITATIBA</</</Ass.: _________________________</</RG:   _________________________</CNPJ Cliente: 60.077.336/0001-84</</#806@1</</Cartao: 6035 **** **** 1067</Ltrs:   488,43    Preco/Litro:     2,179</                  Desconto/Litro:  0,000</Subtotal:     1.064,29</Desconto:         0,00</Valor Total:  1.064,29</</HZD 8071</Km: 564377</Bomba: 5     DIESEL</Motorista: APARECIDO DONIZETI DA SI</</Saldo Disponivel: 1.935,71</</Cli: 60729 - B J TRANSP DE ITATIBA#808@EXPERS#901@0245485165");
		} else if (codeProcess.equals("002002") == true) {
			TransactionsCommom.setBits_057_062_063(transaction, "#807@1</</Cartao: 6035 **** **** 6579</Ltrs:   260,00    Preco/Litro:     3,000</                  Desconto/Litro:  1,026</Subtotal:       780,00</Desconto:       266,76</Valor Total:    513,24</</Bomba: 1     DIESEL ADITIVADO</</Cli: 59181 - VITORIA AGREGADOS</</</Ass.: _________________________</</RG:   _________________________</CNPJ Cliente: 04.019.283/0001-74</</#806@1</</Cartao: 6035 **** **** 6579</Ltrs:   260,00    Preco/Litro:     3,000</                  Desconto/Litro:  1,026</Subtotal:       780,00</Desconto:       266,76</Valor Total:    513,24</</AMV 5359</Km: 884403</Bomba: 1     DIESEL ADITIVADO</Motorista: ROBERTO CARLOS SCHINDA</</Saldo Disponivel: 3.614,28</</Cli: 59181 - VITORIA AGREGADOS#808@EXPERS#901@0245493163");
		} else if (codeProcess.equals("200030") == true) {
			TransactionsCommom.setBits_057_062_063(transaction, "#808@CANCELAMENTO EXPERS#807@1</</CANCELAMENTO DE TRANSACAO</</Cartao: 6035 **** **** 5190</EPS0042</Valor Total:      9,99</</Bomba: 13   DIESEL#806@1</</CANCELAMENTO DE TRANSACAO</</Cartao: 6035 **** **** 5190</EPS0042</Valor Total:      9,99</</Bomba: 13   DIESEL");
		} else if (codeProcess.equals("900001") == true) {
			TransactionsCommom.setTables(transaction, tables_900001);
		} else if (codeProcess.equals("900002") == true) {
			rc = processExpers_900002(transaction);
		} else if (codeProcess.equals("900010") == true) {
//			TransactionsCommom.setBits_057_062_063(transaction, table_900010);
		} else if (codeProcess.equals("950011") == true) {
			TransactionsCommom.setTables(transaction, tables_950011);
		} else if (codeProcess.equals("402000") == true) {
			rc = processExpers_402000(transaction);
		}
		
		return rc;
	}
	
	private long processExpers_900002(Message transaction) {
		long rc = 0;
        /*900002 - tabela perguntas ciente*/
		String data = transaction.getData();
		String tag870 = Tags.getValue("#870@", "#", data);
		int offset;
		
		if (tag870.startsWith("605663")) {           //cartoes de teste, identificando cliente pelo fim do BIN
			offset = 16;
		} else if (tag870.startsWith("546451")) {
			offset = 16;
		} else if (tag870.startsWith("548827")) {
			offset = 16;
		} else {
			offset = 26;
		}

		String clientFromTrackII = data.substring(offset, offset + 5);
		String clientNumber = "00002004";
		String dateTable = "080113";
		String quenstions = "010";

		switch (Integer.parseInt(clientFromTrackII)) {
           case 48444://cartao fretes emprestado para a certifica��o
        	   clientNumber = "00048444";
           break;
           case 20100://cartao mastercard usado para a montagem dos scripts
        	   clientNumber = "71220100";
           break;
           case 19869://cartao master FAC
        	   clientNumber = "00009869";
           break;
           case 392://cartao fretes
           case 434://case 491://cartao fretes
        	   clientNumber = "00002004";
        	   dateTable = "180313";
        	   quenstions = "012";
           break;
           case 68738://cartao master RAT
        	   clientNumber = "00008738";
        	   dateTable = "180313";
        	   quenstions = "012";
           break;
           case 48656://cartao eco frotas
        	   clientNumber = "00048656";
        	   quenstions = "011";
           break;
           case 20472: // cartao sem perguntas pre definidas. perguntas adicionais s�o enviadas
        	   clientNumber = "00020472";
           break;
		}
		
		StringBuilder dataOut = new StringBuilder(2*999);
		dataOut.append("#860@");
		dataOut.append(String.format("%03d", clientNumber.length() + dateTable.length() + quenstions.length()));// client number + date table + questions
		dataOut.append(clientNumber);
		dataOut.append(dateTable);
		dataOut.append(quenstions);
		
		TransactionsCommom.setBits_057_062_063(transaction, dataOut.toString());
		return rc;
	}
	
	private long processExpers_402000(Message transaction) {
	/*
	 * #012@	#120@	#520@100	#871@1	#902@180313
	 * #841@
	 * <?<+0101<R<-</<B<C<HEXPERS FRETE<h<b<c<~</</<B<CCOMPRA DEBITO<b<c</</Cartao:6056630000000434</Valor: R$ 1,00</Saldo Disponivel: R$ 150,00</</<C1a. Via - Usuario<c</</<_</</<=</</<B<C<HCOMPRA DEBITO<h<c<b</</Cartao:6056630000000434</Valor: R$ 1,00</</<CRECONHECO A DIVIDA AQUI INDICADA.</TRANSACAO AUTORIZADA MEDIANTE A</SENHA PESSOAL.<c</</<C2a. Via - Estabelecimento<c<!
	 * #901@
	 * 0237163210		 
	 */
		long rc = 0;
		String dataIn = transaction.getData();
		String tag012 = Tags.getValue("#012@", "#", dataIn);
		String tag120 = Tags.getValue("#120@", "#", dataIn);
		String value  = Tags.getValue("#520@", "#", dataIn);
		String tag871 = Tags.getValue("#871@", "#", dataIn);
		String date   = Tags.getValue("#902@", "#", dataIn);
		StringBuilder dataOut = new StringBuilder(2*999);
		
		if (tag012 != null && tag120 != null && tag871 != null) {
			dataOut.append("#841@");
			String receipt = "<?<+0101<R<-</<B<C<HEXPERS FRETE<h<b<c<~</</<B<CCOMPRA DEBITO<b<c</</Cartao:6056630000000434</Valor: R$ 1,00</Saldo Disponivel: R$ 150,00</</<C1a. Via - Usuario<c</</<_</</<=</</<B<C<HCOMPRA DEBITO<h<c<b</</Cartao:6056630000000434</Valor: R$ 1,00</</<CRECONHECO A DIVIDA AQUI INDICADA.</TRANSACAO AUTORIZADA MEDIANTE A</SENHA PESSOAL.<c</</<C2a. Via - Estabelecimento<c<!";
			dataOut.append(receipt);
			dataOut.append("#901@");
			dataOut.append("0237163210");
		}
		
		TransactionsCommom.setBits_057_062_063(transaction, dataOut.toString());
		return rc;
	}

	private long setResponse(Message transaction) throws Exception {
		transaction.setCodeResponse("00");
		// bit 57, 62 63
		long rc = setResponseData(transaction);
		boolean status = true;
		// bit 0
		String msgType = transaction.getMsgType();
		// bit 3
		String codeResponse = transaction.getCodeResponse();
		// bit 27
		String replyEspected = "1";
		
		if (codeResponse.equals("00") == false) {
			replyEspected = "0";
		}

		if (msgType.equals("0100")) {
			replyEspected = null;
		} else if (msgType.equals("0200")) {
		} else if (msgType.equals("0202")) {
			status = false;
			replyEspected = null;
		} else if (msgType.equals("0420")) {
		} else if (msgType.equals("0800")) {
			replyEspected = null;
		} else {
			status = false;
			replyEspected = null;
		}
		// bit 3
		String codeProcess = transaction.getCodeProcess();
		
		if (codeProcess == null) {
			codeProcess = "";
		}
		// bit 18
		transaction.setMerchantType(null);
		// bit 26
		if (codeProcess.equals("900001") == true || codeProcess.equals("900002") == true) {
			transaction.setFieldData("provider_0810_026", "1");
		}
		// bit 27
		if (codeProcess.equals("001001") == true || codeProcess.equals("002000") == true || codeProcess.equals("200030") == true) {
			replyEspected = null;
		}
		
		transaction.setReplyEspected(replyEspected);
		// bit 39
		// bit 48
		if (codeProcess.equals("900001") == true) {
		} else if (codeProcess.equals("900010")) {
		}
		// bit 055
//		transaction.setData("chipEMV", null);
		// bit 67
		transaction.setNumPayments(null);
		// STATUS
		transaction.setSendResponse(status);
		// bit 127
		if (codeResponse.equals("00")) {
			if (msgType.equals("0200") || msgType.startsWith("R00")) {
				this.providerNSU++;
				transaction.setProviderNsu(this.providerNSU.toString());
			}
		}
		
		return rc;
	}
	
	private long check_card(Message transaction) {
		long rc = 0;
		return rc;
	}

	private long check_pass(Message message, ISO8583TefProvider conf) {
		long rc = 0;
		String masterKey = conf.getMasterKey();
		
		if (masterKey == null || masterKey.length() != 16) {
			rc = Atalla.getInstance().translatePIN(this.manager, message, conf.getCryptKeyReference(), TransactionsCommom.atallaAkbKeyRefKnown);
			masterKey = TransactionsCommom.masterKeyKnownDes;
		}
		
		if (rc == 0) {
			CardConf.openPassword(message, masterKey);
			String msg = String.format("FTICKET PARA TESTE@@Verificador projeto ABECS@Senha digitada : %s", message.getPassword());
			TransactionsCommom.setBits_057_062_063(message, msg);
		}
		
		return rc;
	}
	
	private long check_ec(Message transaction) {
		long rc = 0;
		return rc;
	}

	private long check_card_credit(Message transaction) {
//		provider_0200_067 [numPayments]           = 01
		long rc = 0;
		// bit 4
		String value = transaction.getTransactionValue();
		return rc;
	}

	public long execute(Object data) {
		Message message = (Message) data;
		CardConf tefConf = CardConf.getTefConf(this.manager.getEntityManager(), message);

		if (tefConf == null) {
			this.manager.log(Logger.LOG_LEVEL_ERROR, "Provider.execute", "Don't found card, provider and flow configuration", message);
			return -1;
		}
		
		ISO8583TefProvider conf = tefConf.getProvider();
		
		if (conf == null) {
			this.manager.log(Logger.LOG_LEVEL_ERROR, "Provider.execute", "Don't found card configuration", message);
			return -1;
		}
		// bit 052 - primeiro verifica a senha
		long rc = check_pass(message, conf);

		if (rc == 0) {
			try {
				rc = setResponse(message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		info.family = "Messages";
		info.name = "Provider";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
