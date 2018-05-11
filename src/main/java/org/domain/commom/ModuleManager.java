package org.domain.commom;

public interface ModuleManager extends Logger {
	
	long execute(String pluginName, Object data);
	ModuleInfo[] getPluginsInfo();
	void log(int logLevel, String header, String text, Object data);
	
}
