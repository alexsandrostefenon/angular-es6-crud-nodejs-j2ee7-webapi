export const app = angular.module("app", ['ngRoute', 'ui.bootstrap']);

export var globalCompileProvider;
export var globalControllerProvider;
export var globalRouteProvider;

app.config(['$controllerProvider', function($controllerProvider) {
	globalControllerProvider = $controllerProvider;
}]);

app.config(['$compileProvider', function($compileProvider) {
	globalCompileProvider = $compileProvider;
}]);

app.config(['$routeProvider', function($routeProvider) {
	$routeProvider.when('/app/login',{templateUrl:'templates/login.html', controller:'LoginController', controllerAs: "vm"});
	$routeProvider.otherwise({redirectTo: '/app/login'});
	globalRouteProvider = $routeProvider;
}]);
