import {Response} from "./server-utils.js";
import {CaseConvert} from "../../../webapp/es6/CaseConvert.js";

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
class LoginResponse {
// Load Company, Services and Categories
	static load(user, entityManager) {
		let loginResponse = new LoginResponse (user);
		return entityManager.findOne("crud_company", {"id": user.company})
		.catch(error => {
			throw new Error("don't get user company : " + error.message);
		})
		.then(company => {
			loginResponse.title = company.name + " - " + user.name;
			// TODO : código temporário para caber o na tela do celular
			loginResponse.title = user.name;

			return entityManager.find("crud_service", {"name": loginResponse.servicesNames}, null)
			.catch(error => {
				throw new Error("don't get user services : " + error.message);
			})
			.then(services => {
				loginResponse.crudServices = services;
				// Add Categories
				return entityManager.find("category_company", {"company": user.company}, null)
				.catch(error => {
					throw new Error("don't match request category for user company : " + error.message);
				})
				.then(categories => {
					loginResponse.categories = [];

					for (let categoryCompany of categories) {
						loginResponse.categories.push(categoryCompany.id);
					}

					return loginResponse;
				});
			});
		});
	}

	constructor(user) {
		this.user = user;
		this.servicesNames = [];
		this.crudServices = [];
		this.websocketServices = [];
		var roles = JSON.parse(user.roles);

		for (let key in roles) {
			this.servicesNames.push(key);
			var jsonAccess = roles[key];
			// verfica a permissao de aviso de alterações via websocket
			if (jsonAccess.read == true) {
				this.websocketServices.push(key);
			}
		}
	}

}

export class RequestFilter {

	constructor(appRestExpress, entityManager) {
//		debugger;
		this.entityManager = entityManager;
		appRestExpress.all("*", (req, res, next) => this.filter(req, res, next));
		
		appRestExpress.options("/", (req, res, next) => {
			return next();
		});
		//Add headers
		appRestExpress.use((req, res, next) => this.filterHeaders(req, res, next));
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
		const userCompany = login.user.company;

		if (userCompany > 1 && serviceFields.company != undefined) {
			let objCompany = obj.company;

			if (objCompany == undefined) {
				obj.company = userCompany;
				objCompany = userCompany;
			}

			if (objCompany == userCompany) {
				if (serviceFields.category != undefined) {
					const category = obj["category"];

					if (login.categories.indexOf(category) < 0) {
						response = Response.unauthorized("unauthorized object category");
					}
				}
			} else {
				response = Response.unauthorized("unauthorized object company");
			}
		}

		return response;
	}
	//
	static getService(user, serviceName) {
		serviceName = CaseConvert.underscoreToCamel (serviceName, false);
		const login = user;

		if (login.servicesNames.indexOf (serviceName) < 0) {
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
			RequestFilter.notify(newObj, serviceName, false);
			return Response.ok(newObj);
		});
	}
	// public
	static getObject(user, uriInfo, entityManager, serviceName) {
		return entityManager.findOne(serviceName, RequestFilter.parseQueryParameters(user, serviceName, uriInfo.query)).catch(error => {
			throw new Error("fail to find object with company, category and query parameters related : " + error.message);
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
				RequestFilter.notify(newObj, serviceName, false);
				return Response.ok(newObj);
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
		// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
		let company = login.user.company;

		if (company != 1) {
			if (fields["company"] != undefined) {
				queryFields["company"] = company;
			} else if (fields["category"] != undefined) {
				queryFields["category"] = login.categories;
			}
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
	// processRequest
	processRequest(req, entityManager, serviceName, uriPath) {
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

		let authorization = user => {
			let access = null;
			const json = JSON.parse(user.roles);
			// verfica a permissao de acesso
			if (json[serviceName] != undefined) {
				const serviceAuth = json[serviceName];

				if (serviceAuth[uriPath] != undefined) {
					access = serviceAuth[uriPath];
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
					const access = authorization(login.user);

					if (access != false) {
						req.user = login;

						if (access == true) {
							return crudProcess(login);
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
		const uri = req.originalUrl;
		const paths =  req.path.split("/");
		const root = paths [1];
		let resource = null;
		let action = null;

		if (root == "rest") {
			if (paths.length > 2) {
				resource = CaseConvert.underscoreToCamel (paths [2], false);

				if (paths.length > 3) {
					action = paths [3];
				}
			}
		}

		console.log ("RemoteAddr : ", ip);
		console.log ("uri : ", uri);
		console.log ("root : ", root);
		console.log ("resource : ", resource);
		console.log ("action : ", action);
		// no login pede usuário e senha
		if (root == "rest") {
			if (resource == "authc") {
				const promisseResponse = this.authenticateByUserAndPassword(this.entityManager, req, ip);
				promisseResponse.then(response => res.status(response.status).send(response.data));
				// em qualquer outro método pede o token
			} else {
				const promisseResponse = this.processRequest(req, this.entityManager, resource, action);
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

	filterHeaders(req, res, next) {
	    // Website you wish to allow to connect
	    res.setHeader('Access-Control-Allow-Origin', '*');
	    // Request methods you wish to allow
	    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
	    // Request headers you wish to allow
	    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,Content-Type,Authorization,Origin');
	    next();
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
		let objCompany = primaryKey["company"];
		let category = undefined;

		if (serviceFields["category"]) {
			category = obj["category"];
		}

		for (let [key, value] of RequestFilter.clients) {
			let login = RequestFilter.logins.get(key);
			let userCompany = login.user.company;
			// enviar somente para os clients de "company"
			if (objCompany == undefined || userCompany == 1 || objCompany == userCompany) {
				// restrição de categoria
				if (category == undefined || login.categories.indexOf(category) >= 0) {
					// envia somente para os usuários com acesso ao serviço alterado
					if (login.websocketServices.indexOf(serviceName) >= 0) {
						Promise.resolve().then(() => {
							console.log("notify user ", login.user.name, ":", msg);
							value.sendUTF(str)
						});
					}
				}
			}
		}
    }
    // public
    static updateCrudServices(entityManager) {
        return entityManager.find("crud_service").then(services => {
            for (let service of services) {
                RequestFilter.mapService.set(service.name, service);
            }
        });
	}
}

RequestFilter.logins = new Map();
RequestFilter.mapService = new Map();
RequestFilter.clients = new Map();
