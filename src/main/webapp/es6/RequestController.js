import {CrudController} from "./CrudController.js";
import {CrudItem} from "./CrudItem.js";

export const name = "RequestController";

export class RequestController extends CrudController {

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

    enableRequestProduct() {
        var onProductsChanged = (list) => {
        	this.instance.productsValue = this.getSumValues(list);
        	this.instance.sumValue = this.instance.productsValue + this.instance.servicesValue + this.instance.transportValue;
        }
        
        var onProductSelected = (id) => {
        	var item = this.serverConnection.services.stock.findOne({product:id});
    		this.crudItemProduct.instance.value = (item != null) ? item.value : 0.0;
        }
        
        this.crudItemProduct = new CrudItem(this.serverConnection, "requestProduct", "request", this.primaryKey, false, 'Produtos', null, list => onProductsChanged(list), (field, id) => onProductSelected(id));
		this.listItemCrud.push(this.crudItemProduct);
    }
    
    enableRequestPayment() {
		if (this.serverConnection.services.requestPayment != undefined && this.serverConnection.services.requestPayment.params.access.update != false) {
		    var onPaymentsChanged = (list) => {
		    	this.instance.paymentsValue = this.getSumValues(list);
		    }
		    
		    var onAccountSelected = (id) => {
				this.crudItemPayment.instance.value = this.instance.sumValue - this.instance.paymentsValue;
		    }
		    
	        this.crudItemPayment = new CrudItem(this.serverConnection, "requestPayment", "request", this.primaryKey, false, 'Pagamentos', null, list => onPaymentsChanged(list), (field, id) => onAccountSelected(id));
			this.listItemCrud.push(this.crudItemPayment);
		}
    }
    
    enableRequestFields() {
    	this.enableRequestProduct();
    	this.enableRequestPayment();
    }

	filterRequestState() {
		var filterResults = [];
		var list = this.fields.state.crudService.list;

		for (var j = 0; j < list.length; j++) {
			var itemRef = list[j];

			if (itemRef.id == this.instance.state) {

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

				break;
			}
		}

		this.setFieldOptions("state", filterResults);
	}

	update() {
    	return super.update().then(response => {
	    	this.filterRequestState();
    		this.$scope.$apply();
        	return response;
    	});
	}

    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
	    	this.filterRequestState();
           	this.enableRequestFields();
    		this.$scope.$apply();
        	return response;
    	});
    }

    constructor(serverConnection, $scope) {
    	super(serverConnection, $scope);
    	this.saveAndExit = false;

    	if (this.action == "new") {
        	this.instance.date = new Date();
        	this.instance.date.setMilliseconds(0);
	    	this.filterRequestState();
		}

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

export class Controller extends RequestController {
}

