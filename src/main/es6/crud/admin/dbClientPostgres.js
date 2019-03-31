import pg from "pg";
import pgCamelCase from "pg-camelcase";
import {CaseConvert} from "../../../webapp/es6/crud/CaseConvert.js";

var revertCamelCase = pgCamelCase.inject(pg);

// Fix for parsing of numeric fields
var types = pg.types
types.setTypeParser(1700, 'text', parseFloat);
types.setTypeParser(1114, str => new Date(str + "+0000"));

class DbClientPostgres {

	constructor(dbConfig) {
		this.dbConfig = {
		  max: 10, // max number of clients in the pool
		  idleTimeoutMillis: 30000, // how long a client is allowed to remain idle before being closed
		};

		if (dbConfig.host != undefined) this.dbConfig.host = dbConfig.host;
		if (dbConfig.port != undefined) this.dbConfig.port = dbConfig.port;
		if (dbConfig.database != undefined) this.dbConfig.database = dbConfig.database;
		if (dbConfig.user != undefined) this.dbConfig.user = dbConfig.user;
		if (dbConfig.password != undefined) this.dbConfig.password = dbConfig.password;
		//connect to our database
		//env var: PGHOST,PGPORT,PGDATABASE,PGUSER,PGPASSWORD
		this.client = new pg.Client(this.dbConfig);
		this.sqlTypes        = ["boolean","character varying","character","integer","jsonb", "numeric",   "timestamp without time zone", "bigint", "text", "date",         , "double precision", "bytea"  ];
		this.crudTypes       = ["b",      "s",                "s",        "i",      "json",  "n",         "datetime-local",              "i",      "s",    "datetime-local", "n",                "s"];
	}

	connect() {
		return this.client.connect();
	}

	static buildQuery(fields, params, orderBy) {
		var i = params.length + 1;
		var str = "";

		for (let fieldName in fields) {
			let field = fields[fieldName];

			if (Array.isArray(field)) {
				str = str + CaseConvert.camelToUnderscore(fieldName) + " = ANY ($" + i + ") AND ";
			} else {
				str = str + CaseConvert.camelToUnderscore(fieldName) + "=$" + i + " AND ";
			}

			params.push(field);
			i++;
		}

		if (str.endsWith(" AND ") > 0) {
			str = str.substring(0, str.length - 5);
		}

		if (str.length > 0) {
			str = " WHERE " + str;
		}

		if (orderBy != undefined && orderBy != null) {
			if (Array.isArray(orderBy)) {
				if (orderBy.length > 0) {
					str = str + " ORDER BY ";

					for (let fieldName of orderBy) {
						str = str + CaseConvert.camelToUnderscore(fieldName) + ",";
					}

					if (str.endsWith(",") > 0) {
						str = str.substring(0, str.length - 1);
					}
				}
			} else {
				str = str + " ORDER BY " + orderBy;
			}
		}

		return str;
	}

	insert(tableName, createObj) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		var params = [];
		var i = 1;
		var strFields = "";
		var strValues = "";

		for (var fieldName in createObj) {
			var obj = createObj[fieldName];
			strFields = strFields + CaseConvert.camelToUnderscore(fieldName) + ",";
			strValues = strValues + "$" + i + ",";

			if (Array.isArray(obj) == true) {
				var strArray = JSON.stringify(obj);
				params.push(strArray);
			} else {
				params.push(obj);
			}

			i++;
		}

		if (strFields.endsWith(",") > 0) {
			strFields = strFields.substring(0, strFields.length - 1);
			strValues = strValues.substring(0, strValues.length - 1);
		}

		const sql = "INSERT INTO " + tableName + " (" + strFields + ") VALUES (" + strValues + ") RETURNING *";
		return this.client.query(sql, params).
		then(result => {
			return result.rows[0];
		}).
		catch(err => {
			err.message = err.message + ` sql : ${sql}`;
			throw err;
		});
	}

	find(tableName, fields, orderBy) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		const params = [];
		const sql = "SELECT * FROM " + tableName + DbClientPostgres.buildQuery(fields, params, orderBy);
//		console.log(sql);
		return this.client.query(sql, params).then(result => result.rows);
	}

	findOne(tableName, fields) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		const params = [];
		const sql = "SELECT * FROM " + tableName + DbClientPostgres.buildQuery(fields, params);
		return this.client.query(sql, params).then(result => {
			if (result.rowCount == 0) {
				throw new Error("NoResultException");
			}

			return result.rows[0]
		});
	}

	findMax(tableName, fieldName, fields) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		const params = [];
		const sql = "SELECT MAX(" + fieldName + ") FROM " + tableName + DbClientPostgres.buildQuery(fields, params);
		return this.client.query(sql, params).then(result => {
			if (result.rowCount == 0) {
				throw new Error("NoResultException");
			}

			return result.rows[0].max;
		});
	}

	update(tableName, primaryKey, updateObj) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		var sql = "UPDATE " + tableName;
		var params = [];
		var i = 1;
		var str = "";

		for (var fieldName in updateObj) {
			str = str + CaseConvert.camelToUnderscore(fieldName) + "=$" + i + ",";
			var obj = updateObj[fieldName];

			if (Array.isArray(obj) == true) {
				var strArray = JSON.stringify(obj);
				params.push(strArray);
			} else {
				params.push(obj);
			}

			i++;
		}

		if (str.endsWith(",") > 0) {
			str = str.substring(0, str.length - 1);
			sql = sql + " SET " + str;
		}

		sql = sql + DbClientPostgres.buildQuery(primaryKey, params) + " RETURNING *";
		
		return this.client.query(sql, params).then(result => {
			if (result.rowCount == 0) {
				throw new Error("NoResultException");
			}

			return result.rows[0]
		})
		.catch(error => {
			console.error(`DbClientPostgres.update(${tableName})\nprimaryKey:\n`, primaryKey, "\nupdateObj:\n", updateObj, "\nsql:\n", sql, "\nerror:\n", error);
			throw error;
		});
	}

	deleteOne(tableName, primaryKey) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		const params = [];
		const sql = "DELETE FROM " + tableName + DbClientPostgres.buildQuery(primaryKey, params) + " RETURNING *";
		return this.client.query(sql, params).then(result => {
			if (result.rowCount == 0) {
				throw new Error("NoResultException");
			}

			return result.rows[0]
		});
	}
	
	getTablesInfo() {
		/*
		select table_name,column_name,data_type,character_maximum_length,is_nullable,column_default,is_updatable,numeric_precision,numeric_scale
		from INFORMATION_SCHEMA.COLUMNS where columns."table_schema"='public' order by table_name,ordinal_position;

		select table_name,column_name,constraint_name from information_schema.constraint_column_usage order by constraint_name;
		 */
		const foreignKeysExportUsage = () => {
			return this.find("informationSchema.constraintColumnUsage").then(list => {
				let mapForeignKeysExport = new Map();
				
				for (let rec of list) {
					if (rec.constraintName.includes("_fkey")) {
						const table = CaseConvert.underscoreToCamel(rec.tableName, false);
						const field = CaseConvert.underscoreToCamel(rec.columnName, false);
						const foreignKey = {table, field};
						
						if (mapForeignKeysExport.has(rec.constraintName) == true) {
							mapForeignKeysExport.get(rec.constraintName).push(foreignKey);
						} else {
							mapForeignKeysExport.set(rec.constraintName, [foreignKey]);
						}
					}
				}
				
				return mapForeignKeysExport;
			}).catch(err => {
				console.error(`DbClientPostgres.getTablesInfo().foreignKeysExportUsage() : EntityClass.getDeclaredFields.find(COLUMNS) : ${err.message}`);
				throw err;
			});
		};
		
		const foreignKeysImportUsage = (mapTables, mapForeignKeysExport) => {
			return this.find("informationSchema.keyColumnUsage").then(list => {
				for (let rec of list) {
					const tableName = CaseConvert.underscoreToCamel(rec.tableName, false);
					
					if (mapTables.has(tableName) == true) {
						const entityClass = mapTables.get(tableName);
						const fieldName = CaseConvert.underscoreToCamel(rec.columnName, false);
						
						if (entityClass.fields.has(fieldName) == true) {
							const field = entityClass.fields.get(fieldName);
							// select table_name,column_name,constraint_name from information_schema.constraint_column_usage order by table_name,column_name,constraint_name;
							if (rec.constraintName.endsWith("_pkey")) {
								field.primaryKey = true; // true,false
							} else if (rec.constraintName.includes("_fkey")) {
								if (field.foreignKeysImport == undefined) field.foreignKeysImport = [];
								let list = mapForeignKeysExport.get(rec.constraintName); 
								list.forEach(item => field.foreignKeysImport.push(item));
							} else if (rec.constraintName.endsWith("_key")) {
								field.unique = rec.constraintName;
							} else {
								console.error(`DbClientPostgres.getTablesInfo().foreignKeysImportUsage() : unknow constraintName ${rec.constraintName} from fieldName ${fieldName} from table ${tableName}, full rec : ${JSON.stringify(rec)}`);
							}
						} else {
							console.error(`DbClientPostgres.getTablesInfo().foreignKeysImportUsage() : unknow type from fieldName ${fieldName} from table ${tableName} : rec : ${JSON.stringify(rec)}`);
						}
					} else {
						console.error(`DbClientPostgres.getTablesInfo().foreignKeysImportUsage() : unknow types from table ${tableName} : rec : ${JSON.stringify(rec)}`);
					}
				}
				
				console.log(`DbClientPostgres.getTablesInfo() : response :`, mapTables);
				return mapTables;
			}).catch(err => {
				console.error(`DbClientPostgres.getTablesInfo().foreignKeysImportUsage() : EntityClass.getDeclaredFields.find(COLUMNS) : ${err.message}`);
				throw err;
			});
		};
		
		const processColumns = () => {
			return this.find("informationSchema.columns", {"table_schema": "public"}, "table_name,ordinal_position").then(list => {
				let mapTables = new Map();
				
				for (let rec of list) {
					// select * from INFORMATION_SCHEMA.COLUMNS where columns."table_schema"='public' order by table_name,ordinal_position;
					// select distinct data_type from INFORMATION_SCHEMA.COLUMNS where columns."table_schema"='public' order by data_type;
					let typeIndex = this.sqlTypes.indexOf(rec.dataType);
					
					if (typeIndex >= 0) {
						const tableName = CaseConvert.underscoreToCamel(rec.tableName, false);
						let entityClass;
						
						if (mapTables.has(tableName) == true) {
							entityClass = mapTables.get(tableName);
						} else {
							entityClass = {};
							entityClass.fields = new Map();
							mapTables.set(tableName, entityClass);
						}
						
						const fieldName = CaseConvert.underscoreToCamel(rec.columnName, false);
						let field = {}
						field.primaryKey = undefined;
						field.unique = undefined;
						field.type = this.crudTypes[typeIndex]; // LocalDateTime,ZonedDateTime,Date,Time
						field.notNull = rec.isNullable == "NO"; // true,false
						field.updatable = rec.isUpdatable == "YES"; // true,false
						field.scale = rec.numericScale; // > 0 // 3,2,1
						field.length = rec.characterMaximumLength; // > 0 // 255
						field.precision = rec.numericPrecision; // > 0
						field.defaultValue = rec.columnDefault; // 'pt-br'::character varying
						
						if (field.defaultValue != undefined && field.defaultValue[0] == "'" && field.defaultValue.length > 2) {
							if (field.type == "s") {
								field.defaultValue = field.defaultValue.substring(1, field.defaultValue.indexOf("'", 1));
							} else {
								field.defaultValue = undefined;
							}
						}
						
						if ((field.type == "i" || field.type == "n") && isNaN(field.defaultValue) == true) field.defaultValue = undefined;
						field.identityGeneration = rec.identityGeneration; // BY DEFAULT,ALWAYS
						entityClass.fields.set(fieldName, field);
					} else {
						console.error(`DbClientPostgres.getTablesInfo().processColumns() : Invalid Database Type : ${rec.dataType}, full rec : ${JSON.stringify(rec)}`);
					}
				}
				
				return mapTables;
			}).catch(err => {
				console.error(`DbClientPostgres.getTablesInfo().processColumns() : find(COLUMNS) : ${err.message}`);
				throw err;
			});
		};
		
		return processColumns().then(mapTables => foreignKeysExportUsage().then(mapForeignKeysExport => foreignKeysImportUsage(mapTables, mapForeignKeysExport)));
	}

}

export {DbClientPostgres}
