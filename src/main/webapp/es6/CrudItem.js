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
		this.isClonable = isClonable;

		if (typeof foreignKey === 'object') {
			this.foreignKey = Object.values(foreignKey)[0];
		} else {
			this.foreignKey = foreignKey;
		}
		
		this.query();
	}

	query() {
		var params = {}
		params[this.fieldName] = this.foreignKey;
		this.filterResults = this.crudService.find(params);
		// pagina e monta a listStr
		this.paginate();

		if (this.queryCallback != undefined && this.queryCallback != null) {
			this.queryCallback(this.filterResults);
		}
		// aproveita e limpa os campos de inserção de novo instance
		this.clear();
		this.instance[this.fieldName] = this.foreignKey;
	}

	clone(foreignKeyRefNew) {
		this.foreignKey = foreignKeyRefNew;

		if (this.isClonable == true) {
			let count = 0;

			for (var item of this.filterResults) {
				let newItem = angular.copy(item);
				newItem[this.fieldName] = this.foreignKey;
				this.crudService.save({}, newItem).then(response => {
					count++;

					if (count == this.filterResults.length) {
						this.query();
					}
				});
			}
		} else {
			this.query();
		}
	}

	remove(primaryKey) {
		return super.remove(primaryKey).then(response => this.query());
	}

	save() {
		return super.save().then(response => this.query());
	}

	update() {
		return super.update().then(response => this.query());
	}
}
