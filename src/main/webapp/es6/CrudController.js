import {Utils} from "./Utils.js";
import {CrudCommom} from "./CrudCommom.js";
import {CrudItem} from "./CrudItem.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {CrudObjJson} from "./CrudObjJson.js";
import {CrudJsonArray} from "./CrudJsonArray.js";
import {app} from "./app-globals.js";
import {CaseConvert} from "./CaseConvert.js";
import { ServerConnectionUI } from "./ServerConnectionUI.js";

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

	constructor(serverConnection, serviceName, objParams, action, $scope) {
    	super(serverConnection, serverConnection.services[serviceName], objParams, action);
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
				let service = this.serverConnection.services[serviceName];

				for (let fieldName in service.params.fields) {
					let field = service.params.fields[fieldName];

					if (field.service == this.crudService.path) {
						if (field.title != undefined && field.title.length > 0) {
							let isClonable = field.isClonable == undefined ? false : field.isClonable;
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
		const url = new URL(window.location.hash.substring(2), window.location.href);
    	const path = url.pathname;
		const list = path.split('/');
		const action = list[list.length-1];
		const serviceName = CaseConvert.underscoreToCamel(list[list.length-2]);
		let objParams = {};

		for (const [key,value] of url.searchParams) {
			objParams[key] = value;
		}

    	super(serverConnection, serviceName, objParams, action, $scope);
    }
    
    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
    		this.$scope.$apply();
    		return response;
    	});
    }
	
	remove(primaryKey) {
		return super.remove(primaryKey).then(data => {
            // data may be null
			this.goToSearch();
			return data;
		});
	}

	update() {
		return super.update().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			if (this.crudService.params.saveAndExit == true) {
				this.goToSearch();
			} else {
				ServerConnectionUI.changeLocationHash(this.crudService.path + "/" + "edit", primaryKey);
			}
			
			return response;
		});
	}

	save() {
		return super.save().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			for (let item of this.listItemCrud) {
				item.clone(primaryKey);
			}
			
			if (this.crudService.params.saveAndExit == true) {
				this.goToSearch();
			} else {
				ServerConnectionUI.changeLocationHash(this.crudService.path + "/" + "edit", primaryKey);
			}
			
			return response;
		});
	}
	
	saveAsNew() {
		if (this.instance.id != undefined) {
			this.instance.id = undefined;
		}
		
		return this.save();
	}

}

app.controller("CrudController", function(ServerConnectionService, $scope) {
	return new CrudController(ServerConnectionService, $scope);
});
