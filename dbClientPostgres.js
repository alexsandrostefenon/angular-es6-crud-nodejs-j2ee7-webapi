import pg from "pg";
import pgCamelCase from "pg-camelcase";
import {CaseConvert} from "./src/main/webapp/es6/CaseConvert.js";

var revertCamelCase = pgCamelCase.inject(pg);

// Fix for parsing of numeric fields
var types = pg.types
types.setTypeParser(1700, 'text', parseFloat);

export class DbClientPostgres {

	constructor(dbName) {
		this.dbConfig = {
				  user: "development", //env var: PGUSER
				  database: dbName, //env var: PGDATABASE
				  password: "123456", //env var: PGPASSWORD
				  host: "localhost", // Server hosting the postgres database
				  port: 5432, //env var: PGPORT
				  max: 10, // max number of clients in the pool
				  idleTimeoutMillis: 30000, // how long a client is allowed to remain idle before being closed
				};

		this.client = new pg.Client(this.dbConfig);

		//connect to our database
		//env var: PGHOST,PGPORT,PGDATABASE,PGUSER,PGPASSWORD
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
		return this.client.query(sql, params).then(result => result.rows[0]);
	}

	find(tableName, fields, orderBy) {
		tableName = CaseConvert.camelToUnderscore(tableName);
		const params = [];
		const sql = "SELECT * FROM " + tableName + DbClientPostgres.buildQuery(fields, params, orderBy);
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

			return result.rows[0]
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
}
