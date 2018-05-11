package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.financial2.messages.comm.router.TransactionsCommom;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0700_007001 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
	public long execute(Object data) {
		long rc = -1;
		
		try {
			Message transaction = (Message) data;
			rc = TransactionsCommom.execConectividadeAutocargaParametros(manager, transaction);
			transaction.setCodeResponse("00");
			transaction.setSendResponse(true);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
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
		info.family = "TrAdministrativa";
		info.name = "Transaction_0700_007001";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
