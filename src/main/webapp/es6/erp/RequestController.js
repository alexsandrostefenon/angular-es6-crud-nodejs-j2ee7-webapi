import {CrudController} from "../CrudController.js";
import {CrudItem} from "../CrudItem.js";

export class RequestController extends CrudController {

    getSumValues(list) {
    	let sum = 0.0;

    	for (let item of list) {
    		let quantity = (item.quantity != undefined && item.quantity != null) ? item.quantity : 1.0;
    		let value = (item.value != undefined && item.value != null) ? item.value : 0.0;
    		item.valueItem = Math.floor(quantity * value * 100.0) / 100.0;
    		sum += item.valueItem;
    	}

    	return sum;
    }

    getSumDescValues(list) {
    	let sum = 0.0;

    	for (let item of list) {
    		sum += item.valueDesc;
    	}

    	return sum;
    }

    enableRequestProduct() {
        const onProductsChanged = (list) => {
        	this.instance.productsValue = this.getSumValues(list);
        	this.instance.descValue = this.getSumDescValues(list);
        	this.instance.sumValue = this.instance.productsValue + this.instance.servicesValue + this.instance.transportValue - this.instance.descValue;
        	this.instance.sumValue = Math.floor(this.instance.sumValue * 100.0) / 100.0;
        }
        
        const onSelected = (fieldName, value) => {
        	if (fieldName == "product") {
				const item = this.serverConnection.services.stock.findOne({product:value});
				this.crudItemProduct.instance.value = (item != null) ? item.value : 0.0;
        	}
        }
        // serverConnection, serviceName, fieldName, primaryKeyForeign, title, numMaxItems, queryCallback, selectCallback
        this.crudItemProduct = new CrudItem(this.serverConnection, "requestProduct", "request", this.primaryKey, 'Produtos', null, list => onProductsChanged(list), onSelected);
		this.listItemCrud.push(this.crudItemProduct);
    }
    
    enableRequestPayment() {
		if (this.serverConnection.services.requestPayment != undefined && this.serverConnection.services.requestPayment.params.access.update != false) {
		    const onPaymentsChanged = (list) => {
		    	this.instance.paymentsValue = this.getSumValues(list);
		    }
		    
		    const onSelected = (fieldName, value) => {
	        	if (fieldName == "type") {
					this.crudItemPayment.instance.dueDate = this.instance.date;
					this.crudItemPayment.instance.value = this.instance.sumValue - this.instance.paymentsValue;
	        	}
		    }
	        // serverConnection, serviceName, fieldName, primaryKeyForeign, title, numMaxItems, queryCallback, selectCallback
	        this.crudItemPayment = new CrudItem(this.serverConnection, "requestPayment", "request", this.primaryKey, 'Pagamentos', null, list => onPaymentsChanged(list), onSelected);
			this.listItemCrud.push(this.crudItemPayment);
		}
    }
    
    enableRequestFreight() {
		if (this.serverConnection.services.requestFreight != undefined && this.serverConnection.services.requestFreight.params.access.update != false) {
		    const onTransportChanged = (list) => {
		    	this.instance.transportValue = this.getSumValues(list);
		    	this.instance.sumValue = this.instance.productsValue + this.instance.servicesValue + this.instance.transportValue;
		    }
	        // serverConnection, serviceName, fieldName, primaryKeyForeign, title, numMaxItems, queryCallback, selectCallback
	        this.crudItemFreight = new CrudItem(this.serverConnection, "requestFreight", "request", this.primaryKey, 'Transportador', 1, list => onTransportChanged(list));
			this.listItemCrud.push(this.crudItemFreight);
		}
    }
    
    enableRequestFields() {
    	this.enableRequestProduct();
    	this.enableRequestPayment();
    	this.enableRequestFreight();
    }

	filterRequestState() {
		const filterResults = [];
		const list = this.fields.state.crudService.list;

		for (let itemRef of list) {
			if (itemRef.id == this.instance.state) {
				for (let item of list) {
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

	generateNFE(request) {
		const ide = {};
		const company = {};
		const nfe = {};
		nfe.nfeProc = {};
		nfe.nfeProc.NFe = {};
		nfe.nfeProc.NFe.infNFe = {};
		nfe.nfeProc.NFe.infNFe.ide = ide;
	}
	
	update() {
    	return super.update().then(response => {
	    	this.filterRequestState();
    		this.generateNFE(response.data);
//    		this.$scope.$apply();
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
    		if (this.instance.date == undefined) {
				this.instance.date = new Date();
				this.instance.date.setMilliseconds(0);
    		}
    		
	    	this.filterRequestState();
		}

		this.fields.type.readOnly = true;

		if (this.serverConnection.services.requestFreight == undefined) {
			this.fields.transportValue.hiden = true;
		}

		if (this.serverConnection.services.requestPayment == undefined) {
			this.fields.paymentsValue.hiden = true;
		}

		if (this.serverConnection.services.requestService == undefined) {
			this.fields.servicesValue.hiden = true;
		}
    }

}
