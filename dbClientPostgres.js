var pg = require('pg');
var pgCamelCase = require('pg-camelcase');
var revertCamelCase = pgCamelCase.inject(pg);

// Fix for parsing of numeric fields
var types = require('pg').types
types.setTypeParser(1700, 'text', parseFloat);

function convertCaseCamelToUnderscore(str) {
	var ret = "";

	for (var i = 0; i < str.length; i++) {
		var ch = str[i];

		if (ch >= 'A' && ch <= 'Z') {
			ch = ch.toLowerCase();
			ret = ret + '_' + ch;
		} else {
			ret = ret + ch;
		}
	}

	if (ret.length > 0 && ret[0] == '_') {
		ret = ret.substring(1);
	}

	return ret;
}

module.exports =

class DbClientPostgres {

	constructor(dbName) {
//		super(dbName);
		var dbConfig = {
				  user: "development", //env var: PGUSER
				  database: dbName, //env var: PGDATABASE
				  password: "123456", //env var: PGPASSWORD
				  host: "localhost", // Server hosting the postgres database
				  port: 5432, //env var: PGPORT
				  max: 10, // max number of clients in the pool
				  idleTimeoutMillis: 30000, // how long a client is allowed to remain idle before being closed
				};

		this.client = new pg.Client(dbConfig);

		//connect to our database
		//env var: PGHOST,PGPORT,PGDATABASE,PGUSER,PGPASSWORD
		this.client.connect(function (err) {
			if (err) throw err;
		});
	}

	static buildQuery(matchExact, matchIn, params, orderBy) {
		var i = params.length + 1;
		var str = "";

		for (var fieldName in matchExact) {
			str = str + fieldName + "=$" + i + " AND ";
			params.push(matchExact[fieldName]);
			i++;
		}

		for (var fieldName in matchIn) {
			str = str + fieldName + " IN (";
			var array = matchIn[fieldName];

			for (var item of array) {
				str = str + "$" + i + ",";
				params.push(item);
				i++;
			}

			if (str.endsWith(",") > 0) {
				str = str.substring(0, str.length - 1);
			}

			str = str + ") AND ";
		}

		if (str.endsWith(" AND ") > 0) {
			str = str.substring(0, str.length - 5);
		}

		if (str.length > 0) {
			str = " WHERE " + str;
		}

		if (orderBy != undefined && orderBy != null) {
			str = str + " ORDER BY ";

			if (Array.isArray(orderBy)) {
				for (fieldName of orderBy) {
					str = str + fieldName + ",";
				}

				if (str.endsWith(",") > 0) {
					str = str.substring(0, str.length - 1);
				}
			} else {
				str = str + orderBy;
			}

		}

		return str;
	}

	insert(tableName, createObj, callback) {
		tableName = convertCaseCamelToUnderscore(tableName);
		var params = [];
		var i = 1;
		var strFields = "";
		var strValues = "";

		for (var fieldName in createObj) {
			var obj = createObj[fieldName];
			strFields = strFields + convertCaseCamelToUnderscore(fieldName) + ",";
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

		var sql = "INSERT INTO " + tableName + " (" + strFields + ") VALUES (" + strValues + ")";
		this.client.query(sql, params, callback);
	}

	find(tableName, matchExact, matchIn, orderBy, callback) {
		tableName = convertCaseCamelToUnderscore(tableName);
		var params = [];
		var sql = "SELECT * FROM " + tableName + DbClientPostgres.buildQuery(matchExact, matchIn, params, orderBy);
		this.client.query(sql, params, callback);
	}

	findMax(tableName, fieldName, matchExact, matchIn, callback) {
		tableName = convertCaseCamelToUnderscore(tableName);
		var params = [];
		var sql = "SELECT MAX(" + fieldName + ") FROM " + tableName + DbClientPostgres.buildQuery(matchExact, matchIn, params);
		this.client.query(sql, params, callback);
	}

	update(tableName, matchExact, matchIn, updateObj, callback) {
		tableName = convertCaseCamelToUnderscore(tableName);
		var sql = "UPDATE " + tableName;
		var params = [];
		var i = 1;
		var str = "";

		for (var fieldName in updateObj) {
			str = str + convertCaseCamelToUnderscore(fieldName) + "=$" + i + ",";
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

		sql = sql + DbClientPostgres.buildQuery(matchExact, matchIn, params);
		this.client.query(sql, params, callback);
	}

	deleteOne(tableName, matchExact, matchIn, callback) {
		tableName = convertCaseCamelToUnderscore(tableName);
		var params = [];
		var sql = "DELETE FROM " + tableName + DbClientPostgres.buildQuery(matchExact, matchIn, params);
		this.client.query(sql, params, callback);
	}
}
