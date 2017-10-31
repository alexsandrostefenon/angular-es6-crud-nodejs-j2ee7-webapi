/**
 * http://usejsdoc.org/
 */
define(["CrudController", "CrudItemJson", "Utils"], function(CrudController, CrudItemJson, Utils) {

class CrudServiceController extends CrudController {

    constructor(serverConnection) {
    	super(serverConnection);
    }

    getCallback(data) {
    	var types = Utils.getFieldTypes();
		// params.fields = {"field": {"flags": ["label 1", "label 2", ...], "type": "text"}}
    	var serviceOptions = [];
    	
    	for (var item of this.serverConnection.services.crudService.list) {
    		serviceOptions.push(item.name);
    	}
    	
    	var fields = {
    			"type":{"options": types},
    			"defaultValue": {},
    			"options": {},
    			"hiden":{"options": [true, false]},
    			"required":{"options": [true, false]},
    			"flags":{},
    			"readOnly":{"options": [true, false]},
    			"primaryKey":{"options": [true, false]},
    			"service":{"options": serviceOptions},
    			"title":{},
    			"isClonable":{"options": [true, false]}
    			};
    	
    	this.listItemCrudJson.push(new CrudItemJson(fields, this.instance, "fields", "Campos dos formulários", this.serverConnection));
    }

}

globalControllerProvider.register("CrudServiceController", function(ServerConnectionService) {
	return new CrudServiceController(ServerConnectionService);
});

return CrudServiceController;

});