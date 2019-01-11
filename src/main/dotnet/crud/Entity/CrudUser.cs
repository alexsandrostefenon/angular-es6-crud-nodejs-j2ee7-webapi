using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("crud_user")]
    public partial class CrudUser
    {
		[Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
		[Key][Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }
		[Column("path", TypeName = "character varying(255)")]
        public string Path { get; set; }
        [Column("menu", TypeName = "character varying(10240)")]
        public string Menu { get; set; }
        [Column("roles", TypeName = "character varying(10240)")]
        public string Roles { get; set; }
		[Column("show_system_menu")][FilterUIHint("", "", "defaultValue", "false")]
        public bool? ShowSystemMenu { get; set; }
        [Column("routes", TypeName = "character varying(10240)")]
        public string Routes { get; set; }
		[Column("authctoken", TypeName = "character varying(255)")]
        public string Authctoken { get; set; }
        [Column("ip", TypeName = "character varying(255)")]
        public string Ip { get; set; }
		[Column("config", TypeName = "character varying(10240)")]
        public string Config { get; set; }
		[Column("password", TypeName = "character varying(255)")]
        public string Password { get; set; }
/*
        [Column("routes_jsonb", TypeName = "jsonb")]
        public string RoutesJsonb { get; set; }
        [ForeignKey("Company")]
        [InverseProperty("CrudUser")]
        public CrudCompany CompanyNavigation { get; set; }
*/
    }
}
