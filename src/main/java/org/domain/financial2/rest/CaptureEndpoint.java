package org.domain.financial2.rest;

import java.io.Serializable;
import java.util.List;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.domain.financial2.beans.LogImporterBean;
import org.domain.financial2.entity.ISO8583Bin;
import org.domain.financial2.entity.CardConf;
import org.domain.financial2.entity.ISO8583TefFlow;
import org.domain.financial2.entity.ISO8583TefProduct;
import org.domain.financial2.entity.ISO8583TefProductWithoutCard;
import org.domain.financial2.messages.capture.Session;
import org.domain.iso8583router.beans.ConnectorBean;
import org.domain.iso8583router.entity.ISO8583RouterTransaction;
import org.domain.iso8583router.messages.Message;

@Path("/capture")
@Stateless
@Named
public class CaptureEndpoint implements Serializable {
	private static final long serialVersionUID = -4923731241679521330L;
	private Logger logger;

	@PersistenceContext
	EntityManager entityManager;

//  @PersistenceContext(unitName = "FinancialSmall")
	@PersistenceContext
	private EntityManager entityManagerSmall;

	@Inject
	LogImporterBean logImporter;

	@Inject
	ConnectorBean manager;
	
	private Session session;

	@PostConstruct
	void postConstruct() {
		this.logger = Logger.getLogger("financialWeb");
		this.session = new Session(this.manager.getManager(), this.entityManager, true);
	}

	@GET
	public Response listAll() {
		return Response.ok("Processed").build();
	}

	@GET
	@Path("loadPosBinDinamicTables")
	public Response loadPosBinDinamicTables() {
		this.logger.log(Level.INFO, ">>>>>>> loadPosBinDinamicTables <<<<<<<<<<");
		this.session.loadPosBinDinamicTables();
		return Response.ok("Processed").build();
	}

	@GET
	@Path("loadTables")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String loadTables(@QueryParam("useHct") Boolean useHct) {
		this.logger.log(Level.INFO, ">>>>>>> loadTables <<<<<<<<<<");
		this.manager.getManager().stop();
		this.manager.getManager().setEntityManager(this.entityManager);
		this.manager.getManager().start();
		String tablesLogs = this.session.loadTefTables(useHct);
//		String commLogs = this.manager.getManager().popLogs();
//		this.logger.log(Level.INFO, logs);
		return tablesLogs;
	}

	@GET
	@Path("logOnOff")
	public Response logOnOff(@QueryParam("state") Boolean state) {
		this.logger.log(Level.INFO, ">>>>>>> logOnOff <<<<<<<<<<");
		this.session.logon();
		return Response.ok("Processed").build();
	}

	@GET
  @Produces("application/json")
	public List<ISO8583TefProductWithoutCard> getProductsWithoutCards() {
		this.logger.log(Level.INFO, ">>>>>>> getProductsWithoutCards <<<<<<<<<<");
		List<ISO8583TefProductWithoutCard> products = CardConf.getProductsWithoutCard(this.entityManager);
		return products;
	}

	@GET
  @Produces("application/json")
	@Path("getProducts")
	public List<ISO8583TefProduct> getProducts(@QueryParam("binAndEmitter") String binAndEmitter) {
		this.logger.log(Level.INFO, ">>>>>>> getProducts <<<<<<<<<<");
		List<ISO8583TefProduct> products = CardConf.getProducts(this.entityManager, binAndEmitter);
		return products;
	}

	@GET
  @Produces("application/json")
	@Path("getBinCfg")
	public ISO8583Bin getBinCfg(
			@QueryParam("binAndEmitter") String binAndEmitter,
			@QueryParam("productId") int productId
			) {
		this.logger.log(Level.INFO, ">>>>>>> getBinCfg <<<<<<<<<<");
		ISO8583Bin bin = CardConf.getBin(this.entityManager, binAndEmitter, productId);
		return bin;
	}

  @GET
  @Produces("application/json")
  @Path("getFlows")
  public List<ISO8583TefFlow> getFlows(@QueryParam("binId") int binId,	@QueryParam("productId") int productId)
  {
	this.logger.log(Level.INFO, ">>>>>>> getFlows <<<<<<<<<<");
  	List<ISO8583TefFlow> flows = CardConf.getTefFlows(this.entityManager, binId, productId);
  	return flows;
  }


	@GET
	@Path("getConnectorState")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConnectorState() {
		this.logger.log(Level.INFO, String.format(">>>>>>> getConnectorState "));
		boolean state = !(this.manager.getManager().getIsStopped()); 
		return Response.ok(String.format("{\"state\": %s}", state)).build();
	}

	@POST
	@Path("setConnectorState")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String setConnectorState(Boolean state) {
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
	
	@GET
	@Path("getEmulatorState")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmulatorState() {
		this.logger.log(Level.INFO, String.format(">>>>>>> getEmulatorState "));
		
		try {
			boolean isRunning = this.manager.getManager().getInfoModule("Emulator").isRunning();
			return Response.ok(isRunning).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("setEmulatorState")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String setEmulatorState(Boolean state) {
		this.logger.log(Level.INFO, String.format(">>>>>>> setEmulatorState "));
		String ret = "";
		
		try {
			boolean isStopped = ! this.manager.getManager().getInfoModule("Emulator").isRunning();
			
			if (state == null || state == false) {
				if (isStopped == false) {
					this.manager.getManager().stopModule("Emulator");
				}
			} else {
				if (isStopped == true) {
					setConnectorState(true);
					this.manager.getManager().setEntityManager(this.entityManagerSmall);
					this.manager.getManager().startModule("Emulator");
				}
			}
		} catch (Exception e) {
			ret = e.getMessage();
			e.printStackTrace();
		}
		
		return ret;
	}

	@POST
	@Path("unitTest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String unitTest(int timeout) {
		try {
			this.logger.log(Level.INFO, String.format(">>>>>>> unitTest "));
			// força o estado do Emulador para "down"
			this.manager.getManager().stopModule("Emulator");
			// parseia todos os logs da pasta "tests" para o banco de dados temporário
			this.logImporter.importFiles(false, true, false, "tests");
			// depois liga o connector
			setConnectorState(true);
			// dispara o Emulator
			this.manager.getManager().setEntityManager(this.entityManagerSmall);
			this.manager.getManager().startModule("Emulator");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@POST
	@Path("request")
	@Consumes(MediaType.APPLICATION_JSON)
  @Produces("application/json")
	public Message request(Message message) {
		this.logger.log(Level.INFO, String.format(">>>>>>> request "));
		this.entityManager.persist((ISO8583RouterTransaction) message);
		setConnectorState(true);
		Message messageIn = Session.execute(this.manager.getManager(), message);
		return messageIn;
	}

}
