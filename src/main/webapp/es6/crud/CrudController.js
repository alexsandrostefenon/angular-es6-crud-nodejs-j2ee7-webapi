import {CrudCommom} from "./CrudCommom.js";
import {CrudItem} from "./CrudItem.js";
import {CrudItemJson} from "./CrudItemJson.js";
import {CrudObjJson} from "./CrudObjJson.js";
import {CrudJsonArray} from "./CrudJsonArray.js";
import {CaseConvert} from "./CaseConvert.js";
import {ServerConnectionUI} from "./ServerConnectionUI.js";
// CrudController differ of CrudCommom by angular $scope dependency, used in $scope.apply() pos promise rendering
class CrudController extends CrudCommom {

    constructor(serverConnection, $scope) {
    	serverConnection.clearRemoteListeners();
    	const url = new URL(window.location.hash.substring(2), window.location.href);
    	const path = url.pathname;
		const list = path.split('/');
		const action = list[list.length-1];
		const serviceName = CaseConvert.underscoreToCamel(list[list.length-2]);
		const searchParams = {};

		for (const [key,value] of url.searchParams) {
			searchParams[key] = JSON.parse(value);
		}

    	super(serverConnection, serverConnection.services[serviceName]);
		this.listItemCrud = [];
		this.listItemCrudJson = [];
		this.listObjCrudJson = [];
		this.listCrudJsonArray = [];
    	this.$scope = $scope;
    	this.process(action, searchParams);
    }
    
	onNotify(primaryKey, action) {
		let ret = super.onNotify(primaryKey, action);
//   		this.$scope.$apply();
		return ret;
	}

    get(primaryKey) {
    	return super.get(primaryKey).then(response => {
			// monta a lista dos CrudItem
			const list = this.serverConnection.getForeignExportCrudServicesFromService(this.crudService.params.name); // [{table, field}]
			
			for (let item of list) {
				let crudService = this.serverConnection.services[item.table];
				
				for (let [fieldName, field] of Object.entries(crudService.fields)) {
					if (field.title != undefined && field.title.length > 0) {
				        // serverConnection, serviceName, fieldName, primaryKeyForeign, title, numMaxItems, queryCallback, selectCallback
				    	this.listItemCrud.push(new CrudItem(this.serverConnection, item.table, fieldName, this.primaryKey));
					}
				}
			}
			
			this.listItemCrudJson.forEach(item => item.get(this.instance));
			this.listObjCrudJson.forEach(item => item.get(this.instance));
			this.listCrudJsonArray.forEach(item => item.get(this.instance));
    		this.$scope.$apply();
    		return response;
    	});
    }
	
	remove(primaryKey) {
		return super.remove(primaryKey).then(data => {
            // data may be null
			this.goToSearch();
			return data;
		});
	}

	update() {
		return super.update().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			if (this.crudService.params.saveAndExit != false) {
				this.goToSearch();
			} else {
				ServerConnectionUI.changeLocationHash(this.crudService.path + "/" + "edit", primaryKey);
			}
			
			return response;
		});
	}

	save() {
		return super.save().then(response => {
			var primaryKey = this.crudService.getPrimaryKey(response.data);

			for (let item of this.listItemCrud) {
				item.clone(primaryKey);
			}
			
			if (this.crudService.params.saveAndExit != false) {
				this.goToSearch();
			} else {
				ServerConnectionUI.changeLocationHash(this.crudService.path + "/" + "edit", primaryKey);
			}
			
			return response;
		});
	}
	
	saveAsNew() {
		if (this.instance.id != undefined) {
			this.instance.id = undefined;
		}
		
		return this.save();
	}

	toggleFullscreen() {
	  let elem = document.documentElement;

	  if (!document.fullscreenElement) {
	    elem.requestFullscreen().then({}).catch(err => {
	      alert(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`);
	    });
	  } else {
	    //document.exitFullscreen();
	  }
	}

}

export {CrudController}
