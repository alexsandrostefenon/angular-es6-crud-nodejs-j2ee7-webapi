package org.domain.financial2.messages.log.importer;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Logger;
import org.domain.financial2.entity.ISO8583CrackerLog;
import org.domain.iso8583router.messages.Message;

class ConverterLogger implements Logger {
	private int count;
	private int flagsLogLevels = 0xffffffff;
	private EntityManager entityManager = null;
	boolean isJta = false;
	private UserTransaction userTransaction;
	private static ConverterLogger instance = null; 
	private StringBuffer bufferToPop;
	
	public static synchronized ConverterLogger getInstance() {
		if (ConverterLogger.instance == null) {
			ConverterLogger.instance = new ConverterLogger();
		}
		
		return ConverterLogger.instance;
	}
	
	public void setFlagsLogLevels(int flagsLogLevels) {
		this.flagsLogLevels = flagsLogLevels;
	}
	
	private ConverterLogger() {
		this.bufferToPop = new StringBuffer(100 * 1024);
	}
	
	public String popLogs() {
		String str = this.bufferToPop.toString();
		this.bufferToPop.setLength(0);
		return str;
	}
	
	public void log(int logLevel, String header, String text, Object data) {
		Message message = (Message) data;
		
		if ((this.flagsLogLevels & logLevel) == 0) {
			return;
		}
		
		String logLevelName = Logger.getLogLevelName(logLevel);
		
		String payloadTypeName = "";
		String root = "";
		String module = "";
		String lineData = "";
		String rawData = "";
		
		if (message != null) {
			lineData = message.toString();
			module = message.getModule();
			root = message.getRoot();
			rawData = message.rawData;
		}
		
		if (text != null) {
			text = ByteArrayUtils.escapeBinaryData(text, true, '?');
		}
		
		if (lineData != null) {
			lineData = ByteArrayUtils.escapeBinaryData(lineData, true, '?');
		}
		
		if (rawData != null) {
			rawData = ByteArrayUtils.escapeBinaryData(rawData, true, '?');
			
			if (rawData.length() > 4096) {
				rawData = rawData.substring(0, 4096);
			}
		}
		
		this.count++;
		ISO8583CrackerLog log = new ISO8583CrackerLog();
		log.setId(this.count);
		log.setLoglevel(logLevelName);
		log.setHeader(header);
		log.setPayloadtype(payloadTypeName);
		log.setRoot(root);
		log.setModule(module);
		log.setMessage(text);
		log.setLineremind(lineData);
		log.setLineoriginal(rawData);
		
		if (this.entityManager != null) {
			synchronized (this.entityManager) {
				try {
					if (this.isJta == true) {
						if (this.userTransaction != null) {
							this.userTransaction.begin();
						}
					} else {
						this.entityManager.getTransaction().begin();
					}
					
					this.entityManager.persist(log);
					
					if (this.bufferToPop.length() < (100 * 1024)) {
						this.bufferToPop.insert(0, log.toString());
					}
				} catch (Exception e) {
					String sql = String.format("INSERT INTO logs VALUES(%d, '%s','%s','%s','%s','%s','%s','%s','%s')", this.count, logLevelName, header, payloadTypeName, root, module, text, lineData, rawData);
					System.out.println(sql);
					System.out.printf("Fail in entityManager.persist(log) : %s : %s", e.getMessage(), message);
				} finally {
					if (this.isJta == true) {
						if (this.userTransaction != null) {
							try {
								this.userTransaction.commit();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (RollbackException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (HeuristicMixedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (HeuristicRollbackException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SystemException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						this.entityManager.getTransaction().commit();
					}
					
					this.entityManager.clear();
				}
			}
		}
	}

	public void setUserTransaction(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
		this.isJta = false;
		
		try {
			entityManager.getTransaction().begin();
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			this.isJta = true;
		}
	}

}
