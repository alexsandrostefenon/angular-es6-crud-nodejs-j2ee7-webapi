import {Utils} from "./Utils.js";
import {CrudCommom} from "./CrudCommom.js";
import {app} from "./app-globals.js";

app.directive('crudItem', function() {
	return {
		restrict: 'E',

		scope: {
			vm: '=',
			edit: '='
		},

		templateUrl: 'templates/crud-item.html'
	};
});

export class CrudItem extends CrudCommom {

	constructor(serverConnection, serviceName, fieldName, foreignKey, isClonable, title, numMaxItems, queryCallback, selectCallback) {
    	super(serverConnection, serverConnection.services[serviceName], {}, 1);
		this.title = title;
		this.numMaxItems = (numMaxItems != undefined && numMaxItems != null) ? numMaxItems : 999;
		this.queryCallback = queryCallback;
		this.selectCallback = selectCallback;
		this.fields[fieldName].hiden = true;
		this.fieldName = fieldName;
		this.foreignKey = foreignKey;
		this.isClonable = isClonable;
		this.query();
	}

	query() {
		var params = {}
		params[this.fieldName] = Object.values(this.foreignKey)[0];
		this.filterResults = this.crudService.find(params);
		// pagina e monta a listStr
		this.paginate();

		if (this.queryCallback != undefined && this.queryCallback != null) {
			this.queryCallback(this.filterResults);
		}
		// aproveita e limpa os campos de inserção de novo instance
		this.clear();
		this.instance[this.fieldName] = Object.values(this.foreignKey)[0];
	}

	clone(foreignKeyRefNew) {
		this.foreignKey = foreignKeyRefNew;

		if (this.isClonable == true) {
			var count = 0;
			var scope = this;

			var callback = function(item) {
				count++;

				if (count == scope.filterResults.length) {
					scope.query();
				}
			}

			for (var item of this.filterResults) {
				var newItem = angular.copy(item);
				newItem[this.fieldName] = Object.values(this.foreignKey)[0];
				this.crudService.save({}, newItem, callback);
			}
		} else {
			this.query();
		}
	}

	removeCallback() {
		this.query();
	}

	saveCallback() {
		this.query();
	}
}
