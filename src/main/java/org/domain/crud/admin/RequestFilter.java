package org.domain.crud.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
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

import org.domain.crud.admin.RequestFilter.QueryMap;
import org.domain.crud.entity.CategoryCompany;
import org.domain.crud.entity.CrudCompany;
import org.domain.crud.entity.CrudService;
import org.domain.crud.entity.CrudUser;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServerEndpoint(value = "/websocket")
@Provider
@PreMatching
@Transactional
public class RequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private final static Logger log = Logger.getLogger("CRUD");

	private static Map<String, LoginResponse> logins = new HashMap<String, LoginResponse>(1024);

	@Context
	HttpServletRequest httpRequest;

	@Resource
	private UserTransaction userTransaction;

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;

	@Inject
	private RequestFilter webSocket;
	// Utils
	private static boolean haveField(Class<?> objectClass, String name) {
		boolean ret = true;

		try {
			objectClass.getDeclaredField(name);
		} catch (NoSuchFieldException e1) {
			ret = false;
		}

		return ret;
	}
	// Utils
	private static boolean haveMethodName(Class<?> objectClass, String name) {
		if (objectClass == null) {
			return false;
		}

		boolean found = false;
		Method[] methods = objectClass.getDeclaredMethods();

		for (Method method : methods) {
			if (method.getName().equals(name)) {
				found = true;
			}
		}

		return found;
	}
	// Utils
	private static Object readField(Object obj, String name) {
		if (obj == null) {
			return null;
		}

		Object data;

		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			data = field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			data = null;
		}

		return data;
	}
	// Utils
	private static void writeField(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Utils
	static class QueryMap extends HashMap<String, Object> {
		/**
		 *
		 */
		private static final long serialVersionUID = 9045195406428349166L;

		private static QueryMap create() {
			return new QueryMap();
		}

		private QueryMap add(String name, Object value) {
			this.put(name, value);
			return this;
		}
	}
	// Utils
	private static boolean findInSet(Set<String> set, String item) {
		boolean found = false;

		for (String key : set) {
			if (key.equals(item)) {
				found = true;
				break;
			}
		}

		return found;
	}
	// Utils
	static String convertCaseUnderscoreToCamel(String str, boolean isFirstUpper) {
		String ret = "";
		boolean nextIsUpper = false;

		if (isFirstUpper == true) {
			nextIsUpper = true;
		} else if (str.length() > 1) {
			char ch = str.charAt(0);
			str = Character.toLowerCase(ch) + str.substring(1);
		}

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);

			if (nextIsUpper == true) {
				ch = Character.toUpperCase(ch);
				nextIsUpper = false;
			}

			if (ch == '_') {
				nextIsUpper = true;
			} else {
				ret = ret + ch;
			}
		}

		return ret;
	}
	// Utils
	private static Integer parseInt(String str) {
		Integer ret = null;

		if (str != null) {
			ret = Integer.parseInt(str);
		}

		return ret;
	}
	// Utils
	private static Object loadObjectFromJson(Class<?> objectClass, InputStream inputStream) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		byte[] buffer = new byte[100*1024];
		int partialRead = 0;
		int totalRead = 0;

		do {
			totalRead += partialRead;
			partialRead = inputStream.read(buffer, totalRead, buffer.length - partialRead);
		} while (partialRead > 0);


		if (totalRead >= buffer.length) {
			throw new Exception("loadObjectFromJson : invalid data size");
		}

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, totalRead);
		inputStream = byteArrayInputStream;
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		Object obj;

		try {
			obj = mapper.readValue(inputStreamReader, objectClass);
		} catch (Exception e) {
			String string = new String(buffer, 0, totalRead);
			throw new Exception(String.format("loadObjectFromJson : %s : %s", e.getMessage(), string));
		}

		return obj;
	}
	// Utils
	private static <T> TypedQuery<T> buildQuery(EntityManager entityManager, Class<T> resultClass, QueryMap fields, String[] orderBy) {
		StringBuilder sql = new StringBuilder(1024);
		sql.append("from " + resultClass.getName() + " o");

		if (fields.isEmpty() == false) {
			sql.append(" where ");

			fields.forEach((name, value) -> {
				if (value instanceof List) {
					sql.append(String.format("o.%s IN (:%s) and ", name, name));
				} else {
					sql.append(String.format("o.%s = :%s and ", name, name));
				}
			});

			sql.setLength(sql.length()-5);
		}

		if (orderBy != null && orderBy.length > 0) {
			sql.append(" order by ");

			for (String field : orderBy) {
				sql.append("o." + RequestFilter.convertCaseUnderscoreToCamel(field, false) + ",");
			}

			sql.setLength(sql.length()-1);
		}

		TypedQuery<T> query = entityManager.createQuery(sql.toString(), resultClass);
		fields.forEach((name, value) -> query.setParameter(name, value));
		return query;
	}
	// Utils
	private static <T> CompletableFuture<List<T>> find(EntityManager entityManager, Class<T> resultClass, QueryMap fields, String[] orderBy, Integer startPosition, Integer maxResult) {
		return CompletableFuture.supplyAsync(() -> {
			TypedQuery<T> query = RequestFilter.buildQuery(entityManager, resultClass, fields, orderBy);

			if (startPosition != null) {
				query.setFirstResult(startPosition);
			}

			if (maxResult != null) {
				query.setMaxResults(maxResult);
			}

			return query.getResultList();
		});
	}
	// Utils
	private static <T> CompletableFuture<T> findOne(EntityManager entityManager, Class<T> resultClass, QueryMap fields) {
		return CompletableFuture.supplyAsync(() -> buildQuery(entityManager, resultClass, fields, null).getSingleResult());
	}
	// Utils
	private static <T> CompletableFuture<T> insert(UserTransaction userTransaction, EntityManager entityManager, T obj) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (userTransaction != null) userTransaction.begin();
				entityManager.persist(obj);
				if (userTransaction != null) userTransaction.commit();
				return obj;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	// Utils
	private static <T> CompletableFuture<T> update(UserTransaction userTransaction, EntityManager entityManager, T obj) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (userTransaction != null) userTransaction.begin();
				entityManager.merge(obj);
				if (userTransaction != null) userTransaction.commit();
				return obj;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	// Utils
	private static <T> CompletableFuture<Void> deleteOne(EntityManager entityManager, T obj) {
		return CompletableFuture.runAsync(() -> {
			entityManager.remove(obj);
		});
	}

	public class LoginResponse implements java.security.Principal {
		private CrudUser user;
		private String title;
		private List<CrudService> crudServices;
		private List<String> websocketServices;
		private List<String> servicesNames;
		private List<Integer> categories;

		public CompletableFuture<Void> load() {
			return RequestFilter.findOne(entityManager, CrudCompany.class, RequestFilter.QueryMap.create().add("id", this.user.getCompany()))
			.exceptionally(error -> {
				throw new RuntimeException("don't get user company : " + error.getMessage());
			})
			.thenCompose(company -> {
				this.setTitle(company.getName() + " - " + user.getName());
				// TODO : código temporário para caber o na tela do celular
				this.setTitle(user.getName());
				return RequestFilter.find(entityManager, CrudService.class, RequestFilter.QueryMap.create().add("name", this.servicesNames), new String[] {"id"}, null, null)
				.exceptionally(error -> {
					throw new RuntimeException("don't get user services : " + error.getMessage());
				})
				.thenCompose(services -> {
					this.crudServices = services;
					// TODO : temporary code, until hibernate persist postgresql jsonb type
					for (CrudService service :this.crudServices) {
						service.setJsonFields(Json.createReader(new StringReader(service.getFields())).readObject());
					}

					return RequestFilter.find(entityManager, CategoryCompany.class, RequestFilter.QueryMap.create().add("company", this.user.getCompany()), null, null, null)
					.exceptionally(error -> {
						throw new RuntimeException("don't match request category for user company : " + error.getMessage());
					})
					.thenAccept(categories -> {
						this.categories = new ArrayList<Integer>(categories.size());

						for (CategoryCompany categoryCompany : categories) {
							this.categories.add(categoryCompany.getId());
						}
					});
				});
			});
		}

		private LoginResponse(CrudUser user) {
			this.user = user;
			this.servicesNames = new ArrayList<String>(256);
			this.websocketServices = new ArrayList<String>(256);
			JsonObject roles = Json.createReader(new StringReader(user.getRoles())).readObject();

			for (String key : roles.keySet()) {
				this.servicesNames.add(key);
				JsonObject jsonAccess = roles.getJsonObject(key);
				// verfica a permissao de aviso de alterações via websocket
				if (jsonAccess.getBoolean("read") == true) {
					this.websocketServices.add(key);
				}
			}
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

		public CrudUser getUser() {
			return user;
		}

		public List<CrudService> getCrudServices() {
			return this.crudServices;
		}

	}
	// private to create,update,delete,read
	static private Response checkObjectAccess(LoginResponse login, EntityManager entityManager, Object obj) {
		Response response = null;
		Integer userCompany = login.user.getCompany();

		if (userCompany > 1 && RequestFilter.haveField(obj.getClass(), "company")) {
			Integer objCompany = (Integer) RequestFilter.readField(obj, "company");

			if (objCompany == null) {
				RequestFilter.writeField(obj, "company", userCompany);
				objCompany = userCompany;
			}

			if (objCompany == userCompany) {
				if (RequestFilter.haveField(obj.getClass(), "category")) {
					Integer category = (Integer) RequestFilter.readField(obj, "category");

					if (login.categories.indexOf(category) < 0) {
						response = Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized object category").build();
					}
				}
			} else {
				response = Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized object company").build();
			}
		}

		return response;
	}
	// public
	static public CompletableFuture<Response> processCreate(Principal login, UserTransaction userTransaction, EntityManager entityManager, RequestFilter webSocket, Object obj) {
		Response response = checkObjectAccess((LoginResponse) login, entityManager, obj);

		if (response != null) CompletableFuture.completedFuture(response);

		return RequestFilter.insert(userTransaction, entityManager, obj).thenApply(newObj -> {
			webSocket.notify(newObj, false);
			return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
		});
	}
	// public
	static public <T> CompletableFuture<T> getObject(LoginResponse login, UriInfo uriInfo, EntityManager entityManager, Class<T> entityClass) {
		MultivaluedMap<String, String> queryParam = uriInfo.getQueryParameters();
		RequestFilter.QueryMap fields = RequestFilter.QueryMap.create();

		if (RequestFilter.haveField(entityClass, "id")) {
			Integer id = RequestFilter.parseInt(queryParam.getFirst("id"));
			fields.add("id", id);
		}

		Integer company = login.user.getCompany();

		if (RequestFilter.haveField(entityClass, "company")) {
			if (company == 1) {
				// se for admin, direciona a busca para a empresa informada
				company = RequestFilter.parseInt(queryParam.getFirst("company"));
			}

			fields.add("company", company);
		} else if (company != 1 && RequestFilter.haveField(entityClass, "category")) {
			// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
			fields.add("category", login.categories);
		}

		return RequestFilter.findOne(entityManager, entityClass, fields)
		.exceptionally(error -> {
			throw new RuntimeException("fail to find object with company, category and query parameters related : " + error.getMessage());
		});
	}
	// public processRead
	static public <T> CompletableFuture<Response> processRead(LoginResponse login, UriInfo uriInfo, EntityManager entityManager, Class<T> objectClass) {
		return RequestFilter.getObject(login, uriInfo, entityManager, objectClass).thenApply(obj -> Response.ok(obj, MediaType.APPLICATION_JSON).build());
	}
	// public processUpdate
	static public CompletableFuture<Response> processUpdate(LoginResponse login, UriInfo uriInfo, UserTransaction userTransaction, EntityManager entityManager, RequestFilter webSocket, Object obj) {
		return RequestFilter.getObject(login, uriInfo, entityManager, obj.getClass()).thenCompose(oldObj -> {
			Response response = checkObjectAccess(login, entityManager, obj);

			if (response != null) return CompletableFuture.completedFuture(response);

			if (RequestFilter.haveField(obj.getClass(), "id")) {
				Integer oldId = (Integer) RequestFilter.readField(oldObj, "id");
				Integer newId = (Integer) RequestFilter.readField(obj, "id");

				if (newId == null || newId.intValue() != oldId.intValue()) {
					return CompletableFuture.completedFuture(Response.status(Response.Status.UNAUTHORIZED).entity("changed id").build());
				}
			}

			return RequestFilter.update(userTransaction, entityManager, obj).thenApply(newObj -> {
				webSocket.notify(newObj, false);
				return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
			});
		});
	}
	// public processDelete
	static public CompletableFuture<Response> processDelete(LoginResponse login, UriInfo uriInfo, EntityManager entityManager, RequestFilter webSocket, Class<?> objectClass) {
		return RequestFilter.getObject(login, uriInfo, entityManager, objectClass).thenCompose(obj -> {
			return RequestFilter.deleteOne(entityManager, obj).thenApply((objDeleted) -> {
				webSocket.notify(objDeleted, true);
				return Response.ok().build();
			});
		});
	}
	// public
	static public <T> CompletableFuture<Response> processQuery(LoginResponse login, UriInfo uriInfo, EntityManager entityManager, Class<T> entityClass) {
		MultivaluedMap<String, String> queryParam = uriInfo.getQueryParameters();
		RequestFilter.QueryMap fields = RequestFilter.QueryMap.create();
		Integer company = login.user.getCompany();

		if (company != 1) {
			if (RequestFilter.haveField(entityClass, "company")) {
				fields.add("company", company);
			} else if (RequestFilter.haveField(entityClass, "category")) {
				// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
				fields.add("category", login.categories);
			}
		}

		String serviceName = RequestFilter.convertCaseUnderscoreToCamel(entityClass.getSimpleName(), false);
		CrudService service = login.getCrudServices().stream().filter(item -> item.getName().equals(serviceName)).findFirst().get();
		String[] orderBy = null;

		if (service.getOrderBy() != null) {
			orderBy = service.getOrderBy().split(",");
		}

		Integer startPosition = RequestFilter.parseInt(queryParam.getFirst("start"));
		Integer maxResult = RequestFilter.parseInt(queryParam.getFirst("max"));
		return RequestFilter.find(entityManager, entityClass, fields, orderBy, startPosition, maxResult).thenApply(results -> Response.ok(results, MediaType.APPLICATION_JSON).build());
	}

	private void processRequest(ContainerRequestContext requestContext, String ip, String resource, final String access) {
		Supplier<Response> crudProcess = () -> {
				String className = RequestFilter.convertCaseUnderscoreToCamel(resource, true);
				Class<?> restClass;
				String domain = this.getClass().getName();
				domain = domain.substring(0, domain.lastIndexOf(".admin"));

				try {
					restClass = Class.forName(domain + ".rest." + className + "Endpoint");
				} catch (ClassNotFoundException e) {
					restClass = null;
				}

				if (access.equals("create") && RequestFilter.haveMethodName(restClass, "create")) {
					return null;
				} else if (access.equals("read") && RequestFilter.haveMethodName(restClass, "read")) {
					return null;
				} else if (access.equals("query") && RequestFilter.haveMethodName(restClass, "query")) {
					return null;
				} else if (access.equals("delete") && RequestFilter.haveMethodName(restClass, "remove")) {
					return null;
				} else if (access.equals("update") && RequestFilter.haveMethodName(restClass, "update")) {
					return null;
				}

				Class<?> objectClass;

				try {
					objectClass = Class.forName(domain + ".entity." + className);
				} catch (ClassNotFoundException e) {
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
				}

				SecurityContext securiryContext = requestContext.getSecurityContext();
				LoginResponse login = (LoginResponse) securiryContext.getUserPrincipal();
				UriInfo uriInfo = requestContext.getUriInfo();
				Object obj = null;

				if (access.equals("create") || access.equals("update")) {
					try {
						InputStream inputStream = requestContext.getEntityStream();
						obj = RequestFilter.loadObjectFromJson(objectClass, inputStream);
					} catch (Exception e) {
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
					}
				}

				CompletableFuture<Response> cf;

				if (access.equals("create")) {
					cf = RequestFilter.processCreate(login, userTransaction, RequestFilter.this.entityManager, RequestFilter.this.webSocket, obj);
				} else if (access.equals("update")) {
					cf = RequestFilter.processUpdate(login, uriInfo, userTransaction, RequestFilter.this.entityManager, RequestFilter.this.webSocket, obj);
				} else if (access.equals("delete")) {
					cf = RequestFilter.processDelete(login, uriInfo, RequestFilter.this.entityManager, RequestFilter.this.webSocket, objectClass);
				} else if (access.equals("read")) {
					cf = RequestFilter.processRead(login, uriInfo, RequestFilter.this.entityManager, objectClass);
				} else if (access.equals("query")) {
					cf = RequestFilter.processQuery(login, uriInfo, RequestFilter.this.entityManager, objectClass);
				} else {
					return null;
				}

				return cf.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build()).join();
		};

		Function<CrudUser, String> authorization = user -> {
				String msgErr;
				String roles = user.getRoles();

				if (roles != null) {
					JsonObject json = Json.createReader(new StringReader(roles)).readObject();
					String serviceName = RequestFilter.convertCaseUnderscoreToCamel(resource, false);

					if (RequestFilter.findInSet(json.keySet(), serviceName) == true) {
						JsonObject serviceAuth = json.getJsonObject(serviceName);
						// verfica a permissao de acesso
						if (serviceAuth.getString(access, "true").equals("true")) {
							msgErr = null;
							log.info(String.format("[authorization] Sucessful authorization : path: = %s, user = %s, roles = %s, token = %s", resource, user.getName(), roles, user.getAuthctoken()));
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
		};

		Supplier<Response> authWithToken = () -> {
			Response response;
			// headers['Authorization'] = 'Token ' + token;
			String authorizationHeader = requestContext.getHeaderString("Authorization");
			log.info("authorization header : " + authorizationHeader);

			if (authorizationHeader != null && authorizationHeader.startsWith("Token ")) {
				String token = authorizationHeader.substring(6);
				LoginResponse login = RequestFilter.getLogin(token);

				if (login != null) {
					String msgErr = authorization.apply(login.user);

					if (msgErr == null) {
						try {
							SecurityContext securityContextOld = requestContext.getSecurityContext();

							if (securityContextOld == null || securityContextOld.getUserPrincipal() == null) {
								class ImplSecurityContext implements SecurityContext {

								    private LoginResponse loginResponse;

								    public ImplSecurityContext(LoginResponse loginResponse) {
								    	this.loginResponse = loginResponse;
								    }

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

								ImplSecurityContext securityContext = new ImplSecurityContext(login);
								requestContext.setSecurityContext(securityContext);
								response = crudProcess.get();
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
						log.warning(String.format("user : %s - path : %s - msgErr : %s", login.user.getName(), resource, msgErr));
					}
				} else {
					response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization replaced by new login in another session").build();
					log.warning("Authorization replaced by new login in another session");
				}
			} else {
				response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization token header invalid").build();
				log.warning("Authorization token header invalid : " + authorization);
			}

			if (response != null) {
				requestContext.abortWith(response);
			}

			return response;
		};

		authWithToken.get();
	}

	private CompletableFuture<Response> authenticateByUserAndPassword(ContainerRequestContext requestContext, String ip) {
		JsonObject json = Json.createReader(requestContext.getEntityStream()).readObject();
		String userId = json.getString("userId");
		QueryMap userQuery = QueryMap.create().add("name", userId).add("password", json.getString("password"));

		return RequestFilter.findOne(entityManager, CrudUser.class, userQuery)
		.thenCompose(user -> {
			String token = UUID.randomUUID().toString();
			user.setAuthctoken(token);
			user.setIp(ip);
			return RequestFilter.update(userTransaction, entityManager, user).thenCompose(userAfterUpdate -> {
				LoginResponse loginResponse = new LoginResponse(userAfterUpdate);
				return loginResponse.load().thenApply((arg) -> {
					log.info(String.format("[authenticateByUserAndPassword] Sucessful login : user = %s, roles = %s, token = %s", userAfterUpdate.getName(), userAfterUpdate.getRoles(), userAfterUpdate.getAuthctoken()));
					RequestFilter.logins.put(token, loginResponse);
					return Response.ok(loginResponse, MediaType.APPLICATION_JSON).build();
				});
			});
		});
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		log.info("Filtering REST Response");
		// TODO : habilitar somente os IPs dos servidores instalados nas empresas
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*"); // USE * for all, https://localhost:9443
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
			Response response = authenticateByUserAndPassword(requestContext, ip)
			.exceptionally(error -> {
				if (error.getCause() instanceof NoResultException) {
					log.warning("mismatched user and password");
					return Response.status(Response.Status.UNAUTHORIZED).entity("mismatched user and password").build();
				} else {
					log.severe(error.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build();
				}
			}).join();
			requestContext.abortWith(response);
		// em qualquer outro método pede o token
		} else {
			String access = path.substring(path.indexOf(resource)+resource.length()+1);
			processRequest(requestContext, ip, resource, access);
		}
	}

	public static LoginResponse getLogin(String token) {
		return RequestFilter.logins.get(token);
	}

    private Logger logger = Logger.getLogger(getClass().getName());

    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    @OnMessage
    public void onMessage(Session session, String token) {
    	RequestFilter.LoginResponse login = RequestFilter.getLogin(token);

		if (login != null) {
			session.getUserProperties().put("login", login);
		    logger.info("New websocket session opened: token : " + token + ", id : " + session.getId());
		    clients.add(session);
		}
    }
    // remove the session after it's closed
    @OnClose
    public void onClose(Session session) {
    	LoginResponse login = (LoginResponse) session.getUserProperties().get("login");
        logger.info("Websoket session closed: " + login.getUser().getAuthctoken());
        clients.remove(session);
    }
    // Exception handling
    @OnError
    public void error(Session session, Throwable t) {
        t.printStackTrace();
    }

    private JsonObject getPrimaryKey(Object obj) {
		JsonObjectBuilder primaryKey = Json.createObjectBuilder();

		if (RequestFilter.haveField(obj.getClass(), "company")) {
			primaryKey.add("company", (Integer) RequestFilter.readField(obj, "company"));
		}

		if (RequestFilter.haveField(obj.getClass(), "id")) {
			primaryKey.add("id", (Integer) RequestFilter.readField(obj, "id"));
		}

		return primaryKey.build();
    }
    // This method sends the same Bidding object to all opened sessions
    public void notify(Object obj, boolean isRemove) {
    	JsonObject primaryKey = getPrimaryKey(obj);
		String serviceName = RequestFilter.convertCaseUnderscoreToCamel(obj.getClass().getSimpleName(), false);
		JsonObjectBuilder msg = Json.createObjectBuilder();
		msg.add("service", serviceName);
		msg.add("primaryKey", primaryKey);

		if (isRemove == false) {
			msg.add("action", "notify");
		} else {
			msg.add("action", "delete");
		}

    	String str = msg.build().toString();
    	Integer objCompany = primaryKey.containsKey("company") ? primaryKey.getInt("company") : null;
    	Integer category = null;

		if (RequestFilter.haveField(obj.getClass(), "category")) {
			category = (Integer) RequestFilter.readField(obj, "company");
		}

		for (Session session : clients) {
			RequestFilter.LoginResponse login = (LoginResponse) session.getUserProperties().get("login");
			Integer userCompany = login.getUser().getCompany();
			// enviar somente para os clients de "company"
			if (objCompany == null || userCompany == 1 || objCompany == userCompany) {
				// restrição de categoria
				if (category == null || login.categories.indexOf(category) >= 0) {
					// envia somente para os usuários com acesso ao serviço alterado
					if (login.getWebsocketServices().contains(serviceName)) {
						System.out.format("notify, user %s : %s\n", login.getUser().getName(), msg);
						CompletableFuture.runAsync(() -> {
							try {
								session.getBasicRemote().sendText(str);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					}
				}
			}
		}
    }
}
