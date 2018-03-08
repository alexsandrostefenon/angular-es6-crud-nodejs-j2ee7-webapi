import {Utils} from "./Utils.js";
import {CrudCommom} from "./CrudCommom.js";
import {CrudItem} from "./CrudItem.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {CrudObjJson} from "./CrudObjJson.js";
import {CrudJsonArray} from "./CrudJsonArray.js";
import {app} from "./app-globals.js";
import {CaseConvert} from "./CaseConvert.js";

app.directive('crudTable', function() {
	return {
		restrict: 'E',

		scope: {
			vm: '=crud'
		},

		templateUrl: 'templates/crud-table.html'
	};
});

class CrudBase extends CrudCommom {

	constructor(serverConnection, serviceName, primaryKey, action) {
    	super(serverConnection, serverConnection.services[serviceName], primaryKey, action);
		this.message = {};
		this.disabled = false;
		this.isCollapsedForm = false;
		this.required = "false";
		this.templateModel = "templates/crud-model_" + action + ".html";

		if (this.action == "search") {
			this.isCollapsedForm = true;
			// TODO : paginate
		} else if (this.action == "new") {
			this.required = "true";
		} else if (this.action == "view") {
			this.get(this.primaryKey);
		} else if (this.action == "edit") {
			this.required = "true";
			this.get(this.primaryKey);
		} else {
			// TODO : throw exception
		}
	}

	get(primaryKey) {
		var scope = this;

		var successCallback = function(data) {
			scope.original = data;
			scope.instance = angular.copy(data);
			scope.isCollapsedForm = false;
			// atualiza as strings de referência
			scope.setValues(scope.instance);
			// monta a lista dos CrudItem
			for (let serviceName in scope.serverConnection.services) {
				var service = scope.serverConnection.services[serviceName];

				for (let fieldName in service.params.fields) {
					var field = service.params.fields[fieldName];
//					console.log("comparando", scope.name, "com", field.service);
					if (field.service == scope.name) {
						if (field.title != undefined && field.title.length > 0) {
							var isClonable = field.isClonable == undefined ? false : field.isClonable;
					    	scope.listItemCrud.push(new CrudItem(scope.serverConnection, serviceName, fieldName, scope.primaryKey, isClonable, field.title));
						}
					}
				}
			}
/*
	    	// CompanyController
	    	this.listItemCrud.push(new CrudItem(this.serverConnection, "categoryCompany", "company", this.primaryKey.id, true, 'Categorias Vinculadas'));
	    	// ProductController
	    	this.listItemCrud.push(new CrudItem(this.serverConnection, "productAssembleProduct", "productOut", this.primaryKey.id, true, 'Componentes para Fabricação/Montagem'));
	    	this.listItemCrud.push(new CrudItem(this.serverConnection, "productAssembleProduct", "productIn", this.primaryKey.id, false, 'Produtos que utilizam este componente na Fabricação/Montagem'));
	    	this.listItemCrud.push(new CrudItem(this.serverConnection, "productAssembleService", "productOut", this.primaryKey.id, true, 'Serviços para Fabricação/Montagem'));
	    	// SpendingController
	    	this.listItemCrud.push(new CrudItem(this.serverConnection, "spendingItem", "spending", this.primaryKey.id, true, "Items"));
*/
	    	if (scope.getCallback != undefined) {
				scope.getCallback(data);
			}
		};

		this.crudService.get(primaryKey, successCallback);
	}

}

export class CrudController extends CrudBase {

    constructor(serverConnection) {
    	var params = serverConnection.$location.search();
		var path = serverConnection.$location.path();
		var list = path.split('/');
		var action = list[list.length-1];
		var serviceName = CaseConvert.underscoreToCamel(list[list.length-2]);
    	super(serverConnection, serviceName, params, action);
    }

}

app.controller("CrudController", function(ServerConnectionService) {
	return new CrudController(ServerConnectionService);
});
