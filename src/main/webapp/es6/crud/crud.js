import {HttpRestRequest} from "./ServerConnection.js";
import {ServerConnectionUI, CrudServiceUI} from "./ServerConnectionUI.js";
import {CrudController} from "./CrudController.js";

class ServerConnectionService extends ServerConnectionUI {

	constructor($locale, $route, $rootScope, $q, $timeout, $controllerProvider, $routeProvider) {
		super($locale, $route, $rootScope, $q, $timeout, $controllerProvider, $routeProvider);
    }

    login(server, user, password, callbackPartial) {
        return super.login(server, user, password, CrudServiceUI, callbackPartial);
    }

}

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

class MenuController {

    constructor(serverConnection) {
    	this.serverConnection = serverConnection;
    	this.isCollapsed = true;
    }

    label(str) {
    	return this.serverConnection.convertCaseAnyToLabel(str);
    }

}

class Crud {
	
    static initialize($controllerProvider, $routeProvider, $compileProvider, $provide) {
    	$provide.service("ServerConnectionService", function($locale, $route, $rootScope, $q, $timeout) {
    		HttpRestRequest.$q = $q;
    		return new ServerConnectionService($locale, $route, $rootScope, $q, $timeout, $controllerProvider, $routeProvider);
    	});

    	$controllerProvider.register("CrudController", function(ServerConnectionService, $scope) {
    		return new CrudController(ServerConnectionService, $scope);
    	});

    	$controllerProvider.register('LoginController', function(ServerConnectionService) {
    		const url = new URL(window.location.hash.substring(2), window.location.href);
    		const server = url.searchParams.get("server");
    		console.log("Crud.initialize : LoginController.server = ", server);
    		return new LoginController(ServerConnectionService, server);
    	});

    	$controllerProvider.register("MenuController", function(ServerConnectionService) {
    	    return new MenuController(ServerConnectionService);
    	});

    	$compileProvider.directive('crudTable', () => {
    		return {restrict: 'E', scope: {vm: '=crud'}, templateUrl: 'templates/crud/crud-table.html'};
    	});

    	$compileProvider.directive('crudItem', () => {
    		return {restrict: 'E', scope: {vm: '=', edit: '='}, templateUrl: 'templates/crud/crud-item.html'};
    	});

    	$compileProvider.directive('crudItemJson', () => {
    		return {restrict: 'E', scope: {vm: '=', edit: '='}, templateUrl: 'templates/crud/crud-item-json.html'};
    	});

    	$compileProvider.directive('crudJsonArray', () => {
    		return {restrict: 'E', scope: {vm: '=', edit: '='}, templateUrl: 'templates/crud/crud-json-array.html'};
    	});

    	$compileProvider.directive('crudObjJson', () => {
    		return {restrict: 'E', scope: {vm: '=', edit: '='}, templateUrl: 'templates/crud/crud-obj-json.html'};
    	});
    }
    
}

export {Crud};

