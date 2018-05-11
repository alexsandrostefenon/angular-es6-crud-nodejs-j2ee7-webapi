package org.domain.financial2.beans;

import java.io.LineNumberReader;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.domain.financial2.messages.log.importer.Converter;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@Named
public class LogCrackerBean {
	@Resource
	private EJBContext context;	
	
//@PersistenceContext(unitName="FinancialSmall")
	@PersistenceContext
	EntityManager emLog;

//@PersistenceContext(unitName="FinancialSmall")
	@PersistenceContext
	EntityManager emMessage;

//	@PersistenceContext(unitName="FinancialSmall")
	@PersistenceContext
	EntityManager emRequest;

	boolean lock;
	private UserTransaction userTransaction;
	private Converter converter;
	
	public boolean isLock() {
		return lock;
	}

	@PostConstruct
  void postConstruct() {
		try {
			this.converter = new Converter();
			this.userTransaction = this.context.getUserTransaction();
			Converter.setUserTransaction(this.userTransaction);
			Converter.setBreakAfterEndOfFile(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@PreDestroy
	void preDestroy() {
	}

	public void clearDb() {
		try {
			this.userTransaction.begin();
			this.emLog.createQuery("delete From CrackerLog").executeUpdate();
			this.emMessage.createQuery("delete From Message").executeUpdate();
			this.emRequest.createQuery("delete From Request").executeUpdate();
			this.userTransaction.commit();
			this.emLog.clear();
			this.emMessage.clear();
			this.emRequest.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String crack(String textIn, boolean storeDb) {
		if (this.lock) {
			return null;
		}
		
		String textOut = null;
		this.lock = true;

		try {
			if (storeDb) {
				clearDb();
				Converter.setEntityManagerLog(this.emLog);
				Converter.setEntityManagerMessage(this.emMessage);
				Converter.setEntityManagerRequest(this.emRequest);
			} else {
				Converter.setEntityManagerLog(null);
				Converter.setEntityManagerMessage(null);
				Converter.setEntityManagerRequest(null);
			}

			StringReader reader = new StringReader(textIn);
			LineNumberReader lineNumberReader = new LineNumberReader(reader);
			this.converter.execute(lineNumberReader);
			StringBuilder buffer = this.converter.getBufferCrack(); 
			textOut = buffer.toString();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		this.lock = false;
		return textOut;
	}
}
