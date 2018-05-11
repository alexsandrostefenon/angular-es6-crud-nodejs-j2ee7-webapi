package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0900_900000 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}

	private long requestStep4(Message transaction) throws Exception {
		long rc = -4;
		transaction.setModuleOut("sp_recel_online_solicitacao");
		rc = this.manager.commOut(transaction, null);
		
		if (rc >= 0) {
			String authResponse = transaction.getCodeResponse();
			
			if (authResponse.equals("0")) {
//				capture_0910_bit_060 = 0@@
				transaction.setFieldData("capture_0910_060", "0@@");
//				sp_recel_online_solicitacao_in_006 = <?<+0101<R<-<C<B OI<b<c<~</AUTORIZACAO / NSU OI: 000112252 <C<BRecarga de Celular<b<c</Valor: R$ 10.00         Numero:
//				sp_recel_online_solicitacao_in_007 = 8487032515 </</<BRECARREGUE SEU BOLSO<b</Recargas a partir de R$ 12,00 = cliente e</estabelecimento comercial concorrem a</muit
//				sp_recel_online_solicitacao_in_008 = os premios em dinheiro. Confira</regulamento completo no site</www.getnet.com.br<B
//				sp_recel_online_solicitacao_in_009 = null
//				sp_recel_online_solicitacao_in_010 =  <b</<_<=</AUTORIZACAO / NSU OI: 000112252 <CRecarga de Celular<c</Valor: R$ 10.00         Numero:  8487032515 </</<BRE
//				sp_recel_online_solicitacao_in_011 = CARREGUE SEU BOLSO<b</Recargas a partir de R$ 12,00 = cliente e</estabelecimento comercial concorrem a</muitos premios em dinhe
//				sp_recel_online_solicitacao_in_012 = iro. Confira</regulamento completo no site</www.getnet.com.br<B
//				sp_recel_online_solicitacao_in_013 = null
//				sp_recel_online_solicitacao_in_014 =  <b<!
//				capture_0910_bit_062 = <?<+0101<R<-<C<B OI<b<c<~</AUTORIZACAO / NSU OI: 000112252 <C<BRecarga de Celular<b<c</Valor: R$ 10.00         Numero:8487032515 </</<BRECARREGUE SEU BOLSO<b</Recargas a partir de R$ 12,00 = cliente e</estabelecimento comercial concorrem a</muitos premios em dinheiro. Confira</regulamento completo no site</www.getnet.com.br<B <b</<_<=</AUTORIZACAO / NSU OI: 000112252 <CRecarga de Celular<c</Valor: R$ 10.00         Numero:  8487032515 </</<BRECARREGUE SEU BOLSO<b</Recargas a partir de R$ 12,00 = cliente e</estabelecimento comercial concorrem a</muitos premios em dinheiro. Confira</regulamento completo no site</www.getnet.com.br<B <b<!
				String msgText = transaction.getData();
//				msgText = msgText + transaction.concat("sp_recel_online_solicitacao_in_", 7, 14);
				transaction.setData( msgText);
				transaction.setReplyEspected("1");
			}
			
			rc = TransactionsCommom.execConectividadeAutocargaParametros(manager, transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	private long requestStep3(Message transaction) throws Exception {
		long rc = -3;
		String providerId = transaction.getProviderId();
		
		if (providerId != null && providerId.length() > 0) {
			boolean foundAuth;
			String codeProcess = transaction.getCodeProcess();
			
			if (providerId.equals("7")) {
				foundAuth = true;
				providerId = "OI";
				codeProcess = "002000";
			} else {
				// TODO : verificar o código das outras operadoras
				foundAuth = false;
			}
			
			if (foundAuth) {
	//			sp_recel_solic_transacao_v2_in_002 = 20111103 12:55:42
				String dateTime = transaction.getFieldData("sp_recel_solic_transacao_v2_in_002");
	//			auth_0200_bit_007 = 1103125542
				dateTime = dateTime.replace(" ", "").replace(":", "");
				transaction.setDateTimeGmt(dateTime);
	//			auth_0200_bit_061 = 04
				transaction.setFieldData("auth_0200_061", "04");
	//			sp_recel_solic_transacao_v2_in_004 = 848703251500
	//			sp_recel_solic_transacao_v2_in_008 = 59185000
				String phone = transaction.getFieldData("phone");
				String sp_recel_solic_transacao_v2_in_008 = transaction.getFieldData("sp_recel_solic_transacao_v2_in_008");
	//			auth_0200_bit_062 = 84870325150059185000
				String auth_0200_bit_062 = phone + sp_recel_solic_transacao_v2_in_008;
				transaction.setFieldData("auth_0200_062", auth_0200_bit_062);
				transaction.setProviderName(providerId);
				transaction.setModuleOut("Auth");
				transaction.setReplyEspected("1");
				transaction.setMsgType("0200");
				transaction.setCodeProcess(codeProcess);
				rc = this.manager.commOut(transaction, null);
				
				if (rc >= 0) {
					rc = requestStep4(transaction);
				} else {
					// TODO : disparar desfazimento
				}
			}
		}
		
		return rc;
	}
	
	private long requestStep2(Message transaction) throws Exception {
		long rc = -2;
//		capture_0900_bit_062 = @0019@0035@0166@00971000@00418487032515@00418487032515@
//		sp_recel_solic_transacao_v2_out_014 = '@0035@0166@00971000@00418487032515@00418487032515'
		String dinamicMenu = transaction.getFieldData("DINAMIC_MENU");
		
		if (dinamicMenu.startsWith("@0019@") &&	dinamicMenu.endsWith("@")) {
			int len = dinamicMenu.length();
			
			if (len > 20) {
				dinamicMenu = dinamicMenu.substring(5, len-1);
				transaction.setFieldData("DINAMIC_MENU", dinamicMenu);
				transaction.setModuleOut("sp_recel_solic_transacao_v2");
				rc = this.manager.commOut(transaction, null);
				
				if (rc >= 0) {
					rc = requestStep3(transaction);
				} else {
					// TODO : disparar desfazimento
				}
			}
		}
		
		return rc;
	}
	
	private long requestStep1(Message transaction) throws Exception {
		long rc = -1;
		transaction.setModuleOut("ANTI_FRAUDE");
		// TODO : verificar passagem pelo ANTI-FRAUDE
//		rc = this.manager.commOut(transaction);
		rc = 0;
		
		if (rc >= 0) {
			rc = requestStep2(transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	public long execute(Object data) {
/*
		utils.DinamicMenu bins = new utils.DinamicMenu();
		String bit62 = transaction.getData("062");
		bins.parse(bit62);
		String value = bins.getData("0097");
 */
		long rc = 0;
		Message transaction = (Message) data;
		String codeProcess = transaction.getCodeProcess();
		String date = transaction.getDateLocal();
		String hour = transaction.getHourLocal();
		
		try {
			rc = requestStep1(transaction);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		transaction.setSendResponse(true);
		transaction.setCodeProcess(codeProcess);
		transaction.setDateLocal(date);
		transaction.setHourLocal(hour);
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
		info.name = "Transaction_0900_900000";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
