package org.domain.iso8583router.messages.comm.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.domain.commom.Logger;
import org.domain.commom.Utils;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

public class TddServerDirClientToServer {
	private Connector manager;
	private String[] fieldsCompareIgnore;
	private String[] fieldsCompareAsynchronous;
	private HashMap<String, Integer> mapModulesMessagesListCursor;
	private HashMap<String, List<Message>> mapModulesMessagesList;
	private boolean synchronous;
	private int asynchronousNsu; 
	
	public TddServerDirClientToServer(Connector manager, String[] fieldsCompareIgnore) {
		this.manager = manager;
		this.fieldsCompareIgnore = fieldsCompareIgnore;
		this.synchronous = true;
		this.asynchronousNsu = 0;
		this.mapModulesMessagesListCursor = new HashMap<String, Integer>(1024);
		this.mapModulesMessagesList = new HashMap<String, List<Message>>(1024);
		this.fieldsCompareAsynchronous = new String[] {"root", "msgType", "codeProcess"};
	}

	public void add(String module, Message message) {
		List<Message> listMessages = this.mapModulesMessagesList.get(module);
		
		if (listMessages != null) {
			listMessages.add(message);
		} else {
			List<Message> newList = new ArrayList<Message>();
			newList.add(message);
			this.mapModulesMessagesList.put(module, newList);
			this.mapModulesMessagesListCursor.put(module, 0);
		}
	}

	public void add(String module, List<Message> list) {
		for (Message message : list) {
			add(module, message);
		}
	}

	public void setFieldsCompareIgnore(String[] fieldsCompareIgnore) {
		this.fieldsCompareIgnore = fieldsCompareIgnore;
	}

	public void setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
	}

	public void clear() {
		this.mapModulesMessagesList.clear();
		this.mapModulesMessagesListCursor.clear();
	}
	
	public void reset() {
		Set<String> modules = this.mapModulesMessagesList.keySet();

		for (String module : modules) {
			this.mapModulesMessagesListCursor.put(module, 0);
		}
	}

	private Integer getCursorAsynchronous(List<Message> list, Message message) {
		Integer cursor = null;
		
		try {
			cursor = Message.getFirstCompatible(list, message, fieldsCompareAsynchronous, manager, "ES.TddServer.getCursor");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cursor;
	}
	// Servidor que recebe uma transacao do Cliente : Router -> Provider, Router -> RouterService
	public long execute(String moduleRequest, Message message) {
		long rc = -1;
		Integer cursor = null;
		List<Message> list = this.mapModulesMessagesList.get(moduleRequest);
		
		if (list == null) {
			manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("Not found messages for module [%s]\n", moduleRequest), message);
			return rc;
		}
		
		if (this.synchronous) {
			cursor = this.mapModulesMessagesListCursor.get(moduleRequest);
		} else {
			cursor = this.getCursorAsynchronous(list, message);
		}

		if (cursor == null) {
			manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("Not found compatible messages for module [%s]\n", moduleRequest), message);
			return rc;
		}

		if (cursor >= list.size()) {
			manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("contador de mensagem estourado [%s] : [%s]\n", moduleRequest, cursor), message);
			return rc;
		}

		Message messageRef = list.get(cursor++);
		String moduleRef = messageRef.getModule();

		try {
			manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("recebido [%s]", moduleRef), message);
			String connDirection = messageRef.getConnDirection();
			// verifica se a transacao referencia eh entrada 
			if (Utils.checkValue(connDirection, Message.DIRECTION_NAME_C2S)) {
				// TODO : comparar a entrada e carregar a saida
				String fieldName = Message.compareIgnore(messageRef, message, fieldsCompareIgnore); 

				if (fieldName == null) {
					manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("entrada compativel com o esperado [%s]", moduleRef), message);
					message.setSendResponse(false);

					if (cursor < list.size()) {
						messageRef = list.get(cursor++);
						connDirection = messageRef.getConnDirection();

						if (Utils.checkValue(connDirection, Message.DIRECTION_NAME_S2C)) {
							Long internalNsu = message.getTransactionId();
							String authNsu = message.getAuthNsu();
							String captureNsu = message.getCaptureNsu();
							Long timeStamp = message.getTimeStamp();
							message.clear();
							message.copyFrom(messageRef, false);
							message.setSendResponse(true);
							message.setModuleOut(moduleRef);
							message.setTransactionId(internalNsu);
							message.setTimeStamp(timeStamp);
							
							if (this.synchronous == false) {
								Integer nsu = this.asynchronousNsu++;
								message.setProviderNsu(nsu.toString());
								
								if (authNsu != null) {
									message.setAuthNsu(authNsu);
								} else {
									message.setAuthNsu(nsu.toString());
								}
								
								if (captureNsu != null) {
									message.setCaptureNsu(captureNsu);
								}
							}
							
							manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("enviando resposta especificada [%s]", moduleRef), message);
						} else {
							manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("nao ha resposta especificada [%s]", moduleRef), message);
							cursor--;
						}
					} else {
						manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("nao ha mais transacoes descritas para este modulo [%s]\n", moduleRef), message);
					}
				} else {
					// TODO : a seguinte linha apenas para debug
//					stopThread = true;
					// TODO : adicionar na lista de incompatibilidades
					String dataReceived = message.getFieldData(fieldName);
					String dataEspected = messageRef.getFieldData(fieldName);
					manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("diference in %s - [%s]", fieldName, moduleRef), message);
					manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("received [%04d - %s]", dataReceived != null ? dataReceived.length() : 0, dataReceived), message);
					manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("espected [%04d - %s]", dataEspected != null ? dataEspected.length() : 0, dataEspected), message);
				}
			} else {
				message.copyFrom(messageRef, true);
				// TODO : verficar a melhor forma de saber se deve ou nao enviar resposta
				String nsu = Integer.toString(cursor);
				message.setAuthNsu(nsu);
				message.setProviderNsu(nsu);
				message.setSendResponse(true);
				message.setModuleOut(moduleRef);
				manager.log(Logger.LOG_LEVEL_TRACE, "ES.TddServer.execute", String.format("enviando resposta simulada [%s]", moduleRef), message);
			}

			rc = 0;
		} catch (Exception e) {
			manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddServer.execute", String.format("[%s] : %s", moduleRef, e.getMessage()), message);
		}

		if (this.synchronous) {
			this.mapModulesMessagesListCursor.put(moduleRequest, cursor);
		}
		
		return rc;
	}
}
