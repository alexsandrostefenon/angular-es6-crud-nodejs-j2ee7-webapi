package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0700_007005 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
	private long requestStep2(Message transaction) {
		long rc = -2;
		transaction.setModuleOut("sisprepAjusteHorarioTerminal");
		rc = this.manager.commOut(transaction, null);
		
		if (rc >= 0) {
			rc = requestStep2(transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	private long requestStep1(Message transaction) {
		long rc = -1;
		transaction.setModuleOut("sp_cap_config_remota");
		rc = this.manager.commOut(transaction, null);
		
		if (rc >= 0) {
			rc = requestStep2(transaction);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	public long execute(Object data) {
		long rc = 0;
		Message transaction = (Message) data;
		rc = requestStep1(transaction);
		transaction.setSendResponse(true);
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
		info.name = "TrAdministrativa_0700_007005";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
