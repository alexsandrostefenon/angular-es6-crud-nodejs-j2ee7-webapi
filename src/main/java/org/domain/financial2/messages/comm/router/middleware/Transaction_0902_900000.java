package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0902_900000 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
	private long confirmStep2(Message transaction) {
		long rc = -2;
		transaction.setMsgType("0202");
		transaction.setCodeProcess("002000");
		String providerId = transaction.getProviderId();
		
		if (providerId.equals("7")) {
			providerId = "OI";
		} else {
			// TODO : verificar o código das outras operadoras
		}
		
		transaction.setProviderName(providerId);
		transaction.setModuleOut("Auth");
		transaction.setReplyEspected("0");
		rc = this.manager.commOut(transaction, null);
	
		if (rc >= 0) {
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	private long confirmStep1(Message transaction) {
		long rc = -1;
		transaction.setModuleOut("sp_recel_conf_transacao");
		rc = this.manager.commOut(transaction, null);
		
		if (rc >= 0) {
			rc = confirmStep2(transaction);
		}
		
		transaction.setSendResponse(false);
		return rc;
	}
	
	public long execute(Object data) {
		long rc = 0;
		Message transaction = (Message) data;
		rc = confirmStep1(transaction);
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
		info.name = "Transaction_0902_900000";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
