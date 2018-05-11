package org.domain.financial2.messages.log.importer;

public class ConverterConf {
	private static ConverterConf common;
	// TODO : carregar do banco de dados
	public String persistenceUnitName = "log_importer";
	public String feps = "FEP09,FEP10,FEP03,GN096,GN097";
	// TODO : carregar do banco de dados
	String[] skipLines;
	// TODO : carregar do banco de dados
	String[] rootsXmlNonSiscap;
	// TODO : carregar do banco de dados
	String[] modulesIn;
	// TODO : carregar do banco de dados
	int sleepAfterEndOfFile = 1000;
	// TODO : carregar do banco de dados
	boolean verifyConfigChanges = false;
	// TODO : carregar do banco de dados
	int flowTimeout = 30000;
	private String[] enabledLevels; // ERROR
	// TODO : carregar do banco de dados
	public String dataIn = "./dataIn"; 
	
	private ConverterConf () {
	}
	
	public static synchronized ConverterConf getInstance() {
		if (ConverterConf.common == null) {
			ConverterConf.common = new ConverterConf();
		}
		
		return ConverterConf.common;
	}

	public void reset() {
		// TODO : recarregar configurações do banco de dados
	}
}
