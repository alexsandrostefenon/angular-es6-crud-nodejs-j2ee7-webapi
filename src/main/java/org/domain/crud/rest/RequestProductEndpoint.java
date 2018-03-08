package org.domain.crud.rest;


import javax.ejb.Stateless;
import javax.inject.Inject;
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
import org.domain.crud.entity.Request;
import org.domain.crud.entity.RequestProduct;
import org.domain.crud.entity.RequestState;

/**
 *
 */
@Stateless
@Path("/request_product")
public class RequestProductEndpoint {
	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;

	@Inject
	private RequestFilter webSocket;

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
		RequestFilter.processCreate(securityContext.getUserPrincipal(), this.entityManager, this.webSocket, entity)
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
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class)
		.thenAccept(old -> {
			RequestFilter.processUpdate((LoginResponse) securityContext.getUserPrincipal(), uriInfo, entityManager, webSocket, entity)
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
		RequestFilter.getObject((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class)
		.thenAccept(obj -> {
			RequestFilter.processDelete((LoginResponse) securityContext.getUserPrincipal(), uriInfo, this.entityManager, this.webSocket, RequestProduct.class)
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
