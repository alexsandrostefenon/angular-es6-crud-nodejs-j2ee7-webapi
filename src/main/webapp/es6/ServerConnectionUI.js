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
				let pos = service.findPos(primaryKey);
				value = service.listStr[pos];
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
	// private
	processListUi(response) {
		let str = response.data == undefined ? null : this.buildItemStr(response.data);

        if (response.oldPos == undefined && response.newPos == undefined) {
        	// add
        	this.listStr.push(str);
        } else if (response.oldPos != undefined && response.newPos == undefined) {
        	// remove
        	this.listStr.splice(response.oldPos, 1);
        } else if (response.newPos == response.oldPos) {
        	// replace
        	this.listStr[response.newPos] = str;
        } else if (response.oldPos != undefined && response.newPos != undefined) {
        	// remove and add
        	this.listStr.splice(response.oldPos, 1);
        	this.listStr.splice(response.newPos, 0, str);
        }
        
        return response;
	}
	// used by websocket
	removeInternal(primaryKey) {
		let response = super.removeInternal(primaryKey);
		console.log("CrudService.removeInternal : response = ", response, ", str :", this.listStr[response.oldPos]);
        return response != null ? this.processListUi(response) : null;
	}
	// used by websocket
	getRemote(primaryKey) {
    	return super.getRemote(primaryKey).then(response => this.processListUi(response));
	}

	save(itemSend) {
    	return super.save(itemSend).then(response => this.processListUi(response));
	}

	update(primaryKey, itemSend) {
        return super.update(primaryKey, itemSend).then(response => this.processListUi(response));
	}

	remove(primaryKey) {
        // data may be null
    	return super.remove(primaryKey);//.then(data => this.processListUi(data));
	}

	queryRemote(params) {
    	return super.queryRemote(params).then(list => {
    		this.listStr = this.buildListStr(this.list);
            // também atualiza a lista de nomes de todos os serviços que dependem deste
			for (let serviceName in this.serverConnection.services) {
				let service = this.serverConnection.services[serviceName];

				for (let fieldName in service.databaseUiAdapter.fields) {
					let field = service.databaseUiAdapter.fields[fieldName];

					if (field.service == this.params.name) {
				        console.log("[CrudServiceUI] queryRemote, update listStr from", service.label, service.list.length, "by", this.label, this.list.length);
		    			service.listStr = service.buildListStr(service.list);
						break;
					}
				}
			}
			
			return list;
    	});
	}

}

export class ServerConnectionUI extends ServerConnection {

	static buildLocationHash(hashPath, hashSearchObj) {
		let hash = "#!/app/" + hashPath;

		if (hashSearchObj != undefined) {
			let searchParams = new URLSearchParams(hashSearchObj);
			hash = hash + "?" + searchParams.toString();
		}
		
		return hash;
	}

	static changeLocationHash(hashPath, hashSearchObj) {
		const hash = ServerConnectionUI.buildLocationHash(hashPath, hashSearchObj);
		console.log(`ServerConnectionUI.changeLocationHash(${hashPath}, ${hashSearchObj}) : ${hash}`);
		window.location.assign(hash);
	}

	constructor($locale, $route, $rootScope, $q, $timeout) {
		super();
    	this.localeId = $locale.id;
    	this.$route = $route;
    	this.$rootScope = $rootScope;
    	this.$q = $q;
    	this.$timeout = $timeout;
    	// força o browser a iniciar sempre da página de login
    	if (window.location.hash.indexOf("/login") < 0) {
			ServerConnectionUI.changeLocationHash("login");
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
	loginDone(loginResponse) {
        this.menu = {user:[{path:"login", label:"Exit"}]};
        // user menu
		if (this.user.menu != undefined && this.user.menu.length > 0) {
			const menus = JSON.parse(this.user.menu);

			for (var menuId in menus) {
				let menuItem = menus[menuId];
				let menuName = menuItem.menu;

				if (this.menu[menuName] == undefined) {
					this.menu[menuName] = [];
				}

				this.menu[menuName].unshift(menuItem);
			}
		}
		// system menu
		if (this.user.showSystemMenu == true) {
			for (let serviceName in this.services) {
				let service = this.services[serviceName];
	    		let menuName = service.params.menu;

	    		if (menuName != undefined && menuName != null && menuName.length > 0) {
	    			if (this.menu[menuName] == undefined) {
	    				this.menu[menuName] = [];
	    			}

	    			let menuItem = {path: service.path + "/search", label: service.label};
	    			this.menu[menuName].unshift(menuItem);
	    		}
			}
		}
    	// tradução
		if (this.services.crudTranslation != undefined) {
	    	for (var fieldName in this.translation) {
	    		const str = this.translation[fieldName];
		    	const item = this.services.crudTranslation.findOne({name:str,locale:this.localeId});

	    		if (item != null && item.translation != undefined) {
	        		this.translation[fieldName] = item.translation;
	    		}
	    	}
		}
        // routes and modules
//		var config = JSON.parse(this.user.config);
		const promises = [];
		globalRouteProvider.when('/app/login',{templateUrl:'templates/login.html', controller:'LoginController', controllerAs: "vm"});

		if (this.user.routes != undefined && this.user.routes != null) {
			let routes = null;

			if (Array.isArray(this.user.routes) == true) {
				routes = this.user.routes;
			} else if (typeof this.user.routes === 'string' || this.user.routes instanceof String) {
				routes = JSON.parse(this.user.routes);
			} else {
				console.err("invalid routes");
			}

			for (let route of routes) {
				if (route.templateUrl == undefined) {
					route.templateUrl = "templates/crud.html";
				}
				// consider http://devdocs.io/dom-fetch/
				let promise = import("./" + route.controller + ".js").then(module => {
					const controllerName = route.controller.substring(route.controller.lastIndexOf("/")+1);
					console.log("loaded:", controllerName, "route:", route);

					globalControllerProvider.register(controllerName, function(ServerConnectionService, $scope) {
						const _class = module[controllerName];
						return new _class(ServerConnectionService, $scope);
					});

					globalRouteProvider.when(route.path, {"templateUrl":route.templateUrl, "controller": controllerName, controllerAs: "vm"});
				});
				
				promises.push(promise);
			}
		}

		return Promise.all(promises).then(() => {
			console.log("Promise.all:", promises);
			globalRouteProvider.when("/app/:name/:action", {templateUrl: "templates/crud.html", controller: "CrudController", controllerAs: "vm"});
			globalRouteProvider.otherwise({redirectTo: '/app/login'});

	        if (this.user.path != undefined && this.user.path != null && this.user.path.length > 0) {
	        	this.$route.reload();
	        	ServerConnectionUI.changeLocationHash(this.user.path);
	        }
		});
    }
    // public
    login(server, user, password, CrudServiceClass, callbackPartial) {
    	if (server == null || server.length == 0) {
    		server = window.location.origin + window.location.pathname;
    	}

        super.login(server, user, password, CrudServiceClass, callbackPartial).then(loginResponse => this.loginDone(loginResponse));
    }

    logout() {
    	super.logout();
        this.menu = {};
		ServerConnectionUI.changeLocationHash("login", {"server":this.url});
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
