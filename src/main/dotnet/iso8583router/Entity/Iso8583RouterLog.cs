using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_log")]
    public partial class Iso8583RouterLog
    {
        [Key][Column("time_id", TypeName = "character(19)")]
        public string TimeId { get; set; }
        [Column("transaction_id")]
        public int? TransactionId { get; set; }
        [Required]
        [Column("log_level", TypeName = "character varying(64)")]
        public string LogLevel { get; set; }
        [Required]
        [Column("modules", TypeName = "character varying(128)")]
        public string Modules { get; set; }
        [Column("root", TypeName = "character varying(128)")]
        public string Root { get; set; }
        [Required]
        [Column("header", TypeName = "character varying(128)")]
        public string Header { get; set; }
        [Required]
        [Column("message", TypeName = "character varying(1024)")]
        public string Message { get; set; }
        [Column("transaction", TypeName = "character varying(10240)")]
        public string Transaction { get; set; }

        [ForeignKey("TransactionId")]
        [InverseProperty("Iso8583RouterLog")]
        public Iso8583RouterTransaction TransactionNavigation { get; set; }
    }
}
