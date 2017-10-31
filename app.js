var fs = require('fs');
var https = require('https');
var express = require('express');
var bodyParser = require('body-parser');
var WebSocketServer = require('websocket').server;
const url = require('url');

var dbName = process.argv[2] || "crud";
var webapp = process.argv[3] || './src/main/webapp';
var portListen = process.argv[4] || 9443;
var fileNamePrivateKey = process.argv[5] || "server.key";
var fileNameCertificate = process.argv[6] || "server.crt";

function convertCaseUnderscoreToCamel(str, isFirstUpper) {
	var ret = "";
	var nextIsUpper = false;

	if (isFirstUpper == true) {
		nextIsUpper = true;
	}

	for (var i = 0; i < str.length; i++) {
		var ch = str[i];

		if (nextIsUpper == true) {
			ch = ch.toUpperCase();
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

var DbClientPostgres = require('./dbClientPostgres.js');
var dbClient = new DbClientPostgres(dbName);

/*
class HttpRestRequest {

	constructor(urlString, token) {
		var urlObj = url.parse(urlString);
		this.hostname = urlObj.hostname;
		this.port = urlObj.port;
		this.path = urlObj.path;
	}

	request(path, method, postData, sucessCallback, failCallback) {
		var options = {
				  hostname: this.hostname,
				  port: this.port,
				  path: this.path + "/" + path,
				  method: method,
				  rejectUnauthorized: false
				};


		var req = https.request(options, (res) => {
		  console.log(`STATUS: ${res.statusCode}`);
		  console.log(`HEADERS: ${JSON.stringify(res.headers)}`);
		  res.setEncoding('utf8');
		  res.on('data', (chunk) => {
		    console.log(`BODY: ${chunk}`);
		  });
		  res.on('end', () => {
		    console.log('No more data in response.');
		  });
		});

		req.on('error', (e) => {
		  console.log(`problem with request: ${e.message}`);
		});

		// write data to request body
		req.write(postData);
		req.end();
	}

}
*/

var app = express();
app.use("/" + dbName, express.static(webapp));
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.json());

app.options("/" + dbName + '/rest', function (req, res, next) {
	return next();
});

//Add headers
app.use(function (req, res, next) {
    // Website you wish to allow to connect
    res.setHeader('Access-Control-Allow-Origin', '*');
    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,Content-Type,Authorization,Origin');
    next();
});

app.post("/" + dbName + '/rest/authc', function (req, res, next) {
	var userQuery = {"name":req.body.userId, "password":req.body.password};

	var callbackFind = function(error, result) {
		if (error == null) {
			if (result.rowCount == 1) {
				function guid() {
				  function s4() {
				    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
				  }

				  return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
				}

				var user = result.rows[0];
				user.authctoken = guid();

				var callbackUpdate = function(error, result) {
					if (error == null) {
						var callbackServices = function(error, result) {
							if (error == null) {
								var loginResponse = {};
								loginResponse.user = user;
								loginResponse.crudServices = result.rows;
								loginResponse.title = user.name;
								console.log("loginResponse:", loginResponse);
								res.send(loginResponse);
							} else {
								return next(error);
							}
						}

						var roles = JSON.parse(user.roles);
						var names = Object.keys(roles);
						dbClient.find("crud_service", null, {"name": names}, null, callbackServices);
					} else {
						return next(error);
					}
				}

				var userUpdate = {"authctoken": user.authctoken};
				dbClient.update("crud_user", userQuery, null, userUpdate, callbackUpdate);
			} else {
				next("don't find user");
			}
		} else {
			return next(error);
		}
	}

	dbClient.find("crud_user", userQuery, null, null, callbackFind);
});

// accessName : create,read,update,delete,query
// MethodName : create,findById,listAll,deleteById,update
function authWithToken(req, res, next, path, accessName, callback) {
	var authorizationHeader = req.get("Authorization");
	console.log("authorization header :", authorizationHeader);

	if (authorizationHeader != undefined && authorizationHeader.startsWith("Token ")) {
		var token = authorizationHeader.substring(6);

		var callbackFind = function (err, result) {
			if (err) {
				console.log("error in find user with token:", err);
				res.status(401).send("error in find user with token");
			}

			if (result.rows.length == 1) {
				req.user = result.rows[0];

				if (req.user.roles != undefined) {
					var userRoles = JSON.parse(req.user.roles);
					req.serviceName = convertCaseUnderscoreToCamel(path, false);
					var userRole = userRoles[req.serviceName];

					if (userRole != undefined) {
						// verfica a permissao de acesso ao serviço
						if (accessName == null || userRole[accessName] != false) {
							var callbackFindService = function(error, result) {
								if (error == null) {
									req.service = result.rows[0];
									req.primaryKeys = [];
									req.service.fields = JSON.parse(req.service.fields);

									for (var fieldName in req.service.fields) {
										var field = req.service.fields[fieldName];

										if (field.primaryKey == true) {
											req.primaryKeys.push(fieldName);
										}
									}

									req.primaryKey = {};

									for (var fieldName of req.primaryKeys) {
										req.primaryKey[fieldName] = req.query[fieldName];
									}

									console.log("user:", req.user.name);
									console.log('Client IP:', req.connection.remoteAddress);
									console.log("URL:", req.originalUrl);
									callback(req, res, next);
								} else {
									return next(error);
								}
							}

							dbClient.find("crud_service", {"name": req.serviceName}, null, null, callbackFindService);
						} else {
							console.log("unauthorized access: " + req.serviceName);
							res.status(401).send("unauthorized access");
						}
					} else {
						console.log("unauthorized : " + req.serviceName);
						res.status(401).send("unauthorized");
					}
				} else {
					console.log("report to admin check : " + req.user);
					res.status(401).send("report to admin check");
				}
			} else {
				console.log("Authorization replaced by new login in another session");
				res.status(401).send("Authorization replaced by new login in another session");
			}
		};

		dbClient.find("crud_user", {"authctoken":token}, null, null, callbackFind);
	} else {
		console.log("Authorization token header invalid : ", authorizationHeader);
		res.status(401).send("Authorization token header invalid");
	}
}

app.get("/" + dbName + '/rest/:collectionName/query', function (req, res, next) {
	var callbackAuth = function() {
		var fieldQuery = req.query.fieldQuery;
		var valueQuery = req.query.valueQuery;
		var searchQuery = undefined;

		if (fieldQuery != undefined && valueQuery != undefined) {
			searchQuery = {};
			searchQuery[fieldQuery] = valueQuery;
		}

		console.log("find: searchQuery:", searchQuery);

		var callbackFind = function (e, results) {
			if (e) {
				return next(e);
			}

			res.send(results.rows);
		};

		var orderBy;

		if (req.service.orderBy != undefined && req.service.orderBy != null) {
			orderBy = req.service.orderBy;
		} else {
			orderBy = req.primaryKeys.toString();
		}

		dbClient.find(req.params.collectionName, searchQuery, null, orderBy, callbackFind);
	}

	authWithToken(req, res, next, req.params.collectionName, "query", callbackAuth);
});

app.get("/" + dbName + '/rest/:collectionName/read', function (req, res, next) {
	var callbackAuth = function() {
		var callbackDb = function (e, results) {
			if (e) {
				return next(e);
			}

			res.send(results.rows[0]);
		};

		dbClient.find(req.params.collectionName, req.primaryKey, null, null, callbackDb);
	}

	authWithToken(req, res, next, req.params.collectionName, "read", callbackAuth);
});

app.delete("/" + dbName + '/rest/:collectionName/delete', function(req, res, next) {
	var callbackAuth = function() {
		var callbackDb = function (e, results) {
			if (e) {
				return next(e);
			}

			res.send("OK");
		};

		dbClient.deleteOne(req.params.collectionName, req.primaryKey, null, callbackDb);
	}

	authWithToken(req, res, next, req.params.collectionName, "delete", callbackAuth);
});

app.put("/" + dbName + '/rest/:collectionName/update', function(req, res, next) {
	var callbackAuth = function() {
		var obj = req.body;

		var callbackDb = function (e, results) {
			if (e) {
				return next(e);
			}

			res.send(obj);
		};

		dbClient.update(req.params.collectionName, req.primaryKey, null, obj, callbackDb);
	}

	authWithToken(req, res, next, req.params.collectionName, "update", callbackAuth);
});

function checkObjectAccess(req, obj, callback) {
	if (req.user.company > 1 && req.service.fields.company != undefined) {
		obj.company = req.user.company;

		if (req.service.fields.category != undefined && req.serviceName != "categoryCompany") {
			var callbackFindCategory = function(error, result) {
				if (error != null) {
					callback(error);
				} else if (result.rowCount > 0) {
					callback(null);
				} else {
					callback("don't match request category for user company");
				}
			}

			dbClient.find("category_company", null, {"company": obj.company, "category": obj.category}, null, callbackFindCategory);
		} else {
			callback(null);
		}
	} else {
		callback(null);
	}
}

app.post("/" + dbName + '/rest/:collectionName/create', function (req, res, next) {
	var callbackAuth = function(error) {
		if (error != undefined && error != null) {
			return next(error);
		}

		var tableName = req.params.collectionName;
		var obj = req.body;

		var callback = function(error, result) {
			if (error) {
				return next(error);
			}

			res.send(obj);
		}

		if (req.primaryKeys.indexOf("id") >= 0) {
			var callbackFindMaxId = function(error, result) {
				if (error == null && result.rows.length == 1) {
					var lastId = result.rows[0].max;

					if (lastId == null) {
						lastId = 0;
					}

					obj.id = lastId + 1;
					dbClient.insert(tableName, obj, callback);
				} else {
					callback(error, null);
				}
			}

			dbClient.findMax(tableName, "id", null, null, callbackFindMaxId);
		} else {
			dbClient.insert(tableName, obj, callback);
		}
	}

	var callbackAccess = function(error) {
		checkObjectAccess(req, req.body, callbackAuth);
	}

	authWithToken(req, res, next, req.params.collectionName, "create", callbackAccess);
});

var privateKey  = fs.readFileSync(fileNamePrivateKey, 'utf8');
var certificate = fs.readFileSync(fileNameCertificate, 'utf8');
var credentials = {key: privateKey, cert: certificate};
var server = https.createServer(credentials, app);

server.listen(portListen, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
});

class WebSocketNotifierServer {

	constructor() {
    	this.clients = [];
    }

	onConnect(session) {
        this.clients.push(session);
	}

    onClose(session) {
//        console.log("Websoket session closed: " + session.user.authctoken);
        var index = this.clients.indexOf(session);

        if (index >= 0) {
        	this.clients = this.clients.slice(index, 1);
        }
    }

    onMessage(session, token) {
		var callbackFind = function (err, result) {
			if (err == null) {
				if (result.rows.length == 1) {
			    	session.user = result.rows[0];
			        console.log("New websocket session opened: " + session.user);
				} else {
			        console.log("Fail tl new websocket session: " + session + ", token: " + token);
				}
			} else {
				console.log("Fail on find user");
			}
		};

		dbClient.find("crud_user", {"authctoken":token}, null, null, callbackFind);
    }

    notify(objClassName, obj, id, isRemove) {
    	var msg = {"action":"notify", "service":objClassName, "id":id};

    	if (isRemove == true) {
    		msg.action = "delete";
    	}

		for (var session of this.clients) {
			var roles = JSON.parse(session.user.roles);

			if (roles[objClassName]) {
				console.log("WebSocket.Notifier, session: " + session);
				session.sendUTF(msg.toString());
			}
		}
    }

}

var webSocketNotifierServer = new WebSocketNotifierServer();
var wsServer = new WebSocketServer({httpServer: server, autoAcceptConnections: true});

wsServer.on('connect', function(connection) {
    console.log((new Date()) + ' Connection accepted.');
    webSocketNotifierServer.onConnect(connection);

    connection.on("message", function(message) {
        if (message.type === 'utf8') {
            console.log('Received Message: ' + message.utf8Data);
            webSocketNotifierServer.onMessage(connection, message.utf8Data);
        }
    });

    connection.on("close", function(reasonCode, description) {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
        webSocketNotifierServer.onClose(connection);
    });
});
