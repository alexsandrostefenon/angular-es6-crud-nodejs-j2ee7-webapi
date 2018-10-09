using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_comm")]
    public partial class Iso8583RouterComm
    {
        [Column("session", TypeName = "character varying(64)")]
        public string Session { get; set; }
        [Key][Column("name", TypeName = "character varying(64)")]
        public string Name { get; set; }
        [Column("enabled")]
        public bool? Enabled { get; set; }
        [Column("listen")]
        public bool? Listen { get; set; }
        [Column("ip", TypeName = "character varying(64)")]
        public string Ip { get; set; }
        [Column("port")]
        public int? Port { get; set; }
        [Column("permanent")]
        public bool? Permanent { get; set; }
        [Column("size_ascii")]
        public bool? SizeAscii { get; set; }
        [Column("adapter", TypeName = "character varying(64)")]
        public string Adapter { get; set; }
        [Column("backlog")]
        public int? Backlog { get; set; }
        [Column("direction")]
        public int? Direction { get; set; }
        [Column("endian_type")]
        public int? EndianType { get; set; }
        [Column("max_opened_connections")]
        public int? MaxOpenedConnections { get; set; }
        [Column("message_adapter", TypeName = "character varying(255)")]
        public string MessageAdapter { get; set; }
    }
}
