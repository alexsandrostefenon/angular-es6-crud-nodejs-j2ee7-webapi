package org.domain.crud.rest;

import java.sql.Timestamp;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.domain.crud.admin.RequestFilter;
import org.domain.crud.admin.WebSocket;
import org.domain.crud.entity.Request;
import org.domain.crud.entity.RequestProduct;
import org.domain.crud.entity.RequestState;

/**
 *
 */
@Stateless
@Path("/request")
public class RequestEndpoint {
	@PersistenceContext(unitName = "primary")
	private EntityManager em;

	@Inject
	private WebSocket webSocket;

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
	public Response create(@Context SecurityContext securityContext, @Context UriInfo uriInfo, Request entity) {
		// TODO : validar se entity.getState() é um State com antecessor vazio.
		if (entity.getDate() == null) {
			entity.setDate(new Timestamp(System.currentTimeMillis()));
		}

		return RequestFilter.processCreate(securityContext.getUserPrincipal(), this.em, this.webSocket, entity);
	}

	@PUT
	@Path("/update")
	@Consumes("application/json")
	public Response update(@Context SecurityContext securityContext, @Context UriInfo uriInfo, Request request) {
		// TODO : validar se entity.getState() é um State com antecessor e precedente validos.
		Request requestOld = (Request) RequestFilter.getObject(securityContext.getUserPrincipal(), uriInfo, this.em, Request.class);
		Response response = RequestFilter.processUpdate(securityContext.getUserPrincipal(), uriInfo, this.em, this.webSocket, request);

		if (response.getStatusInfo() == Response.Status.OK) {
			RequestState stateOld = RequestProductEndpoint.getRequestState(this.em, requestOld.getState());
			RequestState state = RequestProductEndpoint.getRequestState(this.em, request.getState());
			TypedQuery<RequestProduct> query = this.em.createQuery("from RequestProduct o where o.company = :company and o.request = :request", RequestProduct.class);
			query.setParameter("company", request.getCompany());
			query.setParameter("request", request.getId());
			List<RequestProduct> list = query.getResultList();

			for (RequestProduct requestProduct : list) {
			}
		}

		return response;
	}

	@DELETE
	@Path("/delete")
	public Response remove(@Context SecurityContext securityContext, @Context UriInfo uriInfo) {
		// TODO : validar se entity.getState() é um State de status iniciais que permite exclusão.
		Request requestOld = (Request) RequestFilter.getObject(securityContext.getUserPrincipal(), uriInfo, this.em, Request.class);
		Response response = RequestFilter.processDelete(securityContext.getUserPrincipal(), uriInfo, this.em, this.webSocket, Request.class);

		if (response.getStatusInfo() == Response.Status.OK) {
			RequestState stateOld = RequestProductEndpoint.getRequestState(this.em, requestOld.getState());
			TypedQuery<RequestProduct> query = this.em.createQuery("from RequestProduct o where o.company = :company and o.request = :request", RequestProduct.class);
			query.setParameter("company", requestOld.getCompany());
			query.setParameter("request", requestOld.getId());
			List<RequestProduct> list = query.getResultList();

			for (RequestProduct requestProduct : list) {
			}
		}

		return response;
	}

	@GET
	@Path("/query")
	@Produces("application/json")
	public Response query(@Context SecurityContext context, @Context UriInfo uriInfo,
			@QueryParam("fieldQuery") String fieldQuery,
			@QueryParam("valueQuery") Integer valueQuery,
			@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		// TODO : verificar as condições para carregar as requests encerradas
		return RequestFilter.processQuery(context, this.em, Request.class, fieldQuery, valueQuery, startPosition, maxResult);
	}

}
