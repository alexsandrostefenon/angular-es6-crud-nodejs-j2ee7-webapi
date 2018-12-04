using System;
using System.Collections.Generic;
using System.Linq;
using AspNetCoreWebApi.Entity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace org.domain.iso8583router.rest {
	[Route("rest/iso8583router/connector")]
    [ApiController]
	public class ConnectorEndpoint : ControllerBase {
		private readonly DbContext entityManager;

		public ConnectorEndpoint (CrudContext dbContext) {
			this.entityManager = dbContext;
		}

		[HttpPost ("create")]
		public ActionResult<Iso8583RouterComm> Create ([FromBody] Iso8583RouterComm obj) {
			entityManager.Add (obj);
			entityManager.SaveChanges ();
			return obj;
		}

		[HttpGet ("read")]
		public ActionResult<Iso8583RouterComm> Read ([FromQuery] String name) {
			return this.entityManager.Set<Iso8583RouterComm> ().Find(name);
		}

		[HttpPut ("update")]
		public ActionResult<Iso8583RouterComm> Update ([FromQuery] String name, [FromBody] Iso8583RouterComm newObj) {
			if (String.Equals(name, newObj.Name) == false) {
				Iso8583RouterComm oldObj = this.entityManager.Set<Iso8583RouterComm> ().Find (name);
				this.entityManager.Entry (oldObj).State = EntityState.Detached;
			}

			this.entityManager.Update (newObj);
			this.entityManager.SaveChanges ();
			return newObj;
		}

		[HttpDelete ("delete")]
		public ActionResult Delete([FromQuery] String name) {
			Iso8583RouterComm oldObj = this.entityManager.Set<Iso8583RouterComm> ().Find (name);
			this.entityManager.Remove (oldObj);
			this.entityManager.SaveChanges ();
			return this.Ok ();
        }

		[HttpGet ("query")]
		public ActionResult<List<Iso8583RouterComm>> Query () {
			return this.entityManager.Set<Iso8583RouterComm> ().ToList ();
		}
    }
}
