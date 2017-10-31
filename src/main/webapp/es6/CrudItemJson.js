/**
 * http://usejsdoc.org/
 */
define(["Utils"], function(Utils) {

globalCompileProvider.directive('crudItemJson', function() {
	return {
		restrict: 'E',
		
		scope: {
			vm: '=',
			edit: '='
		},
		
		templateUrl: 'templates/crud-item-json.html'
	};
});

class CrudItemJson {
//	var fields = {
//			"type":{"options":["integer", "string", "password", "datetime", "date", "hour"]},
//			"service":{},
//			"defaultValue":{},
//			"hiden":{},
//			"required":{},
//			"readOnly":{}
//			};
	
// objItems = {"id":{"type":"i"},"menu":{},"name":{},"filterFields":{},"fields":{"readOnly":true}}	
	
	// obj, this.instance.fields, "Campos dos formulários"
	constructor(fields, instanceExternal, fieldNameExternal, title, serverConnection, nameOptions) {
		this.fields = angular.copy(fields);
		this.instanceExternal = instanceExternal;
		this.fieldNameExternal = fieldNameExternal;
		this.formId = this.fieldNameExternal;
		this.title = title;
		this.nameOptionsOriginal = nameOptions;
		this.list = [];
		
		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			field._label = serverConnection.convertCaseAnyToLabel(fieldName);
		}
		
		var objItemsStr = this.instanceExternal[this.fieldNameExternal];

		if (objItemsStr != undefined && objItemsStr.length > 0) {
			var objItems = JSON.parse(objItemsStr);

			for (var itemName in objItems) {
				var objItem = objItems[itemName];
				var item = {};
				item._name = itemName;

				for (var fieldName in this.fields) {
					var field = this.fields[fieldName];
					item[fieldName] = objItem[fieldName];
				}

				this.list.push(item);
			}
		}
		
		this.clear();
	}
	
	clear(form) {
		if (form != undefined) {
			  form.$setPristine();
//			  form.$setUntouched();
		}
		
		if (this.nameOptionsOriginal != undefined) {
			this.nameOptions = angular.copy(this.nameOptionsOriginal);
			
			for (var item of this.list) {
				var index = Utils.findInList(this.nameOptions, item._name);
				
				if (index >= 0) {
					this.nameOptions.splice(index, 1);
				}
			}
		}

		this.instance = {_name:""};
		
		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			
			if (field.type == "i" && field.defaultValue != undefined) {
				this.instance[fieldName] = Number.parseInt(field.defaultValue);
			} else {
				this.instance[fieldName] = field.defaultValue;
			}
		}
	}
	
	// private, use in addItem, updateItem and removeItem
	updateExternal() {
		var objItems = {};
		
		for (let item of this.list) {
			objItems[item._name] = Utils.clone(item, Object.keys(this.fields));
		}
		
		this.instanceExternal[this.fieldNameExternal] = JSON.stringify(objItems);
	}
	
	save() {
		// já verifica se é um item novo ou um update
		var isNewItem = true;

		for (var i = 0; i < this.list.length; i++) {
			var item = this.list[i];
			
			if (item._name == this.instance._name) {
				this.list[i] = this.instance;
				isNewItem = false;
				break;
			}
		}

		if (isNewItem == true) {
			this.list.push(this.instance);
		}
		
		this.updateExternal();
		this.clear();
	}

	remove(index) {
		this.list.splice(index, 1);
		this.updateExternal();
	}

	edit(index) {
		var item = this.list[index];
		this.instance = angular.copy(item);

		if (this.nameOptions != undefined) {
			this.nameOptions.push(this.instance._name);
		}
	}

	moveUp(index) {
		if (index > 0) {
			var tmp = this.list[index-1];
			this.list[index-1] = this.list[index];
			this.list[index] = tmp;
		}
		
		this.updateExternal();
	}

	moveDown(index) {
		if (index < (this.list.length-1)) {
			var tmp = this.list[index+1];
			this.list[index+1] = this.list[index];
			this.list[index] = tmp;
		}
		
		this.updateExternal();
	}

}

return CrudItemJson;

});
