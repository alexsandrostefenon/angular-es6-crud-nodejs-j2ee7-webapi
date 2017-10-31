require.config({
	baseUrl: 'es6',
    waitSeconds: 30
});

var app = angular.module('app', ['ngRoute', 'ui.bootstrap']);

var globalRouteProvider;
var globalControllerProvider;
var globalCompileProvider;

app.config(['$routeProvider', function($routeProvider) {
	$routeProvider.when('/app/login',{templateUrl:'templates/login.html', controller:'LoginController', controllerAs: "vm"});
	$routeProvider.otherwise({redirectTo: '/app/login'});
	globalRouteProvider = $routeProvider;
}]);

app.config(['$controllerProvider', function($controllerProvider) {
	globalControllerProvider = $controllerProvider;
}]);

app.config(['$compileProvider', function($compileProvider) {
	globalCompileProvider = $compileProvider;
}]);

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
