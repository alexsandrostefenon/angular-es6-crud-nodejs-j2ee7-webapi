import {Utils} from "./Utils.js";
import {DataStoreItem} from "./DataStore.js";
// differ of DataStoreItem by UI features, serverConnection and field.foreignKeysImport dependencies
class CrudUiSkeleton extends DataStoreItem {
	
	static calcPageSize() {
		let pageSize;
		let avaiableHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		const style = window.getComputedStyle(document.getElementById("header"));
		avaiableHeight -= parseFloat(style.height);
		const rowHeight = parseFloat(style.fontSize) * 2.642857143;
    	pageSize = Math.trunc(avaiableHeight / rowHeight);
    	pageSize -= 4;
    	return pageSize;
	}

	constructor(serverConnection, name, databaseUiAdapter, selectCallback) {
		super(name, databaseUiAdapter.fields);
		this.serverConnection = serverConnection;
		this.translation = serverConnection.translation;
		this.formId = name + "Form";
		this.databaseUiAdapter = databaseUiAdapter;
		this.selectCallback = selectCallback;
	}

	buildFieldFilterResults() {
		// faz uma referencia local a field.filterResultsStr, para permitir opção filtrada, sem alterar a referencia global
		for (let [fieldName, field] of Object.entries(this.fields)) {
			if (field.foreignKeysImport != undefined) {
				const crudService = this.serverConnection.getForeignImportCrudService(field);

				if (crudService != undefined) {
					field.filterResults = crudService.list;
					field.filterResultsStr = crudService.listStr;
				} else {
					console.warn("don't have acess to service ", field.foreignKeysImport);
					field.filterResults = [];
					field.filterResultsStr = [];
				}
			} else if (field.options != undefined) {
				field.filterResults = field.options;
				
				if (field.optionsLabels != undefined) {
					field.filterResultsStr = field.optionsLabels;
				} else {
					field.filterResultsStr = field.options;
				}
			}
			
			if (field.htmlType.includes("date")) {
				field.filterRangeOptions = [
					" hora corrente ", " hora anterior ", " uma hora ",
					" dia corrente ", " dia anterior ", " um dia ",
					" semana corrente ", " semana anterior ", " uma semana ", 
					" quinzena corrente ", " quinzena anterior ", " uma quinzena ",
					" mês corrente ", " mês anterior ", " um mês ",
					" ano corrente ", " ano anterior ", " um ano "
				];
				
				field.aggregateRangeOptions = ["", "hora", "dia", "mês", "ano"];
			}
		}
	}

	process(action, params) {
		super.process(action, params);
		this.buildFieldFilterResults();
	}
	
	setValues(obj) {
		super.setValues(obj);
		// fieldFirst is used in form_body html template
		this.fieldFirst = undefined;
		const list = Object.entries(this.fields);
		let filter;
		filter = list.filter(([fieldName, field]) => field.hiden != true && field.readOnly != true && field.required == true && this.instance[fieldName] == undefined);
		if (filter.length == 0) filter = list.filter(([fieldName, field]) => field.hiden != true && field.readOnly != true && field.required == true);
		if (filter.length == 0) filter = list.filter(([fieldName, field]) => field.hiden != true && field.readOnly != true);
		if (filter.length == 0) filter = list.filter(([fieldName, field]) => field.hiden != true);
		if (filter.length > 0) this.fieldFirst = filter[0][0];
	}

	paginate() {
		let pageSize = CrudUiSkeleton.calcPageSize();
		if (pageSize < 10) pageSize = 10;
		this.pagination.paginate(this.filterResults, pageSize);
	}
	
	buildFieldStr(fieldName, item) {
		super.buildFieldStr(fieldName, item);
		return this.databaseUiAdapter.buildFieldStr(fieldName, item);
	}

	parseValue(fieldName) {
		const field = this.fields[fieldName];

		if (field.flags != undefined && field.flags != null) {
			// field.flags : String[], vm.instanceFlags[fieldName] : Boolean[]
			this.instance[fieldName] = Number.parseInt(Utils.flagsToStrAsciiHex(this.instanceFlags[fieldName]), 16);
		} else {
			let pos = field.filterResultsStr.indexOf(field.externalReferencesStr);
			
			if (pos >= 0) {
				if (field.foreignKeysImport != undefined) {
					const foreignData = field.filterResults[pos];
					this.instance[fieldName] = foreignData[field.foreignKeysImport.field];
				} else if (field.options != undefined) {
					this.instance[fieldName] = field.filterResults[pos];
				}

				if (this.selectCallback != undefined && this.selectCallback != null) {
					this.selectCallback(fieldName, this.instance[fieldName]);
				}
			}
		}
	}

}

export {CrudUiSkeleton}
