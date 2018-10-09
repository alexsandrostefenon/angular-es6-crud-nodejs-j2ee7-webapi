using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using AspNetCoreWebApi.Entity;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.WebSockets;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json.Serialization;
using org.domain.crud.admin;

namespace AspNetCoreWebApi {

    public class Startup {
        public Startup(IConfiguration configuration) {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services) {

			services.AddMvcCore()
					//			        .AddAuthorization()
			        //.AddJsonFormatters(setup => { setup.ContractResolver = new CamelCasePropertyNamesContractResolver(); })
			        .AddJsonFormatters()
			        //.AddJsonOptions(opts => { opts.SerializerSettings.ContractResolver = new CamelCasePropertyNamesContractResolver(); })
			        .AddCors();
			
			var connectionString = Configuration.GetConnectionString ("CrudContext");
			services.AddEntityFrameworkNpgsql().AddDbContext<CrudContext> (options => options.UseNpgsql(connectionString));
		}

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
		public void Configure(IApplicationBuilder app, IHostingEnvironment env, CrudContext entityManager) {
            if (env.IsDevelopment()) {
                app.UseDeveloperExceptionPage();
            } else {
                app.UseHsts();
            }

			//			app.UseHttpsRedirection();
			app.UseWebSockets();
			app.UseMiddleware<RequestFilter> ();
            app.UseMvc();
			String root = Path.Combine(Directory.GetCurrentDirectory(), "src/main/webapp");

			if (Directory.Exists (root)) {
				app.UseFileServer (new FileServerOptions {
					FileProvider = new PhysicalFileProvider (root)
				});
			} else {
				Console.WriteLine ("Directory fail : " + root);
			}

			//			app.UseResponseCompression();
			//			app.UseMvcWithDefaultRoute();
			RequestFilter.UpdateCrudServices (entityManager);
        }
    }

}
