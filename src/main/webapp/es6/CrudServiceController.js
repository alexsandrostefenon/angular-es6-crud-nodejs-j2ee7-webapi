import {CrudController} from "./CrudController.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {Utils} from "./Utils.js";

export class CrudServiceController extends CrudController {

    constructor(serverConnection, $scope) {
    	super(serverConnection, $scope);
    }

    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
        	var types = Utils.getFieldTypes();
    		// params.fields = {"field": {"flags": ["label 1", "label 2", ...], "type": "text"}}
        	var serviceOptions = [];

        	for (let item of this.serverConnection.services.crudService.list) {
        		serviceOptions.push(item.name);
        	}
			// type (columnDefinition), readOnly, hiden, primaryKey, required (insertable), updatable, defaultValue, length, precision, scale 
        	var fields = {
        			"type":{"options": types},
        			"service":{"options": serviceOptions},
        			"fieldNameForeign":{},
        			"primaryKey":{"options": [true, false]},
        			"readOnly":{"options": [true, false]},
        			"hiden":{"options": [true, false]},
        			"required":{"options": [true, false]},
        			"isClonable":{"options": [true, false]},
        			"defaultValue": {},
        			"updatable":{"options": [true, false]},
        			"length": {},
        			"precision": {},
        			"scale": {},
        			"options": {},
        			"optionsStr": {},
        			"flags":{},
        			"title":{},
        			};

        	this.listItemCrudJson.push(new CrudItemJson(fields, this.instance, "fields", "Campos dos formulários", this.serverConnection));
    		this.$scope.$apply();
        	return response;
    	});
    }

}
