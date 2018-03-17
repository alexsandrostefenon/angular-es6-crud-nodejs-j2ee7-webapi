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
	static findOneIn(list, listParams) {
		var filterResults = [];

		for (var params of listParams) {
			filterResults.push(Filter.findOne(list, params));
		}

		return filterResults;
	}

}

export class HttpRestRequest {

	constructor(url) {
		this.url = url;
	}

	getToken() {
		return this.token;
	}

	setToken(token) {
		this.token = token;
	}
	
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

	requestXHR(path, method, params, objSend, sucessCallback, failCallback) {
//        console.log("[HttpRestRequest] getRemote : successCallback :", scope.path, params, "response :", JSON.stringify(item));
		var httpRequest = new XMLHttpRequest();

		if (!httpRequest) {
	    	failCallback("Cannot create an XMLHTTP instance");
		}

		var alertContents = function() {
		    if (httpRequest.readyState === XMLHttpRequest.DONE) {
		      if (httpRequest.status === 200) {
		    	  if (sucessCallback) {
		    		  var objReceive = null;

		    		  if (httpRequest.response) {
		    			objReceive = JSON.parse(httpRequest.response);
		    		  }

//		              console.log("[HttpRestRequest] response : success : objReceive : " + JSON.stringify(objReceive));
			    	  sucessCallback(objReceive);
		    	  }
		      } else {
		    	  if (failCallback) {
			    	  failCallback(httpRequest.statusText);
		    	  }
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

	requestAngularHttp(path, method, params, objSend, sucessCallback, failCallback) {
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

		var callbackOk = function(response) {
			if (sucessCallback) {
				sucessCallback(response.data);
			}
		}

		var callbackFail = function(response) {
			if (failCallback) {
				failCallback(response.statusText + " : " + response.data);
			}
		}

		HttpRestRequest.$http(req).then(callbackOk, callbackFail);
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

	request(path, method, params, objSend, sucessCallback, failCallback) {
		if (HttpRestRequest.$http) {
			return this.requestAngularHttp(path, method, params, objSend, sucessCallback, failCallback);
		} else {
			return this.requestXHR(path, method, params, objSend, sucessCallback, failCallback);
		}
	}

	save(path, params, itemSend, successCallback, errorCallback) {
		return this.request(path, "POST", params, itemSend, successCallback, errorCallback);
	}

	update(path, params, itemSend, successCallback, errorCallback) {
		return this.request(path, "PUT", params, itemSend, successCallback, errorCallback);
	}

	remove(path, params, successCallback, errorCallback) {
		return this.request(path, "DELETE", params, null, successCallback, errorCallback);
	}

	get(path, params, successCallback, errorCallback) {
		return this.request(path, "GET", params, null, successCallback, errorCallback);
	}

	query(path, params, successCallback, errorCallback) {
		return this.request(path, "GET", params, null, successCallback, errorCallback);
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

	findOne(params, callback) {
        return Filter.findOne(this.list, params, callback);
	}

	onDelete(primaryKey) {
		var ret = -1;
		var scope = this;

        this.findOne(primaryKey, function(pos) {
        	scope.list.splice(pos, 1);
        	ret = pos;
        });

        return ret;
	}

	// private, used in getRemote and saveAsNew
	processAdd(newItem, callback, oldPos) {
		var newPos;

		if (this.params.orderBy != undefined) {
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

			for (newPos = 0; newPos < this.list.length; newPos++) {
				var item = this.list[newPos];

				for (var j = 0; j < fieldNames.length; j++) {
					var fieldName = fieldNames[j];
					var isAsc = fieldNamesOrderAsc[j];
					var _value = item[fieldName];
					var value = newItem[fieldName];

					if (isAsc == true && _value > value) {
						found = true;
						break;
					}

					if (isAsc == false && _value < value) {
						found = true;
						break;
					}

					if (_value != value) {
						break;
					}
				}

				if (found == true) {
					break;
				}
			}

			if (found == true) {
				this.list.splice(newPos, 0, newItem);
			} else {
				newPos = null;
				this.list.push(newItem);
			}
		} else {
	    	this.list.push(newItem);
	    	newPos = (this.list.length)-1;
		}

		if (callback) {
			callback(newItem, newPos, oldPos);
		}
	}

	// private, used in getRemote and update
	processReplace(newItem, oldPos, callback) {
		if (this.params.orderBy != undefined) {
			this.list.splice(oldPos, 1);
			this.processAdd(newItem, callback, oldPos);
		} else {
			this.list[oldPos] = newItem;

			if (callback) {
				callback(newItem, null, oldPos);
			}
		}
	}

	getRemote(primaryKey, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(item) {
            var itemFind = scope.findOne(primaryKey, function(pos) {
            	scope.processReplace(item, pos, successCallback);
            });

            if (itemFind == null) {
            	scope.processAdd(item, successCallback, pos);
            }
        };

    	this.httpRest.get(this.path + "/read", primaryKey, callback, errorCallback);
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

	save(primaryKey, itemSend, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(itemReceived) {
        	scope.processAdd(itemReceived, successCallback, null);
        };

    	this.httpRest.save(this.path + "/create", primaryKey, this.copyFields(itemSend), callback, errorCallback);
	}

	update(primaryKey, itemSend, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(itemReceived) {
            scope.findOne(primaryKey, function(pos) {
            	scope.processReplace(itemReceived, pos, successCallback);
            });
        };

    	this.httpRest.update(this.path + "/update", primaryKey, this.copyFields(itemSend), callback, errorCallback);
	}

	remove(primaryKey, successCallback, errorCallback) {
		var scope = this;

        var callback = function(response) {
            // depois que removeu no servidor, remove também na lista local
            scope.findOne(primaryKey, function(pos) {
        		scope.list.splice(pos, 1);

            	if (successCallback) {
            		successCallback(response, pos);
            	}
            });
        };

    	this.httpRest.remove(this.path + "/delete", primaryKey, callback, errorCallback);
	}

	queryRemote(params, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(list) {
        	// TODO : sort
            scope.list = list;

            if (successCallback) {
	            successCallback(scope);
            }
        };

    	this.httpRest.query(this.path + "/query", params, callback, errorCallback);
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
    	var scope = this;

    	this.webSocket.onopen = function(event) {
    		scope.webSocket.send(scope.httpRest.getToken());
    	};

    	this.webSocket.onmessage = function(event) {
			var item = JSON.parse(event.data);
            console.log("[ServerConnection] webSocketConnect : onMessage :", item);
            var service = scope.services[item.service];

            if (service != undefined) {
        		if (item.action == "delete") {
        			service.onDelete(item.primaryKey);
        		} else {
        			service.getRemote(item.primaryKey);
        		}
            }
		};
	}
    // public
    login(server, user, password, CrudServiceClass, callbackFinish, callbackFail, callbackPartial) {
        var scope = this;

        var callbackLoginOk = function(loginResponse) {
        	scope.title = loginResponse.title;
        	scope.user = loginResponse.user;
        	scope.httpRest.setToken(scope.user.authctoken);
    		var acess = JSON.parse(scope.user.roles);
        	var listQueryRemote = [];
            // depois carrega os serviços autorizados
            for (var params of loginResponse.crudServices) {
    			params.access = acess[params.name];
    			params.name = CaseConvert.camelUpToCamelLower(params.name);
    			var service = new CrudServiceClass(scope, params, scope.httpRest);
    			scope.services[service.params.name] = service;

    			if (service.isOnLine != true && service.params.access.query != false) {
    				listQueryRemote.push(service);
    			}
            }

        	var queryRemoteServices = function() {
        		if (listQueryRemote.length > 0) {
            		var service = listQueryRemote.shift();
            		console.log("[ServerConnection] loading", service.label, "...");
            		callbackPartial("loading... " + service.label);

            		service.queryRemote(null, function(crudService) {
            			console.log("[ServerConnection] ...loaded", crudService.label, crudService.list.length);
            			queryRemoteServices();
            		}, callbackFail);
        		} else {
        	    	scope.webSocketConnect();
                	callbackFinish();
        		}
        	}

            queryRemoteServices();
        }

		this.url = server;
    	this.httpRest = new HttpRestRequest(this.url + "/rest");
    	this.httpRest.request("authc", "POST", null, {"userId":user, "password":password}, callbackLoginOk, callbackFail);
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
