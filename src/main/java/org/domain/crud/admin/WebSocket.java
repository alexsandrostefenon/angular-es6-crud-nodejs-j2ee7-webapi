package org.domain.crud.admin;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.domain.crud.admin.RequestFilter.LoginResponse;


@ServerEndpoint(value = "/websocket")
public class WebSocket {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    @OnMessage
    public void onMessage(Session session, String token) {
    	RequestFilter.LoginResponse login = RequestFilter.getLogin(token);
    	session.getUserProperties().put("login", login);
    	System.out.println("Text message: " + token);
        logger.info("New websocket session opened: " + session.getId());
        clients.add(session);
    }    

    // remove the session after it's closed
    @OnClose
    public void onClose(Session session) {
        logger.info("Websoket session closed: " + session.getId());
        clients.remove(session);
    }

    // Exception handling
    @OnError
    public void error(Session session, Throwable t) {
        t.printStackTrace();
    }
    
	private class Notifier extends Thread {
		private Integer company;
		private String service;
		private String msg;

		public void run() {
			try {
				// dá uma folga para dar tempo de gravar no banco
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (Session s : clients) {
				RequestFilter.LoginResponse login = (LoginResponse) s.getUserProperties().get("login");
				Integer userCompany = login.getUser().getCompany();
				// TODO : também adicionar restrição de categoria
				// enviar somente para os clients de "company"
				if (this.company == null || userCompany == 1 || this.company == userCompany) {
					// envia somente para os usuários com acesso ao serviço alterado
					if (login.getWebsocketServices().contains(service)) {
						try {
							System.out.format("WebSocket.Notifier, user %s : %s\n", login.getUser().getName(), msg);
							s.getBasicRemote().sendText(this.msg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		public Notifier(Integer company, String service, String msg) {
			this.company = company;
			this.service = service;
			this.msg = msg;
		}
	}
    
    // This method sends the same Bidding object to all opened sessions
    public void notify(Object obj, Integer id, boolean isRemove) {
    	String service = obj.getClass().getSimpleName();
		service = Utils.convertCaseUnderscoreToCamel(service, false);
		Integer company = null;
		
		if (Utils.haveField(obj.getClass(), "company")) {
			company = (Integer) Utils.readField(obj, "company");
		}
		
		JsonObjectBuilder builderPrimaryKey = Json.createObjectBuilder();
		builderPrimaryKey.add("id", id);
		
		if (company != null) {
			builderPrimaryKey.add("company", company);
		}
		
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		if (isRemove == false) {
			builder.add("action", "notify");
		} else {
			builder.add("action", "delete");
		}
		
		builder.add("service", service);
		builder.add("primaryKey", builderPrimaryKey);
		JsonObject json = builder.build();
		String str = json.toString();
		// TODO : adicionar controle de categoria
    	Notifier notifier = new Notifier(company, service, str);
//    	Notifier notifier = new Notifier(String.format("{\"action\":\"delete\",\"service\":\"%s\",\"id\":\"%s\"}", service, id.toString()));
		notifier.start();
    }

}
