// minimal wrapper to Html5 IndexedDb
class DataStore {

	static getPrimaryKeysFromFields(fields) {
		var primaryKeys = [];

		for (var fieldName in fields) {
			var field = fields[fieldName];

			if (field.primaryKey == true) {
				primaryKeys.push(fieldName);
			}
		}

		return primaryKeys;
	}

	constructor(name, fields, list) {
		const entries = Object.entries(fields);
		this.shortDescriptionList = [];
		
		for (let [fieldName, field] of entries) {
			if (field.type == undefined) field.type = "s";
			if (field.orderIndex == undefined) field.orderIndex = entries.length;
			if (field.tableVisible == undefined) field.tableVisible = true;
			if (field.hiden == true) field.tableVisible = false;
			if (field.shortDescription == undefined && fieldName == "name") field.shortDescription = true;
			if (field.shortDescription == undefined) field.shortDescription = false;
			if (field.shortDescription == true) this.shortDescriptionList.push(fieldName);
		}

		if (this.shortDescriptionList.length == 0) {
			for (let [fieldName, field] of entries) {
				if (field.tableVisible == true) this.shortDescriptionList.push(fieldName);
			}
		}
		
		this.name;
		this.fields = fields;
		this.list = list || [];
		this.primaryKeys = DataStore.getPrimaryKeysFromFields(this.fields);
	}

	clear() {
		for (let fieldName in this.fields) {
			let field = this.fields[fieldName];
			delete field.externalReferencesStr;
		}
	}

	process(action, params) {
		this.clear();
	}
	
	checkPrimaryKey(obj) {
		var check = true;

		for (var fieldName of this.primaryKeys) {
			if (obj[fieldName] == undefined) {
				check = false;
				break;
			}
		}

		return check;
	}

	getPrimaryKey(obj) {
		let primaryKey = {};

		for (var fieldName of this.primaryKeys) {
			primaryKey[fieldName] = obj[fieldName];
		}

		return primaryKey;
	}

	getPrimaryKeyFromForeignData(dataForeign, fieldNameForeign, fieldName) {
		let primaryKey = this.getPrimaryKey(dataForeign);

		if (fieldName != undefined && fieldName != null) {
			primaryKey[fieldName] = dataForeign[fieldNameForeign];
		} else if (this.primaryKeys.indexOf(fieldNameForeign) < 0 && this.primaryKeys.indexOf("id") >= 0) {
			primaryKey["id"] = dataForeign[fieldNameForeign];
		}

		return primaryKey;
	}
	
	find(params) {
        return Filter.find(this.list, params);
	}

	findOneIn(paramsList) {
        return Filter.findOneIn(this.list, paramsList);
	}

	findPos(params) {
		return Filter.findPos(this.list, params);
	}

	findOne(params) {
		let pos = this.findPos(params);
		return pos >= 0 ? this.list[pos] : null;
	}
	// private, use in getRemote, save, update and remove
	updateList(data, oldPos, newPos) {
        if (oldPos == undefined && newPos == undefined) {
        	// add
			this.list.push(data);
			newPos = this.list.length - 1;
        } else if (oldPos != undefined && newPos == undefined) {
        	// remove
        	this.list.splice(oldPos, 1);
        } else if (oldPos != undefined && oldPos == newPos) {
        	// replace
           	this.list[oldPos] = data;
        }
        
        return {"data": data, "oldPos": oldPos, "newPos": newPos};
	}
	// used by websocket
	removeInternal(primaryKey) {
        let pos = this.findPos(primaryKey);
		console.log("CrudService.removeInternal : pos = ", pos, ", data :", this.list[pos]);
        return pos >= 0 ? this.updateList(this.list[pos], pos) : null;
	}
	// private, use in save and update methods
	copyFields(dataIn) {
		var dataOut = {};

		for (let fieldName in this.fields) {
			let field = this.fields[fieldName];
			let value = dataIn[fieldName];
			
			if (field.type == "i" && (typeof value) === "string") {
				dataOut[fieldName] = Number.parseInt(value);
			} else {
				dataOut[fieldName] = value;
			}
		}

		return dataOut;
	}
	
	getFilteredItems(objFilter, objFilterMin, objFilterMax, matchPartial) {
		var list = [];

		if (objFilter != undefined && objFilter != null) {
			list = Filter.process(objFilter, objFilterMin, objFilterMax, this.list, matchPartial);
		} else {
			list = this.list;
		}

		return list;
	}
	
}
// differ to DataStore by instance and filter, aggregate, sort and pagination features
class DataStoreItem extends DataStore {
	
	constructor(name, fields, list) {
		super(name, fields, list);
		this.pagination = new Pagination(100);
	}

	isClean() {
		var ret = angular.equals(this.original, this.instance);
		return ret;
	}

	process(action, params) {
		super.process(action, params);
		this.clearFilter();
		this.clearSort();
		this.clearAggregate();
	}
// Instance section
	clear() {
		super.clear();
		this.instance = {};
		this.instanceFlags = {};
		this.setValues(); // set default values
	}
	
	setValues(obj) {
		let getDefaultValue = field => {
			let value;

			if (field.defaultValue != undefined) {
				if (field.type == "i") {
					value = Number.parseInt(field.defaultValue);
				} else if (field.type == "n") {
					value = Number.parseFloat(field.defaultValue);
				} else if (field.type.includes("date") || field.type.includes("time")) {
					value = new Date();
					value.setMilliseconds(0);
				} else {
					value = field.defaultValue;
				}
			} else {
				value = undefined;
			}

			return value;
		};

		if (obj == undefined) {
			obj = {};
		}

		for (let [fieldName, field] of Object.entries(this.fields)) if (obj[fieldName] == undefined && field.defaultValue != undefined) obj[fieldName] = getDefaultValue(field);

		for (let [fieldName, field] of Object.entries(this.fields)) {
			let value = obj[fieldName];
			delete field.externalReferencesStr;

			if (value != undefined) {
				if (field.foreignKeysImport != undefined) {
					field.externalReferencesStr = this.buildFieldStr(fieldName, obj);
				} else if (field.flags != undefined && field.flags != null) {
					// field.flags : String[], vm.instanceFlags[fieldName] : Boolean[]
					this.instanceFlags[fieldName] = Utils.strAsciiHexToFlags(value.toString(16));
				} else if (field.options != undefined) {
					let pos;

					if (value instanceof Object) {
						let strValue = JSON.stringify(value);
						pos = field.filterResultsStr.indexOf(strValue);
						field.externalReferencesStr = field.filterResultsStr[pos];
					} else {
						pos = field.filterResults.indexOf(value);
						field.externalReferencesStr = field.filterResultsStr[pos];
					}

					if (pos < 0) {
						console.error(`DataStoreItem.setValue(${fieldName}) : don\'t found\nvalue:`, value, `\nstr:\n`, field.externalReferences, `\noptions:\n`, field.filterResultsStr);
					}
				} else {
					if (field.type == "datetime-local") {
						value = new Date(value);
					}
				}
			}

			this.instance[fieldName] = value;
		}
	}
// Aggregate Section
	clearAggregate() {
		this.instanceAggregateRange = {};
		this.aggregateResults = new Map();
	}
	// TODO implement in descent instances
	buildFieldStr(fieldName, item) {
		return "";
	}

	applyAggregate(aggregate) {
		if (aggregate == undefined) aggregate = this.instanceAggregateRange; else this.instanceAggregateRange = aggregate;
		const dateRanges = ["secound", "minute", "hora", "dia", "mês", "ano"];
		
		const labelFromDate = (date, range) => {
			let type = dateRanges.indexOf(range);
			let str = "";
			if (type <= 5) str = date.getFullYear() + " " + str;
			if (type <= 4) str = date.getMonth()+1 + "/" + str;
			if (type <= 3) str = date.getDate() + "/" + str;
			if (type <= 2) str = date.getHours() + " " + str;
			return str;
		};
		
		this.aggregateResults = new Map();
		
		for (let item of this.filterResults) {
			let label = "";
			
			for (let fieldName in aggregate) {
				let value = item[fieldName];
				let range = aggregate[fieldName];
				let field = this.fields[fieldName];
				
				if (range != false && range != "" && range != 0) {
					if (field.foreignKeysImport != undefined) {
						label = label + this.buildFieldStr(fieldName, item) + ",";
					} else if (field.flags != undefined && field.flags != null) {
						label = label + value.toString(16) + ",";
					} else if (field.options != undefined) {
						let pos = field.filterResults.indexOf(JSON.stringify(value));
						label = label + field.filterResultsStr[pos] + ",";
					} else if (field.htmlType == "number") {
						label = label + Math.trunc(value / range) * range + ",";
					} else if (field.htmlType.includes("date") || field.htmlType.includes("time")) {
						label = label + labelFromDate(value, range) + ",";
					}
				}
			}
			
			if (label.length > 0) {
				if (this.aggregateResults.has(label) == true) {
					this.aggregateResults.set(label, this.aggregateResults.get(label) + 1);
				} else {
					this.aggregateResults.set(label, 1);
				}
			}
		}
	}
// Sort section
	// format fieldsTable in correct order;
	orderFieldsSort() {
		const entries = Object.entries(this.fieldsSort);
		entries.sort((a, b) => a[1].orderIndex - b[1].orderIndex);
		this.fieldsTable = [];
		for (let [fieldName, field] of entries) if (field.hiden != true && field.tableVisible != false) this.fieldsTable.push(fieldName);
	}

	clearSort() {
		this.fieldsSort = {};

		for (let fieldName in this.fields) {
			this.fieldsSort[fieldName] = {};
			this.fieldsSort[fieldName].type = this.fields[fieldName].type;
			this.fieldsSort[fieldName].orderIndex = this.fields[fieldName].orderIndex;
			this.fieldsSort[fieldName].sortType = this.fields[fieldName].sortType;
			this.fieldsSort[fieldName].tableVisible = this.fields[fieldName].tableVisible;
		}

		this.applySort();
	}
	// sortType, orderIndex, tableVisible
	applySort(sort) {
		if (sort != undefined) {
			for (let [fieldName, field] of Object.entries(this.fieldsSort)) {
				if (sort[fieldName] != undefined) {
					field.sortType = sort[fieldName].sortType;
					field.orderIndex = sort[fieldName].orderIndex;
					field.tableVisible = sort[fieldName].tableVisible;
				}
			}
		}
		// format fieldsTable in correct order;
		this.orderFieldsSort();

		this.filterResults.sort((a, b) => {
			let ret = 0;
			
			for (let fieldName of this.fieldsTable) {
				let field = this.fieldsSort[fieldName];
				
				if (field.sortType != undefined) {
					let valA = a[fieldName];
					let valB = b[fieldName];
					
					if (valA != valB) {
						if (valB == undefined) ret = -1;
						else if (valA == undefined) ret = +1;
						else if (field.type == "i" || field.type == "n") ret = valA - valB;
						else if (field.type == "s") ret = valA.localeCompare(valB);
						else if (field.type == "b") ret = valA - valB;
						else if (field.type.includes("date") == true || field.type.includes("time") == true) ret = valA.valueOf() - valB.valueOf();
						if (field.sortType == "desc") ret *= -1;
						if (ret != 0) break;
					}
				}
			}
			
			return ret;
		});

		this.pagination.changePage();
	}

	sortToggle(fieldName) {
		const field = this.fieldsSort[fieldName];
		field.sortType = field.sortType == "asc" ? "desc" : "asc";
		this.applySort();
	}

	sortLeft(fieldName) {
		this.fieldsSort[fieldName].orderIndex--;
		this.applySort();
	}

	sortRigth(fieldName) {
		this.fieldsSort[fieldName].orderIndex++;
		this.applySort();
	}
// Filter section	
	clearFilter() {
		// hora corrente, hora anterior, uma hora, hoje, ontem, um dia, semana corrente, semana anterior, uma semana, quinzena corrente, quinzena anterior, 15 dias, mês corrente, mês anterior, 30 dias, ano corrente, ano anterior, 365 dias
		this.instanceFilter = {};
		this.instanceFilterRange = {};
		this.instanceFilterRangeMin = {};
		this.instanceFilterRangeMax = {};
		this.filterResults = this.list;
		this.paginate();
	}

	applyFilter(filter, filterRangeMin, filterRangeMax) {
		if (filter == undefined) filter = this.instanceFilter; else this.instanceFilter = filter; 
		if (filterRangeMin == undefined) filterRangeMin = this.instanceFilterRangeMin; else this.instanceFilterRangeMin = filterRangeMin; 
		if (filterRangeMax == undefined) filterRangeMax = this.instanceFilterRangeMax; else this.instanceFilterRangeMax = filterRangeMax;  
		this.filterResults = this.getFilteredItems(filter, filterRangeMin, filterRangeMax, true);
		this.paginate();
	}

	setFilterRange(fieldName, range) {
		const periodLabels =     [" minuto ", " hora ", " dia ", " semana ", " quinzena ",    " mês ",     " ano "];
		const periods =          [        60,     3600,   86400,  7 * 86400,   15 * 86400, 30 * 86400, 365 * 86400];
		let period;
		
		for (let i = 0; i < periodLabels.length; i++) {
			if (range.includes(periodLabels[i])) {
				period = periods[i] * 1000;
				break;
			}
		}
		
		let now = new Date().valueOf();
		let nowPeriodTrunc = Math.trunc(now / period) * period; 
		let dateIni = undefined;
		let dateEnd = undefined;
		
		if (range.includes(" corrente ")) {
			dateIni = new Date(nowPeriodTrunc);
		} else if (range.includes(" anterior ")) {
			dateEnd = new Date(nowPeriodTrunc);
			dateIni = new Date(nowPeriodTrunc - period);
		} else {
			dateIni = new Date(now - period);
		}
		
		const nowDate = new Date(); 
		let dayActiveStart = dateFns.startOfDay(nowDate);
		let dayLastStart = dateFns.startOfDay(nowDate);
		dayLastStart.setDate(dayLastStart.getDate()-1);
		let weekActiveStart = dateFns.startOfWeek(nowDate);
		let weekLastStart = new Date(weekActiveStart);
		weekLastStart.setDate(weekLastStart.getDate()-7);
		let monthActiveStart = dateFns.startOfMonth(nowDate);
		let monthLastStart = new Date(monthActiveStart);
		monthLastStart.setMonth(monthLastStart.getMonth()-1);
		let yearActiveStart = dateFns.startOfYear(nowDate);
		let yearLastStart = new Date(yearActiveStart);
		yearLastStart.setFullYear(yearLastStart.getFullYear()-1);
		
		if (range.includes("dia corrente") == true) {
			dateIni = dayActiveStart;
		} else if (range.includes("dia anterior") == true) {
			dateIni = dayLastStart;
			dateEnd = dayActiveStart;
		} else if (range.includes("semana corrente") == true) {
			dateIni = weekActiveStart;
		} else if (range.includes("semana anterior") == true) {
			dateIni = weekLastStart;
			dateEnd = weekActiveStart;
		} else if (range.includes("quinzena corrente") == true) {
			dateIni = nowDate.getDate() <= 15 ? monthActiveStart : new Date(monthActiveStart.setDate(15));
		} else if (range.includes("quinzena anterior") == true) {
			dateEnd = nowDate.getDate() <= 15 ? monthActiveStart : new Date(monthActiveStart.setDate(15));
			dateIni = new Date(dateEnd);
			if (dateEnd.getDate() > 15) dateIni.setDate(15); else dateIni.setDate(1); 
		} else if (range.includes("mês corrente") == true) {
			dateIni = monthActiveStart;
		} else if (range.includes("mês anterior") == true) {
			dateIni = monthLastStart;
			dateEnd = monthActiveStart;
		} else if (range.includes("ano corrente") == true) {
			dateIni = yearActiveStart;
		} else if (range.includes("ano anterior") == true) {
			dateIni = yearLastStart;
			dateEnd = yearActiveStart;
		}
		
		this.instanceFilterRangeMin[fieldName] = dateIni;
		this.instanceFilterRangeMax[fieldName] = dateEnd;
	}

}
/*
IDBKeyRange.bound
IDBKeyRange.lowerBound
IDBKeyRange.upperBound
IDBKeyRange.only
 */
class Filter {

	// private
	static matchObject(expectedFields, actualObject, testFunc, matchStringPartial, recursive) {
        let flag = true;

        for (let key in expectedFields) {
            let expectedProperty = expectedFields[key];
            
            if (expectedFields.hasOwnProperty(key) && expectedProperty != undefined) {
                let actualProperty = actualObject[key];

                if (expectedProperty == null) {
                	if (actualProperty != null) {
                        flag = false;
                	}
                } else if (actualProperty == undefined || actualProperty == null) {
                    flag = false;
                } else if (expectedProperty instanceof Date) {
                	if (typeof actualProperty === "string") {
                		flag = testFunc(expectedProperty.valueOf(), Date.parse(actualProperty));
                	} else if (actualProperty instanceof Date) {
                		flag = testFunc(expectedProperty.valueOf(), actualProperty.valueOf());
                	} else {
                		flag = false;
                	}
                } else if (typeof expectedProperty === "number") {
                	if (typeof actualProperty === "number") {
                    	flag = testFunc(expectedProperty, actualProperty);
                	} else {
                		flag = false;
                	}
                } else if (typeof expectedProperty === "string") {
                	if (typeof actualProperty === "string") {
                    	if (matchStringPartial == true) {
                            if (expectedProperty != "") {
                                flag = (actualProperty.indexOf(expectedProperty) >= 0);
                            }
                        } else {
                            flag = (actualProperty == expectedProperty);
                        }
                	} else {
                		flag = false;
                	}
                } else if (expectedProperty instanceof Object && recursive == true) {
                    flag = matchObject(expectedProperty, actualProperty, matchStringPartial, recursive, testFunc);
                } else {
                	throw new Error(`Invalid type of field ${key}, contents : ${expectedProperty}`);
                }
                
                if (flag == false) {
                	return false;
                }
            }
        }

        return flag;
    }
    // public
	static process(expectedFields, expectedFieldsMin, expectedFieldsMax, list, matchStringPartial, recursive) {
		return list.filter(candidate => {
			let flag = Filter.matchObject(expectedFields, candidate, (a,b) => a == b, matchStringPartial, recursive);
			
			if (flag == true) {
				flag = Filter.matchObject(expectedFieldsMin, candidate, (a,b) => a <= b, matchStringPartial, recursive);
				
				if (flag == true) {
					flag = Filter.matchObject(expectedFieldsMax, candidate, (a,b) => a >= b, matchStringPartial, recursive);
				}
			}
			
			return flag;
		});
	}
	// public
	static checkMatchExact(item, obj) {
    	var match = true;

    	for (var fieldName in obj) {
        	var expected = obj[fieldName];
        	var value = item[fieldName];

        	if (value != expected) {
        		match = false;
        		break;
        	}
    	}

    	return match;
	}
	// public
	static find(list, obj) {
		return list.filter(item => Filter.checkMatchExact(item, obj));
	}
	// public
	static findOne(list, obj, callback) {
		var ret = null;

        for (var i = 0; i < list.length; i++) {
        	var item = list[i];
        	var match = Filter.checkMatchExact(item, obj);

        	if (match == true) {
        		ret = item;

        		if (callback) {
        			callback(i, item);
        		}

        		break;
        	}
        }

        return ret;
	}
	// public
	static findPos(list, params) {
		var ret = -1;
        Filter.findOne(list, params, pos => ret = pos);
        return ret;
	}
	// public
	static findOneIn(list, listParams) {
		var filterResults = [];

		if (list.length > 0) {
			for (var params of listParams) {
				filterResults.push(Filter.findOne(list, params));
			}
		}

		return filterResults;
	}

}

class Pagination {

    constructor(pageSize) {
    	this.list = [];
    	this.setPageSize(pageSize);
    }
    
    setPageSize(pageSize) {
    	this.paginate(this.list, pageSize);
    }

    paginate(list, pageSize) {
    	if (pageSize != undefined) this.pageSize = pageSize;
    	this.list = list;
        var result = Math.ceil(list.length/this.pageSize);
        this.numPages = (result == 0) ? 1 : result;
    	this.currentPage = 1;
    	this.changePage();
    }

    changePage() {
     	this.listPage = this.list.slice((this.currentPage-1) * this.pageSize, this.currentPage * this.pageSize);
     }

}

export {DataStore, DataStoreItem, Filter, Pagination}
