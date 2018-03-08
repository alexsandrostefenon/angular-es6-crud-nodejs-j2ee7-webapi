import {app} from "./app-globals.js";
import {ServerConnectionUI, CrudServiceUI} from "./ServerConnectionUI.js";
import {HttpRestRequest} from "./ServerConnection.js";

class ServerConnectionService extends ServerConnectionUI {

	constructor($location, $locale, $route, $rootScope) {
		super($location, $locale, $route, $rootScope);
    }

    login(server, user, password, callbackFinish, callbackFail, callbackPartial) {
        super.login(server, user, password, CrudServiceUI, callbackFinish, callbackFail, callbackPartial);
    }

}

app.service("ServerConnectionService", function($location, $locale, $route, $rootScope, $http) {
    	HttpRestRequest.$http = $http;
    	return new ServerConnectionService($location, $locale, $route, $rootScope);
});

class LoginController {

    constructor(serverConnection, server, $window) {
		this.serverConnection = serverConnection;
		this.server = server;
		this.user = "";
		this.password = "";
		this.message = "";

	  	if (this.serverConnection.user != undefined) {
	    	this.serverConnection.logout();
	    	$window.location.reload();
		}
    }

    login() {
    	var scope = this;

		var callback = function(message) {
			scope.message = message;
		}

    	this.serverConnection.login(this.server, this.user, this.password, callback, callback, callback);
    }

}

app.controller('LoginController', function(ServerConnectionService, $window) {
    	var params = ServerConnectionService.$location.search();
    	return new LoginController(ServerConnectionService, params.server, $window);
});

class MenuController {

    constructor(serverConnection) {
    	this.serverConnection = serverConnection;
    	this.isCollapsed = true;
    }

    label(str) {
    	return this.serverConnection.convertCaseAnyToLabel(str);
    }

}

app.controller("MenuController", function(ServerConnectionService) {
    	return new MenuController(ServerConnectionService);
});
