package org.domain.commom;

import java.io.Serializable;


public class ModuleInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// dados preenchidos pelo PluginManager
	public final String className;
	// campos preenchidos na inicialização do plugin
	public String name;
	public String family = null;
	public int version = 1;
	public String[] dependencies;
	public String[] dependenciesFamily;
	// campo preenchido pelo plugin ao longo da sua vida
	public String statistic = "";
	private boolean isRunning = false;
	
	public ModuleInfo(String className, Module plugin) {
		this.className = className;
		this.statistic = "";
		// TODO : VERIFICAR
		int pos = className.lastIndexOf('.');
		this.name = className.substring(pos+1);
		plugin.getInfo(this);
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}
