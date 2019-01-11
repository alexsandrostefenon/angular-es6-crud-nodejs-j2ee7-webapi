import {Utils} from "./Utils.js";
import {CrudUiSkeleton} from "./CrudUiSkeleton.js";
import {ServerConnectionUI} from "./ServerConnectionUI.js";

export class Pagination {

    constructor() {
		let avaiableHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		const style = window.getComputedStyle(document.getElementById("header"));
		avaiableHeight -= parseFloat(style.height);
		const rowHeight = parseFloat(style.fontSize) * 2.642857143;
    	this.pageSize = avaiableHeight / rowHeight;
    	this.pageSize -= 4;
    	this.process([]);
    }

    process(list) {
    	this.list = list;
        var result = Math.ceil(list.length/this.pageSize);
        this.numPages = (result == 0) ? 1 : result;
    	this.currentPage = 1;
    	this.changePage();
    }

    changePage(n) {
     	this.listPage = this.list.slice((this.currentPage-1) * this.pageSize, this.currentPage * this.pageSize);
     }

}

export class CrudCommom extends CrudUiSkeleton {

	constructor(serverConnection, crudService, objParams, action) {
		super(serverConnection, crudService.params.name, crudService.databaseUiAdapter);
		this.crudService = crudService;
		this.primaryKey = this.crudService.getPrimaryKey(objParams);
		this.action = action;
		this.filterResults = this.crudService.list;
		this.setValues(objParams);
		this.pagination = new Pagination();
		this.paginate();

		if (this.crudService.params.template != undefined && this.crudService.params.template.length > 0) {
			this.template = "templates/impl/crud-" + this.crudService.params.template + ".html";
		} else {
			this.template = "templates/crud-model_form_body.html";
		}
	}

	buildLocationHash(hashPath, hashSearchObj) {
		return ServerConnectionUI.buildLocationHash(this.crudService.path + "/" + hashPath, hashSearchObj)
	}

	goToSearch() {
		if (window.location.hash.endsWith("/search") == false) {
			window.history.back();
		}
	}
	// fieldName, 'view', item, false
    goToField(fieldName, action, obj, isGoNow) {
    	const field = this.fields[fieldName];
    	let service = field.crudService;
    	let primaryKey = {};

    	if (obj != null && obj != undefined) {
    		if (service != undefined) {
    			// neste caso, obj[fieldName] contém o id do registro de referência
				// dataForeign, fieldNameForeign, fieldName
    			primaryKey = service.getPrimaryKeyFromForeignData(obj, fieldName, field.fieldNameForeign);
    		} else {
    			service = this.crudService;
    			primaryKey = this.crudService.getPrimaryKey(obj);
    		}
    	}

		const url = ServerConnectionUI.buildLocationHash(service.path + "/" + action, primaryKey);

    	if (isGoNow == true) {
    		ServerConnectionUI.changeLocationHash(service.path + "/" + action, primaryKey);
    	}

    	return url;
    }

	remove(primaryKey) {
		if (primaryKey == undefined) {
			primaryKey = this.primaryKey;
		}

		return this.crudService.remove(primaryKey);
	}

	update() {
		return this.crudService.update(this.primaryKey, this.instance);
	}

	save() {
		this.primaryKey = {};
		return this.crudService.save(this.instance);
	}

	paginate() {
		this.pagination.process(this.filterResults);
	}

	clearFilter() {
		if (this.filterResults != this.crudService.list) {
			this.filterResults = this.crudService.list;
			this.paginate();
		}
	}

	applyFilter() {
		this.filterResults = this.crudService.getFilteredItems(this.instance, true);
		this.paginate();
	}

}
