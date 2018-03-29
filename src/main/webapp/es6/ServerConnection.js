import {CaseConvert} from "./CaseConvert.js";

export class Filter {

	constructor() {
	}
	// private
	static matchObjectProperties(expectedObject, actualObject, matchPartial) {
        var flag = true;

        for(var key in expectedObject) {
            if(expectedObject.hasOwnProperty(key)) {
                var expectedProperty = expectedObject[key];

                if (expectedProperty == null || expectedProperty === "") {
                    continue;
                }

                var actualProperty = actualObject[key];

                if (angular.isUndefined(actualProperty)) {
                    continue;
                }

                if (actualProperty == null) {
                    flag = false;
                } else if (angular.isObject(expectedProperty)) {
                    flag = flag && matchObjectProperties(expectedProperty, actualProperty);
                } else if (typeof expectedProperty === "number" && typeof actualProperty === "number") {
                	flag = (expectedProperty == actualProperty);
                } else if (matchPartial == true) {
                    flag = flag && (actualProperty.toString().indexOf(expectedProperty.toString()) != -1);
                } else {
                    flag = flag && (actualProperty.toString() == expectedProperty.toString());
                }
            }
        }

        return flag;
    }
    // public
	static process(expectedObject, list, matchPartial) {
    	var filteredResults = [];

        for (var ctr = 0; ctr < list.length; ctr++) {
            var actualObject = list[ctr];
            var flag = Filter.matchObjectProperties(expectedObject, actualObject, matchPartial);

            if (flag == true) {
            	filteredResults.push(actualObject);
            }
        }

        return filteredResults;
	}
	// public
	static checkMatchExact(item, obj) {
    	var match = true;

    	for (var fieldName in obj) {
        	var expected = obj[fieldName];
        	var value = item[fieldName];

        	if (value != expected) {
        		match = false;
        		break;
        	}
    	}

    	return match;
	}
	// public
	static find(list, obj) {
		var ret = [];

        for (var i = 0; i < list.length; i++) {
        	var item = list[i];
        	var match = Filter.checkMatchExact(item, obj);

        	if (match == true) {
        		ret.push(item);
        	}
        }

        return ret;
	}
	// public
	static findOne(list, obj, callback) {
		var ret = null;

        for (var i = 0; i < list.length; i++) {
        	var item = list[i];
        	var match = Filter.checkMatchExact(item, obj);

        	if (match == true) {
        		ret = item;

        		if (callback) {
        			callback(i, item);
        		}

        		break;
        	}
        }

        return ret;
	}
	// public
	static findPos(list, params) {
		var ret = -1;
        Filter.findOne(list, params, pos => ret = pos);
        return ret;
	}
	// public
	static findOneIn(list, listParams) {
		var filterResults = [];

		if (list.length > 0) {
			for (var params of listParams) {
				filterResults.push(Filter.findOne(list, params));
			}
		}

		return filterResults;
	}

}

export class HttpRestRequest {

	constructor(url) {
		this.url = url;
		this.message = "";
	}

	getToken() {
		return this.token;
	}

	setToken(token) {
		this.token = token;
	}
	// private
	requestFetch() {
/*
Response.headers Read only Contains the Headers object associated with the response.
Response.ok Read only Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
Response.redirected Read only Indicates whether or not the response is the result of a redirect; that is, its URL list has more than one entry.
Response.status Read only Contains the status code of the response (e.g., 200 for a success).
Response.statusText Read only Contains the status message corresponding to the status code (e.g., OK for 200).
Response.type Read only Contains the type of the response (e.g., basic, cors).
Response.url Read only Contains the URL of the response.
Response.useFinalURL Contains a boolean stating whether this is the final URL of the response.
Response implements Body, so it also has the following properties available to it:

Body.body Read only A simple getter used to expose a ReadableStream of the body contents.
Body.bodyUsed Read only Stores a Boolean that declares whether the body has been used in a response yet.
 * */		
	}
	// private
	requestNodeJsNative(path, method, params, objSend) {
		return new Promise((resolve, reject) => {
			const https = require('https');

			const options = {
			  hostname: this.url,
			  port: this.port,
			  path: "/" + path,
			  method: method
			};

			const req = https.request(options, (res) => {
			  console.log('statusCode:', res.statusCode);
			  console.log('headers:', res.headers);

			  res.on('data', (d) => {
				 resolve(d);
			  });
			});

			req.on('error', (e) => {
				reject(new Error(e));
			});
			
			req.end();
		});
	}
	// private
	requestXHR(path, method, params, objSend) {
//        console.log("[HttpRestRequest] getRemote : successCallback :", scope.path, params, "response :", JSON.stringify(item));
		return new Promise((resolve, reject) => {
			var httpRequest = new XMLHttpRequest();
	
			if (!httpRequest) {
		    	reject(new Error("Cannot create an XMLHTTP instance"));
			}
	
			var alertContents = function() {
			    if (httpRequest.readyState === XMLHttpRequest.DONE) {
			      if (httpRequest.status >= 200 && httpRequest.status < 300) {
		    		  var objReceive = null;
		    			
		    		  if (httpRequest.response) {
		    			objReceive = JSON.parse(httpRequest.response);
		    		  }

//		              console.log("[HttpRestRequest] response : success : objReceive : " + JSON.stringify(objReceive));
			    	  resolve(objReceive);
			      } else {
			    	  reject(new Error(httpRequest.response));
			      }
			    }
			}
	
		    httpRequest.onreadystatechange = alertContents;
		    httpRequest.open(method, this.url + "/" + path); // GET, POST, PUT, DELETE
		    httpRequest.setRequestHeader('accept', 'application/json');
	//	    httpRequest.setRequestHeader('Content-Type', 'application/json');
	
		    if (this.token) {
			    httpRequest.setRequestHeader('Authorization', 'Token ' + this.token);
		    }
	
		    if (objSend) {
			    httpRequest.send(JSON.stringify(objSend));
		    } else {
			    httpRequest.send();
		    }
	    
		});
/*
4.6 Response
4.6.1 The responseURL attribute
4.6.2 The status attribute
4.6.3 The statusText attribute
4.6.4 The getResponseHeader() method
4.6.5 The getAllResponseHeaders() method
4.6.6 Response body
4.6.7 The overrideMimeType() method
4.6.8 The responseType attribute
4.6.9 The response attribute
4.6.10 The responseText attribute
4.6.11 The responseXML attribute */	    
	}
	// private
	requestAngularHttp(path, method, params, objSend) {
		var req = {};
		req.method = method;
		req.url = this.url + "/" + path;
		req.params = params;

		if (objSend) {
			req.data = objSend;
		}

		if (this.token) {
			req.headers = {"Authorization": "Token " + this.token};
		}

		return HttpRestRequest.$http(req).then(response => response.data).catch(response => {
			throw new Error(response.data);
		});
/*
The response object has these properties:

data – {string|Object} – The response body transformed with the transform functions.
status – {number} – HTTP status code of the response.
headers – {function([headerName])} – Header getter function.
config – {Object} – The configuration object that was used to generate the request.
statusText – {string} – HTTP status text of the response.
xhrStatus – {string} – Status of the XMLHttpRequest (complete, error,  timeout or abort).
 */		
	}
	// private
	request(path, method, params, objSend) {
		var promise;
		this.message = "Processando...";
		
		if (HttpRestRequest.$http) {
			promise = this.requestAngularHttp(path, method, params, objSend);
		} else {
			promise = this.requestXHR(path, method, params, objSend);
		}
		
		return promise.then(data => {
			this.message = "";
			return data;
		}).catch(error => {
			this.message = error.message;
			throw error;
		});
	}

	save(path, params, itemSend, successCallback, errorCallback) {
		return this.request(path, "POST", params, itemSend);
	}

	update(path, params, itemSend, successCallback, errorCallback) {
		return this.request(path, "PUT", params, itemSend);
	}

	remove(path, params, successCallback, errorCallback) {
		return this.request(path, "DELETE", params, null);
	}

	get(path, params, successCallback, errorCallback) {
		return this.request(path, "GET", params, null);
	}

	query(path, params, successCallback, errorCallback) {
		return this.request(path, "GET", params, null);
	}
}

export class CrudService {

	static getPrimaryKeysFromFields(fields) {
		var primaryKeys = [];

		for (var fieldName in fields) {
			var field = fields[fieldName];

			if (field.primaryKey == true) {
				primaryKeys.push(fieldName);
			}
		}

		return primaryKeys;
	}

	constructor(serverConnection, params, httpRest) {
		this.httpRest = httpRest;
		// params.fields = {"field": {"flags": ["label 1", "label 2", ...], "type": "text"}}
		params.fields = (params.fields != undefined && params.fields != null) ? JSON.parse(params.fields) : {};
        this.serverConnection = serverConnection;
        this.params = params;
        //
        this.path = CaseConvert.camelToUnderscore(params.name);
		this.isOnLine = params.isOnLine;
		this.list = [];
		this.primaryKeys = CrudService.getPrimaryKeysFromFields(this.params.fields);
	}

	checkPrimaryKey(obj) {
		var check = true;

		for (var fieldName of this.primaryKeys) {
			if (obj[fieldName] == undefined) {
				check = false;
				break;
			}
		}

		return check;
	}

	getPrimaryKey(objRef, valueReplace) {
		var primaryKey = {};

		for (var fieldName of this.primaryKeys) {
			primaryKey[fieldName] = objRef[fieldName];
		}

		if (valueReplace != undefined) {
			if (this.primaryKeys.indexOf("id") >= 0) {
				primaryKey.id = valueReplace;
			} else {
				for (var fieldName of this.primaryKeys) {
					if (primaryKey[fieldName] == undefined) {
						primaryKey[fieldName] = valueReplace;
						break;
					}
				}
			}
		}

		return primaryKey;
	}

	find(params) {
        return Filter.find(this.list, params);
	}

	findOneIn(paramsList) {
        return Filter.findOneIn(this.list, paramsList);
	}

	findPos(params) {
		return Filter.findPos(this.list, params);
	}

	findOne(params) {
		let pos = this.findPos(params);
		return pos >= 0 ? this.list[pos] : null;
	}
	// private, use in getRemote, save, update and remove
	processList(data, oldPos, newPos) {
		let findSortedPos = () => {
			var fieldNames = this.params.orderBy.split(",");
			var fieldNamesOrderAsc = new Array(fieldNames.length);

			for (var i = 0; i < fieldNames.length; i++) {
				fieldNamesOrderAsc[i] = true;
				var fieldName = fieldNames[i];
				var args = fieldName.split(" ");
				fieldNames[i] = args[0];

				if (args.length > 1 && args[1] == "desc") {
					fieldNamesOrderAsc[i] = false;
				}
			}

			var found = false;

			for (var pos = 0; pos < this.list.length && found == false; pos++) {
				var item = this.list[pos];

				for (var j = 0; j < fieldNames.length; j++) {
					var fieldName = fieldNames[j];
					var isAsc = fieldNamesOrderAsc[j];
					var _value = item[fieldName];
					var value = data[fieldName];

					if ((isAsc == true && _value > value) || (isAsc == false && _value < value)) {
						found = true;
						break;
					}
				}
			}

			return pos;
		}
		
        if (oldPos == undefined && newPos == undefined) {
        	// add
    		if (this.params.orderBy != undefined) {
            	newPos = findSortedPos(data);
            	this.list.splice(newPos, 0, data);
    		} else {
            	this.list.push(data);
            	newPos = this.list.length - 1;
    		}
        } else if (oldPos != undefined && newPos == undefined) {
        	// remove
        	this.list.splice(oldPos, 1);
        } else if (oldPos != undefined && oldPos == newPos) {
        	// replace
    		if (this.params.orderBy != undefined) {
            	this.list.splice(oldPos, 1);
            	newPos = findSortedPos(data);
            	this.list.splice(newPos, 0, data);
    		} else {
            	this.list[oldPos] = data;
    		}
        }
        
        return {"data": data, "oldPos": oldPos, "newPos": newPos};
	}
	// used by websocket
	removeInternal(primaryKey) {
        let pos = this.findPos(primaryKey);
		console.log("CrudService.removeInternal : pos = ", pos, ", data :", this.list[pos]);
        return pos >= 0 ? this.processList(this.list[pos], pos) : null;
	}
	// used by websocket
	getRemote(primaryKey) {
    	return this.httpRest.get(this.path + "/read", primaryKey).then(data => {
            let pos = this.findPos(primaryKey);

            if (pos < 0) {
            	return this.processList(data);
            } else {
            	return this.processList(data, pos, pos);
            }
    	});
	}

	get(primaryKey) {
        let pos = this.findPos(primaryKey);

        if (pos < 0) {
        	return this.getRemote(primaryKey);
        } else {
        	return Promise.resolve({"data": this.list[pos]});
        }
	}
	// private, use in save and update methods
	copyFields(dataIn) {
		var dataOut = {};

		for (let fieldName in this.params.fields) {
			let field = this.params.fields[fieldName];
			let value = dataIn[fieldName];
			
			if (field.type == "i" && (typeof value) === "string") {
				dataOut[fieldName] = Number.parseInt(value);
			} else {
				dataOut[fieldName] = value;
			}
		}

		return dataOut;
	}

	save(primaryKey, itemSend) {
    	return this.httpRest.save(this.path + "/create", primaryKey, this.copyFields(itemSend)).then(data => this.processList(data));
	}

	update(primaryKey, itemSend) {
        return this.httpRest.update(this.path + "/update", primaryKey, this.copyFields(itemSend)).then(data => {
            let pos = this.findPos(primaryKey);
        	return this.processList(data, pos, pos);
        });
	}

	remove(primaryKey) {
        return this.httpRest.remove(this.path + "/delete", primaryKey).then(data => {
            let pos = this.findPos(primaryKey);
        	return data;//this.processList(data, pos);
        });
	}

	queryRemote(params) {
        return this.httpRest.query(this.path + "/query", params).then(list => this.list = list);
	}

}

export class ServerConnection {

	constructor() {
    	this.services = {};
	}
	// private -- used in login()
	webSocketConnect() {
		// Open a WebSocket connection
		// 'wss://localhost:8443/xxx/websocket'
		var url = this.url;

		if (url.startsWith("https://")) {
			url = url.substring(8);
		}

		if (url.endsWith("/") == false) {
			url = url + "/";
		}

		url = "wss://" + url + "websocket";
		this.webSocket = new WebSocket(url);

    	this.webSocket.onopen = event => {
    		this.webSocket.send(this.httpRest.getToken());
    	};

    	this.webSocket.onmessage = event => {
			var item = JSON.parse(event.data);
            console.log("[ServerConnection] webSocketConnect : onMessage :", item);
            var service = this.services[item.service];

            if (service != undefined) {
        		if (item.action == "delete") {
        			service.removeInternal(item.primaryKey);
        		} else {
        			service.getRemote(item.primaryKey);
        		}
            }
		};
	}
    // public
    login(server, user, password, CrudServiceClass, callbackPartial) {
		this.url = server;
    	this.httpRest = new HttpRestRequest(this.url + "/rest");
    	return this.httpRest.request("authc", "POST", null, {"userId":user, "password":password})
    	.then(loginResponse => {
    		this.title = loginResponse.title;
    		this.user = loginResponse.user;
    		this.httpRest.setToken(this.user.authctoken);
    		var acess = JSON.parse(this.user.roles);
        	var listQueryRemote = [];
            // depois carrega os serviços autorizados
            for (var params of loginResponse.crudServices) {
    			params.access = acess[params.name];
    			params.name = CaseConvert.camelUpToCamelLower(params.name);
    			var service = new CrudServiceClass(this, params, this.httpRest);
    			this.services[service.params.name] = service;

    			if (service.isOnLine != true && service.params.access.query != false) {
    				listQueryRemote.push(service);
    			}
            }

            return new Promise((resolve, reject) => {
            	var queryRemoteServices = () => {
            		if (listQueryRemote.length > 0) {
                		var service = listQueryRemote.shift();
                		console.log("[ServerConnection] loading", service.label, "...");
                		callbackPartial("loading... " + service.label);

                		service.queryRemote(null).then(list => {
                			console.log("[ServerConnection] ...loaded", service.label, list.length);
                			queryRemoteServices();
                		}).catch(error => reject(error));
            		} else {
            	    	this.webSocketConnect();
                    	resolve(loginResponse);
            		}
            	}

                queryRemoteServices();
        	});
    	});
    }
    // public
    logout() {
		this.webSocket.close();
    	delete this.user;
        // limpa todos os dados da sessão anterior
        for (var serviceName in this.services) {
        	delete this.services[serviceName];
        }
    }

}
