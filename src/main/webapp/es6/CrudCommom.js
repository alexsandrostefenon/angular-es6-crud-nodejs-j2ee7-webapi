import {Utils} from "./Utils.js";
import {CrudUiSkeleton} from "./CrudUiSkeleton.js";

export class Pagination {

    constructor(pageSize) {
    	this.pageSize = pageSize;
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

	constructor(serverConnection, crudService, searchParams, action) {
		super(serverConnection, crudService.params.name, crudService.databaseUiAdapter);
		this.crudService = crudService;
		this.primaryKey = this.crudService.getPrimaryKey(searchParams);
		this.action = action;
		this.filterResults = this.crudService.list;
		this.filterResultsStr = this.crudService.listStr;
		this.setValues(searchParams);
		this.pagination = new Pagination(10);
		this.paginate();

		if (this.crudService.params.template != undefined && this.crudService.params.template.length > 0) {
			this.template = "templates/impl/crud-" + this.crudService.params.template + ".html";
		} else {
			this.template = "templates/crud-model_form_body.html";
		}
	}

	goToView(primaryKey) {
//		this.serverConnection.$location.path(this.buildUrl(this.crudService, primaryKey, "view"));
		this.serverConnection.$location.path("/app/" + this.crudService.path + "/view").search(primaryKey);
	}

	goToEdit(primaryKey) {
//		this.serverConnection.$location.path(this.buildUrl(this.crudService, primaryKey, "edit"));
		this.serverConnection.$location.path("/app/" + this.crudService.path + "/edit").search(primaryKey);
	}

	goToNew() {
//		this.serverConnection.$location.path(this.buildUrl(this.crudService, {}, "new"));
		this.serverConnection.$location.path("/app/" + this.crudService.path + "/new").search({});
	}

	goToSearch() {
//		this.serverConnection.$location.path(this.buildUrl(this.crudService, {}, "search"));
//		this.serverConnection.$location.path("/app/" + this.crudService.path + "/search").search({});
		window.history.back();
	}
	// fieldName, 'view', item, false
    goToField(fieldName, action, obj, isGoNow) {
    	var service = this.fields[fieldName].crudService;
    	var primaryKey = {};

    	if (obj != null && obj != undefined) {
    		if (service != undefined) {
    			// neste caso, obj[fieldName] contém o id do registro de referência
    			primaryKey = service.getPrimaryKey(obj, obj[fieldName]);
    		} else {
    			service = this.crudService;
    			primaryKey = this.crudService.getPrimaryKey(obj);
    		}
    	}

    	var obj = this.buildUrl(service, primaryKey, action);

    	if (isGoNow == true && this.serverConnection.$location != undefined) {
    		this.serverConnection.$location.path(obj.path).search(obj.search);
    	}

    	return "#!" + obj.url;
    }

	remove(primaryKey) {
		if (primaryKey == undefined) {
			primaryKey = this.primaryKey;
		}

		this.crudService.remove(primaryKey, data => {
			if (this.removeCallback != undefined) {
				this.removeCallback();
			} else if (this.primaryKey) {
				this.goToSearch();
			}
		});
	}

	save(forceReload) {
		for (var fieldName in this.fields) {
			var field = this.fields[fieldName];
			// primeiro, parseia as flags
			if (field.flagsInstance != undefined) {
				// field.flags -> tipagem : String[]
				// field.flagsInstance -> tipagem : Boolean[]
				this.instance[fieldName] = Utils.flagsToStrAsciiHex(field.flagsInstance);
			}
			// depois parseia os campos json
			if (field.strJson != undefined) {
				this.instance[fieldName] = field.strJson;
			}
		}

		var scope = this;

		var callback = function(data) {
			if (scope.saveCallback != undefined) {
				scope.saveCallback(data);
			} else if (forceReload != true && scope.crudService.params.saveAndExit == true) {
				scope.goToSearch();
			} else {
				scope.goToEdit(scope.crudService.getPrimaryKey(data));
			}
		}

		var callbackNew = function(data) {
			var primaryKey = scope.crudService.getPrimaryKey(data);

			for (var item of scope.listItemCrud) {
				item.clone(primaryKey);
			}

			callback(data);
		}

		if (this.crudService.checkPrimaryKey(this.primaryKey) == true) {
			this.crudService.update(this.primaryKey, this.instance, callback);
		} else {
			this.crudService.save({}, this.instance, callbackNew);
		}
	}

	saveAsNew() {
		this.primaryKey = {};
		this.save(true);
	}

	paginate() {
		this.pagination.process(this.filterResults);
	}

	clearFilter() {
		if (this.filterResults != this.crudService.list) {
			this.filterResults = this.crudService.list;
			this.filterResultsStr = this.crudService.listStr;
			this.paginate();
		}
	}

	applyFilter() {
		this.filterResults = this.crudService.getFilteredItems(this.instance, true);
		this.filterResultsStr = this.crudService.buildListStr(this.filterResults);
		this.paginate();
	}

}
