using System;
using System.Threading.Tasks;
using AspNetCoreWebApi.Entity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using org.domain.commom;
using org.domain.crud.admin;

public class RequestProductEndpoint : ControllerBase {
	private readonly DbContext entityManager;

	public RequestProductEndpoint(CrudContext dbContext) {
        this.entityManager = dbContext;
    }

	public static RequestState GetRequestState(DbContext entityManager, int id) {
		return entityManager.Set<RequestState>().Find(id);
    }

	[HttpPost("create")]
	public Task<ActionResult> Create([FromBody] RequestProduct obj) {
		String serviceName = CaseConvert.UnderscoreToCamel(obj.GetType().Name, false);
		return RequestFilter.ProcessCreate<RequestProduct>(this.User.Identity, this.entityManager, serviceName, obj);
	}

	[HttpPut("update")]
	public Task<ActionResult> Update([FromBody] RequestProduct newObj) {
		String serviceName = newObj.GetType().Name;
		// TODO : validar se entity.getState() é um State com antecessor e precedente validos.
		return RequestFilter.GetObject<RequestProduct>(this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult>(taskOldObj => {
			return RequestFilter.ProcessUpdate(this.User.Identity, this.Request, this.entityManager, serviceName, newObj).ContinueWith<ActionResult>(taskResponse => {
				ActionResult response = taskResponse.Result;


				return response;
			}).Result;
		});
	}

	[HttpDelete ("delete")]
	public Task<ActionResult> Remove () {
		// TODO : validar se entity.getState() é um State de status iniciais que permite exclusão.
		String serviceName = typeof (AspNetCoreWebApi.Entity.RequestProduct).Name;
		return RequestFilter.GetObject<RequestProduct> (this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult> (taskOldObj => {
			var obj = taskOldObj.Result;
			return RequestFilter.ProcessDelete<RequestProduct> (this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult> (taskResponse => {
				ActionResult response = taskResponse.Result;


				return response;
			}).Result;
		});
	}

}
