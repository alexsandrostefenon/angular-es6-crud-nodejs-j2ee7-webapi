package org.domain.iso8583router.messages.comm.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Logger;
import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.Iso8583RouterComm;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class Emulator implements Module {
	// nao ha mais transacoes descritas para este modulo
	// PluginInfo
	private static final String name = "Emulator";
	private static final int version = 1;
	// atributos internos
	private Connector manager;
	private boolean stopThreads;
	private ArrayList<EmulatorClientDirClientToServer> listClientDirClientToServer;
	// TODO : carregar configuração do banco de dados
	private String[] fieldsCompareIgnore;
	// private long timeSleepAfterXXXX2;
	// TODO : carregar configuração do banco de dados
	private int numLoops = 1;
	// TODO : carregar configuração do banco de dados
	private long sleepInterMessages = 10; // 100 transacções por segundo
	private Integer lastCaptureNSU = 1;
	private Integer lastOkNSU = 1;
	private final Lock _mutex = new ReentrantLock(true);
	private TddClientDirClientToServer tddClientEmulator;
	private TddServerDirClientToServer tddServerDirClientToServer;
	private Thread threadTddClientEmulator = null;

	// REVISADO
	class Session implements Runnable {
		private int count;
		private Iso8583RouterComm conf;
		private Queue<Message> queue;

		// REVISADO
		public int getCount() {
			return count;
		}

		public void add(Message message) {
			this.queue.add(message);
		}

		private boolean checkSendConfirmation(Message message) {
			boolean sendResponse = false;
			String msgType = message.getMsgType();
			String codeProcess = message.getCodeProcess();
			String codeResponse = message.getCodeResponse();
			String data = message.getData();

			if (Utils.checkValue(msgType, "0910") && Utils.checkValue(codeProcess, "900000")) {
				// na recarga, o bit 27 diz se deve enviar resposta
				String reply = message.getReplyEspected();
				sendResponse = Utils.checkValue(reply, "1");
			} else if (Utils.checkValue(msgType, "0210") && Utils.checkValue(codeResponse, "0") && data != null) {
				sendResponse = true;
			} else {
				manager.log(Logger.LOG_LEVEL_ERROR, "ES.checkSendConfirmation",
						String.format("[%s] : invalid message received", conf.getName()), message);
			}

			return sendResponse;
		}

		// REVISADO
		public void run() {
			Message messageRef = this.queue.poll();

			if (messageRef == null) {
				manager.log(Logger.LOG_LEVEL_ERROR, "ES.Session.run", String.format("[%s] : this.transactionRef == null", conf.getName()), messageRef);
				return;
			}

			String msgType = messageRef.getMsgType();

			if (msgType.endsWith("10") || msgType.endsWith("02")) {
				return;
			}

			try {
				Message messageOut = new Message(messageRef);
//				messageOut.setLogger(manager);
				_mutex.lock();
				this.count++;
//				messageOut.setTransactionId(Message.nextInternalNSU());
				// send request, meio de captura gerencia a data, hora, activeIndex de
				// captura e �ltimo activeIndex ok.
				// bit 11
				lastCaptureNSU++;
				
				if (messageRef.getCaptureNsu() != null) {
					messageOut.setCaptureNsu(lastCaptureNSU.toString());
				}
				
				_mutex.unlock();
				manager.log(Logger.LOG_LEVEL_TRACE, "ES.Session.run", String.format("Conectando ao servidor [%s]", this.conf.getName()), messageOut);
				// bit 125
				if (messageRef.getLastOkNsu() != null) {
					messageOut.setLastOkNsu(lastOkNSU.toString());
				}
				
				String systemDateTime = Utils.getYYYYMMDDhhmmss();
				// bit 7
				messageOut.setDateTimeGmt(systemDateTime.substring(0, 14));
				// bit 13
				messageOut.setDateLocal(systemDateTime.substring(4, 8));
				// bit 12
				messageOut.setHourLocal(systemDateTime.substring(8, 14));
				// bit 28
				if (messageRef.getLastOkDate() != null) {
					messageOut.setLastOkDate(systemDateTime.substring(2, 8));
				}
				
				manager.log(Logger.LOG_LEVEL_TRACE, "ES.Session.run", String.format("sending request [%s]", conf.getName()), messageOut);
				messageOut.setModuleOut(this.conf.getName());
				messageOut.setReplyEspected("1");
				Message messageIn = new Message();
				manager.commOut(messageOut, messageIn);
				// bit 039
				String codeResponse = messageIn.getCodeResponse();
				// gerencia o �ltimo activeIndex ok.
				// bit 127
				String authNsu = messageIn.getAuthNsu();

				if (authNsu == null) {
					authNsu = messageIn.getProviderNsu();
				}

				if (authNsu != null) {
					lastOkNSU = Integer.parseInt(authNsu);
				}

				manager.log(Logger.LOG_LEVEL_TRACE, "ES.Session.run", String.format("recebido [%s]", conf.getName()), messageIn);
				// send confirmation
				if (checkSendConfirmation(messageIn) == true) {
					manager.log(Logger.LOG_LEVEL_TRACE, "ES.Session.run", String.format("sending confirmation [%s]", conf.getName()), messageOut);
					messageOut.setMsgTypeConfirmation();
					messageOut.setCodeResponse(codeResponse);
					messageOut.setAuthNsu(authNsu);
					messageOut.setModuleOut(this.conf.getName());
					messageOut.setReplyEspected(null);
					manager.commOut(messageOut, null);
				} else {
//					manager.commRelease(messageOut);
				}
			} catch (Exception e) {
				manager.log(Logger.LOG_LEVEL_ERROR, "ES.Session.run", String.format("[%s] : %s", conf.getName(), e.getMessage()), messageRef);
				e.printStackTrace();
			}

			this.count--;
		}

		// REVISADO
		public Session(Iso8583RouterComm conf) throws Exception {
			this.conf = conf;
			this.count = 0;
			this.queue = new ConcurrentLinkedQueue<Message>();
		}
	}

	// REVISADO
	class EmulatorClientDirClientToServer extends Thread {
		private Iso8583RouterComm conf;
		List<Message> list;

		@Override
		public void run() {
			try {
				ExecutorService tpes = Executors.newFixedThreadPool(conf.getMaxOpenedConnections());
				// usa pool de threads
				Session session = new Session(this.conf);

				for (int loop = 0; stopThreads == false && loop < numLoops; loop++) {
					for (int j = 0; stopThreads == false && j < list.size(); j++) {
						// obt�m uma task livre
						while (stopThreads == false) {
							// verificar se existe uma instância livre
							int count = session.getCount();

							if (count < conf.getMaxOpenedConnections()) {
								session.add(list.get(j));
								tpes.execute(session);

								if (sleepInterMessages > 0) {
									Thread.sleep(sleepInterMessages);
								}

								break;
							}
							// aguarda um tempo antes de tentar novamente
							Thread.sleep(100);
						}
					}

					tddServerDirClientToServer.reset();
				}
				// aguarda as tasks terminarem
				tpes.shutdown();
				tpes.awaitTermination(1, TimeUnit.DAYS);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		public EmulatorClientDirClientToServer(Iso8583RouterComm conf, List<Message> list) throws Exception {
			this.conf = conf;
			this.list = list;
		}
	}

	// REVISADO
	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
		this.listClientDirClientToServer = new ArrayList<EmulatorClientDirClientToServer>(256);
		this.tddServerDirClientToServer = new TddServerDirClientToServer(this.manager, this.fieldsCompareIgnore);
		this.tddClientEmulator = new TddClientDirClientToServer(this.manager, this.fieldsCompareIgnore);
	}

	// REVISADO
	private List<Message> getMessages(String sql) throws Exception {
		EntityManager entityManager = this.manager.getEntityManager();

		if (entityManager == null) {
			return null;
		}

		List<?> listAux = entityManager.createQuery(sql).getResultList();
		@SuppressWarnings("unchecked")
		List<org.domain.iso8583router.entity.Iso8583RouterTransaction> listStorage = (List<org.domain.iso8583router.entity.Iso8583RouterTransaction>) listAux;
		List<Message> list = new ArrayList<Message>(listStorage.size());

		for (org.domain.iso8583router.entity.Iso8583RouterTransaction messageStore : listStorage) {
			String dataEscaped = messageStore.getData();
			String dataUnEscaped = ByteArrayUtils.unEscapeBinaryData(dataEscaped, "(0x", ")", 2);
			messageStore.setData(dataUnEscaped);
			messageStore.setDinamicFields(ByteArrayUtils.unEscapeBinaryData(messageStore.getDinamicFields(), "(0x", ")", 2));
			Message message = new Message(messageStore);
			list.add(message);
		}

		return list;
	}
	
	private String getEmulatorMode(Iso8583RouterComm conf, Message message) {
		// TODO : verificar se a modificação abaixo é suficiente para a mudança de CommConf.emulator para CommConf.session
		String emulatorMode = conf.getDirection() == Utils.CommRequestDirection.CLIENT_TO_SERVER ? "client" : "server";
		
		if (message != null) {
			String msgType = message.getMsgType();
			String connDirection = message.getConnDirection();
			
			if (emulatorMode == null || msgType == null || connDirection == null) {
				return emulatorMode;
			}
			
			if (emulatorMode.equals("client")) {
				if (msgType.endsWith("00") && connDirection.equals("_s2c")) {
					emulatorMode = "server";
				} else if (msgType.endsWith("10") && connDirection.equals("_c2s")) {
					emulatorMode = "server";
				}
			}
		}
		
		return emulatorMode;
	}

	private void startAsynchronous() throws Exception {
		List<Iso8583RouterComm> confs = this.manager.getConfs();

		for (Iso8583RouterComm conf : confs) {
			if (conf.getEnabled() == true) {
				String emulatorMode = getEmulatorMode(conf, null);
				String sql = String.format("from Message m where m.module='%s' order by m.id", conf.getName());
				List<Message> list = getMessages(sql);
				
				if (list.size() > 0) {
					if ("client".equals(emulatorMode)) {
						this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start", String.format("disparando cliente : %s porta %s", conf.getName(), conf.getPort()), null);
						EmulatorClientDirClientToServer emulator = new EmulatorClientDirClientToServer(conf, list);
						this.listClientDirClientToServer.add(emulator);
						emulator.start();
					} else if ("server".equals(emulatorMode)) {
						this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start", String.format("habilitando servidor : %s porta %s", conf.getName(), conf.getPort()), null);
						this.tddServerDirClientToServer.add(conf.getName(), list);
					}
				}
			}
		}
	}

	private void startSynchronous() throws Exception {
		List<Iso8583RouterComm> confs = this.manager.getConfs();
		List<Message> list = getMessages("from Message m order by m.id");

		for (Message message : list) {
//			message.setLogger(manager);
			String moduleName = message.getModule();

			for (Iso8583RouterComm conf : confs) {
				if (conf.getEnabled() == true && conf.getName().equals(moduleName)) {
					String emulatorMode = getEmulatorMode(conf, message);
					
					if ("client".equals(emulatorMode)) {
						this.manager.log(Logger.LOG_LEVEL_DEBUG, "TDD.startSingle",	String.format("ES.startSingle : incluindo fila cliente [%s]", moduleName), message);
						this.tddClientEmulator.add(message);
					} else if ("server".equals(emulatorMode)) {
						this.manager.log(Logger.LOG_LEVEL_DEBUG, "TDD.startSingle", String.format("ES.startSingle : incluindo fila servidor [%s]", moduleName), message);
						this.tddServerDirClientToServer.add(conf.getName(), message);
					}

					break;
				}
			}
		}

		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.startSingle", "disparando clientes", null);
		this.threadTddClientEmulator = new Thread(tddClientEmulator);
		this.threadTddClientEmulator.start();
	}

	private void clearRequests() {
		EntityManager entityManager = this.manager.getEntityManager();
		
		try {
			Integer cnt = (Integer) entityManager.createQuery("select count(*) from Request").getSingleResult();
			
			if (cnt != null && cnt > 0) {
				cnt = entityManager.createQuery("delete from Request").executeUpdate();
				System.out.format("Removed from requests : %d\n", cnt);
			}
		} catch (Exception e) {
			try {
//				userTransaction.begin();
				entityManager.createQuery("delete from Request").executeUpdate();
//				userTransaction.commit();
			} catch (Exception e2) {
				// TODO: handle exception
//				e.printStackTrace();
			}
		}
	}

	// REVISADO
	public synchronized void start() {
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start",	"--------------------------------------------------------------------------------------", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start", "Iniciando sessao de testes...", null);
		stop();
		this.stopThreads = false;

		if (this.numLoops > 1) {
			this.tddServerDirClientToServer.setSynchronous(false);
		} else {
			this.tddServerDirClientToServer.setSynchronous(true);
			// TODO : adicionar em this.fieldsCompareIgnore os campos de contagem 
		}
		// Apply configurations from file
		this.tddServerDirClientToServer.setFieldsCompareIgnore(this.fieldsCompareIgnore);
		this.tddClientEmulator.setFieldsCompareIgnore(this.fieldsCompareIgnore);
		this.clearRequests();

		try {
			if (this.numLoops > 1) {
				startAsynchronous();
			} else {
				startSynchronous();
			}
		} catch (Exception e) {
			this.manager.log(Logger.LOG_LEVEL_ERROR, "TDD.start", String.format("Error in startSingle : %s", e.getMessage()), null);
			e.printStackTrace();
		}
		
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start", "...sessao de testes iniciada", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.start",	"--------------------------------------------------------------------------------------", null);
	}

	private void waitThreads() {
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitThreads",	"--------------------------------------------------------------------------------------", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitThreads", "Aguardando encerramento das threads...", null);

		for (EmulatorClientDirClientToServer emulator : this.listClientDirClientToServer) {
			try {
				manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitInstances", String.format("aguardando finalizacao [%s]...", emulator.conf.getName()), null);
				emulator.join();
				manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitInstances", String.format("...finalizado [%s]", emulator.conf.getName()), null);
			} catch (InterruptedException e) {
				System.out.println("Emulator.waitInstances : error in join for module " + emulator.conf.getName());
			}
		}
		
		if (this.threadTddClientEmulator != null) {
			try {
				this.threadTddClientEmulator.join();
				Thread.sleep(1000);
				this.threadTddClientEmulator = null;
			} catch (InterruptedException e) {
				System.out.println("Emulator.waitInstances : error in join for module TddClientEmulator");
			}
		}
		
		this.listClientDirClientToServer.clear();
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitThreads", "...threads encerradas", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.waitThreads",	"--------------------------------------------------------------------------------------", null);
	}
	// REVISADO
	public synchronized void stop() {
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.stop",	"--------------------------------------------------------------------------------------", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.stop", "Finalizando sessao de testes...", null);
		this.stopThreads = true;
		this.tddClientEmulator.cancel();
		waitThreads();
		this.tddClientEmulator.clear();
		this.tddServerDirClientToServer.clear();
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.stop", "...Finalizada sessao de emulacao.", null);
		this.manager.log(Logger.LOG_LEVEL_TRACE, "TDD.stop",	"--------------------------------------------------------------------------------------", null);
	}
	// REVISADO
	private long processAtalla(Message message) {
		long rc = -1;
		String dataIn = message.getData();
		
		if (dataIn.equals("<00#ping#>")) {
			message.setData("<00#000011#ping#>");
			message.setSendResponse(true);
			rc = 0;
		} else if (dataIn.startsWith("<31#1#")) {
			manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("dataIn : %s", dataIn), message);
			String[] params = dataIn.split("#");
			manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("params : %s", params.toString()), message);
			String passwordIn = params[4];
			manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("passwordIn : %s", passwordIn), message);
			String lastPanBeforeDv = params[5];
			manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("lastPanBeforeDv : %s", lastPanBeforeDv), message);
			EntityManager entityManager = this.manager.getEntityManager();

			if (entityManager != null) {
				String sql = String.format(
						"select distinct password from storage where (password != '') and (provider_ec != '') and (track_ii like '%%%s%%' or pan like '%%%s%%')",
						lastPanBeforeDv, lastPanBeforeDv);
				manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("sql : %s", sql), message);
				Object passwordOut = entityManager.createNativeQuery(sql).getSingleResult();
				manager.log(Logger.LOG_LEVEL_DEBUG, "ES.processAtalla", String.format("passwordOut : %s", passwordOut), message);
				
				if (passwordOut != null) {
					String dataOut = String.format("<41#%s#Y#>", passwordOut.toString());
					message.setData(dataOut);
					message.setSendResponse(true);
					rc = 0;
				}
			}
		}

		return rc;
	}
	// disparado quando um simulador de Servidor recebe uma requisição (->
	// [Provider] 0200, 0202, 0420, etc...])
	// Router -> Provider (Operadora recebe do Router)
	// Router -> RouterSystems (Sistemas periféricos recebem do Router))
	public long execute(Object obj) {
		long rc = -1;

		if (obj != null) {
			Message message = (Message) obj;
			String module = message.getModuleIn();
			
			if (module.equals("ATALLA_AKB")) {
				rc = processAtalla(message);
			} else {
				rc = this.tddServerDirClientToServer.execute(module, message);
			}
		} else {
			start();
			waitThreads();
		}

		return rc;
	}

	// REVISADO
	public void getInfo(ModuleInfo info) {
		info.family = "Connector";
		info.name = name;
		info.version = version;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
		info.setRunning(!this.stopThreads);
	}

	// REVISADO
	public void config(String section, String value) {
		// readConfs();
	}
}
