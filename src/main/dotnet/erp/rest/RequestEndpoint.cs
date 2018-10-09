
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using AspNetCoreWebApi.Entity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using org.domain.commom;
using org.domain.crud.admin;
using static org.domain.commom.DbUtils;
using static org.domain.crud.admin.RequestFilter;

[Route ("rest/request")]
[ApiController]
public class RequestEndpoint : ControllerBase {
	private readonly DbContext entityManager;

	public RequestEndpoint(CrudContext dbContext) {
		this.entityManager = dbContext;
	}

	[HttpPost("create")]
	public Task<ActionResult> Create([FromBody] AspNetCoreWebApi.Entity.Request obj) {
		Console.WriteLine ("Dedicated implement of route {0}", this.RouteData.ToString());
		// TODO : validar se entity.getState() é um State com antecessor vazio.
		if (obj.Date == null) {
			obj.Date = System.DateTime.Now;
		}

		String serviceName = CaseConvert.UnderscoreToCamel(obj.GetType().Name, false);
		return RequestFilter.ProcessCreate<AspNetCoreWebApi.Entity.Request>(this.User.Identity, this.entityManager, serviceName, obj);
	}

	[HttpPut("update")]
	public Task<ActionResult> Update([FromBody] AspNetCoreWebApi.Entity.Request newObj) {
		Console.WriteLine ("Dedicated implement of route {0}", this.RouteData.ToString ());
		String serviceName = CaseConvert.UnderscoreToCamel(newObj.GetType().Name, false);
        // TODO : validar se entity.getState() é um State com antecessor e precedente validos.
		return RequestFilter.GetObject<AspNetCoreWebApi.Entity.Request>(this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult>(taskOldObj => {
			return RequestFilter.ProcessUpdate(this.User.Identity, this.Request, this.entityManager, serviceName, newObj).ContinueWith<ActionResult>(taskResponse => {
				ActionResult response = taskResponse.Result;

				if (response is OkResult) {
					RequestState stateOld = RequestProductEndpoint.GetRequestState(this.entityManager, taskOldObj.Result.State);
					RequestState state = RequestProductEndpoint.GetRequestState(this.entityManager, newObj.State);
					List<RequestProduct> list = DbUtils.Find<RequestProduct>(this.entityManager, null, QueryMap.Create().AddNext("company", newObj.Company).AddNext("request", newObj.Id), null, null, null).Result;

					foreach (RequestProduct requestProduct in list) {
					}
				}

				return response;
			}).Result;
		});
	}

	[HttpDelete("delete")]
	public Task<ActionResult> Remove() {
		Console.WriteLine ("Dedicated implement of route {0}", this.RouteData.ToString());
		// TODO : validar se entity.getState() é um State de status iniciais que permite exclusão.
		String serviceName = "request";
		return RequestFilter.GetObject<AspNetCoreWebApi.Entity.Request>(this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult>(taskOldObj => {
			var obj = taskOldObj.Result;
			return RequestFilter.ProcessDelete<AspNetCoreWebApi.Entity.Request> (this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult> (taskResponse => {
				ActionResult response = taskResponse.Result;

				if (response is OkResult) {
					RequestState stateOld = RequestProductEndpoint.GetRequestState (this.entityManager, obj.State);
					RequestState state = RequestProductEndpoint.GetRequestState (this.entityManager, obj.State);
					List<RequestProduct> list = DbUtils.Find<RequestProduct> (this.entityManager, null, QueryMap.Create ().AddNext ("company", obj.Company).AddNext ("request", obj.Id), null, null, null).Result;

					foreach (RequestProduct requestProduct in list) {
					}
				}

				return response;
			}).Result;
		});
	}

	[HttpGet("query")]
	public Task<ActionResult> Query() {
		Console.WriteLine ("Dedicated implement of route {0}", this.RouteData.ToString());
		// TODO : verificar as condições para carregar as requests encerradas
		String serviceName = "request";
		return RequestFilter.ProcessQuery<AspNetCoreWebApi.Entity.Request>(this.User.Identity, this.Request, this.entityManager, serviceName).ContinueWith<ActionResult> (taskResponse => {
			return taskResponse.Result;
		});
	}

}
