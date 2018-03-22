import {CrudController} from "./CrudController.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {CrudObjJson} from "./CrudObjJson.js";
import {CrudJsonArray} from "./CrudJsonArray.js";
import {Utils} from "./Utils.js";

export const name = "UserController";

export class Controller extends CrudController {

    constructor(serverConnection, $scope) {
    	super(serverConnection, $scope);
    }

    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
        	// Regras de acesso aos serviços
        	var fieldsRoles = {
        			"read":{"defaultValue":true, "options":[false,true]},
        			"query":{"defaultValue":true, "options":[false,true]},
        			"create":{"defaultValue":true, "options":[false,true]},
        			"update":{"defaultValue":true, "options":[false,true]},
        			"delete":{"defaultValue":true, "options":[false,true]}
        			};

        	var nameOptionsRoles = [];

        	for (var item of this.serverConnection.services.crudService.list) {
        		nameOptionsRoles.push(item.name);
        	}

        	this.listItemCrudJson.push(new CrudItemJson(fieldsRoles, this.instance, "roles", "Controle de Acesso", this.serverConnection, nameOptionsRoles));
        	// Menu do usuário
//        	$routeProvider.when("/app/:name/:action", {templateUrl: "templates/crud.html", controller: "CrudController", controllerAs: "vm"});
        	var fieldsMenu = {
        			"menu":{"defaultValue":"actions", "options":["actions","help","config","form"]},
        			"label":{},
        			"path":{"defaultValue":"service/action?search1=1&search2=2"}
        			};

        	this.listItemCrudJson.push(new CrudItemJson(fieldsMenu, this.instance, "menu", "Menu", this.serverConnection));
        	// Configurações Json
    /*
        	var fieldsConfig = {
        			"modules":{"defaultValue":"es6/Utils.js,es6/CrudController.js"}
        			};

        	this.listObjCrudJson.push(new CrudObjJson(fieldsConfig, this.instance, "config", "Configurações", this.serverConnection));
    */
        	var fieldsRoute = {
        			"path":{"primaryKey":true, "defaultValue":"/app/xxx/:action"},
        			"templateUrl":{"defaultValue":"templates/crud.html"},
        			"controller":{"defaultValue":"CrudController"},
        			};
        	// fields, instanceExternal, fieldNameExternal, title, serverConnection, selectCallback
        	this.listCrudJsonArray.push(new CrudJsonArray(fieldsRoute, this.instance, "routes", "Rotas de URL AngularJs", this.serverConnection));
    		this.$scope.$apply();
        	return response;
    	});
    }

}
