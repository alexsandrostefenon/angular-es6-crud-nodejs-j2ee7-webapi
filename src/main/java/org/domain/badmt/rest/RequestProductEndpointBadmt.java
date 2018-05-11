package org.domain.badmt.rest;


import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.domain.crud.admin.RequestFilter;
import org.domain.crud.admin.RequestFilter.LoginResponse;
import org.domain.crud.entity.CompanyIdPK;
import org.domain.erp.entity.Request;
import org.domain.erp.entity.RequestProduct;
import org.domain.erp.entity.RequestState;
import org.domain.erp.entity.Stock;

/**
 * 
 */
@Stateless
@Path("/request_product_badmt")
@TransactionManagement(TransactionManagementType.BEAN)
@Named 
public class RequestProductEndpointBadmt {
	@Resource
	private EJBContext context;	

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;
	
	@Inject
	private RequestFilter webSocket;
	
	/*
	"sumValueIn",
	"sumValueOut"
	"sumValueStock",
	"estimedValue",
	"value",
	"valueWholesale",
	
	"countIn",
	"countOut",
	"stock",
	"reservedIn",
	"reservedOut",
	"estimedIn",
	"estimedOut",

	"stockSerials",
	 */
	
	private static int STOCK_ACTION_NONE = 0;
	private static int STOCK_ACTION_COUNT_IN = 1;
	private static int STOCK_ACTION_COUNT_OUT = 2;
	private static int STOCK_ACTION_RESERVED_IN = 4;
	private static int STOCK_ACTION_RESERVED_OUT = 8;
	private static int STOCK_ACTION_ESTIMED_IN = 16;
	private static int STOCK_ACTION_ESTIMED_OUT = 32;
	
	public static RequestState getRequestState(EntityManager entityManager, int id) {
		TypedQuery<RequestState> query = entityManager.createQuery("FROM RequestState o WHERE o.id = :id", RequestState.class);
		query.setParameter("id", id);
		RequestState o = query.getSingleResult();
		return o;
	}
	
	private static Stock getStock(EntityManager entityManager, int id, int company) {
		Stock stock;
		
		try {
			stock = entityManager.find(Stock.class, new CompanyIdPK(company, id));
		} catch (NoResultException e) {
			synchronized (Stock.class) {
				stock = new Stock();
				stock.setCompany(company);
				stock.setId(id);
				entityManager.persist(stock);
			}
		}
		
		return stock;
	}
	
	private static BigDecimal getNewValue(BigDecimal value, BigDecimal quantity, boolean isAdd) {
		if (isAdd == true) {
			value.add(quantity);
		} else {
			value.subtract(quantity);
		}
		
		return value;
	}
	
	public static void stockProcess(EntityManager entityManager, RequestProduct entity, Request request, RequestState state, boolean isAdd, RequestFilter webSocket) {
/*
		this.stockSerials = "";
		this.sumValueOut = new BigDecimal(0.0);
		this.sumValueStock = new BigDecimal(0.0);
 */
		Stock stock = getStock(entityManager, entity.getProduct(), entity.getCompany());
		Integer action = state.getStockAction();
		BigDecimal quantity = entity.getQuantity();
		BigDecimal value = entity.getValue();
		boolean change = true;
		
		if (action == null) {
			action = STOCK_ACTION_NONE;
		}
		
		if (action == STOCK_ACTION_COUNT_IN) {
			stock.setCountIn(getNewValue(stock.getCountIn(), quantity, isAdd));
			stock.setSumValueIn(getNewValue(stock.getSumValueIn(), quantity.multiply(value), isAdd));
			stock.setEstimedValue(value);
			stock.setStock(stock.getCountIn().subtract(stock.getCountOut()));
		} else if (action == STOCK_ACTION_COUNT_OUT) {
			stock.setCountOut(getNewValue(stock.getCountOut(), quantity, isAdd));
			stock.setStock(stock.getCountIn().subtract(stock.getCountOut()));
		} else if (action == STOCK_ACTION_RESERVED_IN) {
			stock.setReservedIn(getNewValue(stock.getReservedIn(), quantity, isAdd));
		} else if (action == STOCK_ACTION_RESERVED_OUT) {
			stock.setReservedOut(getNewValue(stock.getReservedOut(), quantity, isAdd));
		} else if (action == STOCK_ACTION_ESTIMED_IN) {
			stock.setEstimedIn(getNewValue(stock.getEstimedIn(), quantity, isAdd));
		} else if (action == STOCK_ACTION_ESTIMED_OUT) {
			stock.setEstimedOut(getNewValue(stock.getEstimedOut(), quantity, isAdd));
		} else {
			change = false;
		}
		
		if (action == STOCK_ACTION_COUNT_IN || action == STOCK_ACTION_RESERVED_IN) {
			BigDecimal valueSale = value.multiply(stock.getMarginSale());
			BigDecimal valueWholesale = value.multiply(stock.getMarginWholesale());
			
			if (valueSale.doubleValue() > stock.getValue().doubleValue()) {
				stock.setValue(valueSale);
			}
			
			if (valueWholesale.doubleValue() > stock.getValueWholesale().doubleValue()) {
				stock.setValueWholesale(valueWholesale);
			}
		}
		
		if (action == STOCK_ACTION_COUNT_OUT || action == STOCK_ACTION_RESERVED_OUT || action == STOCK_ACTION_ESTIMED_OUT) {
			BigDecimal stockIn = stock.getCountIn().add(stock.getReservedIn()).add(stock.getEstimedIn());
			BigDecimal stockOut = stock.getCountOut().add(stock.getReservedOut()).add(stock.getEstimedOut());
			BigDecimal stockEstimed = stockIn.subtract(stockOut);
			
			if (stockEstimed.doubleValue() < stock.getStockMinimal().doubleValue()) {
//				BigDecimal quantityToBuy = stock.getStockDefault().subtract(stockEstimed);
				// TODO : criar ordem de compra
			}
		}
		
		if (change == true) {
			entityManager.persist(stock);
			webSocket.notify(stock, false);
		}
	}

	private void stockProcess(RequestProduct entity, boolean isAdd) {
		Request request = this.entityManager.find(Request.class, new CompanyIdPK(entity.getCompany(), entity.getRequest()));
		RequestState state = getRequestState(this.entityManager, request.getState());
		stockProcess(this.entityManager, entity, request, state, isAdd, this.webSocket);		
	}
	
	@POST
	@Path("/create")
	@Consumes("application/json")
	public void create(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity, @Suspended AsyncResponse ar) {
		RequestFilter.processCreate(securityContext.getUserPrincipal(), this.context.getUserTransaction(), this.entityManager, this.webSocket, entity)
		.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
		.thenAccept(response -> {
			ar.resume(response);
		
		if (response.getStatusInfo() == Response.Status.OK) {
			stockProcess(entity, true);
		}
		});
	}

	@PUT
	@Path("/update")
	@Consumes("application/json")
	public void update(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity, @Suspended AsyncResponse ar) {
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class)
		.thenAccept(old -> {
			RequestFilter.processUpdate((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.context.getUserTransaction(), entityManager, webSocket, entity)
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);
		
		if (response.getStatusInfo() == Response.Status.OK) {
			stockProcess(old, false);
			stockProcess(entity, true);
		}
			});
		})
		.exceptionally(error -> {
			ar.resume(Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build());
			return null;
		});
	}

	@DELETE
	@Path("/delete")
	public void remove(@Context SecurityContext securityContext, @Context UriInfo uriInfo, @Suspended AsyncResponse ar) {
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class)
		.thenAccept(obj -> {
			RequestFilter.processDelete((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, this.webSocket, RequestProduct.class)
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);
		
				if (response.getStatusInfo() == Response.Status.OK) {
					stockProcess(obj, false);
				}
			});
		})
		.exceptionally(error -> {
			ar.resume(Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build());
			return null;
		});
	}
	
}
