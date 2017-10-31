package org.domain.crud.admin;

import java.io.InputStream;
import java.io.StringReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.domain.crud.entity.CategoryCompany;
import org.domain.crud.entity.CrudCompany;
import org.domain.crud.entity.CrudService;
import org.domain.crud.entity.CrudUser;

@Provider
@PreMatching
@Transactional
public class RequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private final static Logger log = Logger.getLogger("CRUD");

	private static Map<String, LoginResponse> logins = new HashMap<String, LoginResponse>(1024);

	@Context
	HttpServletRequest httpRequest;

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;

	@Inject
	private WebSocket webSocket;

	static private CrudCompany getCompany(EntityManager entityManager, Integer companyId) {
		TypedQuery<CrudCompany> query = entityManager
				.createQuery("FROM CrudCompany o WHERE o.id = :company", CrudCompany.class);
		query.setParameter("company", companyId);
		CrudCompany company = query.getSingleResult();
		return company;
	}

	static private CategoryCompany checkCategory(EntityManager entityManager, int company, int category) {
		TypedQuery<CategoryCompany> query = entityManager
				.createQuery("FROM CategoryCompany o WHERE o.company = :company and o.category = :category", CategoryCompany.class);
		query.setParameter("company", company);
		query.setParameter("category", category);
		CategoryCompany ret = query.getSingleResult();
		return ret;
	}

	public class LoginResponse implements java.security.Principal {
		private CrudUser user;
		private String title;
		private Map<String, CrudService> mapServices = new HashMap<String, CrudService>(100);
		private List<CrudService> crudServices;
		private List<String> websocketServices;

		private LoginResponse(CrudUser user) {
			this.setUser(user);
			CrudCompany company = RequestFilter.getCompany(entityManager, user.getCompany());
			this.setTitle(company.getName() + " - " + user.getName());
			// TODO : código temporário para caber o na tela do celular
			this.setTitle(user.getName());
			this.websocketServices = new ArrayList<String>(256);
			StringBuilder services = new StringBuilder(1024);

			{
				JsonReader jsonReader = Json.createReader(new StringReader(user.getRoles()));
				JsonObject json = jsonReader.readObject();

				for (String key : json.keySet()) {
					services.append('\'');
					services.append(key);
					services.append('\'');
					services.append(',');

					JsonObject jsonAccess = json.getJsonObject(key);
					String fieldValue = jsonAccess.getString("read", "true");
					// verfica a permissao de aviso de alterações via websocket
					if (fieldValue.equals("true")) {
						this.websocketServices.add(key);
					}
				}

				if (services.length() > 0) {
					services.setLength(services.length() - 1);
				}
			}

			String sql = String.format("SELECT o FROM CrudService o WHERE o.name in (%s) ORDER BY o.id", services);
			TypedQuery<CrudService> query = entityManager.createQuery(sql, CrudService.class);
			this.crudServices = query.getResultList();

			for (CrudService crudService : this.crudServices) {
				this.mapServices.put(Utils.convertCaseUnderscoreToCamel(crudService.getName(), true), crudService);
			}
		}

		public CrudUser getUser() {
			return user;
		}

		public void setUser(CrudUser user) {
			this.user = user;
		}

		public List<String> getWebsocketServices() {
			return websocketServices;
		}

		public void setWebsocketServices(List<String> websocketServices) {
			this.websocketServices = websocketServices;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public String getName() {
			return this.user.getName();
		}

		public Map<String, CrudService> getMapServices() {
			return mapServices;
		}

		public List<CrudService> getCrudServices() {
			return crudServices;
		}
	}

	public class ImplSecurityContext implements SecurityContext {

	    private LoginResponse loginResponse;

	    public ImplSecurityContext(LoginResponse loginResponse) {
	    	this.loginResponse = loginResponse;
	    }

	    /**
	     * User entity implements Principal
	     * @return user
	     */
	    @Override
	    public Principal getUserPrincipal() {
	        return this.loginResponse;
	    }

	    @Override
	    public boolean isUserInRole(String role) {
	        return false;
	    }

	    @Override
	    public boolean isSecure() {
	        return false;
	    }

	    @Override
	    public String getAuthenticationScheme() {
	        return SecurityContext.BASIC_AUTH;
	    }

	}

	private Response authenticateByUserAndPassword(ContainerRequestContext requestCtx, String ip) {
		Response response;
		JsonReader jsonReader = Json.createReader(requestCtx.getEntityStream());
		JsonObject json = jsonReader.readObject();
		String userId = json.getString("userId");
		String password = json.getString("password");

		if (userId != null && userId.length() > 0 && password != null && password.length() >= 4) {
			TypedQuery<CrudUser> query = entityManager.createQuery(
					"SELECT u FROM CrudUser u WHERE u.name = :name and u.password = :password", CrudUser.class);
			query.setParameter("name", userId);
			query.setParameter("password", password);
			CrudUser user = null;

			try {
				user = query.getSingleResult();
			} catch (NoResultException nre) {
				log.warning("mismatched user and password : " + userId);
			} catch (Exception e) {
				log.warning("error in user and password matching : " + e.getMessage());
				e.printStackTrace();
			}

			if (user != null) {
				// console.log("[INFO] Token is :" + response.authctoken);
				String token = UUID.randomUUID().toString();
				user.setAuthctoken(token);
				user.setIp(ip);
				this.entityManager.merge(user);
				LoginResponse loginResponse = new LoginResponse(user);
				RequestFilter.logins.put(token, loginResponse);
				response = Response.ok(loginResponse, MediaType.APPLICATION_JSON_TYPE).build();
				String msg = String.format(
						"[authenticateByUserAndPassword] Sucessful login : user = %s, roles = %s, token = %s",
						user.getName(), user.getRoles(), user.getAuthctoken());
				log.info(msg);
			} else {
				response = Response.status(Response.Status.UNAUTHORIZED).entity("mismatched user and password").build();
			}
		} else {
			response = Response.status(Response.Status.UNAUTHORIZED).entity("invalid user or password data").build();
			log.warning("invalid user or password data");
		}

		return response;
	}

	private static String authorization(CrudUser user, String resource, String access) {
		String msgErr;
		String roles = user.getRoles();

		if (roles != null) {
			JsonReader jsonReader = Json.createReader(new StringReader(user.getRoles()));
			JsonObject json = jsonReader.readObject();
			String role = Utils.convertCaseUnderscoreToCamel(resource, false);

			if (Utils.findInSet(json.keySet(), role) == true) {
				JsonObject jsonAccess = json.getJsonObject(role);
				String fieldName = access;
				String fieldValue = jsonAccess.getString(fieldName, "true");
				// verfica a permissao de acesso
				if (fieldValue.equals("true")) {
					msgErr = null;
					String msg = String.format(
							"[authorization] Sucessful authorization : path: = %s, user = %s, roles = %s, token = %s",
							resource, user.getName(), user.getRoles(), user.getAuthctoken());
					log.info(msg);
				} else {
					msgErr = "unauthorized access";
				}
			} else {
				msgErr = "unauthorized resource";
			}
		} else {
			msgErr = "report to admin check";
		}

		return msgErr;
	}

	private Response processRequest(ContainerRequestContext requestContext, String ip, String authorization, String resource, String access) {
		Response response;

		if (authorization != null && authorization.startsWith("Token ")) {
			String token = authorization.substring(6);
			LoginResponse login = RequestFilter.getLogin(token);

			if (login != null) {
				String msgErr = RequestFilter.authorization(login.getUser(), resource, access);

				if (msgErr == null) {
					try {
						SecurityContext securityContextOld = requestContext.getSecurityContext();

						if (securityContextOld == null || securityContextOld.getUserPrincipal() == null) {
							ImplSecurityContext securityContext = new ImplSecurityContext(login);
							requestContext.setSecurityContext(securityContext);
							response = crudProcess(requestContext, resource, access);
						} else {
							response = Response.status(Response.Status.BAD_REQUEST).entity("SecurityContext").build();
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
						response = Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
					}
				} else {
					response = Response.status(Response.Status.UNAUTHORIZED).entity(msgErr).build();
					log.warning(String.format("user : %s - path : %s - msgErr : %s", login.getUser().getName(), resource, msgErr));
				}
			} else {
				response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization replaced by new login in another session").build();
				log.warning("Authorization replaced by new login in another session");
			}
		} else {
			response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization token header invalid").build();
			log.warning("Authorization token header invalid : " + authorization);
		}

		return response;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		log.info("Filtering REST Response");
		// TODO : habilitar somente os IPs dos servidores instalados nas empresas
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "https://localhost:9443"); // USE * for all
		responseContext.getHeaders().add("Access-Control-Allow-Methods", "PUT,DELETE"); // GET, POST, HEAD, OPTIONS
		responseContext.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
		// responseContext.getHeaders().add("Access-Control-Expose-Headers",
		// "Location, Content-Disposition");
		// responseContext.getHeaders().add("Access-Control-Allow-Credentials",
		// "true" );
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String path = requestContext.getUriInfo().getPath();
		System.out.println(String.format("uri : %s", path));
		System.out.println(String.format("method : %s", requestContext.getMethod()));
		System.out.println(String.format("headers : %s", requestContext.getHeaders()));
		String ip = this.httpRequest.getRemoteAddr();
		System.out.println(String.format("RemoteAddr : %s", ip));

		String method = requestContext.getMethod();
		// When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
		if (method.equals("OPTIONS")) {
			// Just send a OK signal back to the browser
			requestContext.abortWith(Response.status(Response.Status.OK).build());
			return;
		}

		String[] paths = path.split("/");
		String resource = paths[1];

		// no login pede usuário e senha
		if (resource.equals("authc")) {
			requestContext.abortWith(authenticateByUserAndPassword(requestContext, ip));
			// em qualquer outro método pede o token
		} else {
			// headers['Authorization'] = 'Token ' + token;
			String authorization = requestContext.getHeaderString("Authorization");
			log.info("authorization header : " + authorization);
			String access = path.substring(path.indexOf(resource)+resource.length()+1);
			Response response = processRequest(requestContext, ip, authorization, resource, access);

			if (response != null) {
				requestContext.abortWith(response);
			}
		}
	}

	static private Response checkObjectAccess(Principal userPrincipal, EntityManager entityManager, Object obj) {
		Response response = null;
		LoginResponse login = (LoginResponse) userPrincipal;
		Integer userCompany = login.getUser().getCompany();

		if (userCompany > 1 && Utils.haveField(obj.getClass(), "company")) {
			Integer company = (Integer) Utils.readField(obj, "company");

			if (company == null) {
				Utils.writeField(obj, "company", userCompany);
				company = userCompany;
			}

			if (userCompany == company) {
				if (Utils.haveField(obj.getClass(), "category") && obj.getClass().getSimpleName().equals("CategoryCompany") == false) {
					Integer category = (Integer) Utils.readField(obj, "category");

					if (RequestFilter.checkCategory(entityManager, company, category) == null) {
						return Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized object category").build();
					}
				}
			} else {
				return Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized object company").build();
			}
		}

		return response;
	}

	static public Response processCreate(Principal user, EntityManager entityManager, WebSocket webSocket, Object obj) {
		Response response = null;
		response = checkObjectAccess(user, entityManager, obj);

		if (response == null) {
			Integer id = null;

			synchronized (obj.getClass()) {
				if (Utils.haveField(obj.getClass(), "id")) {
					Integer company = null;

					if (Utils.haveField(obj.getClass(), "company")) {
						company = (Integer) Utils.readField(obj, "company");
					}

					String className = obj.getClass().getSimpleName();
					String tableName = className.substring(className.lastIndexOf('.') + 1);
					Query query;

					if (company != null) {
						query = entityManager.createQuery("SELECT max(id) FROM " + tableName + " WHERE company = " + company);
					} else {
						query = entityManager.createQuery("SELECT max(id) FROM " + tableName);
					}

					try {
						id = (Integer) query.getSingleResult();

						if (id == null) {
							id = 0;
						}
					} catch (NoResultException nre) {
						id = 0;
					}

					Utils.writeField(obj, "id", ++id);
				}

				entityManager.persist(obj);
			}

			response = Response.ok(obj, MediaType.APPLICATION_JSON).build();
			webSocket.notify(obj, id, false);
		}

		return response;
	}

	static private TypedQuery<?> buildQuery(Principal userPrincipal, UriInfo uriInfo, EntityManager entityManager, Class<?> entityClass) {
		MultivaluedMap<String, String> queryParam = uriInfo.getQueryParameters();
		Integer company = null;
		Integer id = null;

		if (Utils.haveField(entityClass, "company")) {
			LoginResponse login = (LoginResponse) userPrincipal;
			company = login.getUser().getCompany();

			if (company == 1) {
				company = Utils.parseInt(queryParam.getFirst("company"));
			}
		}

		if (Utils.haveField(entityClass, "id")) {
			id = Utils.parseInt(queryParam.getFirst("id"));
		}

		String sql = "from " + entityClass.getName() + " o where ";
		// company
		boolean haveCompany = Utils.haveField(entityClass, "company") == true && company != null;
		if (haveCompany) {
			sql = sql + "o.company = :company and ";
		}
/*
		// category
		Integer category = (Integer) Utils.readField(obj, "category");

		if (category != null) {
			sql = sql + "o.category = :category and ";
		}
*/
		// id
		if (id != null) {
			sql = sql + "o.id = :id and ";
		}

		if (sql.endsWith(" and ")) {
			sql = sql.substring(0, sql.length() - 5);
		}

		if (haveCompany && id != null) {
			sql = sql + " order by o.company,o.id";
		} else if (id != null) {
			sql = sql + " order by o.id";
		}

		TypedQuery<?> query = entityManager.createQuery(sql, entityClass);

		if (haveCompany) {
			query.setParameter("company", company);
		}
/*
		if (category != null) {
			query.setParameter("category", category);
		}
*/
		if (id != null) {
			query.setParameter("id", id);
		}

		return query;
	}

	static public Object getObject(Principal user, UriInfo uriInfo, EntityManager entityManager, Class<?> objectClass) {
		TypedQuery<?> query = RequestFilter.buildQuery(user, uriInfo, entityManager, objectClass);
		Object obj = query.getSingleResult();
		return obj;
	}

	static public Response processUpdate(Principal user, UriInfo uriInfo, EntityManager entityManager, WebSocket webSocket, Object obj) {
		Object oldObj = RequestFilter.getObject(user, uriInfo, entityManager, obj.getClass());
		Response response = checkObjectAccess(user, entityManager, oldObj);

		if (response == null) {
			response = checkObjectAccess(user, entityManager, obj);

			if (response == null) {
				Integer oldId = (Integer) Utils.readField(oldObj, "id");
				Integer newId = (Integer) Utils.readField(obj, "id");

				if (newId.intValue() == oldId.intValue()) {
					entityManager.merge(obj);
					response = Response.ok(obj, MediaType.APPLICATION_JSON).build();
					webSocket.notify(obj, oldId, false);
				} else {
					response = Response.status(Response.Status.UNAUTHORIZED).entity("changed id").build();
				}
			}
		}

		return response;
	}

	static public Response processRead(Principal user, UriInfo uriInfo, EntityManager entityManager, Class<?> objectClass) {
		Object obj = RequestFilter.getObject(user, uriInfo, entityManager, objectClass);
		Response response = checkObjectAccess(user, entityManager, obj);

		if (response == null) {
			response = Response.ok(obj, MediaType.APPLICATION_JSON).build();
		}

		return response;
	}

	static public Response processDelete(Principal user, UriInfo uriInfo, EntityManager entityManager, WebSocket webSocket, Class<?> objectClass) {
		Object obj = RequestFilter.getObject(user, uriInfo, entityManager, objectClass);
		Response response = checkObjectAccess(user, entityManager, obj);

		if (response == null) {
			entityManager.remove(obj);
			response = Response.ok().build();
			Integer id = (Integer) Utils.readField(obj, "id");
			webSocket.notify(obj, id, true);
		}

		return response;
	}

	static public Response processQuery(SecurityContext context, EntityManager entityManager, Class<?> entityClass, String fieldQuery, Integer valueQuery, Integer startPosition, Integer maxResult) {
		String serviceName = entityClass.getSimpleName();
		String sql = String.format("from %s o", serviceName);
		boolean haveWhere = false;

		LoginResponse login = (LoginResponse) context.getUserPrincipal();
		CrudService service = login.getMapServices().get(serviceName);

		if (login.getUser().getCompany() > 1 && Utils.haveField(entityClass, "company")) {
			sql = sql + String.format(" where o.company = %s", login.getUser().getCompany());
			haveWhere = true;
		}

		if (Utils.haveField(entityClass, "category")) {
			if (haveWhere) {
				sql = sql + " and ";
			} else {
				sql = sql + " where ";
			}

			haveWhere = true;
			sql = sql + String.format("o.category in (select p.category from CategoryCompany p where p.company = %s)", login.getUser().getCompany());
		}

		if (fieldQuery != null && valueQuery != null) {
			if (haveWhere) {
				sql = sql + " and ";
			} else {
				sql = sql + " where ";
			}

			haveWhere = true;
			sql = sql + String.format("o.%s = %s", fieldQuery, valueQuery);
		}

		if (service.getOrderBy() != null) {
			sql = sql + " ORDER BY " + service.getOrderBy();
		} else {
			sql = sql + " ORDER BY o.id";
		}

		TypedQuery<?> findAllQuery = entityManager.createQuery(sql, entityClass);

		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}

		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}

		Response response;

		if (fieldQuery != null && fieldQuery == "id") {
			final Object result = findAllQuery.getSingleResult();
			response = Response.ok(result, MediaType.APPLICATION_JSON).build();
		} else {
			final List<?> results = findAllQuery.getResultList();
			response = Response.ok(results, MediaType.APPLICATION_JSON).build();
		}

		return response;
	}

	private Response crudProcess(ContainerRequestContext requestContext, String resource, String acess) throws Exception {
		String className = Utils.convertCaseUnderscoreToCamel(resource, true);
		Class<?> restClass;
		String domain = this.getClass().getName();
		domain = domain.substring(0, domain.lastIndexOf(".admin"));

		try {
			restClass = Class.forName(domain + ".rest." + className + "Endpoint");
		} catch (ClassNotFoundException e) {
			restClass = null;
		}

		if (acess.equals("create") && Utils.haveMethodName(restClass, "create")) {
			return null;
		} else if (acess.equals("read") && Utils.haveMethodName(restClass, "read")) {
			return null;
		} else if (acess.equals("query") && Utils.haveMethodName(restClass, "query")) {
			return null;
		} else if (acess.equals("delete") && Utils.haveMethodName(restClass, "remove")) {
			return null;
		} else if (acess.equals("update") && Utils.haveMethodName(restClass, "update")) {
			return null;
		}

		Class<?> objectClass;

		try {
			objectClass = Class.forName(domain + ".entity." + className);
		} catch (ClassNotFoundException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		Response response = null;
		SecurityContext securiryContext = requestContext.getSecurityContext();
		Principal user = securiryContext.getUserPrincipal();
		UriInfo uriInfo = requestContext.getUriInfo();

		if (acess.equals("create")) {
			InputStream inputStream = requestContext.getEntityStream();
			Object obj = Utils.loadObjectFromJson(objectClass, inputStream);
			response = processCreate(user, this.entityManager, this.webSocket, obj);
		} else if (acess.equals("update")) {
			InputStream inputStream = requestContext.getEntityStream();
			Object obj = Utils.loadObjectFromJson(objectClass, inputStream);
			response = processUpdate(user, uriInfo, this.entityManager, this.webSocket, obj);
		} else if (acess.equals("delete")) {
			response = processDelete(user, uriInfo, this.entityManager, this.webSocket, objectClass);
		} else if (acess.equals("read")) {
			response = processRead(user, uriInfo, this.entityManager, objectClass);
		} else if (acess.equals("query")) {
			MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
			String fieldQuery = queryParam.getFirst("fieldQuery");
			Integer valueQuery = Utils.parseInt(queryParam.getFirst("valueQuery"));
			Integer startPosition = Utils.parseInt(queryParam.getFirst("start"));
			Integer maxResult = Utils.parseInt(queryParam.getFirst("max"));
			response = processQuery(securiryContext, this.entityManager, objectClass, fieldQuery, valueQuery, startPosition, maxResult);
		}

		return response;
	}

	public static LoginResponse getLogin(String token) {
		// TODO Auto-generated method stub
		return RequestFilter.logins.get(token);
	}

}
