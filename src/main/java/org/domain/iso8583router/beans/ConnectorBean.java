package org.domain.iso8583router.beans;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Named;

import org.domain.iso8583router.messages.comm.Connector;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@Named
public class ConnectorBean {
	private Connector manager;

	@PostConstruct
	void postConstruct() {
		this.manager = Connector.getInstance();
	}

	@PreDestroy
	void preDestroy() {
		this.manager.stop();
	}

	public Connector getManager() {
		return manager;
	}

}
