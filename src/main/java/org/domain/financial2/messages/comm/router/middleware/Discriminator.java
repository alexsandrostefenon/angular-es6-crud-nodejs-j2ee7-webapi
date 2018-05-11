package org.domain.financial2.messages.comm.router.middleware;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Comm;
import org.domain.iso8583router.messages.comm.Connector;

public class Discriminator implements Module {
	Connector manager;
    
	public void enable(ModuleManager manager) {
		this.manager = (Connector) manager;
	}

	public long execute(Object obj) {
		Message messageIn = (Message) obj;
		String moduleIn = messageIn.getModuleIn();
		long rc = 0;
		String providerName = messageIn.getProviderName();
		// se for PTG tem devolver a mesma tag IT
		String captureProtocol = messageIn.getCaptureProtocol();
		messageIn.setCaptureProtocol(null);
		String data = messageIn.getData();
		messageIn.setData(null);
		
		try {
			Comm.parseMessage(messageIn, providerName, null, data, Message.DIRECTION_NAME_C2S);
			String msgType = messageIn.getMsgType();
			messageIn.setModuleOut(providerName);
			// verifica se deve esperar uma resposta (0110, 0710, 0810, 0210, etc..)
			if (msgType.equals("0202") == true) {
				messageIn.setReplyEspected(null);
			} else {
				messageIn.setReplyEspected("1");
			}
			
			Message messageOut = new Message();
			rc = this.manager.commOut(messageIn, messageOut);
			
			if (rc >= 0) {
				try {
					// verifica se deve enviar uma resposta (0110, 0710, 0810, 0210, etc..)
					if (msgType.equals("0202") == true) {
						messageIn.setSendResponse(false);
					} else {
						messageIn.clear();
						messageIn.copyFrom(messageOut, true);
						data = Comm.generateMessage(messageIn, providerName, null);
						messageIn.setData(data);
						messageIn.setSendResponse(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
					rc = -2;
				}
			} else {
				// TODO : disparar desfazimento
			}
		} catch (Exception e) {
			rc = -1;
		}
		
		if (moduleIn.equals("GtwOut")) {
			messageIn.setCaptureProtocol(captureProtocol);
			messageIn.setCodeResponse("0000");;
			messageIn.setRoot("root_s2c");
		} else {
			messageIn.setRoot("DISCRIMINATOR_c2s");
		}

		return rc;
	}

	public void start() {
	}

	public void stop() {
		// TODO Auto-generated method stub
	}

	public void getInfo(ModuleInfo info) {
		info.family = "Discriminator";
		info.name = "Discriminator";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}

	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
