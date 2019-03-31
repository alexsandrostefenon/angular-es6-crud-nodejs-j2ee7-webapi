import {Response} from "./server-utils.js";
import {CaseConvert} from "../../../webapp/es6/crud/CaseConvert.js";
import {DbClientPostgres} from "./dbClientPostgres.js";

function getPrimaryKeys(service) {
	var orderBy = [];
	const fields = JSON.parse(service.fields);

	for (var fieldName in fields) {
		var field = fields[fieldName];

		if (field.primaryKey == true) {
			orderBy.push(fieldName);
		}
	}

	return orderBy;
}

function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }

  return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}
// Class LoginResponse
// Response with login data access to client
class LoginResponse {
// Load crudGroupOwner, Services and Groups
	static load(user, entityManager) {
		let loginResponse = new LoginResponse (user);
		const foreignKey = RequestFilter.getForeignKey("crudUser", "crudGroupOwner", user);
		return entityManager.findOne("crud_group_owner", foreignKey)
		.catch(error => {
			throw new Error("don't get user crudGroupOwner : " + error.message);
		})
		.then(crudGroupOwner => {
			loginResponse.title = crudGroupOwner.name + " - " + user.name;
			// TODO : código temporário para caber o na tela do celular
			loginResponse.title = user.name;
			loginResponse.roles = JSON.parse(user.roles);
			const serviceNames = Object.keys(loginResponse.roles);
			return entityManager.find("crud_service", {"name": serviceNames}, null)
			.catch(error => {
				throw new Error("don't get user services : " + error.message);
			})
			.then(services => {
				services.sort((a,b) => serviceNames.indexOf(a.name) - serviceNames.indexOf(b.name));
				loginResponse.crudServices = services;
				// Add Groups
				return entityManager.find("crud_group_user", {"crudUser": user.id}, null)
				.catch(error => {
					throw new Error("don't match request crud_group for user : " + error.message);
				})
				.then(userGroups => {
					loginResponse.groups = [];

					for (let userGroup of userGroups) {
						loginResponse.groups.push(userGroup.crudGroup);
					}

					return loginResponse;
				});
			});
		});
	}

	constructor(user) {
		this.user = user;
		this.crudServices = [];
	}

}

class RequestFilter {
	static getForeignKeyEntries(serviceName, foreignServiceName) {
		const serviceFields = JSON.parse(RequestFilter.mapService.get(serviceName).fields);
		let foreignKeyEntries = [];

		for (let [fieldName, field] of Object.entries(serviceFields)) {
			if (field.foreignKeysImport != undefined && field.foreignKeysImport.table == foreignServiceName) {
				foreignKeyEntries.push({fieldName, field});
			}
		}

		return foreignKeyEntries;
	}

	static getForeignKey(serviceName, foreignServiceName, obj) {
		const foreignKeyEntries = RequestFilter.getForeignKeyEntries(serviceName, foreignServiceName);
		let foreignKey = {};

		for (let foreignKeyEntry of foreignKeyEntries) {
			foreignKey[foreignKeyEntry.field.foreignKeysImport.field] = obj[foreignKeyEntry.fieldName];
		}

		return foreignKey;
	}

	constructor(appRestExpress, entityManager) {
		this.appRestExpress = appRestExpress;
		this.entityManager = entityManager;
		appRestExpress.all("*", (req, res, next) => this.filter(req, res, next));
	}
// private to create,update,delete,read
	static checkObjectAccess(user, serviceName, obj) { // LoginResponse login, EntityManager entityManager, Object obj
		let service;

		try {
			service = RequestFilter.getService (user, serviceName);
		} catch (e) {
			return Response.unauthorized(e.Message);
		}

		const login = user;
		const serviceFields = JSON.parse(service.fields);
		let response = null;
		const userCrudGroupOwner = RequestFilter.getForeignKey("crudUser", "crudGroupOwner", login.user);
		const crudGroupOwnerEntries = RequestFilter.getForeignKeyEntries(serviceName, "crudGroupOwner");

		if (userCrudGroupOwner.id > 1 && crudGroupOwnerEntries.length > 0) {
			const objCrudGroupOwner = RequestFilter.getForeignKey(serviceName, "crudGroupOwner", obj);

			if (objCrudGroupOwner.id == undefined) {
				obj.crudGroupOwner = userCrudGroupOwner.id;
				objCrudGroupOwner.id = userCrudGroupOwner.id;
			}

			if (objCrudGroupOwner.id == userCrudGroupOwner.id) {
				const crudGroupEntries = RequestFilter.getForeignKeyEntries(serviceName, "crudGroup");

				if (crudGroupEntries.length > 0) {
					const crudGroup = RequestFilter.getForeignKey(serviceName, "crudGroup", obj);

					if (login.groups.indexOf(crudGroup.id) < 0) {
						response = Response.unauthorized("unauthorized object crudGroup");
					}
				}
			} else {
				response = Response.unauthorized("unauthorized object crudGroupOwner");
			}
		}

		return response;
	}
	//
	static getService(login, serviceName) {
		serviceName = CaseConvert.underscoreToCamel (serviceName, false);

		if (login.roles[serviceName] == undefined) {
			throw new Exception ("Unauthorized service Access");
		}

		const service = RequestFilter.mapService.get(serviceName);
		return service;
	}
	// public
	static processCreate(user, entityManager, serviceName, obj) {
		const response = RequestFilter.checkObjectAccess(user, serviceName, obj);

		if (response != null) Promise.resolve(response);

		return entityManager.insert(serviceName, obj).then(newObj => {
			const primaryKey = RequestFilter.notify(newObj, serviceName, false);
			// force read, cases of triggers before break result value
			return entityManager.findOne(serviceName, primaryKey).then(_obj => Response.ok(_obj));
		});
	}
	// public
	static getObject(user, uriInfo, entityManager, serviceName) {
		return entityManager.findOne(serviceName, RequestFilter.parseQueryParameters(user, serviceName, uriInfo.query)).catch(error => {
			throw new Error("fail to find object with crudGroup and query parameters related : " + error.message);
		});
	}
	// public processRead
	static processRead(user, uriInfo, entityManager, serviceName) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).then(obj => Response.ok(obj));
	}
	// public processUpdate
	static processUpdate(user, uriInfo, entityManager, serviceName, obj) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).then(oldObj => {
			const response = RequestFilter.checkObjectAccess(user, serviceName, obj);

			if (response != null) return Promise.resolve(response);

			return entityManager.update(serviceName, RequestFilter.parseQueryParameters(user, serviceName, uriInfo.query), obj).then(newObj => {
				const primaryKey = RequestFilter.notify(newObj, serviceName, false);
				// force read, cases of triggers before break result value
				return entityManager.findOne(serviceName, primaryKey).then(_obj => Response.ok(_obj));
			});
		});
	}
	// public processDelete
	static processDelete(user, uriInfo, entityManager, serviceName) {
		return RequestFilter.getObject(user, uriInfo, entityManager, serviceName).then(obj => {
			return entityManager.deleteOne(serviceName, RequestFilter.parseQueryParameters(user, serviceName, uriInfo.query)).then(objDeleted => {
				RequestFilter.notify(objDeleted, serviceName, true);
				return Response.ok(objDeleted);
			});
		});
	}
	// private
	static parseQueryParameters(login, serviceName, queryParameters) {
		const service = RequestFilter.getService (login, serviceName);
		let queryFields = {};
		const fields = JSON.parse(service.fields);

		for (var fieldName in fields) {
			var field = fields[fieldName];

			if (field.primaryKey == true) {
				const value = queryParameters[fieldName];
				
				if (value != undefined) {
					const type = field["type"];
					
					if (type == undefined || type == "s") {
		    			queryFields[fieldName] = value;
					} else if (type == "n" || type == "i") {
		    			queryFields[fieldName] = Number.parseInt(value);
					} else if (type == "b") {
		    			queryFields[fieldName] = (value == "true");
					}
				}
			}
		}
		// se não for admin, limita os resultados para as crudGroup vinculadas a empresa do usuário
		const userCrudGroupOwner = RequestFilter.getForeignKey("crudUser", "crudGroupOwner", login.user);
		const crudGroupOwnerEntries = RequestFilter.getForeignKeyEntries(serviceName, "crudGroupOwner");
		const crudGroupEntries = RequestFilter.getForeignKeyEntries(serviceName, "crudGroup");

		if (userCrudGroupOwner.id > 1) {
			if (crudGroupOwnerEntries.length > 0) queryFields[crudGroupOwnerEntries[0].fieldName] = userCrudGroupOwner.id;
			if (crudGroupEntries.length > 0) queryFields[crudGroupEntries[0].fieldName] = login.groups;
		}

		return queryFields;
   	}
	// public
	static processQuery(user, uriInfo, entityManager, serviceName) {
		const fields = RequestFilter.parseQueryParameters(user, serviceName, uriInfo.query);
		let orderBy = null;

		{
			const service = RequestFilter.getService (user, serviceName);

			if (service.orderBy != undefined && service.orderBy != null) {
				orderBy = service.orderBy.split(",");
			}
		}


		return entityManager.find(serviceName, fields, orderBy).then(results =>
			Response.ok(results)
		);
	}
	// verify for dedicated EndPoint
	verifyDedicatedRoute(req) {
		let found = false;
		
		mainLoop: for (let item of this.appRestExpress._router.stack) {
			if (item.name == "router" && req.path.match(item.regexp) != null) {
				for (let subItem of item.handle.stack) {
					const path = req.path.substring(req.path.lastIndexOf("/"));
//					console.log(`${path} match (${subItem.regexp}) :`, path.match(subItem.regexp), "subItem :", subItem);
					if (path.match(subItem.regexp) != null) {
						found = true;
						break mainLoop;
					}
				}
			}
		}
		
		return found;
	}
	// processRequest
	processRequest(req, res, next, entityManager, serviceName, uriPath) {
		// crudProcess
		let crudProcess = login => {
			const uriInfo = req;
			let obj = null;

			if (uriPath == "create" || uriPath == "update") {
				obj = req.body;
			}

			let cf;

			if (uriPath == "create") {
				cf = RequestFilter.processCreate(login, entityManager, serviceName, obj);
			} else if (uriPath == "update") {
				cf = RequestFilter.processUpdate(login, uriInfo, entityManager, serviceName, obj);
			} else if (uriPath == "delete") {
				cf = RequestFilter.processDelete(login, uriInfo, entityManager, serviceName);
			} else if (uriPath == "read") {
				cf = RequestFilter.processRead(login, uriInfo, entityManager, serviceName);
			} else if (uriPath == "query") {
				cf = RequestFilter.processQuery(login, uriInfo, entityManager, serviceName);
			} else {
				return Promise.resolve(null);
			}

			return cf.catch(error => {
				console.log("ProcessRequest error : ", error);
				return Response.internalServerError(error.message);
			});
		};

		let authorization = login => {
			// TODO : if serviceName in crudServices, returned access must be true or false !!!
			let access = null;
			const serviceAuth = login.roles[serviceName];
			// verfica a permissao de acesso
			if (serviceAuth != undefined) {
				const defaultAccess = {query: true, read: true, create: true, update: false, delete: false};

				if (serviceAuth[uriPath] != undefined) {
					access = serviceAuth[uriPath];
				} else {
					access = defaultAccess[uriPath];
				}
			}

			return access;
		};

		let authWithToken = () => {
			let response;
			const authorizationHeader = req.get("Authorization");

			if (authorizationHeader != undefined && authorizationHeader.startsWith("Token ")) {
				const token = authorizationHeader.substring(6);
				const login = RequestFilter.logins.get(token);

				if (login != null) {
					const access = authorization(login);

					if (access != false) {
						req.user = login;

						if (access == true) {
							if (this.verifyDedicatedRoute(req) == false) {
								return crudProcess(login);
							}
						} else {
							response = null;
						}
					} else {
						response = Response.unauthorized("Explicit Unauthorized");
					}
				} else {
					response = Response.unauthorized("Authorization replaced by new login in another session");
				}
			} else {
				response = Response.unauthorized("Authorization token header invalid");
			}

			return Promise.resolve(response);
		};

		return authWithToken();
	}
	// private
	authenticateByUserAndPassword(entityManager, req, ip) {
		const userQuery = {"name":req.body.userId, "password":req.body.password};
		return entityManager.findOne("crud_user", userQuery).then(user => {
			const token = guid();
			user.authctoken = token;
			user.ip = ip;
			return entityManager.update("crud_user", userQuery, {"authctoken": user.authctoken}).then(userAfterUpdate => {
				return LoginResponse.load(userAfterUpdate, entityManager).then(loginResponse => {
					RequestFilter.logins.set(token, loginResponse);
					return Response.ok(loginResponse);
				});
			});
		}).catch(err => {
			if (err.message == "NoResultException") {
				return Response.unauthorized("Don't match user and password.");
			} else {
				return Response.internalServerError(err.message);
			}
		});
	}
	// filter
	filter(req, res, next) {
		//debugger;
		const method = req.method;
		console.log("method : ", method);
		const ip = req.ip;
		const uri = req.baseUrl + req.path;
		const paths =  uri.split("/");
		const root = paths [2];
		let resource = null;
		let action = null;

		if (root == "rest") {
			if (paths.length > 3) {
				resource = CaseConvert.underscoreToCamel (paths [3], false);

				if (paths.length > 4) {
					action = paths [4];
				}
			}
		}

		console.log ("RemoteAddr : ", ip);
		console.log ("uri : ", uri);
		console.log ("root : ", root);
		console.log ("resource : ", resource);
		console.log ("action : ", action);

		res.header("Access-Control-Allow-Origin", "*");
		res.header("Access-Control-Allow-Methods", "GET, PUT, OPTIONS, POST, DELETE");
		res.header("Access-Control-Allow-Headers", req.header('Access-Control-Request-Headers'));
		
		if (req.method === 'OPTIONS') {
			res.send("Ok");
			return;
		}

		// no login pede usuário e senha
		if (root == "rest") {
			if (resource == "authc") {
				const promisseResponse = this.authenticateByUserAndPassword(this.entityManager, req, ip);
				promisseResponse.then(response => res.status(response.status).send(response.data));
				// em qualquer outro método pede o token
			} else {
				const promisseResponse = this.processRequest(req, res, next, this.entityManager, resource, action);
				promisseResponse.then(response => {
					if (response != null) {
						res.status(response.status).send(response.data);
					} else {
						next();
					}
				});
			}
		}

	}

    onMessage(session, token) {
    	const login = RequestFilter.logins.get(token);

		if (login != null) {
			session.token = token;
	        RequestFilter.clients.set(token, session);
		    console.log("New websocket session opened: token : ", token, ", id : ", session.id);
		}
    }
    // remove the session after it's closed
    onClose(session) {
    	const token = session.token;
        const login = RequestFilter.logins.get(token);

		if (login != null) {
		    console.log("Websoket session closed: " + token);
			RequestFilter.clients.delete(token);
			RequestFilter.logins.delete(token);
		}
    }
    // This method sends the same Bidding object to all opened sessions
    static notify(obj, serviceName, isRemove) {
		const serviceFields = JSON.parse(RequestFilter.mapService.get(serviceName).fields);

        let getPrimaryKey = () => {
    		let primaryKeyBuilder = {};
    		
    		for (let fieldName in serviceFields) {
    			let field = serviceFields[fieldName];

    			if (field["primaryKey"] == true) {
    				primaryKeyBuilder[fieldName] = obj[fieldName];
    			}
    		}
    		
    		return primaryKeyBuilder;
        };
        
    	let primaryKey = getPrimaryKey();
		var msg = {};
		msg.service = serviceName;
		msg.primaryKey = primaryKey;

		if (isRemove == false) {
			msg.action = "notify";
		} else {
			msg.action = "delete";
		}

		let str = JSON.stringify(msg);
		const objCrudGroupOwner = RequestFilter.getForeignKey(serviceName, "crudGroupOwner", obj);
		const crudGroup = RequestFilter.getForeignKey(serviceName, "crudGroup", obj);

		for (let [key, value] of RequestFilter.clients) {
			let login = RequestFilter.logins.get(key);
			const userCrudGroupOwner = RequestFilter.getForeignKey("crudUser", "crudGroupOwner", login.user);
			// enviar somente para os clients de "crudGroupOwner"
			let checkCrudGroupOwner = objCrudGroupOwner.id == undefined || objCrudGroupOwner.id == userCrudGroupOwner.id;
			let checkCrudGroup = crudGroup.id == undefined || login.groups.indexOf(crudGroup.id) >= 0;
			// restrição de crudGroup
			if (userCrudGroupOwner.id == 1 || (checkCrudGroupOwner && checkCrudGroup)) {
				let role = login.roles[serviceName];

				if (role != undefined && role.read != false) {
					Promise.resolve().then(() => {
						console.log("notify user ", login.user.name, ":", msg);
						value.sendUTF(str)
					});
				}
			}
		}

		return primaryKey;
    }
    
    static updateCrudServices(entityManager) {
		return entityManager.find("crudService").then(rows => {
			for (let crudService of rows) RequestFilter.mapService.set(crudService.name, crudService);
		});
	}
	
}

RequestFilter.logins = new Map();
RequestFilter.mapService = new Map();
RequestFilter.clients = new Map();

export {RequestFilter}
