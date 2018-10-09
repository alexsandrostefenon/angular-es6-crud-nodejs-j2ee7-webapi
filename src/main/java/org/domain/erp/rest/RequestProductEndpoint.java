package org.domain.erp.rest;


import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Named;
import javax.persistence.EntityManager;
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
import org.domain.erp.entity.RequestProduct;
import org.domain.erp.entity.RequestState;

/**
 *
 */
@Stateless
@Path("/request_product")
@TransactionManagement(TransactionManagementType.BEAN)
@Named 
public class RequestProductEndpoint {
	@Resource
	private EJBContext context;	

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;

	public static RequestState getRequestState(EntityManager entityManager, int id) {
		TypedQuery<RequestState> query = entityManager.createQuery("FROM RequestState o WHERE o.id = :id", RequestState.class);
		query.setParameter("id", id);
		RequestState o = query.getSingleResult();
		return o;
	}

	@POST
	@Path("/create")
	@Consumes("application/json")
	public void create(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity, @Suspended AsyncResponse ar) {
		RequestFilter.processCreate(securityContext.getUserPrincipal(), this.context.getUserTransaction(), this.entityManager, "requestProduct", entity)
		.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
		.thenAccept(response -> {
			ar.resume(response);

			if (response.getStatusInfo() == Response.Status.OK) {
			}
		});
	}

	@PUT
	@Path("/update")
	@Consumes("application/json")
	public void update(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity, @Suspended AsyncResponse ar) {
		RequestFilter.<RequestProduct>getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, "requestProduct")
		.thenAccept(old -> {
			RequestFilter.processUpdate(securityContext.getUserPrincipal(), uriInfo, this.context.getUserTransaction(), entityManager, "requestProduct", entity)
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);

				if (response.getStatusInfo() == Response.Status.OK) {
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
		RequestFilter.<RequestProduct>getObject(securityContext.getUserPrincipal(), uriInfo, this.entityManager, "requestProduct")
		.thenAccept(obj -> {
			RequestFilter.processDelete(securityContext.getUserPrincipal(), uriInfo, this.context.getUserTransaction(), this.entityManager, "requestProduct")
			.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build())
			.thenAccept(response -> {
				ar.resume(response);

				if (response.getStatusInfo() == Response.Status.OK) {
				}
			});
		})
		.exceptionally(error -> {
			ar.resume(Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build());
			return null;
		});
	}

}
