import {Utils} from "./Utils.js";

export class CrudUiSkeleton {

	constructor(serverConnection, name, databaseUiAdapter, selectCallback) {
		this.serverConnection = serverConnection;
		this.translation = serverConnection.translation;
		this.formId = name + "Form";
		this.databaseUiAdapter = databaseUiAdapter;
		this.selectCallback = selectCallback;
		this.fields = angular.copy(databaseUiAdapter.fields);
		// faz uma referencia local a field.filterResultsStr, para permitir opção filtrada, sem alterar a referencia global
		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];

			if (field.service != undefined) {
				field.crudService = this.serverConnection.services[field.service];
				field.filterResultsStr = field.crudService.listStr;
			} else {
				field.filterResultsStr = [];
			}
		}

		this.clear();

		this.listItemCrud = [];
		this.listItemCrudJson = [];
		this.listObjCrudJson = [];
		this.listCrudJsonArray = [];
	}

	clear() {
		this.instance = {};

		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			field.externalReferencesStr = undefined;

			if (field.defaultValue != undefined) {
				if (field.htmlType == "number") {
					this.instance[fieldName] = Number.parseInt(field.defaultValue);
				} else if (field.htmlType == "datetime-local") {
					this.instance[fieldName] = new Date();
				} else {
					this.instance[fieldName] = field.defaultValue;
				}
			}
		}
	}

	setValues(obj) {
		for (var fieldName in this.fields) {
			var value = obj[fieldName];

			if (value != undefined) {
				var field = this.fields[fieldName];

				if (field.service != undefined && field.service != null) {
					this.instance[fieldName] = value;
					field.externalReferencesStr = this.databaseUiAdapter.getUiValue(obj, fieldName, false);
			/*
				// field.flags -> tipagem : String[]
				} else if (field.flags != undefined) {
					this.flags[fieldName] = Utils.strAsciiHexToFlags(value);
			 */
				} else {
					this.instance[fieldName] = this.databaseUiAdapter.getUiValue(obj, fieldName, false);
				}
			}
		}
	}

	setFieldOptions(fieldName, list) {
		var field = this.fields[fieldName];

		if (field != undefined) {
			if (field.crudService != undefined) {
				if (list != null) {
					field.filterResultsStr = field.crudService.buildListStr(list);
				} else {
					field.filterResultsStr = field.crudService.listStr;
				}
			} else {
				field.filterResultsStr = list;
			}
		}
	}

	setFieldId(fieldName, str) {
		// TODO : utilizar str.split(" - ");
    	var pos = str.indexOf(" - ");
		var id = str.substring(0, pos);
		var crudService = this.fields[fieldName].crudService;

		if (crudService != undefined) {
			id = Number.parseInt(id);
		}

		this.instance[fieldName] = id;

		if (this.selectCallback != undefined && this.selectCallback != null) {
			this.selectCallback(fieldName, this.instance[fieldName]);
		}
	}

	isClean() {
		var ret = angular.equals(this.original, this.instance);
		return ret;
	}

	buildUrl(service, primaryKey, action) {
		var obj = {};
		obj.path = "/app/" + service.path + "/" + action;
		obj.search = "";

		for (var fieldName in primaryKey) {
			obj.search = obj.search + encodeURIComponent(fieldName) + "=" + encodeURIComponent(primaryKey[fieldName]) + "&";
		}

		obj.url = obj.path + "?" + obj.search;
    	return obj;
	}

}
