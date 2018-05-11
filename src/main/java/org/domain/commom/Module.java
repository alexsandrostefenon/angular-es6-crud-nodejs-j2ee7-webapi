package org.domain.commom;

public interface Module {
	void getInfo(ModuleInfo info);
	void enable(ModuleManager manager);
	void start();
	void stop();
	long execute(Object data);
	void config(String section, String value);
}
