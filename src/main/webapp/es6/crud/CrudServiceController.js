import {CrudController} from "./CrudController.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {CaseConvert} from "./CaseConvert.js";

class CrudServiceController extends CrudController {

    constructor(serverConnection, $scope) {
    	super(serverConnection, $scope);
//		const defaultValues = {type: "s", updatable: true, length: 255, precision: 9, scale: 3, hiden: false, primaryKey: false, required: false};
    	this.crudServicefields = {
    			"type":{"options": ["s", "i", "b", "n", "datetime-local", "date", "time"]},
    			"defaultValue":{},
    			"options": {},
    			"optionsLabels": {},
    			"sortType":{"options": ["asc", "desc"]},
    			"orderIndex":{"type": "i"},
    			"tableVisible":{"type": "b"},
    			"required":{"type": "b"},
    			"length":{"type": "i"},
    			"precision":{"type": "i"},
    			"scale":{"type": "i"},
    			"primaryKey":{"type": "b"},
    			"identityGeneration":{"options": ["ALWAYS", "BY DEFAULT"]},
    			"foreignKeysImport":{}, // [table, field]
    			"shortDescription":{"type": "b"},
    			"comment":{},
    			"title":{},
    			"isClonable":{"type": "b"},
    			"unique":{"type": "b"},
    			"updatable":{"type": "b"},
    			"hiden":{"type": "b"},
    			"readOnly":{"type": "b"},
    			};

    	this.crudServicefields.foreignKeysImport.options = [];
    	this.crudServicefields.foreignKeysImport.optionsLabels = [];
    	
    	for (let service of this.crudService.list) {
    		let fields = JSON.parse(service.fields);
    		
    		for (let [field, fieldObj] of Object.entries(fields)) {
    			if (fieldObj.primaryKey == true) {
    				let value = {table: service.name, field};
    				this.crudServicefields.foreignKeysImport.options.push(value);
    				this.crudServicefields.foreignKeysImport.optionsLabels.push(JSON.stringify(value));
    			}
    		}
    	}
    	
       	this.listItemCrudJson.push(new CrudItemJson(this, this.crudServicefields, "fields", "Campos dos formulários", this.serverConnection));
    }

    save() {
		this.instance.name =  CaseConvert.underscoreToCamel(this.instance.name);
		return super.save();
    }

}

export {CrudServiceController}
