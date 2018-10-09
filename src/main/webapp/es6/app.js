import {app} from "./app-globals.js";
import {ServerConnectionUI, CrudServiceUI} from "./ServerConnectionUI.js";
import {HttpRestRequest} from "./ServerConnection.js";

class ServerConnectionService extends ServerConnectionUI {

	constructor($locale, $route, $rootScope, $q, $timeout) {
		super($locale, $route, $rootScope, $q, $timeout);
    }

    login(server, user, password, callbackPartial) {
        super.login(server, user, password, CrudServiceUI, callbackPartial);
    }

}

app.service("ServerConnectionService", function($locale, $route, $rootScope, $q, $timeout) {
    	HttpRestRequest.$q = $q;
    	return new ServerConnectionService($locale, $route, $rootScope, $q, $timeout);
});

class LoginController {

    constructor(serverConnection, server) {
		this.serverConnection = serverConnection;
		this.server = server;
		this.user = "";
		this.password = "";
		this.message = "";

	  	if (this.serverConnection.user != undefined) {
	    	this.serverConnection.logout();
	    	window.location.reload();
		}
    }

    login() {
    	return this.serverConnection.login(this.server, this.user.toLowerCase(), this.password, message => this.message = message);
    }

}

app.controller('LoginController', function(ServerConnectionService) {
	const url = new URL(window.location.hash.substring(2), window.location.href);
	const server = url.searchParams.get("server");
	return new LoginController(ServerConnectionService, server);
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
