import express from "express";
import fs from "fs";
import fetch from "node-fetch";

import {RequestFilter} from "../../crud/admin/RequestFilter.js";
import {CaseConvert} from "../../../webapp/es6/crud/CaseConvert.js";

class CrudServiceDbSync {
	
	constructor(entityManager) {
		this.entityManager = entityManager;
	}

	genSqlColumnDescription(fieldName, field) {
		if (field.type == undefined) {
			if (field.identityGeneration != undefined) field.type = "i"; else field.type = "s";
		}

		let pos = this.entityManager.crudTypes.indexOf(field.type);
		if (pos < 0) throw new Error(`DbClientPostgres.createTable() : table ${name}, field ${fieldName} : unknow type : ${field.type}`);
		let sqlType = this.entityManager.sqlTypes[pos];
		if (field.type == "s" && field.length < 32) sqlType = "character";

		if (field.length == undefined) {
			if (field.type == "s") field.length = 255;
			if (field.type == "n") field.length = 9;
		}

		if (field.type == "n" && field.scale == undefined) field.scale = 3;

		let sqlLengthScale = "";

		if (field.length != undefined && field.scale != undefined) {
			sqlLengthScale = `(${field.length},{field.scale})`;
		} else if (field.length != undefined) {
			sqlLengthScale = `(${field.length})`;
		}

		let sqlDefault = "";
		if (field.identityGeneration != undefined) sqlDefault = `GENERATED ${field.identityGeneration} AS IDENTITY`;

		if (field.defaultValue != undefined) {
			if (field.type == "s") sqlDefault = ` DEFAULT '${field.defaultValue}'`; else sqlDefault = " DEFAULT " + field.defaultValue;
		}

		let sqlNotNull = field.notNull == true ? "NOT NULL" : "";  
		return `${CaseConvert.camelToUnderscore(fieldName)} ${sqlType}${sqlLengthScale} ${sqlDefault} ${sqlNotNull}`;
	}

	genSqlForeignKey(fieldName, field, mapTables) {
		let tableOut = CaseConvert.camelToUnderscore(field.foreignKeysImport.table);
		let fieldOut = CaseConvert.camelToUnderscore(field.foreignKeysImport.field);
		let str = "";
//		console.log(`CrudServiceDbSync.genSqlForeignKey(${fieldName}) : field.foreignKeysImport.table : ${field.foreignKeysImport.table}, mapTables[field.foreignKeysImport.table] : ${mapTables[field.foreignKeysImport.table]}`);
		if (tableOut != "crud_group_owner" && mapTables.get(field.foreignKeysImport.table).crudGroupOwner != undefined) {
			str = `FOREIGN KEY(crud_group_owner,${fieldName}) REFERENCES ${tableOut}(crud_group_owner, ${fieldOut})`;
		} else {
			str = `FOREIGN KEY(${CaseConvert.camelToUnderscore(fieldName)}) REFERENCES ${tableOut}(${fieldOut})`;
		}

		return str;
	}

	createTable(name, fields) {
		if (fields == undefined) throw new Error(`DbClientPostgres.createTable(${name}, ${fields}) : fields : Invalid Argument Exception`);
		if (typeof(fields) == "string") fields = JSON.parse(fields);

		const genSql = mapTables => {
			let tableBody = "";
			for (let [fieldName, field] of Object.entries(fields)) tableBody = tableBody + this.genSqlColumnDescription(fieldName, field) + ", ";
			// add foreign keys
			for (let [fieldName, field] of Object.entries(fields)) if (field.foreignKeysImport != undefined) tableBody = tableBody + this.genSqlForeignKey(fieldName, field, mapTables) + ", ";
			// add unique keys
			let mapUniqueKey = new Map();

			for (let [fieldName, field] of Object.entries(fields)) {
				if (field.unique != undefined) {
					if (mapUniqueKey.has(field.unique) == false) mapUniqueKey.set(field.unique, []);
					mapUniqueKey.get(field.unique).push(fieldName);
				}
			}

			for (let [uniqueKey, listField] of Object.entries(mapUniqueKey)) {
				tableBody = tableBody + `UNIQUE(`;
				for (fieldName of listField) tableBody = tableBody + `${CaseConvert.camelToUnderscore(fieldName)}, `;
				tableBody = tableBody.substring(0, tableBody.length-2) + `)`;
			}
			// add primary key
			tableBody = tableBody + `PRIMARY KEY(`;
			for (let [fieldName, field] of Object.entries(fields)) if (field.primaryKey == true) tableBody = tableBody + `${CaseConvert.camelToUnderscore(fieldName)}, `;
			tableBody = tableBody.substring(0, tableBody.length-2) + `)`;
			let tableName = CaseConvert.camelToUnderscore(name);
			const sql = `CREATE TABLE ${tableName} (${tableBody})`;
			console.log(`CrudServiceDbSync.createTable() : table ${name}, sql : \n${sql}\n`, mapTables);
			return sql;
		};
		
		return this.entityManager.getTablesInfo().then(mapTables => genSql(mapTables)).then(sql => this.entityManager.client.query(sql));
	}

	alterTable(name, newFields, oldFields) {
		if (newFields == undefined) throw new Error(`RequestFilter.alterTable(${name}, ${newFields}) : newFields : Invalid Argument Exception`);
		if (typeof(newFields) == "string") newFields = JSON.parse(newFields);
		if (typeof(oldFields) == "string") oldFields = JSON.parse(oldFields);
		
		const genSql = mapTables => {
			let sql = null;
			let tableBody = "";
			// fields to remove
			for (let fieldName in oldFields) if (newFields[fieldName] ==  undefined) tableBody = tableBody + `DROP COLUMN ${CaseConvert.camelToUnderscore(fieldName)}, `;
			// fields to add
			for (let [fieldName, field] of Object.entries(newFields)) if (oldFields[fieldName] ==  undefined) tableBody = tableBody + "ADD COLUMN " + this.genSqlColumnDescription(fieldName, field) + ", ";
			// add foreign keys for new fields or existent fields without foreign keys 
			for (let [fieldName, field] of Object.entries(newFields)) if ((field.foreignKeysImport != undefined) && (oldFields[fieldName] ==  undefined || oldFields[fieldName].foreignKeysImport ==  undefined)) tableBody = tableBody + "ADD " + this.genSqlForeignKey(fieldName, field, mapTables) + ", ";
			//
			if (tableBody.length > 0) {
				tableBody = tableBody.substring(0, tableBody.length-2);
				let tableName = CaseConvert.camelToUnderscore(name);
				sql = `ALTER TABLE ${tableName} ${tableBody}`;
			}

			console.log(`DbClientPostgres.alterTable() : table ${name}, sql : \n${sql}\n`, mapTables);
			return sql;
		};
		
		return this.entityManager.getTablesInfo().
		then(mapTables => genSql(mapTables)).
		then(sql => {
			if (sql != null) {
				return this.entityManager.client.query(sql).catch(err => {
					console.error(`CrudServiceDbSync.alterTable(${name}) : error :\n${err.message}\nsql:\n${sql}`);
					throw err;
				});
			}
		});
	}

	dropTable(name) {
		let tableName = CaseConvert.camelToUnderscore(name);
		const sql = `DROP TABLE ${tableName}`;
		console.log(`DbClientPostgres.dropTable() : table ${name}, sql : \n${sql}\n`);
		return this.entityManager.client.query(sql);
	}

	generateFieldsStr(tabelName, newFields, oldFields) {
		if (newFields == undefined) throw new Error(`RequestFilter.generateFieldsStr(${tabelName}, ${newFields}) : newFields : Invalid Argument Exception`);
		if (typeof(newFields) == "string") newFields = Object.entries(JSON.parse(newFields));
		if (typeof(oldFields) == "string") oldFields = JSON.parse(oldFields);
		let jsonBuilder = {}; 

		for (let [fieldName, field] of newFields) {
			if (field.type == undefined) field.type = "s";
			if (field.hiden == undefined && field.identityGeneration != undefined) field.hiden = true;
			if (field.readOnly == undefined && field.identityGeneration != undefined) field.readOnly = true;

			if (this.entityManager.crudTypes.indexOf(field.type) < 0) {
				console.error(`${tabelName} : ${fieldName} : Unknow type : ${field.type}`);
				continue;
			}
			// type (columnDefinition), readOnly, hiden, primaryKey, required (insertable), updatable, defaultValue, length, precision, scale 
			let jsonBuilderValue = {};
			// registra conflitos dos valores antigos com os valores detectados do banco de dados
			jsonBuilderValue["type"] = field.type;

			if (field.updatable == false) {
				jsonBuilderValue["updatable"] = false;
			}

			if (field.length > 0) {
				jsonBuilderValue["length"] = field.length;
			}

			if (field.precision > 0) {
				jsonBuilderValue["precision"] = field.precision;
			}

			if (field.scale > 0) {
				jsonBuilderValue["scale"] = field.scale;
			}

			if (field.notNull == true) {
				jsonBuilderValue["required"] = true;
			} else {
				jsonBuilderValue["required"] = field.required;
			}
			//
			if (field.foreignKeysImport != undefined) {
				if (Array.isArray(field.foreignKeysImport) == true) {
					if (field.foreignKeysImport.length > 1) {
						for (let i = 0; i < field.foreignKeysImport.length; i++) {
							let item = field.foreignKeysImport[i];

							if (item.field == "crudGroupOwner") {
								field.foreignKeysImport.splice(i, 1);
							}
						}
					}

					if (field.foreignKeysImport.length > 1) {
						console.error(`RequestFilter.generateFieldsStr() : table [${tabelName}], field [${fieldName}], conflict foreignKeysImport : `, field.foreignKeysImport);
					}

					jsonBuilderValue["foreignKeysImport"] = field.foreignKeysImport[0];
				} else {
					jsonBuilderValue["foreignKeysImport"] = field.foreignKeysImport;
				}
			}

			jsonBuilderValue["primaryKey"] = field.primaryKey;
			jsonBuilderValue["defaultValue"] = field.defaultValue;
			jsonBuilderValue["unique"] = field.unique;
			jsonBuilderValue["identityGeneration"] = field.identityGeneration;
			jsonBuilderValue["isClonable"] = field.isClonable;
			jsonBuilderValue["hiden"] = field.hiden;
			jsonBuilderValue["readOnly"] = field.readOnly;
			// oculta tipos incompatíveis
			if (jsonBuilderValue["type"] != "s") {
				delete jsonBuilderValue["length"];
			}

			if (jsonBuilderValue["type"].startsWith("n") == false) {
				delete jsonBuilderValue["precision"];
				delete jsonBuilderValue["scale"];
			}
			// habilita os campos PLENAMENTE não SQL
			jsonBuilderValue.title = field.title;
			jsonBuilderValue.options = field.options;
			jsonBuilderValue.optionsLabels = field.optionsLabels;
			jsonBuilderValue.sortType = field.sortType;
			jsonBuilderValue.orderIndex = field.orderIndex;
			jsonBuilderValue.tableVisible = field.tableVisible;
			jsonBuilderValue.shortDescription = field.shortDescription;
			// exceções
			if (oldFields != undefined && oldFields[fieldName] != undefined) {
				let fieldOriginal = oldFields[fieldName];
				// copia do original os campos PLENAMENTE não SQL
				jsonBuilderValue.title = fieldOriginal.title;
				jsonBuilderValue.options = fieldOriginal.options;
				jsonBuilderValue.optionsLabels = fieldOriginal.optionsLabels;
				jsonBuilderValue.sortType = fieldOriginal.sortType;
				jsonBuilderValue.orderIndex = fieldOriginal.orderIndex;
				jsonBuilderValue.tableVisible = fieldOriginal.tableVisible;
				jsonBuilderValue.shortDescription = fieldOriginal.shortDescription;
				// registra conflitos dos valores antigos com os valores detectados do banco de dados
				const exceptions = ["service", "isClonable", "hiden", "foreignKeysImport"];

				for (let subFieldName in fieldOriginal) {
					if (exceptions.indexOf(subFieldName) < 0 && fieldOriginal[subFieldName] != jsonBuilderValue[subFieldName]) {
						console.warn(`RequestFilter.generateFieldsStr() : table [${tabelName}], field [${fieldName}], property [${subFieldName}] conflict previous declared [${fieldOriginal[subFieldName]}] new [${jsonBuilderValue[subFieldName]}]\nold:\n`, fieldOriginal, "\nnew:\n", jsonBuilderValue);
					}
				}
				// copia do original os campos PARCIALMENTE não SQL
				jsonBuilderValue.isClonable = fieldOriginal.isClonable;
				jsonBuilderValue.readOnly = fieldOriginal.readOnly;
				jsonBuilderValue.hiden = fieldOriginal.hiden;
			}
			// oculta os valores dafault
			const defaultValues = {type: "s", updatable: true, length: 255, precision: 9, scale: 3, hiden: false, primaryKey: false, required: false};

			for (let subFieldName in defaultValues) {
				if (jsonBuilderValue[subFieldName] == defaultValues[subFieldName]) {
					delete jsonBuilderValue[subFieldName];
				}
			}
			// troca todos os valores null por undefined
			for (let [key, value] of Object.entries(jsonBuilderValue)) {
				if (value == null) delete jsonBuilderValue[key];
			}

			jsonBuilder[fieldName] = jsonBuilderValue;
		}

		console.log(`RequestFilter.generateFieldsStr() : tableInfo(${tabelName}) :`, jsonBuilder);
		// TODO : NEXT LINE ONLY IN DEBUG
//		jsonBuilder = oldFields;
		return JSON.stringify(jsonBuilder);
	};

    updateCrudServices() {
		return this.entityManager.getTablesInfo().then(map => {
			const iterator = map.entries();
			
			const process = it => {
				if (it.done == true) {
					return;
				}
				
				let [name, tableInfo] = it.value;
				console.log(`RequestFilter.updateCrudServices.entityManager.getTablesInfo().process(${name})`);
				
				return this.entityManager.findOne("crudService", {name}).then(service => {
            		service.fields = this.generateFieldsStr(name, tableInfo.fields, service.fields);
            		return this.entityManager.update("crudService", {name}, service);
	        	}).catch(err => {
	        		if (err.message != "NoResultException") {
		        		console.error(`RequestFilter.updateCrudServices.entityManager.getTablesInfo().entityManager.find(${name}) :`, err);
		        		throw err;
	        		}
	        		
            		let service = {};
            		service.name = name;
            		service.fields = this.generateFieldsStr(name, tableInfo.fields);
            		return this.entityManager.insert("crudService", service);
	        	}).then(serviceUpdated => {
        			return process(iterator.next());
            	});
			};
			
			return process(iterator.next());
		});
	}

}

class CrudServiceEndPoint {
	
	constructor(appRestExpress, entityManager) {
		this.entityManager = entityManager;
		this.router = express.Router();
		appRestExpress.use("/crud_service", this.router);
		this.router.put("/update", (req, res, next) => this.update(req, res, next));
		this.router.delete("/delete", (req, res, next) => this.remove(req, res, next));
		this.crudServiceDbSync = new CrudServiceDbSync(this.entityManager);
	}
		
	update(req, res, next) {
		return this.entityManager.findOne("crudService", {name: req.query.name}).
		then(objOld => {
			let obj = req.body;
			console.log(`CrudServiceEndPoint.update : [${obj.name}] :\nold fields :\n`, objOld.fields, "\nnew fields :\n", obj.fields);

			if (objOld.fields == undefined) {
				if (obj.fields != undefined && obj.isOnLine == true) return this.crudServiceDbSync.createTable(obj.name, obj.fields).then(resSqlCreate => obj);
			} else {
				if (obj.fields == undefined) obj.fields = "{}";
				if (obj.isOnLine == true) return this.crudServiceDbSync.alterTable(obj.name, obj.fields, objOld.fields).then(resSqlAlter => obj);
			}
			
			return obj;
		}).
		then(objChanged => {
			objChanged.fields = this.crudServiceDbSync.generateFieldsStr(objChanged.name, objChanged.fields);
				return this.entityManager.update("crudService", {name: objChanged.name}, objChanged);			
		}).		
		then(objUpdated => {
			res.send(objUpdated);
			return RequestFilter.notify(objUpdated, "crudService", false);
		}).
		catch(err => {
			console.error(`CrudServiceEndPoint.update : error :`, err);
		});
	}
		
	remove(req, res, next) {
		return this.entityManager.findOne("crudService", {name: req.query.name}).
		then(objOld => {
			console.log(`CrudServiceEndPoint.remove : [${objOld.name}] : old fields`);
			return this.crudServiceDbSync.dropTable(objOld.name).then(resSqlDrop => objOld);
		}).
		then(objChanged => this.entityManager.remove("crudService", {name: objChanged.name})).
		then(objUpdated => {
			res.send(objUpdated);
			return RequestFilter.notify(objUpdated, "crudService", true);
		}).
		catch(err => {
			console.error(`CrudServiceEndPoint.remove : error :`, err);
		});
	}

}

let tablesCrud = {
		crudService: {
		    name: {primaryKey: true},
		    menu: {},
		    template:{},
		    saveAndExit: {type: "b"},
		    isOnLine: {type: "b"},
		    title: {},
		    fields: {length: 10240}
		},
		crudGroupOwner: {
		    id: {type: "i", identityGeneration: "BY DEFAULT", primaryKey: true},
		    name: {notNull: true, unique:true}
		},
		crudUser: {
		    id: {type: "i", identityGeneration: "BY DEFAULT", primaryKey: true},
		    crudGroupOwner: {type: "i", notNull: true, foreignKeysImport: {table: "crudGroupOwner", field: "id"}},
		    name: {length: 32, notNull: true, unique:true},
		    password: {notNull: true},
		    roles: {length: 10240},
		    routes: {length: 10240},
		    path: {},
		    menu: {length: 10240},
		    showSystemMenu: {type: "b", defaultValue: false},
		    authctoken: {}
		},
		crudGroup: {
		    id: {type: "i", identityGeneration: "BY DEFAULT", primaryKey: true},
		    name: {notNull: true, unique:true}
		},
		crudGroupUser: {
		    crudUser: {type: "i", primaryKey: true, foreignKeysImport: {table: "crudUser", field: "id"}},
		    crudGroup: {type: "i", primaryKey: true, foreignKeysImport: {table: "crudGroup", field: "id"}}
		},
		crudTranslation: {
		    id: {type: "i", identityGeneration: "BY DEFAULT", primaryKey: true},
		    locale: {notNull: true, defaultValue: "pt-br"},
		    name: {notNull: true},
		    translation: {}
		}
	};

function setup(appRestExpress, entityManager) {
	let instance = new CrudServiceEndPoint(appRestExpress, entityManager);

	return entityManager.getTablesInfo().then(tablesExistents => {
		let tablesMissing = new Map();
		for (let tableName in tablesCrud) if (tablesExistents.has(tableName) == false) tablesMissing.set(tableName, tablesCrud[tableName]);
		
		const createTable = iterator => {
			let it = iterator.next();
			if (it.done == true) return Promise.resolve();
			let [name, fields] = it.value;
			console.log(`RequestFilter.setup.getTablesInfo().createTable(${name})`, fields);
			return instance.crudServiceDbSync.createTable(name, fields).then(() => createTable(iterator));
		};
		const userAdmin = {
				name: "admin", crudGroupOwner: 1, password: "admin", path: "crud_service/search", showSystemMenu: true,
				roles: '{"crudService":{"create":true,"update":true,"delete":true},"crudGroupOwner":{"create":true,"update":true,"delete":true},"crudUser":{"create":true,"update":true,"delete":true},"crudGroup":{"create":true,"update":true,"delete":true},"crudGroupUser":{"create":true,"update":true,"delete":true},"crudTranslation":{"create":true,"update":true,"delete":true}}',
				routes: '[{"path": "/app/crud_service/:action", "controller": "crud/CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "crud/UserController"}]'
			};
		return createTable(tablesMissing.entries()).
		then(() => instance.crudServiceDbSync.updateCrudServices()).
		then(() => entityManager.findOne("crudGroupOwner", {name: "ADMIN"}).catch(() => entityManager.insert("crudGroupOwner", {name: "ADMIN"}))).
		then(() => entityManager.findOne("crudUser", {name: "admin"}).catch(() => entityManager.insert("crudUser", userAdmin))).
		then(() => instance);
	});
}

export {setup};
