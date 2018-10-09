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
	request(path, method, params, objSend) {
		this.message = "Processando...";
		let url = this.url + "/" + path;
		
		if (params != undefined && params != null) {
			url = url + "?";
			
			for (let fieldName in params) {
				url = url + fieldName + "=" + params[fieldName] + "&";
			}
		}
		
		let options = {};
		let headers = {};
		options.method = method;

		if (this.token != undefined) {
			headers["Authorization"] = "Token " + this.token;
			options.headers = headers;
		}

		if (objSend != null) {
			headers["content-type"] = "application/json";
			options.body = JSON.stringify(objSend);
			options.headers = headers;
		}
		
		let _fetch = HttpRestRequest.fetch != undefined ? HttpRestRequest.fetch : fetch;    
		let promise;
		
		if (HttpRestRequest.$q) {
			promise = HttpRestRequest.$q.when(_fetch(url, options));
		} else {
			promise = _fetch(url, options);
		}
		
		return promise.then(response => {
			this.message = "";
			const contentType = response.headers.get("content-type");
			
			if (response.status === 200) {
				if (contentType) {
					if (contentType.indexOf("application/json") >= 0) {
						return response.json();
					} else {
						return response.text();
					}
				} else {
					return Promise.resolve(null);
				}
			} else {
				throw new Error(response.statusText + " : " + response.text());
			}
		}).catch(error => {
			this.message = error.message;
			throw error;
		});
	}

	save(path, itemSend) {
		return this.request(path, "POST", null, itemSend);
	}

	update(path, params, itemSend) {
		return this.request(path, "PUT", params, itemSend);
	}

	remove(path, params) {
		return this.request(path, "DELETE", params, null);
	}

	get(path, params) {
		return this.request(path, "GET", params, null);
	}

	query(path, params) {
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

	save(itemSend) {
    	return this.httpRest.save(this.path + "/create", this.copyFields(itemSend)).then(data => this.processList(data));
	}

	update(primaryKey, itemSend) {
        return this.httpRest.update(this.path + "/update", primaryKey, this.copyFields(itemSend)).then(data => {
            let pos = this.findPos(primaryKey);
        	return this.processList(data, pos, pos);
        });
	}

	remove(primaryKey) {
        return this.httpRest.remove(this.path + "/delete", primaryKey).then(data => {
            // data may be null
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
			url = "wss://" + url.substring(8);
		} else if (url.startsWith("http://")) {
			url = "ws://" + url.substring(7);
		}

		if (url.endsWith("/") == false) {
			url = url + "/";
		}

		url = url + "websocket";
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
    	this.httpRest = new HttpRestRequest(this.url + "rest");
    	return this.httpRest.request("authc", "POST", null, {"userId":user, "password":password})
    	.then(loginResponse => {
    		this.title = loginResponse.title;
    		this.user = loginResponse.user;
    		this.httpRest.setToken(this.user.authctoken);
    		const acess = JSON.parse(this.user.roles);
    		const listQueryRemote = [];
            // depois carrega os serviços autorizados
            for (let params of loginResponse.crudServices) {
            	if (params != null) {
					params.access = acess[params.name];
					params.name = CaseConvert.camelUpToCamelLower(params.name);
					let service = new CrudServiceClass(this, params, this.httpRest);
					this.services[service.params.name] = service;

					if (service.isOnLine != true && service.params.access.query != false) {
						listQueryRemote.push(service);
					}
            	}
            }

            return new Promise((resolve, reject) => {
            	var queryRemoteServices = () => {
            		if (listQueryRemote.length > 0) {
            			let service = listQueryRemote.shift();
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
