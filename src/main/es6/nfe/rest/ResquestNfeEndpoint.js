import express from "express";

class Endpoint {
	
	constructor(path, appRestExpress, dbClient) {
		this.dbClient = dbClient;
		this.router = express.Router();
		appRestExpress.use(path, this.router);
	}
	
}

export class ResquestNfeEndpoint extends Endpoint {
	
	constructor(appRestExpress, dbClient) {
		super("/erp/request_nfe", appRestExpress, dbClient);

		this.router.get("/import_nfec", this.importNfec);
	}
	
	importNfec(req, res, next) {
		res.send('respond with a resource');
	}
	
}