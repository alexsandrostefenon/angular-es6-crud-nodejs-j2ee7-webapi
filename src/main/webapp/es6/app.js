import {app} from "./app-globals.js";
import {ServerConnectionUI, CrudServiceUI} from "./ServerConnectionUI.js";
import {HttpRestRequest} from "./ServerConnection.js";

class ServerConnectionService extends ServerConnectionUI {

	constructor($location, $locale, $route, $rootScope) {
		super($location, $locale, $route, $rootScope);
    }

    login(server, user, password, callbackPartial) {
        super.login(server, user, password, CrudServiceUI, callbackPartial);
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
    	return this.serverConnection.login(this.server, this.user.toLowerCase(), this.password, message => this.message = message);
    }

}

app.controller('LoginController', function(ServerConnectionService, $window) {
    	const params = ServerConnectionService.$location.search();
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
