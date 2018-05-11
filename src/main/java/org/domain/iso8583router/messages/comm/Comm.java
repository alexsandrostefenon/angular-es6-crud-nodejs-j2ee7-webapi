package org.domain.iso8583router.messages.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.domain.commom.Logger;
import org.domain.commom.RefInt;
import org.domain.iso8583router.entity.ISO8583RouterCommConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConf;
import org.domain.iso8583router.entity.ISO8583RouterMessageAdapterConfItem;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.MessageAdapter;

public class Comm {
	private static Map<String, ISO8583RouterMessageAdapterConf> mapConf = new Hashtable<String, ISO8583RouterMessageAdapterConf>(100);// key adapterConfName
	private static Map<String, MessageAdapter> mapAdapters = new Hashtable<String, MessageAdapter>(100);// key className
	// atributos
	public ISO8583RouterCommConf conf;
	public byte[] bufferReceive;
	byte[] bufferSend;
	private Logger logger;
	private String directionNameReceive;
	// configurações desta sessão
	private CommAdapter commAdapter;
	private Socket socket;
	public OutputStream os;
	public InputStream is;

	@SuppressWarnings("unchecked")
	public static void loadConfs(EntityManager entityManager, Logger logger) {
		List<ISO8583RouterMessageAdapterConf> listMessageAdapterConf = entityManager.createQuery("from MessageAdapterConf").getResultList();
		
		if (listMessageAdapterConf.size() == 0) {
			entityManager.getTransaction().begin();
			entityManager.persist(new ISO8583RouterMessageAdapterConf("iso8583default", null));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(1, "msgType", "0", 4, 4, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(2, "pan", "2", 12, 16, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(3, "codeProcess", "3", 6, 6, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(4, "transactionValue", "4", 12, 12, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(11, "captureNsu", "11", 6, 6, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(42, "captureEc", "42", 15, 15, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(41, "equipamentId", "41", 8, 8, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.persist(new ISO8583RouterMessageAdapterConfItem(67, "numPayments", "67", 2, 2, 0, "\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d", "iso8583default", 0x00000001));
			entityManager.getTransaction().commit();
			listMessageAdapterConf = entityManager.createQuery("from MessageAdapterConf").getResultList();
		}
		
		for (ISO8583RouterMessageAdapterConf messageAdapterConf : listMessageAdapterConf) {
			messageAdapterConf.setItems(entityManager.createQuery("from MessageAdapterConfItem o where o.messageAdapterConfName='" + messageAdapterConf.getName() + "'").getResultList());
			Comm.mapConf.put(messageAdapterConf.getName(), messageAdapterConf);
			String className = messageAdapterConf.getAdapterClass();
			
			if (Comm.mapAdapters.get(className) == null) {
				try {
					Comm.mapAdapters.put(className, (MessageAdapter) Class.forName(className).newInstance());
				} catch (Exception e) {
					logger.log(Logger.LOG_LEVEL_ERROR, "Comm.loadConfs", e.getMessage(), null);
				}
			}
			
		}
	}
	
	public static enum BinaryEndian {
		UNKNOW, BIG, LITTLE;
	}
	// retorna o novo offset
	public static int pack(byte[] buffer, int offset, int numBytes, BinaryEndian packageType, int val) {
		if (numBytes < 0 || numBytes > 4) {
			throw new InvalidParameterException();
		}

		if (packageType == BinaryEndian.LITTLE) {
			for (int i = 0; i < numBytes; i++) {
				buffer[offset++] = (byte) (val & 0x000000ff);
				val >>= 8;
			}
		} else if (packageType == BinaryEndian.BIG) {
			for (int i = numBytes-1; i >= 0; i--) {
				buffer[offset+i] = (byte) (val & 0x000000ff);
				val >>= 8;
			}

			offset += numBytes;
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}
	// retorna o novo offset
	public static int pack(byte[] buffer, int offset, int numBytes, byte[] val) {
		if (numBytes < 0 || numBytes > val.length) {
			throw new InvalidParameterException();
		}

		for (int i = 0; i < numBytes; i++) {
			buffer[offset++] = val[i];
		}

		return offset;
	}
	// retorna o novo offset
	public static int unpack(InputStream is, byte[] buffer, int offset, int numBytes, BinaryEndian packageType, RefInt val) throws IOException {
		int readen = is.read(buffer, offset, numBytes);
		
		if (readen < 0) {
			return readen;
		}

		if (readen != numBytes) {
			throw new IOException();
		}

		val.value = 0;

		if (numBytes < 0 || numBytes > 4) {
			throw new InvalidParameterException();
		}

		if (packageType == BinaryEndian.LITTLE) {
			for (int i = numBytes-1; i >= 0; i--) {
				val.value <<= 8;
				val.value |= (buffer[offset+i] & 0x000000ff);
			}

			offset += numBytes;
		} else if (packageType == BinaryEndian.BIG) {
			for (int i = 0; i < numBytes; i++) {
				val.value <<= 8;
				val.value |= (buffer[offset++] & 0x000000ff);
			}
		} else {
			throw new InvalidParameterException();
		}

		return offset;
	}	

	public void log(int logLevel, String header, String text, Message message) {
		if (this.logger != null) {
			this.logger.log(logLevel, header, text, message);
		}
	}

	public static synchronized String generateMessage(Message message, String messageAdapterConfName, String root) throws Exception {
		String ret = null;

		try {
			message.bufferParseGenerateDebug.setLength(0);
			ret = message.rawData;
			ISO8583RouterMessageAdapterConf adapterConf = Comm.mapConf.get(messageAdapterConfName);
			
			if (adapterConf != null) {
				MessageAdapter adapter = Comm.mapAdapters.get(adapterConf.getAdapterClass());
				
				if (adapter != null) {
					ret = adapter.generate(message, adapterConf, root);
					
					if (ret == null) {
						throw new Exception("MessageAdapterConfManager.generateMessage : fail in generate String for message : " + message.toString());
					}
				} else {
					throw new Exception(String.format("MessageAdapterConfManager.generateMessage : don't found adapter for root = %s and message = %s", root, message.toString()));
				}
			} else {
				throw new Exception(String.format("MessageAdapterConfManager.generateMessage : don't found MessageAdapterConf for module = %s", messageAdapterConfName));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return ret;
	}
	
	public void send(Message message) throws Exception {
		if (this.socket == null) {
			throw new Exception("disposed");
		}

		// TODO : se o servidor desconectou, tentar reconectar
		if (this.socket.isConnected() == false) {
			// this.socket.connect(this.serverSocketAdress);
		}

		if (this.socket.isOutputShutdown()) {
			// this.socket.connect(this.serverSocketAdress);
		}

		message.lockNotify = true;
		message.setModuleOut(conf.getName());
		message.transmissionTimeout = true;
		log(Logger.LOG_LEVEL_DEBUG, "Comm.send", String.format("exported [%s]", this.conf.getName()), message);
		String str = Comm.generateMessage(message, conf.getMessageAdapter(), message.getRoot());
		log(Logger.LOG_LEVEL_DEBUG, "Comm.send", String.format("sending buffer [%s - %s - %s] : [%04d] %s", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort(), str.length(), str), message);
		
		try {
			if (this.socket.isConnected() == false) {
				// this.socket.connect(this.serverSocketAdress);
			}

			if (this.socket.isOutputShutdown()) {
				// this.socket.connect(this.serverSocketAdress);
			}

			byte[] buffer = str.getBytes("ISO-8859-1");
			this.commAdapter.send(this, message, buffer);
		} catch (Exception e) {
			log(Logger.LOG_LEVEL_ERROR, "Comm.send", String.format("fail to send buffer [%s] : [%04d] %s : %s", this.conf.getName(), str.length(), str, e.getMessage()), message);
			e.printStackTrace();
			throw e;
		}

		log(Logger.LOG_LEVEL_INFO, "Comm.send", String.format("sended [%s] : [%04d] %s", this.conf.getName(), str.length(), str), message);
	}

	public static synchronized void parseMessage(Message message, String messageAdapterConfName, String root, String data, String directionSuffix) throws Exception {
		if (data == null) {
			throw new InvalidParameterException();
		}
		
		message.bufferParseGenerateDebug.setLength(0);
		ISO8583RouterMessageAdapterConf adapterConf = Comm.mapConf.get(messageAdapterConfName);
		
		if (adapterConf != null) {
			MessageAdapter adapter = Comm.mapAdapters.get(adapterConf.getAdapterClass());
			
			if (adapter != null) {
				adapter.parse(message, adapterConf, root, data, directionSuffix);
			} else {
				throw new Exception(String.format("MessageAdapterConfManager.generateMessage : don't found adapter for root = %s and message = %s", root, message.toString()));
			}
		} else {
			throw new Exception(String.format("MessageAdapterConfManager.generateMessage : don't found MessageAdapterConf for module = %s", messageAdapterConfName));
		}
	}

	public int receive(Message messageIn, Message messageRef) throws Exception {
		if (this.socket == null) {
			throw new Exception("disposed");
		}
		
		if (messageIn.getId() == null) {
			if (messageRef != null) {
				messageIn.setId(messageRef.getId());
			} else {
				messageIn.setId(Message.nextId());
			}
		}

		log(Logger.LOG_LEVEL_DEBUG, "Comm.receive", String.format("wait receive [%s - %s - %s] ...", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort()), messageIn);
		int size = this.commAdapter.receive(this, messageIn, messageIn.bufferComm);

		if (size > 0) {
			String str = new String(messageIn.bufferComm, 0, size, "ISO-8859-1");
			String rawData = str;
			// TODO : estou forçando um sleep para sincronizar o debug das threads
//			Thread.sleep(10);

			try {
				Comm.parseMessage(messageIn, this.conf.getMessageAdapter(), messageIn.getRoot(), str, this.directionNameReceive);
			} catch (Exception e) {
				log(Logger.LOG_LEVEL_ERROR, "Comm.receive",
						String.format("parseMessage [%s] : [msgSize = %d] : %s", this.conf.getName(), size, e.getMessage()),
						messageIn);
				throw e;
			}

			messageIn.transmissionTimeout = false;
			messageIn.setModuleIn(conf.getName());
			messageIn.setModuleOut(null);
			messageIn.rawData = rawData;
			log(Logger.LOG_LEVEL_INFO, "Comm.receive", String.format("received [%s - %s - %s] : %s", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort(), str), messageIn);
		} else {
			log(Logger.LOG_LEVEL_INFO, "Comm.receive", String.format("not data received [%s - %s - %s]", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort()), messageIn);
		}

		return size;
	}

	private void initialize(ISO8583RouterCommConf conf, Logger logger, boolean isServer, Socket socket) throws Exception {
		this.conf = conf;
		this.socket = socket;
		this.bufferReceive = new byte[64 * 1024];
		this.bufferSend = new byte[64 * 1024];
		this.logger = logger;
		log(Logger.LOG_LEVEL_DEBUG, "Comm.initialize", String.format("modulo v2 [%s]", this.conf.getName()), null);
		String adapter = conf.getAdapter();
		Class<?> _class = Class.forName(adapter);
		this.commAdapter = (CommAdapter) _class.newInstance();

		if (isServer == true) {
			this.directionNameReceive = Message.DIRECTION_NAME_C2S;
//			this.directionNameSend = Message.DIRECTION_NAME_S2C;
			this.is = this.socket.getInputStream();
			this.os = this.socket.getOutputStream();
		} else {
			this.directionNameReceive = Message.DIRECTION_NAME_S2C;
//			this.directionNameSend = Message.DIRECTION_NAME_C2S;
			this.os = this.socket.getOutputStream();
			this.is = this.socket.getInputStream();
		}
	}

	// Conecta como servidor
	public Comm(Socket client, ISO8583RouterCommConf conf, Logger logger) throws Exception {
		initialize(conf, logger, true, client);
		log(Logger.LOG_LEVEL_DEBUG, "Comm.Comm",
				String.format("conexao recebida do cliente, modulo [%s]", this.conf.getName()), null);
	}

	// Conecta como cliente
	// este construtor fica bloqueante até que o servidor suba,
	// ou até que a flag de cancelamento seja ativada.
	public Comm(ISO8583RouterCommConf conf, Logger logger) throws Exception {
		Socket socket = null;

		while (socket == null) {
			try {
				socket = new Socket(conf.getIp(), conf.getPort());
//				this.serverSocketAdress = socket.getRemoteSocketAddress();
			} catch (Exception e) {
				System.out.printf("Não foi possível conexão com o servidor na porta %d, aguardando 5000 milisegundos.\n",
						conf.getPort());
				Thread.sleep(5000);
			}
		}

		if (socket != null) {
			initialize(conf, logger, false, socket);
			log(Logger.LOG_LEVEL_DEBUG, "Comm.Comm", String.format("conexao cliente aberta [%s]", this.conf.getName()), null);
		}
	}

	public void close() throws IOException {
		log(Logger.LOG_LEVEL_DEBUG, "Comm.close", String.format("fechando conexao... [%s - %s - %s]", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort()), null);
		
		if (this.socket.isClosed() == false) {
			this.socket.close();
			log(Logger.LOG_LEVEL_DEBUG, "Comm.close", String.format("...conexao fechada [%s - %s - %s]", this.conf.getName(), this.socket.getPort(), this.socket.getLocalPort()), null);
		}
	}
	public static Map<String, ISO8583RouterMessageAdapterConf> getMapConf() {
		return mapConf;
	}
}