using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.IO;
using System.Linq;
using System.Net.WebSockets;
using System.Reflection;
using System.Security.Claims;
using System.Security.Principal;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using AspNetCoreWebApi.Entity;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Primitives;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Serialization;
using org.domain.commom;

namespace org.domain.crud.admin {

	public class RequestFilter {
// RequestFilter static members
		private static Dictionary<String, Type> mapClass = new Dictionary<String, Type> (256);
		private static Dictionary<String, CrudService> mapService = new Dictionary<String, CrudService> (256);
		private static Dictionary<String, LoginResponse> logins = new Dictionary<String, LoginResponse> (1024);
		private static Dictionary<String, WebSocket> clients = new Dictionary<String, WebSocket>(1024);

		private readonly RequestDelegate next;

		public RequestFilter (RequestDelegate next)
		{
			this.next = next;
		}
// Class LoginResponse
// Response with login data access to client
		public class LoginResponse : ClaimsIdentity {

			public CrudUser user;
			public String title;
			public List<CrudService> crudServices;
			public List<String> websocketServices;
			public List<String> servicesNames;
			public List<int> groups;

			override public string AuthenticationType => "Kerberos";

			override public bool IsAuthenticated => this.user != null;

			override public string Name => this.user.Name;
// Load crudGroupOwner, Services and Groups
			public static Task<LoginResponse> Load (CrudUser user, DbContext entityManager) {
				LoginResponse loginResponse = new LoginResponse (user);
				DbUtils.QueryMap query = DbUtils.QueryMap.Create ().AddNext ("id", loginResponse.user.CrudGroupOwner);
				return DbUtils.FindOne<CrudGroupOwner> (entityManager, null, query)
				.ContinueWith<LoginResponse> (crudGroupOwner => {
					if (crudGroupOwner.Exception != null) {
						throw new AggregateException ("don't get user crudGroupOwner : " + crudGroupOwner.Exception.Message);
					}

					loginResponse.title = crudGroupOwner.Result.Name + " - " + user.Name;
					// TODO : código temporário para caber o na tela do celular
					loginResponse.title = user.Name;

					foreach(String serviceName in loginResponse.servicesNames) {
						loginResponse.crudServices.Add (RequestFilter.mapService [serviceName]);
					}
// Add Groups
					DbUtils.QueryMap queryCat = DbUtils.QueryMap.Create ().AddNext ("crud_user", loginResponse.user.Name);
					return DbUtils.Find<CrudGroupUser> (entityManager, null, queryCat, null, null, null)
					.ContinueWith<LoginResponse> (taskGroups => {
						if (taskGroups.Exception != null) {
							throw new AggregateException ("don't match request crudGroup for user : " + taskGroups.Exception.Message);
						}

						loginResponse.groups = new List<int> (taskGroups.Result.Count);

						foreach (CrudGroupUser crudGroupUser in taskGroups.Result) {
							loginResponse.groups.Add (crudGroupUser.CrudGroup);
						}

						return loginResponse;
					}).Result;
				});
			}

			private LoginResponse (CrudUser user) {
				this.user = user;
				this.servicesNames = new List<String> (256);
				this.crudServices = new List<CrudService> (256);
				this.websocketServices = new List<String> (256);
				JObject roles = JObject.Parse (user.Roles);

				foreach (var item in roles) {
					this.servicesNames.Add (item.Key);
					// verfica a permissao de aviso de alterações via websocket
					if (item.Value.Value<Boolean> ("read") == true) {
						this.websocketServices.Add (item.Key);
					}
				}
			}

		}
// private to create,update,delete,read
		static private ActionResult CheckObjectAccess(IIdentity user, String serviceName, Object obj) {
			CrudService service;

			try {
				service = RequestFilter.GetService (user, serviceName);
			} catch (Exception e) {
				return Response.Unauthorized(e.Message);
			}

			LoginResponse login = (LoginResponse) user;
			JObject serviceFields = JObject.Parse (service.Fields);
			ActionResult response = null;
            int? userCrudGroupOwner = login.user.CrudGroupOwner;

			if (userCrudGroupOwner > 1 && serviceFields.ContainsKey("crudGroupOwner")) {
				int? objCrudGroupOwner = (int?)obj.GetType().GetProperty("CrudGroupOwner").GetValue(obj);

                if (objCrudGroupOwner == null) {
					obj.GetType ().GetProperty ("CrudGroupOwner").SetValue (obj, userCrudGroupOwner);
                    objCrudGroupOwner = userCrudGroupOwner;
                }

                if (objCrudGroupOwner == userCrudGroupOwner) {
                    if (serviceFields.ContainsKey("crudGroup")) {
						int crudGroup = (int)obj.GetType ().GetProperty ("CrudGroup").GetValue (obj);

                        if (login.groups.IndexOf(crudGroup) < 0) {
							response = Response.Unauthorized("unauthorized object crudGroup");
                        }
                    }
                } else {
					response = Response.Unauthorized("unauthorized object crudGroupOwner");
                }
            }

            return response;
        }
		//
		static public CrudService GetService(IIdentity user, String serviceName) {
			LoginResponse login = (LoginResponse)user;

			if (login.servicesNames.Contains (serviceName) == false) {
				throw new Exception ("Unauthorized service Access");
			}

			CrudService service = RequestFilter.mapService[serviceName];
			return service;
		}
        // public
		static public Task<ActionResult> ProcessCreate<T>(IIdentity user, DbContext entityManager, String serviceName, T obj) where T : class {
			ActionResult response = RequestFilter.CheckObjectAccess(user, serviceName, obj);

			if (response != null) return Task.Run (() => response);

			return DbUtils.Insert<T>(null, entityManager, obj).ContinueWith<ActionResult>(taskInsert => {
				Object newObj = taskInsert.Result;
				RequestFilter.Notify(newObj, serviceName, false);
                return Response.Ok(newObj);
			}, TaskContinuationOptions.NotOnFaulted);
        }
        // public
		static public  Task<T> GetObject<T>(IIdentity user, HttpRequest uriInfo, DbContext entityManager, String serviceName) where T : class {
			Type entityClass = RequestFilter.mapClass[serviceName];
			return DbUtils.FindOne<T>(entityManager, entityClass, ParseQueryParameters((LoginResponse)user, serviceName, uriInfo.Query)).ContinueWith<T>(taskFind => {
				if (taskFind.Exception != null) {
					throw new Exception("fail to find object with crudGroupOwner, crudGroup and query parameters related : " + taskFind.Exception);
				}

				return taskFind.Result;
            });
        }
        // public processRead
		static public Task<ActionResult> ProcessRead<T>(IIdentity user, HttpRequest uriInfo, DbContext entityManager, String serviceName) where T : class {
			return RequestFilter.GetObject<T>(user, uriInfo, entityManager, serviceName).ContinueWith<ActionResult>(taskGet => {
				return Response.Ok (taskGet.Result);
			}, TaskContinuationOptions.NotOnFaulted);
        }
        // public processUpdate
		static public Task<ActionResult> ProcessUpdate<T>(IIdentity user, HttpRequest uriInfo, DbContext entityManager, String serviceName, T obj) where T : class {
			return RequestFilter.GetObject<T>(user, uriInfo, entityManager, serviceName).ContinueWith<ActionResult>(taskGet => {
				ActionResult response = CheckObjectAccess(user, serviceName, obj);

				if (response != null) return response;

				entityManager.Entry (taskGet.Result).State = EntityState.Detached;
				return DbUtils.Update<T>(null, entityManager, obj).ContinueWith<ActionResult>(taskNewObj => {
					RequestFilter.Notify(taskNewObj.Result, serviceName, false);
					return Response.Ok(taskNewObj.Result);
				}, TaskContinuationOptions.NotOnFaulted).Result;
			}, TaskContinuationOptions.NotOnFaulted);
        }
        // public processDelete
		static public Task<ActionResult> ProcessDelete<T>(IIdentity user, HttpRequest uriInfo, DbContext entityManager, String serviceName) where T : class {
			return RequestFilter.GetObject<T>(user, uriInfo, entityManager, serviceName).ContinueWith<ActionResult>(taskGet => {
				return DbUtils.DeleteOne<T>(entityManager, taskGet.Result).ContinueWith<ActionResult>((taskObjDeleted) => {
					RequestFilter.Notify(taskObjDeleted.Result, serviceName, true);
                    return Response.Ok();
				}, TaskContinuationOptions.NotOnFaulted).Result;
			}, TaskContinuationOptions.NotOnFaulted);
        }
		// private
		static private DbUtils.QueryMap ParseQueryParameters (LoginResponse login, String serviceName, IQueryCollection queryParameters) {
			CrudService service = RequestFilter.GetService (login, serviceName);
			DbUtils.QueryMap queryFields = DbUtils.QueryMap.Create ();
			JObject serviceFields = JObject.Parse (service.Fields);

			foreach (var item in serviceFields) {
				JToken field = item.Value;

				if (field.Value<Boolean> ("primaryKey") == true) {
					StringValues values = queryParameters [item.Key];

					if (values.Count > 0) {
						String type = field.Value<String> ("type");

						if (type == null || type.Equals("s")) {
							queryFields.Add (item.Key, values.First ());
						} else if (type.Equals("n") || type.Equals("i")) {
							queryFields.Add (item.Key, int.Parse (values.First ()));
						} else if (type.Equals("b")) {
							queryFields.Add (item.Key, Boolean.Parse (values.First ()));
						}
					}
				}
			}
			// se não for admin, limita os resultados para as crudGroup vinculadas a empresa do usuário
			int? crudGroupOwner = login.user.CrudGroupOwner;

			if (crudGroupOwner != 1) {
				if (serviceFields.ContainsKey ("crudGroupOwner")) {
					queryFields["crudGroupOwner"] = crudGroupOwner;
				} else if (serviceFields.ContainsKey ("crudGroup")) {
					queryFields["crudGroup"] = login.groups;
				}
			}

			return queryFields;
		}
		// public
		static public Task<ActionResult> ProcessQuery<T>(IIdentity user, HttpRequest uriInfo, DbContext entityManager, String serviceName) where T : class {
			IQueryCollection queryParameters = uriInfo.Query;
			DbUtils.QueryMap fields = RequestFilter.ParseQueryParameters((LoginResponse)user, serviceName, queryParameters);
            String[] orderBy = null;

			{
				CrudService service = RequestFilter.GetService (user, serviceName);

				if (service.OrderBy != null) {
					orderBy = service.OrderBy.Split (',');
				}
			}

			int? startPosition = null;
			int? maxResult = null;
			startPosition = queryParameters["start"].Count == 1 ? int.Parse(queryParameters["start"]) : startPosition;
			maxResult = queryParameters["max"].Count == 1 ? int.Parse(queryParameters["max"]) : maxResult;
			Type entityClass = RequestFilter.mapClass[serviceName];
			return DbUtils.Find<T>(entityManager, entityClass, fields, orderBy, startPosition, maxResult).ContinueWith(taskResults => {
				if (taskResults.Exception != null) {
					return Response.BadRequest("ProcessQuery.Find : " + taskResults.Exception.Message);
				}

				ActionResult response = Response.Ok(taskResults.Result);
				return response;
			});
        }

		public class ImplSecurityContext : ClaimsPrincipal {
			private readonly LoginResponse loginResponse;

			public ImplSecurityContext (LoginResponse loginResponse)
			{
				this.loginResponse = loginResponse;
				this.AddIdentity (loginResponse);
			}

			override
			public bool IsInRole (string role) => this.loginResponse.servicesNames.Contains (role);
		}
// processRequest
// main
		private ActionResult ProcessRequest (HttpContext requestContext, DbContext entityManager, String serviceName, String uriPath) {
// crudProcess
			Func<LoginResponse, ActionResult> crudProcess = login => {
				Type objectClass = RequestFilter.mapClass[serviceName];
				HttpRequest uriInfo = requestContext.Request;
				Object obj = null;

				if (uriPath.Equals("create") || uriPath.Equals("update")) {
					try {
						var str = new StreamReader(requestContext.Request.Body).ReadToEnd();
						obj = JsonConvert.DeserializeObject(str, objectClass, new JsonSerializerSettings { ContractResolver = new CamelCasePropertyNamesContractResolver() });
					} catch (Exception e) {
						return Response.InternalServerError(e.Message);
					}
				}

				Task<ActionResult> cf;

				if (uriPath.Equals ("create")) {
					cf = RequestFilter.ProcessCreate<dynamic> (login, entityManager, serviceName, obj);
				} else if (uriPath.Equals ("update")) {
					cf = RequestFilter.ProcessUpdate<dynamic> (login, uriInfo, entityManager, serviceName, obj);
				} else if (uriPath.Equals ("delete")) {
					cf = RequestFilter.ProcessDelete<dynamic> (login, uriInfo, entityManager, serviceName);
				} else if (uriPath.Equals ("read")) {
					cf = RequestFilter.ProcessRead<dynamic> (login, uriInfo, entityManager, serviceName);
				} else if (uriPath.Equals("query")) {
					cf = RequestFilter.ProcessQuery<dynamic>(login, uriInfo, entityManager, serviceName);
				} else {
					return null;
				}

				cf.Wait();

				if (cf.Exception != null) {
					return Response.InternalServerError(cf.Exception.Message);
				} else {
					return cf.Result;
				}
			};

			Func<CrudUser, Boolean?> authorization = user => {
				Boolean? access = null;
				JObject json = JObject.Parse(user.Roles);
				// verfica a permissao de acesso
                if (json[serviceName] != null) {
                    var serviceAuth = json[serviceName];

					if (serviceAuth[uriPath] != null) {
						access = serviceAuth[uriPath].Value<Boolean?>();
					}
                }

				return access;
			};

			Func<ActionResult> authWithToken = () => {
				ActionResult response;
				String authorizationHeader = requestContext.Request.Headers["Authorization"];

				if (authorizationHeader != null && authorizationHeader.StartsWith ("Token ")) {
					String token = authorizationHeader.Substring (6);
					LoginResponse login = RequestFilter.logins[token];

					if (login != null) {
						Boolean? access = authorization (login.user);

						if (access != false) {
							ClaimsPrincipal securityContextOld = requestContext.User;

                            if (securityContextOld == null || securityContextOld.Identity == null || securityContextOld.Identity.Name == null) {
                                ImplSecurityContext securityContext = new ImplSecurityContext(login);
                                requestContext.User = securityContext;

								if (access == true) {
									response = crudProcess(login);
								} else {
									response = null;
								}
                            } else {
								response = Response.BadRequest("Already Identity found : " + securityContextOld.Identity.Name);
                            }
						} else {
							response = Response.Unauthorized("Explicit Unauthorized");
						}
					} else {
						response = Response.Unauthorized("Authorization replaced by new login in another session");
					}
				} else {
					response = Response.Unauthorized("Authorization token header invalid");
				}

				return response;
			};

			return authWithToken();
		}
		// private
		private Task<ActionResult> AuthenticateByUserAndPassword (DbContext entityManager, HttpContext requestContext, String ip) {
			JObject json = JObject.Parse (new StreamReader (requestContext.Request.Body).ReadToEnd ());
			String userId = json.GetValue ("userId").Value<String> ();
			String password = json.GetValue("password").Value<String>();
            DbUtils.QueryMap userQuery = DbUtils.QueryMap.Create ().AddNext ("name", userId).AddNext ("password", password);

			return DbUtils.FindOne<CrudUser> (entityManager, null, userQuery).ContinueWith<ActionResult> (taskFindUser => {
				if (taskFindUser.Exception != null) {
					return Response.Unauthorized("don't found match user and password : " + taskFindUser.Exception.InnerException.Message);
				}

				CrudUser user = taskFindUser.Result;
				String token = Guid.NewGuid ().ToString ();
				user.Authctoken = token;
				user.Ip = ip;
				return DbUtils.Update<CrudUser> (null, entityManager, user).ContinueWith<ActionResult> (taskUpdateUser => {
					if (taskUpdateUser.Result == null) {
						return Response.InternalServerError ("don't update user : " + taskUpdateUser.Exception.Message);
					}

					CrudUser userAfterUpdate = taskUpdateUser.Result;
					return LoginResponse.Load (userAfterUpdate, entityManager).ContinueWith<ActionResult> (taskLoginResponse => {
						if (taskLoginResponse.Result == null) {
							return Response.InternalServerError ("don't load login : " + taskLoginResponse.Exception);
						}

						LoginResponse loginResponse = taskLoginResponse.Result;
						RequestFilter.logins.Add (token, loginResponse);
						return Response.Ok (loginResponse);
					}).Result;
				}).Result;
			});
		}
		// filter
		public async Task InvokeAsync (HttpContext requestContext) {
			Task Respond (ActionResult actionResult) {
				ContentResult contentResult = (Microsoft.AspNetCore.Mvc.ContentResult)actionResult;
				requestContext.Response.StatusCode = (int)contentResult.StatusCode;

				if (contentResult.ContentType != null) {
					requestContext.Response.ContentType = contentResult.ContentType;
				}

				if (contentResult.Content != null) {
					return requestContext.Response.WriteAsync (contentResult.Content);
				} else {
					return Task.CompletedTask;
				}
			}

			String method = requestContext.Request.Method;
			Console.WriteLine (String.Format ("method : {0}", method));
			// When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
			if (method.Equals ("OPTIONS")) {
				// Just send a OK signal back to the browser	
				requestContext.Response.StatusCode = 200;
				// TODO : habilitar somente os IPs dos servidores instalados nas empresas
                //responseContext.getHeaders().add("Access-Control-Allow-Origin", "*"); // USE * for all, https://localhost:9443
                //responseContext.getHeaders().add("Access-Control-Allow-Methods", "PUT,DELETE"); // GET, POST, HEAD, OPTIONS
                //responseContext.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
                //// responseContext.getHeaders().add("Access-Control-Expose-Headers", "Location, Content-Disposition");
                //// responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true" );
				return;
			}

			String ip = requestContext.Connection.RemoteIpAddress.ToString ();
			String uri = requestContext.Request.Path.ToUriComponent ();
			String[] paths = uri.Split ('/');
			String root = paths [1];
			String resource = null;
			String action = null;

			if (root.Equals ("rest") == true) {
				if (paths.Length > 2) {
					resource = CaseConvert.UnderscoreToCamel (paths [2], false);

					if (paths.Length > 3) {
						action = paths [3];
					}
				}
			}

			Console.WriteLine (String.Format ("RemoteAddr : {0}", ip));
			Console.WriteLine (String.Format ("uri : {0}", uri));
			Console.WriteLine (String.Format ("root : {0}", root));
			Console.WriteLine (String.Format ("resource : {0}", resource));
			Console.WriteLine (String.Format ("action : {0}", action));
			DbContext entityManager = (DbContext)requestContext.RequestServices.GetService (typeof (CrudContext));

			// no login pede usuário e senha
			if (root.Equals("rest")) {
				if (resource.Equals("authc")) {
					await this.AuthenticateByUserAndPassword(entityManager, requestContext, ip).ContinueWith<Task>(taskAuthenticate => {
						ActionResult response = taskAuthenticate.Result;
						return Respond(response);
					}).Result;
					// em qualquer outro método pede o token
				} else {
					ActionResult response = this.ProcessRequest(requestContext, entityManager, resource, action);

					if (response != null) {
						await Respond(response);
					} else {
						await this.next(requestContext);
					}
				}
			} else if (root.Equals("websocket")) {
				if (requestContext.WebSockets.IsWebSocketRequest) {
					await requestContext.WebSockets.AcceptWebSocketAsync().ContinueWith<WebSocketMessageType>(taskAccept => {
						WebSocket webSocket = taskAccept.Result;
						var buffer = new byte[1024 * 4];
						// READ TOKEN
						return webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None).ContinueWith<WebSocketMessageType>(taskToken => {
							if (taskToken.Result.MessageType == WebSocketMessageType.Text) {
								String token = Encoding.UTF8.GetString(buffer, 0, taskToken.Result.Count);
								this.OnMessage(webSocket, token);
								// READ DISCONECT
                                return webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None).ContinueWith<WebSocketMessageType>(taskClose => {
                                    if (taskClose.Result.MessageType == WebSocketMessageType.Close) {
										this.OnClose(webSocket, token);
                                    }

                                    return taskClose.Result.MessageType;
                                }).Result;
							} else {
								return taskToken.Result.MessageType;
							}
						}).Result;
					});
                } else {  
					requestContext.Response.StatusCode = StatusCodes.Status400BadRequest;  
                }  
			} else {
				await this.next(requestContext);
			}
			// Call the next delegate/middleware in the pipeline
		}

		public void OnMessage(WebSocket session, String token) {
			LoginResponse login = RequestFilter.logins[token];

            if (login != null) {
				RequestFilter.clients.Add(token, session);
                Console.WriteLine("New websocket session opened: token : " + token + ", id : " + session);
            }
        }
		// remove the session after it's closed
		public void OnClose(WebSocket session, String token) {
            LoginResponse login = RequestFilter.logins[token];

            if (login != null) {
                Console.WriteLine("Websoket session closed: " + token);
				RequestFilter.clients.Remove(token);
				RequestFilter.logins.Remove(token);
            }
        }
		// This method sends the same Bidding object to all opened sessions
		public static void Notify (Object obj, String serviceName, Boolean isRemove) {
			JObject serviceFields = JObject.Parse(RequestFilter.mapService[serviceName].Fields);

			Func<JObject> GetPrimaryKey = () => {
				JObject primaryKeyBuilder = new JObject();

				foreach (var field in serviceFields) {
					if (field.Value["primaryKey"] != null && field.Value.Value<Boolean> ("primaryKey") == true) {
						Object value = obj.GetType().GetProperty(CaseConvert.UnderscoreToCamel(field.Key, true)).GetValue(obj);

						if (value is String) {
							primaryKeyBuilder.Add (field.Key, (String)value);
						} else if (value is int) {
							primaryKeyBuilder.Add (field.Key, (int)value);
						} else if (value is Boolean) {
							primaryKeyBuilder.Add (field.Key, (Boolean)value);
						}
					}
				}

				return primaryKeyBuilder;
			};

			JObject primaryKey = GetPrimaryKey();
			JObject msg = new JObject ();
			msg.Add("service", serviceName);
			msg.Add("primaryKey", primaryKey);

			if (isRemove == false) {
				msg.Add("action", "notify");
			} else {
				msg.Add("action", "delete");
			}

			String str = msg.ToString(Formatting.Indented);
			int? objCrudGroupOwner = primaryKey.ContainsKey ("crudGroupOwner") ? primaryKey.Value<int?>("crudGroupOwner") : null;
			int? crudGroup = null;

			if (serviceFields.ContainsKey("crudGroup")) {
				crudGroup = (int) obj.GetType().GetProperty("CrudGroup").GetValue(obj);
			}

			foreach (var item in RequestFilter.clients) {
				RequestFilter.LoginResponse login = RequestFilter.logins[item.Key];
				int? userCrudGroupOwner = login.user.CrudGroupOwner;
				// enviar somente para os clients de "crudGroupOwner"
				if (objCrudGroupOwner == null || userCrudGroupOwner == 1 || objCrudGroupOwner == userCrudGroupOwner) {
					// restrição de crudGroup
					if (crudGroup == null || login.groups.IndexOf((int)crudGroup) >= 0) {
						// envia somente para os usuários com acesso ao serviço alterado
						if (login.websocketServices.Contains (serviceName)) {
							var bytes = System.Text.Encoding.UTF8.GetBytes (str);
							item.Value.SendAsync (new System.ArraySegment<byte> (bytes), WebSocketMessageType.Text, true, CancellationToken.None).ContinueWith(taskWS => {
								if (taskWS.Exception != null) {
									Console.WriteLine ("notify error, user {0} : {1}\n", login.user.Name, taskWS.Exception.InnerException.Message);
								} else {
									Console.WriteLine ("notify successful, user {0} : {1}\n", login.user.Name, msg);
								}
							});
						}
					}
				}
			}
	    }

		public static void UpdateCrudServices (DbContext entityManager) {

			Func<Type, String, String> generateFieldsStr = (entityClass, strFields) => {
				var fields = entityClass.GetProperties();
				JObject jsonOriginal = JObject.Parse(strFields);
				JObject jsonBuilder = new JObject();

				foreach (var field in fields) {
					String fieldName = CaseConvert.UnderscoreToCamel(field.Name, false);
					ColumnAttribute column = field.GetCustomAttribute<ColumnAttribute> ();
					ForeignKeyAttribute foreignKey  = field.GetCustomAttribute<ForeignKeyAttribute>();
					DisplayAttribute display = field.GetCustomAttribute<DisplayAttribute>();
					MaxLengthAttribute maxLength = field.GetCustomAttribute<MaxLengthAttribute>();
					EditableAttribute editable = field.GetCustomAttribute<EditableAttribute>();
					FilterUIHintAttribute filterUIHint = field.GetCustomAttribute<FilterUIHintAttribute>();

					String typeDesc = field.PropertyType.FullName;

					if (typeDesc.Contains(".String")) {
						typeDesc = "s";
					} else if (typeDesc.Contains(".Int32")) {
						typeDesc = "i";
					} else if (typeDesc.Contains(".Boolean")) {
						typeDesc = "b";
					} else if (typeDesc.Contains(".Decimal")) {
						typeDesc = "n3"; // numero com separação de milhar com casas decimais

						if (column != null && column.TypeName != null) {
							if (column.TypeName.Equals("numeric(9,2)")) {
								typeDesc = "n2"; // numero com separação de cents
							} else if (column.TypeName.Equals("numeric(9,1)")) {
								typeDesc = "n1"; // numero com separação de decimais
							}
						}
					} else if (typeDesc.Contains(".DateTime")) {
						typeDesc = "datetime-local"; // data e hora completa
					} else if (typeDesc.Contains(".Date")) {
						typeDesc = "date"; // data
					} else if (typeDesc.Contains(".Time")) {
						typeDesc = "time"; // hora completa
					} else {
						Console.WriteLine ("{0} : {1} : Unknow type : {2}", entityClass.Name, field.Name, typeDesc);
						continue;
					}
					// type (columnDefinition), readOnly, hiden, primaryKey, required (insertable), updatable, defaultValue, length, precision, scale 
					JObject jsonNewValue = new JObject ();

					if (typeDesc != null) {
						jsonNewValue.Add("type", typeDesc);
					}
					
					//if (column != null && column.updatable() == false) {
					//	jsonBuilderValue.Add("updatable", false);
					//}
					
					if (maxLength != null && maxLength.Length != 255) {
						jsonNewValue.Add("length", maxLength.Length);
					}
					
					//if (column != null && column.precision() != 0) {
					//	jsonBuilderValue.Add("precision", column.precision());
					//}
					
					//if (column != null && column.scale() != 0) {
					//	jsonBuilderValue.Add("scale", column.scale());
					//}

					if (fieldName.Equals("crudGroupOwner") && entityClass.Name.Equals("CrudGroupOwner") == false) {
                        jsonNewValue.Add("hiden", true);
                    }
                    
					if (field.GetCustomAttribute<KeyAttribute> () != null) {
						jsonNewValue.Add("primaryKey", true);
					}
					
					if (filterUIHint != null && filterUIHint.ControlParameters.ContainsKey("defaultValue")) {
                        jsonNewValue.Add("defaultValue", (String)filterUIHint.ControlParameters["defaultValue"]);
                    }

					if (filterUIHint != null && filterUIHint.ControlParameters.ContainsKey("isClonable")) {
						jsonNewValue.Add("isClonable", (String)filterUIHint.ControlParameters["isClonable"]);
                    }

					if (filterUIHint != null && filterUIHint.ControlParameters.ContainsKey("options")) {
						jsonNewValue.Add("options", (String)filterUIHint.ControlParameters["options"]);
                    }

					if (field.GetCustomAttribute<RequiredAttribute>() != null || (field.GetCustomAttribute<KeyAttribute> () != null && field.GetCustomAttribute<DatabaseGeneratedAttribute> () == null)) {
                        jsonNewValue.Add("required", true);
                    }

					if (foreignKey != null && foreignKey.Name != null) {
						jsonNewValue.Add("service", CaseConvert.UnderscoreToCamel(foreignKey.Name, false));
                    }
                    
					if (display != null && display.Name != null) {
						jsonNewValue.Add("title", display.Name);
                    }
                    
					if (field.GetCustomAttribute<DatabaseGeneratedAttribute>() != null) {
                        jsonNewValue.Add("hiden", true);
                    }
                    
					if (editable != null && editable.AllowEdit == false) {
						jsonNewValue.Add("readOnly", true);
                    }

					if (jsonOriginal.ContainsKey(fieldName)) {
						JToken jsonOldValue = jsonOriginal.GetValue (fieldName);
						JObject jsonValue = new JObject ();
						jsonValue.Merge(jsonNewValue);
						jsonValue.Merge(jsonOldValue);
						jsonBuilder.Add (fieldName, jsonValue);
						// SHOW WARNING
						{
							String oldFieldStr = jsonOldValue.ToString (Formatting.Indented);
							String newFieldStr = jsonNewValue.ToString (Formatting.Indented);

							if (oldFieldStr.Equals (newFieldStr) == false) {
								Console.WriteLine ("{0} : {1} : diference (database data / classes data) :", entityClass.Name, field.Name);
								Console.WriteLine (oldFieldStr);
								Console.WriteLine (newFieldStr);
							}
						}
					} else {
						jsonBuilder.Add (fieldName, jsonNewValue);
					}
				}

				return jsonBuilder.ToString(Formatting.None);
			};

			foreach (var assembly in AppDomain.CurrentDomain.GetAssemblies()) {
				foreach(Type entityClass in assembly.GetTypes()) {
					if (entityClass.GetCustomAttributes(typeof(TableAttribute), true).Length > 0) {
						String serviceName = CaseConvert.UnderscoreToCamel (entityClass.Name, false);
						CrudService service = entityManager.Find<CrudService> (serviceName);

			        	if (service != null) {
							String oldFields = service.Fields;
							String newFields = generateFieldsStr(entityClass, oldFields);

							if (oldFields.Equals(newFields) == false) {
								service.Fields = newFields;
                                entityManager.Update(service);
								entityManager.SaveChanges();
							}
			        	} else {
			        		service = new CrudService ();
							service.Name = serviceName;
							service.Fields = generateFieldsStr(entityClass, "{}");
							entityManager.Add(service);
							entityManager.SaveChanges();
			        	}

						RequestFilter.mapClass.Add(serviceName, entityClass);
						RequestFilter.mapService.Add (serviceName, service);
		        	}
	    		}
			}
		}
	}
    
}
