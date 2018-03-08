import {Utils} from "./Utils.js";
import {CrudUiSkeleton} from "./CrudUiSkeleton.js";
import {app} from "./app-globals.js";
import {DatabaseUiAdapter} from "./ServerConnectionUI.js";
import {CrudService, ServerConnection} from "./ServerConnection.js";

app.directive('crudJsonArray', function() {
	return {
		restrict: 'E',

		scope: {
			vm: '=',
			edit: '='
		},

		templateUrl: 'templates/crud-json-array.html'
	};
});

export class CrudJsonArray extends CrudUiSkeleton {

	constructor(fields, instanceExternal, fieldNameExternal, title, serverConnection, selectCallback) {
		super(serverConnection, fieldNameExternal, new DatabaseUiAdapter(serverConnection, fields), selectCallback);
		this.primaryKeys = CrudService.getPrimaryKeysFromFields(fields);
		this.instanceExternal = instanceExternal;
		this.fieldNameExternal = fieldNameExternal;
		this.title = title;
		this.list = [];
		var data = this.instanceExternal[fieldNameExternal];

		if (Array.isArray(data) == true) {
			this.list = data;
		} else if (typeof data === 'string' || data instanceof String) {
			this.list = JSON.parse(data);
		}

		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			field._label = serverConnection.convertCaseAnyToLabel(fieldName);
		}

		this.clear();
	}

	save() {
		// já verifica se é um item novo ou um update
		var primaryKey = {};

		for (var fieldName of this.primaryKeys) {
			primaryKey[fieldName] = this.instance[fieldName];
		}

		var scope = this;

		var callbackReplace = function(index) {
			scope.list[index] = scope.instance;
		};

		if (Filter.findOne(this.list, primaryKey, callbackReplace) == null) {
			this.list.push(this.instance);
		}

		this.instanceExternal[this.fieldNameExternal] = this.list;
		this.clear();
	}

	remove(index) {
		this.list.splice(index, 1);
		this.instanceExternal[this.fieldNameExternal] = this.list;
	}

	edit(index) {
		var item = this.list[index];
		this.instance = angular.copy(item);
	}

	moveUp(index) {
		if (index > 0) {
			var tmp = this.list[index-1];
			this.list[index-1] = this.list[index];
			this.list[index] = tmp;
		}

		this.instanceExternal[this.fieldNameExternal] = this.list;
	}

	moveDown(index) {
		if (index < (this.list.length-1)) {
			var tmp = this.list[index+1];
			this.list[index+1] = this.list[index];
			this.list[index] = tmp;
		}

		this.instanceExternal[this.fieldNameExternal] = this.list;
	}

}
