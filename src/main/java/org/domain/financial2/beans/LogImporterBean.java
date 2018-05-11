package org.domain.financial2.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

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

import org.domain.commom.Logger;
import org.domain.financial2.messages.log.importer.Converter;
import org.domain.financial2.messages.log.importer.ConverterConf;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@Named 
public class LogImporterBean {
	@Resource
	private EJBContext context;	
	
	@PersistenceContext
	EntityManager emLog;

	@PersistenceContext
	EntityManager emMessage;

	@PersistenceContext
	EntityManager emRequest;

	private InputStream inputStream;
	boolean lock;
	private UserTransaction userTransaction;
	private Converter converter;
	private boolean keepRunning;
	private String dataIn;
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	class DaemonConverter implements Runnable {
		
		@Override
		public void run() {
			if (lock == true) {
				return;
			}
			
			lock = true;
			Converter.setFlagsLogLevel(Logger.LOG_LEVEL_ERROR);
	
			try {
				if (inputStream != null && keepRunning == false) {
					LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
					converter.execute(lineNumberReader);
				} else {
					if (dataIn == null) {
						dataIn = ConverterConf.getInstance().dataIn;					
					}
					
					File root = new File(dataIn);
	
					if (root.exists() && root.isDirectory()) {
						String[] files = root.list();
						java.util.Arrays.sort(files);
						String folder = root.getAbsolutePath();
	
						for (int i = 0; i < files.length; i++) {
							String filename = folder + File.separator + files[i];
							// ConverterCommon.getInstance().breakAfterEndOfFile = false;
							LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(new FileInputStream(filename)));
							converter.execute(lineNumberReader);
							lineNumberReader.close();
						}
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			Converter.setFlagsLogLevel(0xffffffff);
			lock = false;
		}
	
	}
	
	public boolean isLock() {
		return lock;
	}

	@PostConstruct
  void postConstruct() {
		try {
			this.converter = new Converter();
			this.userTransaction = this.context.getUserTransaction();
			Converter.setUserTransaction(this.userTransaction);
			Converter.setEntityManagerLog(this.emLog);
			Converter.setEntityManagerMessage(this.emMessage);
			Converter.setEntityManagerRequest(this.emRequest);
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

	public void importFiles(boolean keepRunning, boolean clearDb, boolean background, String dataIn) {
		if (this.lock) {
			return;
		}
		
		if (clearDb == true) {
			clearDb();
		}
		
		this.keepRunning = keepRunning;
		this.dataIn = dataIn;
		
		if (keepRunning) {
			Converter.setBreakAfterEndOfFile(false);
		} else {
			Converter.setBreakAfterEndOfFile(true);
		}
		// implement your business logic here
		if (background) {
			Thread thread = new Thread(new DaemonConverter());
			thread.start();
		} else {
			DaemonConverter d = new DaemonConverter();
			d.run();
		}
	}

	public String popLogs() {
		return this.converter.popLogs();
	}
	
}
