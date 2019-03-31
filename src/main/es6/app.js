import fs from "fs";
import https from "https";
import http from "http";
import express from "express";
import bodyParser from "body-parser";
import websocket from "websocket";
import url from "url";
import fetch from "node-fetch";
import {DbClientPostgres} from "./crud/admin/dbClientPostgres.js";
import {RequestFilter} from "./crud/admin/RequestFilter.js";

const WebSocketServer = websocket.server;

function getArg(name, defaultValue) {
	let value = defaultValue;
	
	for (let arg of process.argv) {
		let tmp = "--" + name + "=";
		
		if (arg.startsWith(tmp)) {
			value = arg.substring(tmp.length);
			break;
		}
	}
	
	return value;
}

const appName = getArg("name", "");
const webapp = getArg("www-data", "./src/main/webapp");

const appExpress = express();
appExpress.use("/" + appName, express.static(webapp));

const appRestExpress = express();
appRestExpress.use(express.urlencoded({extended:true}));
appRestExpress.use(express.json());
appRestExpress.use(bodyParser.raw({type: ["application/octet-stream", "image/jpeg"]}));

appExpress.use(`/${appName}/rest`, appRestExpress);

let server;
let portListen;

try {
	portListen = getArg("port", "9443");
	const fileNamePrivateKey = getArg("private-key", "key.pem");
	const fileNameCertificate = getArg("certificate", "cert.pem");
	
	const privateKey  = fs.readFileSync(fileNamePrivateKey, 'utf8');
	const certificate = fs.readFileSync(fileNameCertificate, 'utf8');
	server = https.createServer({key: privateKey, cert: certificate}, appExpress);
} catch (error) {
	portListen = getArg("port", "9080");
	server = http.createServer(appExpress);
}

let dbConfig = {};
dbConfig.host = getArg("db_host");//localhost// Server hosting the postgres database
dbConfig.port = getArg("db_port");//5432//env var: PGPORT
dbConfig.database = getArg("db_name");//env var: PGDATABASE
dbConfig.user = getArg("db_user");//"development", //env var: PGUSER
dbConfig.password = getArg("db_password");//"123456", //env var: PGPASSWORD
const dbClient = new DbClientPostgres(dbConfig);

const requestFilter = new RequestFilter(appRestExpress, dbClient);

dbClient.connect().then(() => {
	// load modules
	const modules = getArg("modules", "").split(",");
	let promises = [];

	for (let path of modules) {
		if (path.length > 0) {
			console.log(`loading module ${path}...`);

			let promise = import(path).then(module => {
				console.log(`...loaded module ${path}\nsetup starting for module ${path} ...`);
				return module.setup(appRestExpress, dbClient).
				then(() => console.log(`...setup finished for module ${path}`)).
				catch(err => console.error(`...setup fail for module ${path}, err :\n`, err));
			});
			
			promises.push(promise);
		}
	}
	
	return Promise.all(promises).
	then(instances => {
		console.log(`starting updateCrudServices...`);
		return RequestFilter.updateCrudServices(dbClient).then(() => console.log(`...finished updateCrudServices...`));
	}).
	then(() => {
		console.log(`starting listen in port ${portListen}...`);
		return server.listen(portListen, () => console.log(`...listening at http://${server.address().address}:${server.address().port}`));
	}).
	catch(err => console.error(`Unknow error :`, err));
});

const wsServer = new WebSocketServer({httpServer: server, autoAcceptConnections: true});

wsServer.on('connect', (connection) => {
    console.log((new Date()) + ' Connection accepted.');

    connection.on("message", (message) => {
        if (message.type === 'utf8') {
            console.log('Received Message: ' + message.utf8Data);
            requestFilter.onMessage(connection, message.utf8Data);
        }
    });

    connection.on("close", (reasonCode, description) => {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
        requestFilter.onClose(connection);
    });
});
// COPYRIGHT : copy and past from https://github.com/ccoenraets/cors-proxy/blob/master/server.js
appExpress.all("/" + appName + "/proxy", (req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PUT, PATCH, POST, DELETE");
    res.header("Access-Control-Allow-Headers", req.header('access-control-request-headers'));

    if (req.method === 'OPTIONS') {
        res.send();
    } else {
        var targetURL = req.header('Target-URL');
        
        if (!targetURL) {
            res.send(500, { error: 'There is no Target-Endpoint header in the request' });
            return;
        }
        
        fetch(targetURL, {method: req.method, body: req.body}).
        then(fetchRes => {
	    	const type = fetchRes.headers.get("Content-Type");
	    	
	    	if (type.startsWith("text")) {
	    		fetchRes.textConverted().then(text => res.send(text)).catch(err => console.error(err));
	    	}
//	    	res.set(fetchRes.headers);
        }).
        catch(err => console.error(err));
    }
});
