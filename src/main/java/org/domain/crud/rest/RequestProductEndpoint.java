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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.domain.crud.admin.RequestFilter;
import org.domain.crud.admin.WebSocket;
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
	private WebSocket webSocket;

	public static RequestState getRequestState(EntityManager entityManager, int id) {
		TypedQuery<RequestState> query = entityManager.createQuery("FROM RequestState o WHERE o.id = :id", RequestState.class);
		query.setParameter("id", id);
		RequestState o = query.getSingleResult();
		return o;
	}

	@POST
	@Path("/create")
	@Consumes("application/json")
	public Response create(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity) {
		Response response = RequestFilter.processCreate(securityContext.getUserPrincipal(), entityManager, webSocket, entity);

		if (response.getStatusInfo() == Response.Status.OK) {
		}

		return response;
	}

	@PUT
	@Path("/update")
	@Consumes("application/json")
	public Response update(@Context SecurityContext securityContext, @Context UriInfo uriInfo, RequestProduct entity) {
		RequestProduct old = (RequestProduct) RequestFilter.getObject(securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class);
		Response response = RequestFilter.processUpdate(securityContext.getUserPrincipal(), uriInfo, entityManager, webSocket, entity);

		if (response.getStatusInfo() == Response.Status.OK) {
		}

		return response;
	}

	@DELETE
	@Path("/delete")
	public Response remove(@Context SecurityContext securityContext, @Context UriInfo uriInfo) {
		RequestProduct old = (RequestProduct) RequestFilter.getObject(securityContext.getUserPrincipal(), uriInfo, this.entityManager, RequestProduct.class);
		Response response = RequestFilter.processDelete(securityContext.getUserPrincipal(), uriInfo, entityManager, webSocket, RequestProduct.class);

		if (response.getStatusInfo() == Response.Status.OK) {
		}

		return response;
	}

}
