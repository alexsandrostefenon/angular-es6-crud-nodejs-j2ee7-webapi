package org.domain.crud.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
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

import org.domain.commom.CaseConvert;
import org.domain.commom.DbUtils;
import org.domain.commom.ReflectionUtils;
import org.domain.commom.Utils;
import org.domain.crud.entity.CategoryCompany;
import org.domain.crud.entity.CrudCompany;
import org.domain.crud.entity.CrudService;
import org.domain.crud.entity.CrudUser;

@ServerEndpoint(value = "/websocket")
@Provider
@PreMatching
@Transactional
public class RequestFilter implements ContainerRequestFilter, ContainerResponseFilter {
// RequestFilter static members
	private final static Map<String, Class<?>> mapClass = new HashMap<String, Class<?>>(256);
	private final static Map<String, CrudService> mapService = new HashMap<String, CrudService>(256);
	private final static Map<String, LoginResponse> logins = new HashMap<String, LoginResponse>(1024);
	private final static Map<String, Session> clients = new HashMap<String, Session>(1024);
	private final static Logger log = Logger.getLogger("CRUD");

	@Context
	HttpServletRequest httpRequest;

	@Resource
	private UserTransaction userTransaction = null;

	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;

// Class LoginResponse
// Response with login data access to client
	public static class LoginResponse implements java.security.Principal {
		private CrudUser user;
		private String title;
		private List<CrudService> crudServices;
		private List<String> websocketServices;
		private List<String> servicesNames;
		private List<Integer> categories;

		@Override
		public String getName() {
			return this.user.getName();
		}

// Load Company, Services and Categories
		public static CompletableFuture<LoginResponse> load(CrudUser user, EntityManager entityManager) {
			LoginResponse loginResponse = new LoginResponse (user);
			return DbUtils.findOne(entityManager, CrudCompany.class, DbUtils.QueryMap.create().add("id", user.getCompany()))
			.exceptionally(error -> {
				throw new RuntimeException("don't get user company : " + error.getMessage());
			})
			.thenCompose(company -> {
				loginResponse.setTitle(company.getName() + " - " + user.getName());
				// TODO : código temporário para caber o na tela do celular
				loginResponse.setTitle(user.getName());

				for(String serviceName : loginResponse.servicesNames) {
					loginResponse.crudServices.add (RequestFilter.mapService.get(serviceName));
				}
// Add Categories
				DbUtils.QueryMap queryCat = DbUtils.QueryMap.create().add("company", user.getCompany());
				return DbUtils.find(entityManager, CategoryCompany.class, queryCat, null, null, null)
				.exceptionally(error -> {
					throw new RuntimeException("don't match request category for user company : " + error.getMessage());
				})
				.thenApply(categories -> {
					loginResponse.categories = new ArrayList<Integer>(categories.size());

					for (CategoryCompany categoryCompany : categories) {
						loginResponse.categories.add(categoryCompany.getId());
					}

					return loginResponse;
				});
			});
		}

		private LoginResponse(CrudUser user) {
			this.user = user;
			this.servicesNames = new ArrayList<String>(256);
			this.crudServices = new ArrayList<CrudService>(256);
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

		public CrudUser getUser() {
			return user;
		}

		public List<CrudService> getCrudServices() {
			return this.crudServices;
		}

	}
// private to create,update,delete,read
	static private Response checkObjectAccess(Principal user, EntityManager entityManager, String serviceName, Object obj) {
		CrudService service = RequestFilter.getService (user, serviceName);
		
		if (service == null) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized service Access").build();
		}

		LoginResponse login = (LoginResponse) user;
		JsonObject serviceFields = Json.createReader(new StringReader(service.getFields())).readObject();
		Response response = null;
		Integer userCompany = login.user.getCompany();

		if (userCompany > 1 && serviceFields.containsKey("company")) {
			Integer objCompany = (Integer) ReflectionUtils.readField(obj, "company");

			if (objCompany == null) {
				ReflectionUtils.writeField(obj, "company", userCompany);
				objCompany = userCompany;
			}

			if (objCompany == userCompany) {
				if (serviceFields.containsKey("category")) {
					Integer category = (Integer) ReflectionUtils.readField(obj, "category");

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
	//
	static public CrudService getService(Principal user, String serviceName) {
		serviceName = CaseConvert.convertCaseUnderscoreToCamel (serviceName, false);
		LoginResponse login = (LoginResponse)user;

		if (login.servicesNames.contains (serviceName) == false) {
			return null;
		}

		CrudService service = RequestFilter.mapService.get(serviceName);
		return service;
	}
	// public
	static public CompletableFuture<Response> processCreate(Principal user, UserTransaction userTransaction, EntityManager entityManager, String serviceName, Object obj) {
		Response response = checkObjectAccess((LoginResponse) user, entityManager, serviceName, obj);

		if (response != null) CompletableFuture.completedFuture(response);

		return DbUtils.insert(userTransaction, entityManager, obj).thenApply(newObj -> {
			RequestFilter.notify(newObj, serviceName, false);
			return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
		});
	}
	// public
	static public <T> CompletableFuture<T> getObject(Principal user, UriInfo uriInfo, EntityManager entityManager, String serviceName) {
		@SuppressWarnings("unchecked")
		Class<T> entityClass = (Class<T>) RequestFilter.mapClass.get(CaseConvert.convertCaseUnderscoreToCamel (serviceName, false));
		return DbUtils.findOne(entityManager, entityClass, parseQueryParameters((LoginResponse)user, serviceName, uriInfo.getQueryParameters()))
		.exceptionally(error -> {
			throw new RuntimeException("fail to find object with company, category and query parameters related : " + error.getMessage());
		});
	}
	// public processRead
	static public <T> CompletableFuture<Response> processRead(Principal user, UriInfo uriInfo, EntityManager entityManager, String serviceName) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).thenApply(obj -> Response.ok(obj, MediaType.APPLICATION_JSON).build());
	}
	// public processUpdate
	static public CompletableFuture<Response> processUpdate(Principal user, UriInfo uriInfo, UserTransaction userTransaction, EntityManager entityManager, String serviceName, Object obj) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).thenCompose(oldObj -> {
			Response response = checkObjectAccess(user, entityManager, serviceName, obj);

			if (response != null) return CompletableFuture.completedFuture(response);

			return DbUtils.update(userTransaction, entityManager, obj).thenApply(newObj -> {
				RequestFilter.notify(newObj, serviceName, false);
				return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
			});
		});
	}
	// public processDelete
	static public CompletableFuture<Response> processDelete(Principal user, UriInfo uriInfo, UserTransaction userTransaction, EntityManager entityManager, String serviceName) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).thenCompose(oldObj -> {
			return DbUtils.deleteOne(userTransaction, entityManager, oldObj).thenApply(objDeleted -> {
				RequestFilter.notify(objDeleted, serviceName, true);
				return Response.ok().build();
			});
		});
	}
	// private
    static private DbUtils.QueryMap parseQueryParameters(LoginResponse login, String serviceName, MultivaluedMap<String, String> queryParameters) {
		DbUtils.QueryMap queryFields = DbUtils.QueryMap.create();
		CrudService service = RequestFilter.getService (login, serviceName);
		
		if (service != null) {
			JsonObject serviceFields = Json.createReader(new StringReader(service.getFields())).readObject();
			
			for (String fieldName : serviceFields.keySet()) {
				JsonObject field = serviceFields.getJsonObject(fieldName);

				if (field.containsKey("primaryKey") && field.getBoolean("primaryKey") == true) {
					String value = queryParameters.getFirst(fieldName);
					
					if (value != null) {
						String type = field.containsKey("type") ? field.getString("type") : null;

						if (type == null || type.equals("s")) {
			    			queryFields.add(fieldName, value);
						} else if (type.equals("n") || type.equals("i")) {
			    			queryFields.add(fieldName, Integer.parseInt(value));
						} else if (type.equals("b")) {
			    			queryFields.add(fieldName, Boolean.parseBoolean(value));
						}
					}
				}
			}
			// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
			Integer company = login.user.getCompany();
			
			if (company != 1) {
				if (serviceFields.containsKey("company")) {
					queryFields.put("company", company);
				} else if (serviceFields.containsKey("category")) {
					queryFields.put("category", login.categories);
				}
			}
		}
		
    	return queryFields;
    }
	// public
	static public <T> CompletableFuture<Response> processQuery(Principal user, UriInfo uriInfo, EntityManager entityManager, String serviceName) {
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		DbUtils.QueryMap fields = parseQueryParameters((LoginResponse)user, serviceName, queryParameters);
		String[] orderBy = null;

		{
			CrudService service = RequestFilter.getService (user, serviceName);

			if (service != null && service.getOrderBy() != null) {
				orderBy = service.getOrderBy().split(",");
			}
		}

		Integer startPosition = Utils.parseInt(queryParameters.getFirst("start"));
		Integer maxResult = Utils.parseInt(queryParameters.getFirst("max"));
		Class<?> entityClass = RequestFilter.mapClass.get(CaseConvert.convertCaseUnderscoreToCamel (serviceName, false));
		return DbUtils.find(entityManager, entityClass, fields, orderBy, startPosition, maxResult).thenApply(results -> Response.ok(results, MediaType.APPLICATION_JSON).build());
	}

	public class ImplSecurityContext implements SecurityContext {

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
// processRequest
// main
	private Response  processRequest(ContainerRequestContext requestContext, EntityManager entityManager, String serviceName, String uriPath) {
// crudProcess
		Function<LoginResponse, Response> crudProcess = login -> {
				Class<?> objectClass = RequestFilter.mapClass.get(serviceName);
				UriInfo uriInfo = requestContext.getUriInfo();
				Object obj = null;

				if (uriPath.equals("create") || uriPath.equals("update")) {
					try {
						InputStream inputStream = requestContext.getEntityStream();
						obj = ReflectionUtils.loadObjectFromJson(objectClass, inputStream);
					} catch (Exception e) {
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
					}
				}

				CompletableFuture<Response> cf;

				if (uriPath.equals("create")) {
					cf = RequestFilter.processCreate(login, this.userTransaction, entityManager, serviceName, obj);
				} else if (uriPath.equals("update")) {
					cf = RequestFilter.processUpdate(login, uriInfo, this.userTransaction, entityManager, serviceName, obj);
				} else if (uriPath.equals("delete")) {
					cf = RequestFilter.processDelete(login, uriInfo, this.userTransaction, entityManager, serviceName);
				} else if (uriPath.equals("read")) {
					cf = RequestFilter.processRead(login, uriInfo, entityManager, serviceName);
				} else if (uriPath.equals("query")) {
					cf = RequestFilter.processQuery(login, uriInfo, entityManager, serviceName);
				} else {
					return null;
				}

				return cf.exceptionally(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build()).join();
		};

		Function<CrudUser, Boolean> authorization = user -> {
				Boolean access = null;
				JsonObject json = Json.createReader(new StringReader(user.getRoles())).readObject();
				// verfica a permissao de acesso
				if (Utils.findInSet(json.keySet(), serviceName) == true) {
					JsonObject serviceAuth = json.getJsonObject(serviceName);

					if (serviceAuth.containsKey(uriPath) == true) {
						access = serviceAuth.getBoolean(uriPath);
					}
				}

				return access;
		};

		Supplier<Response> authWithToken = () -> {
			Response response;
			String authorizationHeader = requestContext.getHeaderString("Authorization");

			if (authorizationHeader != null && authorizationHeader.startsWith("Token ")) {
				String token = authorizationHeader.substring(6);
				LoginResponse login = RequestFilter.logins.get(token);

				if (login != null) {
					Boolean access = authorization.apply(login.user);

					if (access != false) {
						SecurityContext securityContextOld = requestContext.getSecurityContext();

						if (securityContextOld == null || securityContextOld.getUserPrincipal() == null) {
							ImplSecurityContext securityContext = new ImplSecurityContext(login);
							requestContext.setSecurityContext(securityContext);

							if (access == true) {
								response = crudProcess.apply(login);
							} else {
								response = null;
							}
						} else {
							response = Response.status(Response.Status.BAD_REQUEST).entity("Already Identity found").build();
						}
					} else {
						response = Response.status(Response.Status.UNAUTHORIZED).entity("Explicit Unauthorized").build();
					}
				} else {
					response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization replaced by new login in another session").build();
				}
			} else {
				response = Response.status(Response.Status.UNAUTHORIZED).entity("Authorization token header invalid").build();
			}

			return response;
		};

		return authWithToken.get();
	}
	// private
	private CompletableFuture<Response> authenticateByUserAndPassword(ContainerRequestContext requestContext, String ip) {
		JsonObject json = Json.createReader(requestContext.getEntityStream()).readObject();
		String userId = json.getString("userId");
		String password = json.getString("password");
		DbUtils.QueryMap userQuery = DbUtils.QueryMap.create().add("name", userId).add("password", password);

		return DbUtils.findOne(entityManager, CrudUser.class, userQuery).thenCompose(user -> {
			String token = UUID.randomUUID().toString();
			user.setAuthctoken(token);
			user.setIp(ip);
			return DbUtils.update(userTransaction, entityManager, user).thenCompose(userAfterUpdate -> {
				return LoginResponse.load(userAfterUpdate, entityManager).thenApply((loginResponse) -> {
					RequestFilter.logins.put(token, loginResponse);
					return Response.ok(loginResponse, MediaType.APPLICATION_JSON).build();
				});
			});
		});
	}
	// filter
	@Override
	public void filter(ContainerRequestContext requestContext) {
		String method = requestContext.getMethod();
		System.out.println(String.format("method : %s", method));
		// When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
		if (method.equals("OPTIONS")) {
			// Just send a OK signal back to the browser
			requestContext.abortWith(Response.status(Response.Status.OK).build());
			return;
		}

		String ip = this.httpRequest.getRemoteAddr();
		String uri = requestContext.getUriInfo().getPath();
		String[] paths = requestContext.getUriInfo().getAbsolutePath().getPath().split("/"); // /crud/rest/authc
		String root = paths[2];
		
		String resource = null;
		String action = null;

		if (root.equals ("rest") == true) {
			if (paths.length > 3) {
				resource = CaseConvert.convertCaseUnderscoreToCamel (paths[3], false);

				if (paths.length > 4) {
					action = paths[4];
				}
			}
		}

		System.out.println(String.format("RemoteAddr : %s", ip));
		System.out.println(String.format("uri : %s", uri));
		System.out.println(String.format ("root : %s", root));
		System.out.println(String.format ("resource : %s", resource));
		System.out.println(String.format ("action : %s", action));

		// no login pede usuário e senha
		if (root.equals("rest")) {
			if (resource.equals("authc")) {
				Response response = this.authenticateByUserAndPassword(requestContext, ip).join();
				requestContext.abortWith(response);
			// em qualquer outro método pede o token
			} else {
				Response response = this.processRequest(requestContext, this.entityManager, resource, action);
				
				if (response != null) {
					requestContext.abortWith(response);
				}

			}
		}
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

    @OnMessage
    public void onMessage(Session session, String token) {
    	RequestFilter.LoginResponse login = RequestFilter.logins.get(token);

		if (login != null) {
			session.getUserProperties().put("token", token);
		    RequestFilter.clients.put(token, session);
		    log.info("New websocket session opened: token : " + token + ", id : " + session.getId());
		}
    }
    // remove the session after it's closed
    @OnClose
    public void onClose(Session session) {
    	String token = (String) session.getUserProperties().get("token");
        LoginResponse login = RequestFilter.logins.get(token);

		if (login != null) {
		    log.info("Websoket session closed: " + token);
		    RequestFilter.clients.remove(token);
		    RequestFilter.logins.remove(token);
		}
    }
    // Exception handling
    @OnError
    public void error(Session session, Throwable t) {
        t.printStackTrace();
    }
    // This method sends the same Bidding object to all opened sessions
    public static void notify(Object obj, String serviceName, boolean isRemove) {
		JsonObject serviceFields = Json.createReader(new StringReader(RequestFilter.mapService.get(serviceName).getFields())).readObject();
		
        Supplier<JsonObject> getPrimaryKey = () -> {
    		JsonObjectBuilder primaryKeyBuilder = Json.createObjectBuilder();
    		
    		for (String fieldName : serviceFields.keySet()) {
    			JsonObject field = serviceFields.getJsonObject(fieldName);

    			if (field.containsKey("primaryKey") && field.getBoolean("primaryKey") == true) {
    				Object value = ReflectionUtils.readField(obj, fieldName);
    				
    				if (value instanceof String) {
    	    			primaryKeyBuilder.add(fieldName, (String) value);
    				} else if (value instanceof Integer) {
    	    			primaryKeyBuilder.add(fieldName, (Integer) value);
    				} else if (value instanceof Boolean) {
    	    			primaryKeyBuilder.add(fieldName, (Boolean) value);
    				}
    			}
    		}
    		
    		return primaryKeyBuilder.build();
        };
        
    	JsonObject primaryKey = getPrimaryKey.get();
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

		if (serviceFields.containsKey("category")) {
			category = (Integer) ReflectionUtils.readField(obj, "category");
		}
		
		for (Map.Entry<String, Session> item : RequestFilter.clients.entrySet()) {
			RequestFilter.LoginResponse login = RequestFilter.logins.get(item.getKey());
			Integer userCompany = login.getUser().getCompany();
			// enviar somente para os clients de "company"
			if (objCompany == null || userCompany == 1 || objCompany == userCompany) {
				// restrição de categoria
				if (category == null || login.categories.indexOf(category) >= 0) {
					// envia somente para os usuários com acesso ao serviço alterado
					if (login.getWebsocketServices().contains(serviceName)) {
						CompletableFuture.runAsync(() -> {
							try {
								item.getValue().getBasicRemote().sendText(str);
								System.out.format("notify, user %s : %s\n", login.getUser().getName(), msg);
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

    public static void updateCrudServices(UserTransaction userTransaction, EntityManager entityManager) throws Exception {

		BiFunction<Class<?>, String, String> generateFieldsStr = (entityClass, strFields) -> {
			Field[] fields = entityClass.getDeclaredFields();
			JsonObject jsonOriginal = Json.createReader(new StringReader(strFields)).readObject();
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder(); 
		
			for (Field field : fields) {
				String fieldName = field.getName();
				javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
				
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				
				if (field.getAnnotation(javax.persistence.Transient.class) != null) {
					continue;
				}
				
				String typeDesc = field.getType().getName();

				if (typeDesc.endsWith(".String")) {
					typeDesc = "s";
				} else if (typeDesc.endsWith(".Integer")) {
					typeDesc = "i";
				} else if (typeDesc.endsWith(".Boolean")) {
					typeDesc = "b";
				} else if (typeDesc.endsWith(".BigDecimal")) {
					typeDesc = "n3"; // numero com separação de milhar com casas decimais
					
					if (column != null) {
						if (column.scale() == 2) {
							typeDesc = "n2"; // numero com separação de cents
						} else if (column.scale() == 1) {
							typeDesc = "n1"; // numero com separação de decimais
						}
					}
				} else if (typeDesc.endsWith(".Timestamp")) {
					typeDesc = "datetime-local"; // data e hora completa
				} else if (typeDesc.endsWith(".Date")) {
					typeDesc = "date"; // data
				} else if (typeDesc.endsWith(".Time")) {
					typeDesc = "time"; // hora completa
				} else {
					System.err.printf("{%s} : {%s} : Unknow type : {%s}\n", entityClass.getName(), field.getName(), typeDesc);
					continue;
				}
				// type (columnDefinition), readOnly, hiden, primaryKey, required (insertable), updatable, defaultValue, length, precision, scale 
				JsonObjectBuilder jsonBuilderValue = Json.createObjectBuilder();
				
				if (typeDesc != null) {
					jsonBuilderValue.add("type", typeDesc);
				}
				
				if (column != null && column.updatable() == false) {
					jsonBuilderValue.add("updatable", false);
				}
				
				if (column != null && column.length() != 255) {
					jsonBuilderValue.add("length", column.length());
				}
				
				if (column != null && column.precision() != 0) {
					jsonBuilderValue.add("precision", column.precision());
				}
				
				if (column != null && column.scale() != 0) {
					jsonBuilderValue.add("scale", column.scale());
				}
				
				if (column != null && column.columnDefinition().toLowerCase().equals("serial")) {
					jsonBuilderValue.add("hiden", true);
				}
				
				if (field.getAnnotation(javax.persistence.Id.class) != null) {
					jsonBuilderValue.add("primaryKey", true);
				}
				
				if (field.getAnnotation(javax.validation.constraints.NotNull.class) != null) {
					jsonBuilderValue.add("required", true);
				}
				
				if (jsonOriginal.containsKey(fieldName)) {
					jsonBuilder.add(fieldName, jsonOriginal.get(fieldName));
					continue;
				}
	
				jsonBuilder.add(fieldName, jsonBuilderValue.build());
			}

			return jsonBuilder.build().toString();
		};
		
        for (EntityType<?> entityType : entityManager.getEntityManagerFactory().getMetamodel().getEntities()) {
        	Class<?> entityClass = entityType.getJavaType();
        	String name = CaseConvert.convertCaseUnderscoreToCamel(entityClass.getSimpleName(), false);
        	CrudService service = entityManager.find(CrudService.class, name);
			userTransaction.begin();
			
        	if (service != null) {
        		service.setFields(generateFieldsStr.apply(entityClass, service.getFields()));
        		entityManager.merge(service);
        	} else {
        		service = new CrudService();
        		service.setName(name);
        		service.setFields(generateFieldsStr.apply(entityClass, "{}"));
        		entityManager.persist(service);
        	}
        	
			userTransaction.commit();
			RequestFilter.mapClass.put(name, entityClass);
			RequestFilter.mapService.put (name, service);
        }
	}
    
}
