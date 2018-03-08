import {Utils} from "./Utils.js";
import {app} from "./app-globals.js";

app.directive('crudObjJson', function() {
	return {
		restrict: 'E',

		scope: {
			vm: '=',
			edit: '='
		},

		templateUrl: 'templates/crud-obj-json.html'
	};
});

export class CrudObjJson {

	constructor(fields, instanceExternal, fieldNameExternal, title, serverConnection) {
		this.fields = angular.copy(fields);
		this.instanceExternal = instanceExternal;
		this.fieldNameExternal = fieldNameExternal;
		this.formId = this.fieldNameExternal;
		this.title = title;

		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			field._label = serverConnection.convertCaseAnyToLabel(fieldName);
		}

		this.clear();
		var obj = JSON.parse(this.instanceExternal[this.fieldNameExternal]);

		for (var fieldName in obj) {
			this.instance[fieldName] = obj[fieldName];
		}
	}

	clear() {
		this.instance = {};

		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];

			if (field.type == "i" && field.defaultValue != undefined) {
				this.instance[fieldName] = Number.parseInt(field.defaultValue);
			} else {
				this.instance[fieldName] = field.defaultValue;
			}
		}
	}

	save() {
		this.instanceExternal[this.fieldNameExternal] = JSON.stringify(this.instance);
	}

}
