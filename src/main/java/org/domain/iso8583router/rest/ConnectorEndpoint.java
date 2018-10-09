package org.domain.iso8583router.rest;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.domain.iso8583router.beans.ConnectorBean;
import org.domain.iso8583router.entity.Iso8583RouterTransaction;
import org.domain.iso8583router.messages.Message;

@Path("/connector")
@Stateless
@Named
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectorEndpoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6609181198285473672L;

	private Logger logger;

	@PersistenceContext
	EntityManager entityManager;

//  @PersistenceContext(unitName = "FinancialSmall")
	@PersistenceContext
	private EntityManager entityManagerSmall;

	@Inject
	ConnectorBean manager;

	@PostConstruct
	void postConstruct() {
		this.logger = Logger.getLogger("iso8583router");
	}

	@GET
	public Response listAll() {
		return Response.ok("Processed").build();
	}

	@GET
	@Path("get_connector_state")
	public Response getConnectorState() {
		this.logger.log(Level.INFO, String.format(">>>>>>> getConnectorState "));
		boolean state = !(this.manager.getManager().getIsStopped()); 
		return Response.ok(String.format("{\"state\": %s}", state)).build();
	}
	
	// TODO : utilizar websocket para sinalizar os clients da mudança do status 

	@POST
	@Path("set_connector_state")
	@Produces(MediaType.TEXT_PLAIN)
	public String setConnectorState(String name, Boolean state) {
		this.logger.log(Level.INFO, String.format(">>>>>>> setConnectorState "));
		Boolean isStopped = this.manager.getManager().getIsStopped();
		boolean change = false;
		
		if (state == null || state == false) {
			if (isStopped == false) {
				this.manager.getManager().stop();
				change = true;
			}
		} else {
			if (isStopped == true) {
				this.manager.getManager().setEntityManager(this.entityManager);
				this.manager.getManager().start();
				change = true;
			}
		}
		
		if (change == true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	@POST
	@Path("set_module_state")
	public String setModuleState(String name, Boolean state) {
		this.logger.log(Level.INFO, String.format(">>>>>>> setModuleState "));
		String ret = "";
		
		try {
			boolean isStopped = ! this.manager.getManager().getInfoModule(name).isRunning();
			
			if (state == null || state == false) {
				if (isStopped == false) {
					this.manager.getManager().stopModule(name);
				}
			} else {
				if (isStopped == true) {
					this.manager.getManager().setEntityManager(this.entityManagerSmall);
					this.manager.getManager().startModule(name);
				}
			}
		} catch (Exception e) {
			ret = e.getMessage();
			e.printStackTrace();
		}
		
		return ret;
	}

	@POST
	@Path("request")
	public Message request(Message message) {
		this.logger.log(Level.INFO, String.format(">>>>>>> request "));
		this.entityManager.persist((Iso8583RouterTransaction) message);
		Message messageIn = null;//Session.execute(this.manager.getManager(), message);
		return messageIn;
	}

}
