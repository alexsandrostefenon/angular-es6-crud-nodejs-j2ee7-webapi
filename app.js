import fs from "fs";
import https from "https";
import express from "express";
import bodyParser from "body-parser";
import websocket from "websocket";
import url from "url";
import {DbClientPostgres} from "./dbClientPostgres.js";
import {Response, MediaType} from "./server-utils.js";
import {CaseConvert} from "./src/main/webapp/es6/CaseConvert.js";

let WebSocketServer = websocket.server;

var dbName = process.argv[2] || "crud";
var webapp = process.argv[3] || "./src/main/webapp";
var portListen = process.argv[4] || 9443;
var fileNamePrivateKey = process.argv[5] || "key.pem";
var fileNameCertificate = process.argv[6] || "cert.pem";

var app = express();
app.use("/" + dbName, express.static(webapp));
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.json());

app.options("/" + dbName + '/rest', (req, res, next) => {
	return next();
});

//Add headers
app.use((req, res, next) => {
    // Website you wish to allow to connect
    res.setHeader('Access-Control-Allow-Origin', '*');
    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,Content-Type,Authorization,Origin');
    next();
});

function getPrimaryKeys(service) {
	var orderBy = [];

	for (var fieldName in service.jsonFields) {
		var field = service.jsonFields[fieldName];

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

class LoginResponse {

	load(dbClient) {
		return dbClient.findOne("crud_company", {"id": this.user.company})
		.catch(error => {
			throw new Error("don't get user company : " + error.message);
		})
		.then(company => {
			this.title = company.name + " - " + this.user.name;
			// TODO : código temporário para caber o na tela do celular
			this.title = this.user.name;
			return dbClient.find("crud_service", {"name": this.servicesNames}, null)
				.catch(error => {
					throw new Error("don't get user services : " + error.message);
				})
				.then(services => {
					this.crudServices = services;
					// TODO : temporary code, until hibernate persist postgresql jsonb type
					for (var service of this.crudServices) {
						service.jsonFields = JSON.parse(service.fields);
					}

					return dbClient.find("category_company", {"company": this.user.company}, null)
					.catch(error => {
						throw new Error("don't match request category for user company : " + error.message);
					})
					.then(categories => {
						this.categories = [];

						for (let categoryCompany of categories) {
							this.categories.push(categoryCompany.id);
						}
					});
				});
		});
	}

	constructor(user) {
		this.user = user;
		this.servicesNames = [];
		this.websocketServices = [];
		var roles = JSON.parse(user.roles);

		for (var key in roles) {
			this.servicesNames.push(key);
			var jsonAccess = roles[key];
			// verfica a permissao de aviso de alterações via websocket
			if (jsonAccess.read == true) {
				this.websocketServices.push(key);
			}
		}
	}
}

class RequestFilter {
	constructor(dbname, app) {
		this.dbClient = new DbClientPostgres(dbName);
    	this.clients = [];
		this.webSocket = this;

		app.post("/" + dbName + '/rest/authc', (req, res, next) => {
			this.authenticateByUserAndPassword(req)
			.catch(error => next(error.message))
			.then(response => res.status(response.status).send(response.data))
			.catch(error => next(error.message));
		});

		app.post("/" + dbName + '/rest/:collectionName/create', (req, res, next) => {
			this.processRequest(req, res, next, req.params.collectionName, "create");
		});

		app.get("/" + dbName + '/rest/:collectionName/read', (req, res, next) => {
			this.processRequest(req, res, next, req.params.collectionName, "read");
		});

		app.put("/" + dbName + '/rest/:collectionName/update', (req, res, next) => {
			this.processRequest(req, res, next, req.params.collectionName, "update");
		});

		app.delete("/" + dbName + '/rest/:collectionName/delete', (req, res, next) => {
			this.processRequest(req, res, next, req.params.collectionName, "delete");
		});

		app.get("/" + dbName + '/rest/:collectionName/query', (req, res, next) => {
			this.processRequest(req, res, next, req.params.collectionName, "query");
		});
	}

	start() {
		return this.dbClient.connect();
	}
	// private to create,update,delete,read
	checkObjectAccess(login, service, obj) { // LoginResponse login, EntityManager entityManager, Object obj
		var response = null;

		if (login.user.company > 1 && service.jsonFields.company != undefined) {
			if (obj.company == undefined) {
				obj.company = login.user.company;
			}

			if (obj.company == login.user.company) {
				if (service.jsonFields.category != undefined) {
					if (login.categories.indexOf(obj.category) < 0) {
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
	processCreate(login, req, service, obj) {
		let response = this.checkObjectAccess(login, service, obj);

		if (response != null) Promise.resolve(response);

		return this.dbClient.insert(service.name, obj).then(newObj => {
			this.notify(newObj, service, false);
			return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
		});
	}
	// public
	getObject(login, req, service) {
		var fields = {};

		for (let fieldName in service.jsonFields) {
			let field = service.jsonFields[fieldName];

			if (field.primaryKey == true) {
				fields[fieldName] = req.query[fieldName];
			}
		}

		var company = login.user.company;

		if (service.jsonFields["company"] != undefined) {
			if (company == 1) {
				// se for admin, direciona a busca para a empresa informada
				company = req.query["company"];
			}

			fields["company"] = company;
		} else if (company != 1 && service.jsonFields["category"] != undefined) {
			// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
			fields["category"] = login.categories;
		}

		return this.dbClient.findOne(service.name, fields)
		.catch(error => {
			throw new Error("fail to find object with company, category and query parameters related : " + error.message);
		});
	}
	// public processRead
	processRead(login, req, service) {
		return this.getObject(login, req, service).then(obj => Response.ok(obj, MediaType.APPLICATION_JSON).build());
	}
	// public processUpdate
	processUpdate(login, req, service, obj) {
		return this.getObject(login, req, service).then(oldObj => {
			let response = this.checkObjectAccess(login, service, obj);

			if (response != null) return Promise.resolve(response);

			if (oldObj["id"] != undefined) {
				let oldId = oldObj["id"];
				let newId = obj["id"];

				if (newId == null || newId != oldId) {
					return Promise.resolve(Response.status(Response.Status.UNAUTHORIZED).entity("changed id").build());
				}
			}

			return this.dbClient.update(service.name, req.primaryKey, obj).then(newObj => {
				this.notify(newObj, service, false);
				return Response.ok(newObj, MediaType.APPLICATION_JSON).build();
			});
		});
	}
	// public processDelete
	processDelete(login, req, service) {
		return this.getObject(login, req, service).then(obj => {
			return this.dbClient.deleteOne(service.name, req.primaryKey).then(objDeleted => {
				this.notify(objDeleted, service, true);
				return Response.ok().build();
			});
		});
	}
	// public
	processQuery(login, req, service) {
		var fields = {};
		var company = login.user.company;

		if (company != 1) {
			if (service.jsonFields["company"] != undefined) {
				fields["company"] = company;
			} else if (service.jsonFields["category"] != undefined) {
				// se não for admin, limita os resultados para as categorias vinculadas a empresa do usuário
				fields["category"] = login.categories;
			}
		}

		var orderBy;

		if (service.orderBy != undefined && service.orderBy != null) {
			orderBy = service.orderBy.split(",");
		} else {
			orderBy = getPrimaryKeys(service);
		}

		return this.dbClient.find(service.name, fields, orderBy).then(results =>
			Response.ok(results, MediaType.APPLICATION_JSON).build()
		);
	}

	// resource : serviceName
	// access : create,read,update,delete,query or custom method
	processRequest(req, res, next, resource, access) {
		let crudProcess = (login) => {
			let service = login.crudServices.find(item => item.name == req.serviceName);

			req.primaryKey = {};

			for (let fieldName in service.jsonFields) {
				let value = req.query[fieldName];
				if (value != undefined) req.primaryKey[fieldName] = value;
			}

			console.log("user:", login.user.name);
			console.log('Client IP:', req.connection.remoteAddress);
			console.log("URL:", req.originalUrl);

			var obj = null;

			if (access == "create" || access == "update") {
				obj = req.body;
			}

			var cf;

			if (access == "create") {
				cf = this.processCreate(login, req, service, obj);
			} else if (access == "update") {
				cf = this.processUpdate(login, req, service, obj);
			} else if (access == "delete") {
				cf = this.processDelete(login, req, service);
			} else if (access == "read") {
				cf = this.processRead(login, req, service);
			} else if (access = "query") {
				cf = this.processQuery(login, req, service);
			} else {
				return next();
			}

			return cf.catch(error => Response.status(Response.Status.BAD_REQUEST).entity(error.message).build())
			.then(response =>
				res.status(response.status).send(response.data)
			);
		}

		let authorization = user => {
			var msgErr;
			var roles = user.roles;

			if (roles != null && roles != undefined) {
				var json = JSON.parse(roles);
				req.serviceName = CaseConvert.underscoreToCamel(resource, false);

				if (json[req.serviceName] != undefined) {
					let serviceAuth = json[req.serviceName];
					// verfica a permissao de acesso
					if (serviceAuth[access] != false) {
						msgErr = null;
						console.log("[authorization] Sucessful authorization : path: = %s, user = %s, roles = %s, token = %s", resource, user.name, roles, user.authctoken);
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

		let authWithToken = () => {
			var authorizationHeader = req.get("Authorization");
			console.log("authorization header :", authorizationHeader);

			if (authorizationHeader != undefined && authorizationHeader.startsWith("Token ")) {
				var token = authorizationHeader.substring(6);
				var login = RequestFilter.getLogin(token);

				if (login != null) {
					var msgErr = authorization(login.user);

					if (msgErr == null) {
						crudProcess(login);
					} else {
						res.status(401).send(msgErr);
						console.log("user : ", login.user.name, " - path : ", resource, " - msgErr : ", msgErr);
					}
				} else {
					res.status(401).send("Authorization replaced by new login in another session");
					console.log("Authorization replaced by new login in another session");
				}
			} else {
				res.status(401).send("Authorization token header invalid");
				console.log("Authorization token header invalid : ", authorizationHeader);
			}
		}

		authWithToken();
	}
	// public
	authenticateByUserAndPassword(req) {
		let userQuery = {"name":req.body.userId, "password":req.body.password};
		return this.dbClient.findOne("crud_user", userQuery)
		.then(user => {
			var token = guid();
			user.authctoken = token;
			return this.dbClient.update("crud_user", userQuery, {"authctoken": user.authctoken}).then(userAfterUpdate => {
				var loginResponse = new LoginResponse(userAfterUpdate);
				return loginResponse.load(this.dbClient).then(() => {
					console.log("[authenticateByUserAndPassword] Sucessful login : user = ", userAfterUpdate.name, ", roles = ", userAfterUpdate.roles, ", token = ", userAfterUpdate.authctoken);
					RequestFilter.logins.set(token, loginResponse);
					return Response.ok(loginResponse, MediaType.APPLICATION_JSON).build();
				});
			});
		});
	}

	static getLogin(token) {
		return RequestFilter.logins.get(token);
	}

    onMessage(session, token) {
    	let login = RequestFilter.getLogin(token);

		if (login != null) {
			session.login = login;
		    console.log("New websocket session opened: token : ", token, ", id : ", session.id);
	        this.clients.push(session);
		}
    }
    // remove the session after it's closed
    onClose(session) {
        console.log("Websoket session closed: " + session.login.authctoken);
        var index = this.clients.indexOf(session);

        if (index >= 0) {
        	this.clients.splice(index, 1);
        }
    }
    // This method sends the same Bidding object to all opened sessions
    notify(obj, service, isRemove) {
    	let getPrimaryKey = (objRef) => {
    		var primaryKey = {};

    		for (var fieldName in service.jsonFields) {
    			var field = service.jsonFields[fieldName];

    			if (field.primaryKey == true) {
        			primaryKey[fieldName] = objRef[fieldName];
    			}
    		}

    		return primaryKey;
    	}

    	let primaryKey = getPrimaryKey(obj);
		let serviceName = CaseConvert.underscoreToCamel(service.name, false);
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
		let category = primaryKey["category"];

		for (var session of this.clients) {
			let login = session.login;
			var userCompany = login.user.company;
			// enviar somente para os clients de "company"
			if (objCompany == undefined || userCompany == 1 || objCompany == userCompany) {
				// restrição de categoria
				if (category == undefined || login.categories.indexOf(category) >= 0) {
					// envia somente para os usuários com acesso ao serviço alterado
					if (login.websocketServices.indexOf(serviceName) >= 0) {
						Promise.resolve().then(() => {
							console.log("notify, user ", login.user.name, ":", msg);
							session.sendUTF(str)
						});
					}
				}
			}
		}
    }

}

var privateKey  = fs.readFileSync(fileNamePrivateKey, 'utf8');
var certificate = fs.readFileSync(fileNameCertificate, 'utf8');
var credentials = {key: privateKey, cert: certificate};
var server = https.createServer(credentials, app);

RequestFilter.logins = new Map();
var requestFilter = new RequestFilter(dbName, app);
requestFilter.start().then(() => {
	server.listen(portListen, () => {
		  var host = server.address().address;
		  var port = server.address().port;

		  console.log('Example app listening at http://%s:%s', host, port);
		});
});

var wsServer = new WebSocketServer({httpServer: server, autoAcceptConnections: true});

wsServer.on('connect', (connection) => {
    console.log((new Date()) + ' Connection accepted.');

    connection.on("message", (message) => {
        if (message.type === 'utf8') {
            console.log('Received Message: ' + message.utf8Data);
            requestFilter.onMessage(connection, message.utf8Data);
        }
    });

    connection.on("close", (reasonCode, description) => {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
        requestFilter.onClose(connection);
    });
});
