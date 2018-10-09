using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;

namespace org.domain.crud.admin {

	public class Response {

		private static ContentResult Create(Object obj, int status) {
			ContentResult contentResult = new ContentResult();
			contentResult.StatusCode = status;

			if (obj != null) {
				if (obj is String) {
					contentResult.Content = (String)obj;
					contentResult.ContentType = "text/plain";
				} else {
					contentResult.Content = JsonConvert.SerializeObject (obj, new JsonSerializerSettings { ContractResolver = new CamelCasePropertyNamesContractResolver () });
					contentResult.ContentType = "application/json";
				}
			}

			return contentResult;
        }


		public static ContentResult Ok(Object obj) {
			return Create(obj, StatusCodes.Status200OK);
        }

		public static ContentResult Ok() {
			return Ok(null);
        }

		public static ContentResult Unauthorized(String msg) {
			return Create (msg, StatusCodes.Status401Unauthorized);
		}

		public static ContentResult BadRequest (String msg) {
			return Create (msg, StatusCodes.Status400BadRequest);
		}

		public static ContentResult InternalServerError (String msg) {
			return Create (msg, StatusCodes.Status500InternalServerError);
		}


    }

}
