import fs from "fs";
import https from "https";
import http from "http";
import express from "express";
import websocket from "websocket";
import url from "url";
import fetch from "node-fetch";
import {DbClientPostgres} from "./admin/dbClientPostgres.js";
import {RequestFilter} from "./admin/RequestFilter.js";
import {ResquestNfeEndpoint} from "./nfe/rest/ResquestNfeEndpoint.js";

const WebSocketServer = websocket.server;

const dbName = process.argv[2] || "crud";
const webapp = process.argv[3] || "./src/main/webapp";
const portListen = process.argv[4] || 9443;
const fileNamePrivateKey = process.argv[5] || "key.pem";
const fileNameCertificate = process.argv[6] || "cert.pem";

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
const resquestNfeEndpoint = new ResquestNfeEndpoint(appRestExpress, dbClient);

dbClient.connect().then(() => {
	server.listen(portListen, () => {
		const host = server.address().address;
		const port = server.address().port;
		
		console.log('Example app listening at http://%s:%s', host, port);
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
