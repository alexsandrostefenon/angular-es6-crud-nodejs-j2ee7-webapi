const ResponseStatus = {
		"OK": 200,
		"UNAUTHORIZED": 401,
		"BAD_REQUEST": 400,
		"INTERNAL_SERVER_ERROR": 500
};

export const MediaType = {
		"APPLICATION_JSON": 1
};

export class Response {

	constructor(status, data, mediaType) {
		this.status = status;
		this.data = data;
		this.mediaType = mediaType;
		// Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized object category").build();
		// Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		// Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()).build()
		// Response.ok(newObj, MediaType.APPLICATION_JSON).build();
	}
	
	static status(id) {
		return new Response(id);
	}
	
	entity(data) {
		this.data = data;
		return this;
	}
	
	build() {
		return {"status": this.status, "data": this.data};
	}

	static ok(data, mediaType) {
		return new Response(200, data, mediaType);
	}
	
	static get Status() {
		return ResponseStatus;
	} 

}
