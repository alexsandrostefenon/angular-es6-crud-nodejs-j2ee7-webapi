import {CrudController} from "./CrudController.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {Utils} from "./Utils.js";

export const name = "CrudServiceController";

export class Controller extends CrudController {

    constructor(serverConnection, $scope) {
    	super(serverConnection, $scope);
    }

    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
        	var types = Utils.getFieldTypes();
    		// params.fields = {"field": {"flags": ["label 1", "label 2", ...], "type": "text"}}
        	var serviceOptions = [];

        	for (var item of this.serverConnection.services.crudService.list) {
        		serviceOptions.push(item.name);
        	}

        	var fields = {
        			"type":{"options": types},
        			"primaryKey":{"options": [true, false]},
        			"service":{"options": serviceOptions},
        			"defaultValue": {},
        			"options": {},
        			"hiden":{"options": [true, false]},
        			"required":{"options": [true, false]},
        			"flags":{},
        			"readOnly":{"options": [true, false]},
        			"title":{},
        			"isClonable":{"options": [true, false]}
        			};

        	this.listItemCrudJson.push(new CrudItemJson(fields, this.instance, "fields", "Campos dos formulários", this.serverConnection));
    		this.$scope.$apply();
        	return response;
    	});
    }

}
