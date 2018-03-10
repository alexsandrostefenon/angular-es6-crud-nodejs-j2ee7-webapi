import {Filter, CrudService, ServerConnection} from "./ServerConnection.js";
import {CrudController} from "./CrudController.js";
import {app, globalRouteProvider, globalControllerProvider} from "./app-globals.js";

export class DatabaseUiAdapter {

	constructor(serverConnection, fields) {
		this.serverConnection = serverConnection;
		// type: "i", service: "serviceName", defaultValue: null, hiden: false, required: false, flags: ["a", "b"], readOnly: false
		this.fields = fields;

		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			var type = field.type;
			field.htmlType = "text";
			field.htmlStep = "any";

			if (type == "b") {
				field.htmlType = "checkbox";
			} else if (type == "i") {
				field.htmlType = "number";
				field.htmlStep = "1";
			} else if (type == "n1") {
				field.htmlType = "number";
				field.htmlStep = "0.1";
			} else if (type == "n2") {
				field.htmlType = "number";
				field.htmlStep = "0.01";
			} else if (type == "n3") {
				field.htmlType = "number";
				field.htmlStep = "0.001";
			} else if (type == "p") {
				field.htmlType = "password";
			} else if (type == "date") {
				field.htmlType = "date";
			} else if (type == "time") {
				field.htmlType = "time";
			} else if (type == "datetime-local") {
				field.htmlType = "datetime-local";
			}

			if (field.label == undefined) {
				var label = serverConnection.convertCaseAnyToLabel(fieldName);
				field.label = label;
			}

			if (field.options != undefined) {
				field.options = field.options.split(",");
			}
		}
	}

	getUiValue(item, fieldName, isConvertToString) {
    	var value = item[fieldName];
    	var field = this.fields[fieldName];

    	if (field != undefined) {
			var serviceName = field.service;

			if (serviceName != undefined) {
				// neste caso, valRef contém o id do registro de referência
				var service = this.serverConnection.services[serviceName];
				var primaryKey = service.getPrimaryKey(item, value);
				service.findOne(primaryKey, function(pos) {
					value = service.listStr[pos];
				});
			} else if (fieldName == "id") {
				// TODO : o "id" não deve fazer parte de StrValue, criar uma lista para armazenar os primaryKeys
				function padLeft(str, size, ch) {
					while (str.length < size) {
						str = ch + str;
					}

					return str;
				}

				value = padLeft(value.toString(), 4, '0');
			} else if (field.htmlType == "datetime-local") {
				value = new Date(value);

				if (isConvertToString == true) {
					value = value.toLocaleString();
				}
			}
    	}

    	return value;
    }

}

export class CrudServiceUI extends CrudService {

	constructor(serverConnection, params, httpRest) {
		super(serverConnection, params, httpRest);
		this.databaseUiAdapter = new DatabaseUiAdapter(serverConnection, params.fields);
		this.label = (params.title == undefined || params.title == null) ? serverConnection.convertCaseAnyToLabel(this.path) : params.title;
		// filterFields
		params.filterFields = (params.filterFields != undefined && params.filterFields != null) ? params.filterFields.split(",") : [];
        this.filterFields = params.filterFields;
        console.log("[CrudServiceUI] constructor : path = " + this.path + ", fields = " + this.databaseUiAdapter.fields + ", filterFields = " + params.filterFields);
        // END PARAMS
		this.listStr = [];
	}

    buildItemStr(item) {
		var str = "";

		for (var fieldName of this.filterFields) {
			str = str + this.databaseUiAdapter.getUiValue(item, fieldName, true) + " - ";
		}

    	return str;
    }

    buildListStr(list) {
    	var ret = [];

    	for (var i = 0; i < list.length; i++) {
    		var item = list[i];
    		var str = this.buildItemStr(item);
    		ret.push(str);
    	}

    	return ret;
    }

	getFilteredItems(objFilter, matchPartial) {
		var list = [];

		if (objFilter != undefined && objFilter != null) {
			list = Filter.process(objFilter, this.list, matchPartial);
		} else {
			list = this.list;
		}

		return list;
	}

	onDelete(primaryKey) {
        var pos = super.onDelete(primaryKey);

        if (pos >= 0) {
        	this.listStr.splice(pos, 1);
        }
	}

	// private, used in getRemote, save and update
	processAddOrReplaceUi(newItem, newPos, oldPos, callback) {
		var str = this.buildItemStr(newItem);

        if (newPos == null) {
        	// add
        	this.listStr.push(str);
        } else if (newPos == oldPos) {
        	// replace
        	this.listStr[newPos] = str;
        } else {
        	// remove and add
        	this.listStr.splice(oldPos, 1);
        	this.listStr.splice(newPos, 0, str);
        }

    	if (callback != null) {
        	callback(newItem, newPos, oldPos);
    	}
	}

	getRemote(primaryKey, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(item, newPos, oldPos) {
        	scope.processAddOrReplaceUi(item, newPos, oldPos, successCallback);
        };

    	super.getRemote(primaryKey, callback, errorCallback);
	}

	get(primaryKey, successCallback, errorCallback) {
        var item = this.findOne(primaryKey);

        if (item == null) {
        	this.getRemote(primaryKey, successCallback, errorCallback);
        } else {
        	if (successCallback) {
            	successCallback(item);
        	}
        }
	}

	save(restParams, itemSend, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(itemReceived, newPos, oldPos) {
        	scope.processAddOrReplaceUi(itemReceived, newPos, oldPos, successCallback);
        };

    	super.save(restParams, itemSend, callback, errorCallback);
	}

	update(primaryKey, itemSend, successCallback, errorCallback) {
	    var scope = this;

        var callback = function(itemReceived, newPos, oldPos) {
        	scope.processAddOrReplaceUi(itemReceived, newPos, oldPos, successCallback);
        };

    	super.update(primaryKey, itemSend, callback, errorCallback);
	}

	remove(primaryKey, successCallback, errorCallback) {
		var scope = this;

        var callback = function(response, pos) {
    		scope.listStr.splice(pos, 1);

        	if (successCallback) {
            	successCallback(response, pos);
        	}
        };

    	super.remove(primaryKey, callback, errorCallback);
	}

	queryRemote(params, successCallback, errorCallback) {
	    var scope = this;

        var callback = function() {
    		scope.listStr = scope.buildListStr(scope.list);
            // também atualiza a lista de nomes de todos os serviços que dependem deste
			for (var serviceName in scope.serverConnection.services) {
				var service = scope.serverConnection.services[serviceName];

				for (var fieldName in service.databaseUiAdapter.fields) {
					var field = service.databaseUiAdapter.fields[fieldName];

					if (field.service == scope.params.name) {
				        console.log("[CrudServiceUI] queryRemote, update listStr from", service.label, service.list.length, "by", scope.label, scope.list.length);
		    			service.listStr = service.buildListStr(service.list);
						break;
					}
				}
			}

            if (successCallback) {
	            successCallback(scope);
            }
        };

    	super.queryRemote(params, callback, errorCallback);
	}

}

export class ServerConnectionUI extends ServerConnection {

	constructor($location, $locale, $route, $rootScope) {
		super();
    	this.$location = $location;
    	this.localeId = $locale.id;
    	this.$route = $route;
    	this.$rootScope = $rootScope;
    	// força o browser a iniciar sempre da página de login
    	if ($location.path().indexOf("/app/login") < 0) {
    		$location.path("/app/login").search({});
    	}

		this.translation = {};
		this.translation.new = "New";
		this.translation.exit = "Exit";
		this.translation.saveAsNew = "Save as New";
		this.translation.view = "View";
		this.translation.edit = "Edit";
		this.translation.delete = "Delete";
		this.translation.create = "Create";
		this.translation.search = "Search";
		this.translation.cancel = "Cancel";
		this.translation.save = "Save";
		this.translation.filter = "Filter";
	}
    // private <- login
	loginDone() {
        this.menu = {user:[{path:"login", label:"Exit"}]};
        // user menu
		if (this.user.menu != undefined && this.user.menu.length > 0) {
			var menus = JSON.parse(this.user.menu);

			for (var menuId in menus) {
				var menuItem = menus[menuId];
				var menuName = menuItem.menu;

				if (this.menu[menuName] == undefined) {
					this.menu[menuName] = [];
				}

				this.menu[menuName].unshift(menuItem);
			}
		}
		// system menu
		if (this.user.showSystemMenu == true) {
			for (var serviceName in this.services) {
				var service = this.services[serviceName];
	    		var menuName = service.params.menu;

	    		if (menuName != undefined && menuName != null && menuName.length > 0) {
	    			if (this.menu[menuName] == undefined) {
	    				this.menu[menuName] = [];
	    			}

	    			var menuItem = {path: service.path + "/search", label: service.label};
	    			this.menu[menuName].unshift(menuItem);
	    		}
			}
		}
    	// tradução
		if (this.services.crudTranslation != undefined) {
	    	for (var fieldName in this.translation) {
	    		var str = this.translation[fieldName];
		    	var item = this.services.crudTranslation.findOne({name:str,locale:this.localeId});

	    		if (item != null && item.translation != undefined) {
	        		this.translation[fieldName] = item.translation;
	    		}
	    	}
		}
        // routes and modules
//		var config = JSON.parse(this.user.config);
		var promises = [];
		globalRouteProvider.when('/app/login',{templateUrl:'templates/login.html', controller:'LoginController', controllerAs: "vm"});

		if (this.user.routes != undefined && this.user.routes != null) {
			var routes = null;

			if (Array.isArray(this.user.routes) == true) {
				routes = this.user.routes;
			} else if (typeof this.user.routes === 'string' || this.user.routes instanceof String) {
				routes = JSON.parse(this.user.routes);
			} else {
				console.err("invalid routes");
			}

			for (var route of routes) {
				if (route.templateUrl == undefined) {
					route.templateUrl = "templates/crud.html";
				}
				// consider http://devdocs.io/dom-fetch/
				var promise = import("./" + route.controller + ".js").then(module => {
					console.log("loaded :", module.name);

					globalControllerProvider.register(module.name, function(ServerConnectionService) {
						return new module.Controller(ServerConnectionService);
					});
				});
				promises.push(promise);
				globalRouteProvider.when(route.path, {"templateUrl":route.templateUrl, "controller": route.controller, controllerAs: "vm"});
			}
		}

		Promise.all(promises).then(() => {
			globalRouteProvider.when("/app/:name/:action", {templateUrl: "templates/crud.html", controller: "CrudController", controllerAs: "vm"});
			globalRouteProvider.otherwise({redirectTo: '/app/login'});

	        if (this.user.path != undefined && this.user.path != null && this.user.path.length > 0) {
	        	this.$route.reload();
	        	this.$location.path(this.user.path);
	        }
		});
    }
    // public
    login(server, user, password, CrudServiceClass, callbackFinish, callbackFail, callbackPartial) {
        var scope = this;

        var internalCallbackFinish = function() {
			scope.loginDone();

        	if (callbackFinish) {
        		callbackFinish();
        	}
        }

    	if (server == null || server.length == 0) {
    		server = this.$location.absUrl();
    		// remove o /#/xxx no final do path
			var pos = server.indexOf("/#");

			if (pos >= 0) {
				server = server.substring(0, pos);
			}
    	}

        super.login(server, user, password, CrudServiceClass, internalCallbackFinish, callbackFail, callbackPartial);
    }

    logout() {
    	super.logout();
        this.menu = {};
        this.$location.path("/app/login").search({"server":this.url});
    }

	convertCaseAnyToLabel(str) {
		var ret = "";
		var nextIsUpper = true;

		for (var i = 0; i < str.length; i++) {
			var ch = str[i];

			if (nextIsUpper == true) {
				ret = ret + ch.toUpperCase();
				nextIsUpper = false;
			} else if (ch >= 'A' && ch <= 'Z') {
				ret = ret + ' ' + ch;
			} else if (ch == '-' || ch == '_') {
				ret = ret + ' ';
				nextIsUpper = true;
			} else {
				ret = ret + ch;
			}
		}

		if (this.services.crudTranslation != undefined) {
	    	var item = this.services.crudTranslation.findOne({name:str,locale:this.localeId});

	    	if (item != null) {
	    		ret = item.translation;
	    	}
		}

		return ret;
	}

}
