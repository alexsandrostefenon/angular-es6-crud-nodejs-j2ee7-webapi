package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Transaction_0700_007007 implements Module {
	Connector manager;

	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}
	
	private long requestStep1(Message transaction) throws Exception {
		long rc = -1;
		transaction.setModuleOut("sisprepConfiguracaoTerminal");
		rc = this.manager.commOut(transaction, null);
		
		if (rc >= 0) {
//			sisprepConfiguracaoTerminal_in_001=EVA EDITH ROA HERRERA|
//			sisprepConfiguracaoTerminal_in_002=VILLA ESPANA CALLE 1 NUMERO 624|
//			sisprepConfiguracaoTerminal_in_003=006356000000000|
//			sisprepConfiguracaoTerminal_in_004=144741|
//			sisprepConfiguracaoTerminal_in_005=14474
//			capture_0710_bit_062=@CABTK1=EVA EDITH ROA HERRERA@CABTK2=VILLA ESPANA CALLE 1 NUMERO 624@ESTOPER=006356000000000
			String sisprepConfiguracaoTerminal_in_001 = transaction.getFieldData("sisprepConfiguracaoTerminal_in_001");
			String sisprepConfiguracaoTerminal_in_002 = transaction.getFieldData("sisprepConfiguracaoTerminal_in_002");
			String sisprepConfiguracaoTerminal_in_003 = transaction.getFieldData("sisprepConfiguracaoTerminal_in_003");
			String capture_0710_bit_062 = 	"@CABTK1=" + sisprepConfiguracaoTerminal_in_001 +
											"@CABTK2=" + sisprepConfiguracaoTerminal_in_002 +
											"@ESTOPER=" + sisprepConfiguracaoTerminal_in_003;
			transaction.setFieldData("capture_0710_bit_062", capture_0710_bit_062);
		} else {
			// TODO : disparar desfazimento
		}
		
		return rc;
	}
	
	public long execute(Object data) {
		long rc = 0;
		Message transaction = (Message) data;
		
		try {
			rc = requestStep1(transaction);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		info.name = "TrAdministrativa_0700_007007";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
