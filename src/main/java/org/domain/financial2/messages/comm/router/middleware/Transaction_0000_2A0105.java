package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0000_2A0105 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
	private long requestStep1(Message transaction) {
		// CRYPTO_processTerminalIDRequest(Message transaction)
		long rc = -1;
		transaction.setModuleOut("p_busca_codigo_terminal");
		rc = this.manager.commOut(transaction, null);
		return rc;
	}
	
	public long execute(Object data) {
		long rc = 0;
		Message transaction = (Message) data;
		rc = requestStep1(transaction);
		transaction.setSendResponse(true);
		// CRYPTO_MSGTYPE_TERMINAL_ID_REPLY; //message type - 0x03
		transaction.setCodeProcess("2A0103");
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
		info.name = "TrAdministrativa_0000_2A0105";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
