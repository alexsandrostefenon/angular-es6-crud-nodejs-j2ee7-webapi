/**
 * http://usejsdoc.org/
 */
define(["CrudController", "CrudItem"], function(CrudController, CrudItem) {

class RequestController extends CrudController {
	
    enableRequestFields() {
    	var scope = this;
    	
        var productsQueryCallback = function(list) {
    		scope.onProductsChanged(list);
        }
        
        var paymentsQueryCallback = function(list) {
    		scope.onPaymentsChanged(list);
        }
        
        var productsSelectCallback = function(field, id) {
    		scope.onProductSelected(id);
        }
        
        var paymentsSelectCallback = function(field, id) {
    		scope.onAccountSelected(id);
        }
        
        // serverConnection, serviceName, fieldName, fieldValue, title, numMaxItems, queryCallback, selectCallback
        this.crudItemProduct = new CrudItem(this.serverConnection, "requestProduct", "request", this.primaryKey, false, 'Produtos', null, productsQueryCallback, productsSelectCallback);
		this.listItemCrud.push(this.crudItemProduct);
		
		if (this.serverConnection.services.requestPayment != undefined && this.serverConnection.services.requestPayment.params.access.update != false) {
	        this.crudItemPayment = new CrudItem(this.serverConnection, "requestPayment", "request", this.primaryKey, false, 'Pagamentos', null, paymentsQueryCallback, paymentsSelectCallback);
			this.listItemCrud.push(this.crudItemPayment);
		}
    }
    
    getSumValues(list) {
    	var sum = 0.0;
    	
    	for (var i = 0; i < list.length; i++) {
    		var item = list[i];
    		var quantity = (item.quantity != undefined && item.quantity != null) ? item.quantity : 1.0;
    		var value = (item.value != undefined && item.value != null) ? item.value : 0.0;
    		item.valueItem = Math.floor(quantity * value * 100.0) / 100.0;
    		sum += item.valueItem;
    	}
    	
    	return sum;
    }
    
    onProductsChanged(list) {
    	this.instance.productsValue = this.getSumValues(list);
    	this.instance.sumValue = this.instance.productsValue + this.instance.servicesValue + this.instance.transportValue;
    }
    
    onPaymentsChanged(list) {
    	this.instance.paymentsValue = this.getSumValues(list);
    }
    
    onProductSelected(id) {
    	var item = this.serverConnection.services.stock.findOne({product:id});
		this.crudItemProduct.instance.value = (item != null) ? item.value : 0.0;
    }
    
    onAccountSelected(id) {
		this.crudItemPayment.instance.value = this.instance.sumValue - this.instance.paymentsValue;
    }
    
	filterRequestState() {
		var list = this.fields.state.crudService.list;
		
		for (var j = 0; j < list.length; j++) {
			var itemRef = list[j];
			
			if (itemRef.id == this.instance.state) {
				var filterResults = [];
				
				for (var i = 0; i < list.length; i++) {
					var item = list[i];
					
					if ((item.next == itemRef.id) ||
						(item.id == itemRef.prev) ||
						(item.id == itemRef.id) ||
						(item.id == itemRef.next) ||
						(item.prev == itemRef.id)) {
						filterResults.push(item);
					}
				}
				
				this.setFieldOptions("state", filterResults);
				break;
			}
		}
	}

    getCallback(data) {
       	this.enableRequestFields();
    }
    
	saveCallback(data) {
		this.goToEdit(this.crudService.getPrimaryKey(data));
	}

    constructor(serverConnection) {
    	super(serverConnection);
    	this.saveAndExit = false;

    	if (this.action == "new") {
        	this.instance.date = new Date();
        	this.instance.date.setMilliseconds(0);
		}

    	this.filterRequestState();
		this.fields.type.readOnly = true;
		var count = 3;

		if (this.serverConnection.services.requestFreight == undefined) {
			this.fields.transportValue.hiden = true;
			count--;
		}

		if (this.serverConnection.services.requestPayment == undefined) {
			this.fields.paymentsValue.hiden = true;
		}

		if (this.serverConnection.services.requestService == undefined) {
			this.fields.servicesValue.hiden = true;
			count--;
		}

		if (count == 1) {
			this.fields.sumValue.hiden = true;
		}
    }
    
}

globalControllerProvider.register("RequestController", function(ServerConnectionService) {
	return new RequestController(ServerConnectionService);
});

return RequestController;

});
