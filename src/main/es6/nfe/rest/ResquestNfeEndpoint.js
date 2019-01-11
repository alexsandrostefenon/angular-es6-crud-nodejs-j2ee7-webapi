import express from "express";

export class ResquestNfeEndpoint {
	
	constructor(appRestExpress, dbClient) {
		this.dbClient = dbClient;
		this.router = express.Router();
		appRestExpress.use("/erp/request_nfe", this.router);
		this.router.get("/import_nfec", this.extract);
	}
	
	extract(req, res, next) {
		res.send('respond with a resource');
	}
	
}

export function setup(appRestExpress, dbClient) {
	let instance = new ResquestNfeEndpoint(appRestExpress, dbClient);
}
