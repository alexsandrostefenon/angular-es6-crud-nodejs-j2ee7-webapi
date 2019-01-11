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

	constructor(serverConnection, serviceName, fieldName, primaryKeyForeign, title, numMaxItems, queryCallback, selectCallback) {
    	super(serverConnection, serverConnection.services[serviceName], {}, 1);
		this.fieldName = fieldName;
		this.foreignKey = this.crudService.getForeignKeyFromPrimaryKeyForeign(primaryKeyForeign, this.fieldName);
		const field = this.crudService.params.fields[fieldName];
		this.title = (title != undefined && title != null) ? title : field.title;
		this.isClonable = field.isClonable == undefined ? false : field.isClonable;
		this.numMaxItems = (numMaxItems != undefined && numMaxItems != null) ? numMaxItems : 999;
		this.queryCallback = queryCallback;
		this.selectCallback = selectCallback;
		
		for (fieldName in this.foreignKey) {
			this.fields[fieldName].hiden = true;
		}

		this.query();
	}

	query() {
		this.filterResults = this.crudService.find(this.foreignKey);
		// pagina e monta a listStr
		this.paginate();

		if (this.queryCallback != undefined && this.queryCallback != null) {
			this.queryCallback(this.filterResults);
		}
		// aproveita e limpa os campos de inserção de novo instance
		this.clear();
	}

	clone(primaryKeyForeign) {
		this.foreignKey = this.service.getForeignKeyFromPrimaryKeyForeign(primaryKeyForeign, this.fieldName);

		if (this.isClonable == true) {
			let count = 0;

			for (var item of this.filterResults) {
				let newItem = angular.copy(item);
				
				for (fieldName in this.foreignKey) {
					this.newItem[fieldName] = this.foreignKey[fieldName];
				}
				
				this.crudService.save(newItem).then(response => {
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
	
	clear() {
		super.clear();
		
		for (let fieldName in this.foreignKey) {
			this.instance[fieldName] = this.foreignKey[fieldName];
		}
	}

	remove(primaryKey) {
        // data may be null
		return super.remove(primaryKey).then(data => this.query());
	}

	save() {
		return super.save().then(response => this.query());
	}

	update() {
		return super.update().then(response => this.query());
	}
}
