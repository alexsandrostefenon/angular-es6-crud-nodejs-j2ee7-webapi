import fs from "fs";
import https from "https";
import http from "http";
import express from "express";
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

const dbName = getArg("database", "crud");
const webapp = getArg("www-data", "./src/main/webapp");
const portListen = getArg("port", "9443");
const fileNamePrivateKey = getArg("private-key", "key.pem");
const fileNameCertificate = getArg("certificate", "cert.pem");

const appExpress = express();
appExpress.use("/" + dbName, express.static(webapp));

const appRestExpress = express();
appRestExpress.use(express.urlencoded({extended:true}));
appRestExpress.use(express.json());

appExpress.use("/" + dbName, appRestExpress);

const privateKey  = fs.readFileSync(fileNamePrivateKey, 'utf8');
const certificate = fs.readFileSync(fileNameCertificate, 'utf8');
const credentials = {key: privateKey, cert: certificate};
const server = https.createServer(credentials, appExpress);

const dbClient = new DbClientPostgres(dbName);

RequestFilter.updateCrudServices(dbClient);
const requestFilter = new RequestFilter(appRestExpress, dbClient);

dbClient.connect().then(() => {
	server.listen(portListen, () => {
		const host = server.address().address;
		const port = server.address().port;
		
		console.log('Example app listening at http://%s:%s', host, port);
		// load modules
		const modules = getArg("modules", "").split(",");
		
		for (let path of modules) {
			if (path.length > 0) {
				console.log("loading ", path, "...");
				
				let promise = import(path).then(module => {
					console.log("loaded:", path);
					module.setup(appRestExpress, dbClient);
				});
			}
		}
	});
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
appExpress.all("/" + dbName + "/proxy", (req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PUT, PATCH, POST, DELETE");
    res.header("Access-Control-Allow-Headers", req.header('access-control-request-headers'));

    if (req.method === 'OPTIONS') {
        res.send();
    } else {
    	debugger;
        var targetURL = req.header('Target-URL');
        
        if (!targetURL) {
            res.send(500, { error: 'There is no Target-Endpoint header in the request' });
            return;
        }
        
        fetch(targetURL, {method: req.method, body: req.body}).
        then(fetchRes => {
	    	debugger;
	    	const type = fetchRes.headers.get("Content-Type");
	    	
	    	if (type.startsWith("text")) {
	    		fetchRes.textConverted().then(text => res.send(text)).catch(err => console.error(err));
	    	}
//	    	res.set(fetchRes.headers);
        }).
        catch(err => console.error(err));
    }
});
