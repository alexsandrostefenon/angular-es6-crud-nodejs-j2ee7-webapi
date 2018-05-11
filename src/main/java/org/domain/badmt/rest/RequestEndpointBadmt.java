package org.domain.badmt.rest;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.domain.crud.admin.RequestFilter;
import org.domain.crud.admin.RequestFilter.LoginResponse;
import org.domain.erp.entity.Request;
import org.domain.erp.entity.RequestProduct;
import org.domain.erp.entity.RequestState;

/**
 * 
 */
@Stateless
@Path("/request_badmt")
@TransactionManagement(TransactionManagementType.BEAN)
@Named 
public class RequestEndpointBadmt {
	@Resource
	private EJBContext context;	

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;
	
	@Inject
	private RequestFilter webSocket;

	/*
		The @Context annotation allows you to inject instances of 
		javax.ws.rs.core.HttpHeaders, 
		javax.ws.rs.core.UriInfo, 
		javax.ws.rs.core.Request, 
		javax.servlet.SecurityContext, 
		javax.servlet.HttpServletResponse, 
		javax.servlet.ServletConfig, 
		javax.servlet.ServletContext, 
		javax.ws.rs.core.SecurityContext 
	 */
	@POST
	@Path("/create")
	@Consumes("application/json")
	public void create(@Context SecurityContext securityContext, @Context UriInfo uriInfo, Request obj, @Suspended AsyncResponse ar) {
		// TODO : validar se entity.getState() é um State com antecessor vazio.
		if (obj.getDate() == null) {
			obj.setDate(new Timestamp(System.currentTimeMillis()));
		}
		
		RequestFilter.processCreate(securityContext.getUserPrincipal(), this.context.getUserTransaction(), this.entityManager, this.webSocket, obj)
		.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
		.thenAccept(response -> ar.resume(response));
	}

	@PUT
	@Path("/update")
	@Consumes("application/json")
	public void update(@Context SecurityContext securityContext, @Context UriInfo uriInfo, Request newObj, @Suspended AsyncResponse ar) {
		// TODO : validar se entity.getState() é um State com antecessor e precedente validos.
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, Request.class)
		.thenAccept(oldObj -> {
			RequestFilter.processUpdate((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.context.getUserTransaction(), this.entityManager, this.webSocket, newObj)
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);
		
				if (response.getStatusInfo() == Response.Status.OK) {
					RequestState stateOld = RequestProductEndpointBadmt.getRequestState(this.entityManager, oldObj.getState());
					RequestState state = RequestProductEndpointBadmt.getRequestState(this.entityManager, newObj.getState());
					TypedQuery<RequestProduct> query = this.entityManager.createQuery("from RequestProduct o where o.company = :company and o.request = :request", RequestProduct.class);
					query.setParameter("company", newObj.getCompany());
					query.setParameter("request", newObj.getId());
					List<RequestProduct> list = query.getResultList();
					
					for (RequestProduct requestProduct : list) {
						RequestProductEndpointBadmt.stockProcess(this.entityManager, requestProduct, oldObj, stateOld, false, this.webSocket);
						RequestProductEndpointBadmt.stockProcess(this.entityManager, requestProduct, newObj, state, true, this.webSocket);
					}
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
		// TODO : validar se entity.getState() é um State de status iniciais que permite exclusão.
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, Request.class)
		.thenAccept(obj -> {
			RequestFilter.processDelete((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, this.webSocket, Request.class)
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);
		
				if (response.getStatusInfo() == Response.Status.OK) {
					RequestState stateOld = RequestProductEndpointBadmt.getRequestState(this.entityManager, obj.getState());
					TypedQuery<RequestProduct> query = this.entityManager.createQuery("from RequestProduct o where o.company = :company and o.request = :request", RequestProduct.class);
					query.setParameter("company", obj.getCompany());
					query.setParameter("request", obj.getId());
					List<RequestProduct> list = query.getResultList();
					
					for (RequestProduct requestProduct : list) {
						RequestProductEndpointBadmt.stockProcess(this.entityManager, requestProduct, obj, stateOld, false, this.webSocket);
					}
				}
			});
		})
		.exceptionally(error -> {
			ar.resume(Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build());
			return null;
		});
	}

	@GET
	@Path("/query")
	@Produces("application/json")
	public void query(@Context SecurityContext context, @Context UriInfo uriInfo, @Suspended AsyncResponse ar) {
		// TODO : verificar as condições para carregar as requests encerradas
		RequestFilter.processQuery((LoginResponse) context.getUserPrincipal(), uriInfo, this.entityManager, Request.class)
		.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
		.thenAccept(response -> {
			ar.resume(response);
		});
	}

}
