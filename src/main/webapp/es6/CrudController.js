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

	constructor(serverConnection, serviceName, primaryKey, action, $scope) {
    	super(serverConnection, serverConnection.services[serviceName], primaryKey, action);
		this.disabled = false;
		this.isCollapsedForm = false;
		this.required = "false";
		this.templateModel = "templates/crud-model_" + action + ".html";
		this.$scope = $scope;

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
		return this.crudService.get(primaryKey).then(response => {
			this.original = response.data;
			this.instance = angular.copy(response.data);
			this.isCollapsedForm = false;
			// atualiza as strings de referência
			this.setValues(this.instance);
			// monta a lista dos CrudItem
			for (let serviceName in this.serverConnection.services) {
				var service = this.serverConnection.services[serviceName];

				for (let fieldName in service.params.fields) {
					var field = service.params.fields[fieldName];

					if (field.service == this.name) {
						if (field.title != undefined && field.title.length > 0) {
							var isClonable = field.isClonable == undefined ? false : field.isClonable;
					    	this.listItemCrud.push(new CrudItem(this.serverConnection, serviceName, fieldName, this.primaryKey, isClonable, field.title));
						}
					}
				}
			}
			
			return response;
		});
	}

}

export class CrudController extends CrudBase {

    constructor(serverConnection, $scope) {
    	var params = serverConnection.$location.search();
		var path = serverConnection.$location.path();
		var list = path.split('/');
		var action = list[list.length-1];
		var serviceName = CaseConvert.underscoreToCamel(list[list.length-2]);
    	super(serverConnection, serviceName, params, action, $scope);
    }
    
    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
    		this.$scope.$apply();
    		return response;
    	});
    }
	
	remove() {
		return super.remove().then(response => {
			this.goToSearch();
			return response;
		});
	}

	update() {
		return super.update().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			if (this.crudService.params.saveAndExit == true) {
				this.goToSearch();
			} else {
				this.goToEdit(primaryKey);
			}
			
			return response;
		});
	}

	save() {
		return super.save().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			for (var item of this.listItemCrud) {
				item.clone(primaryKey);
			}
			
			if (this.crudService.params.saveAndExit == true) {
				this.goToSearch();
			} else {
				this.goToEdit(primaryKey);
			}
			
			return response;
		});
	}

}

app.controller("CrudController", function(ServerConnectionService, $scope) {
	return new CrudController(ServerConnectionService, $scope);
});
