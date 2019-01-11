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
		for (let fieldName in this.fields) {
			let field = this.fields[fieldName];

			if (field.service != undefined) {
				field.crudService = this.serverConnection.services[field.service];

				if (field.crudService != undefined) {
					field.filterResults = field.crudService.list;
					field.filterResultsStr = field.crudService.listStr;
				} else {
					console.warn("don't have acess to service ", field.service);
					field.filterResults = [];
					field.filterResultsStr = [];
				}
			} else if (field.options != undefined) {
				field.filterResults = field.options;
				
				if (field.optionsStr != undefined) {
					field.filterResultsStr = field.optionsStr;
				} else {
					field.filterResultsStr = field.options;
				}
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
		this.instanceFlags = {};

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
	
	setValue(fieldName, obj, parent) {
		var value = obj[fieldName];

		if (value != undefined) {
			var field = this.fields[fieldName];

			if (field.service != undefined && field.service != null) {
				field.externalReferencesStr = this.databaseUiAdapter.buildFieldStr(fieldName, obj, parent);
			} else if (field.flags != undefined && field.flags != null) {
				// field.flags : String[], vm.instanceFlags[fieldName] : Boolean[]
				this.instanceFlags[fieldName] = Utils.strAsciiHexToFlags(value.toString(16));
			} else if (field.options != undefined) {
				let pos = field.filterResults.indexOf(value.toString());
				field.externalReferencesStr = field.filterResultsStr[pos];
			} else {
				if (field.type == "datetime-local") {
					value = new Date(value);
				}
			}

			this.instance[fieldName] = value;
		}
	}

	setValues(obj, parent) {
		for (var fieldName in this.fields) {
			this.setValue(fieldName, obj, parent);
		}
	}
	// for custom filtered purposes, like RequestWithServiceCrontroller 
	setFieldOptions(fieldName, list) {
		var field = this.fields[fieldName];

		if (field != undefined) {
			if (field.crudService != undefined) {
				if (list != null) {
					field.filterResults = list;
					field.filterResultsStr = field.crudService.buildListStr(list, this.instance);
					this.setValue(fieldName, this.instance, this.instance);
				} else {
					field.filterResults = field.crudService.list;
					field.filterResultsStr = field.crudService.listStr;
				}
			} else {
				field.filterResults = list;
				field.filterResultsStr = list;
			}
		}
	}

	parseValue(fieldName) {
		const field = this.fields[fieldName];

		if (field.flags != undefined && field.flags != null) {
			// field.flags : String[], vm.instanceFlags[fieldName] : Boolean[]
			this.instance[fieldName] = Number.parseInt(Utils.flagsToStrAsciiHex(this.instanceFlags[fieldName]), 16);
		} else {
			let pos = field.filterResultsStr.indexOf(field.externalReferencesStr);
			
			if (pos >= 0) {
				if (field.service != undefined) {
					const foreignData = field.filterResults[pos];
					
					if (field.fieldNameForeign != undefined && field.fieldNameForeign != null) {
						this.instance[fieldName] = foreignData[field.fieldNameForeign];
					} else if (foreignData.id != undefined) {
						this.instance[fieldName] = foreignData.id;
					}
				} else if (field.options != undefined) {
					this.instance[fieldName] = field.filterResults[pos];
				}

				if (this.selectCallback != undefined && this.selectCallback != null) {
					this.selectCallback(fieldName, this.instance[fieldName]);
				}
			}
		}
	}

	isClean() {
		var ret = angular.equals(this.original, this.instance);
		return ret;
	}

}
