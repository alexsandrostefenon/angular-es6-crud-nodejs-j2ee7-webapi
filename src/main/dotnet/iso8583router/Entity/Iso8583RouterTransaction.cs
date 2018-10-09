using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_transaction")]
    public partial class Iso8583RouterTransaction
    {
        public Iso8583RouterTransaction()
        {
            Iso8583RouterLog = new HashSet<Iso8583RouterLog>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("auth_nsu", TypeName = "character varying(20)")]
        public string AuthNsu { get; set; }
        [Column("capture_ec", TypeName = "character varying(15)")]
        public string CaptureEc { get; set; }
        [Column("capture_equipament_type", TypeName = "character varying(999)")]
        public string CaptureEquipamentType { get; set; }
        [Column("capture_nsu", TypeName = "character varying(255)")]
        public string CaptureNsu { get; set; }
        [Column("capture_protocol", TypeName = "character varying(64)")]
        public string CaptureProtocol { get; set; }
        [Column("capture_tables_versions_in", TypeName = "character varying(64)")]
        public string CaptureTablesVersionsIn { get; set; }
        [Column("capture_tables_versions_out", TypeName = "character varying(64)")]
        public string CaptureTablesVersionsOut { get; set; }
        [Column("capture_type", TypeName = "character varying(255)")]
        public string CaptureType { get; set; }
        [Column("card_expiration")]
        public int? CardExpiration { get; set; }
        [Column("channel_conn", TypeName = "character varying(255)")]
        public string ChannelConn { get; set; }
        [Column("code_process", TypeName = "character varying(6)")]
        public string CodeProcess { get; set; }
        [Column("code_response", TypeName = "character varying(3)")]
        public string CodeResponse { get; set; }
        [Column("conn_direction", TypeName = "character varying(4)")]
        public string ConnDirection { get; set; }
        [Column("conn_id", TypeName = "character varying(64)")]
        public string ConnId { get; set; }
        [Column("country_code", TypeName = "character varying(255)")]
        public string CountryCode { get; set; }
        [Column("data", TypeName = "character varying(5994)")]
        public string Data { get; set; }
        [Column("data_complement", TypeName = "character varying(5994)")]
        public string DataComplement { get; set; }
        [Column("date_local", TypeName = "character varying(255)")]
        public string DateLocal { get; set; }
        [Column("date_time_gmt", TypeName = "character varying(255)")]
        public string DateTimeGmt { get; set; }
        [Column("dinamic_fields", TypeName = "character varying(5994)")]
        public string DinamicFields { get; set; }
        [Column("emv_data", TypeName = "character varying(999)")]
        public string EmvData { get; set; }
        [Column("emv_pan_sequence", TypeName = "character varying(999)")]
        public string EmvPanSequence { get; set; }
        [Column("equipament_id", TypeName = "character varying(8)")]
        public string EquipamentId { get; set; }
        [Column("financial_date")]
        public int? FinancialDate { get; set; }
        [Column("hour_local", TypeName = "character varying(255)")]
        public string HourLocal { get; set; }
        [Column("last_ok_date", TypeName = "character varying(255)")]
        public string LastOkDate { get; set; }
        [Column("last_ok_nsu", TypeName = "character varying(20)")]
        public string LastOkNsu { get; set; }
        [Column("merchant_type", TypeName = "character varying(4)")]
        public string MerchantType { get; set; }
        [Column("module", TypeName = "character varying(64)")]
        public string Module { get; set; }
        [Column("module_in", TypeName = "character varying(64)")]
        public string ModuleIn { get; set; }
        [Column("module_out", TypeName = "character varying(64)")]
        public string ModuleOut { get; set; }
        [Column("msg_type", TypeName = "character varying(64)")]
        public string MsgType { get; set; }
        [Column("num_payments", TypeName = "character varying(2)")]
        public string NumPayments { get; set; }
        [Column("pan", TypeName = "character varying(32)")]
        public string Pan { get; set; }
        [Column("password", TypeName = "character varying(64)")]
        public string Password { get; set; }
        [Column("provider_ec", TypeName = "character varying(20)")]
        public string ProviderEc { get; set; }
        [Column("provider_id", TypeName = "character varying(255)")]
        public string ProviderId { get; set; }
        [Column("provider_name", TypeName = "character varying(64)")]
        public string ProviderName { get; set; }
        [Column("provider_nsu", TypeName = "character varying(12)")]
        public string ProviderNsu { get; set; }
        [Column("reply_espected", TypeName = "character varying(1)")]
        public string ReplyEspected { get; set; }
        [Column("root", TypeName = "character varying(64)")]
        public string Root { get; set; }
        [Column("route", TypeName = "character varying(4096)")]
        public string Route { get; set; }
        [Column("send_response")]
        public bool? SendResponse { get; set; }
        [Column("sequence_index", TypeName = "character varying(3)")]
        public string SequenceIndex { get; set; }
        [Column("system_date_time", TypeName = "character varying(14)")]
        public string SystemDateTime { get; set; }
        [Column("terminal_serial_number", TypeName = "character varying(64)")]
        public string TerminalSerialNumber { get; set; }
        [Column("time_exec")]
        public int? TimeExec { get; set; }
        [Column("time_stamp")]
        public long? TimeStamp { get; set; }
        [Column("time_stamp_off")]
        public long? TimeStampOff { get; set; }
        [Column("time_stamp_on")]
        public long? TimeStampOn { get; set; }
        [Column("timeout")]
        public int? Timeout { get; set; }
        [Column("track_i", TypeName = "character varying(160)")]
        public string TrackI { get; set; }
        [Column("track_ii", TypeName = "character varying(104)")]
        public string TrackIi { get; set; }
        [Column("transaction_id")]
        public long? TransactionId { get; set; }
        [Column("transaction_value", TypeName = "character varying(255)")]
        public string TransactionValue { get; set; }
        [Column("transport_data", TypeName = "character varying(999)")]
        public string TransportData { get; set; }
        [Column("unique_capture_nsu", TypeName = "character varying(64)")]
        public string UniqueCaptureNsu { get; set; }

        [InverseProperty("TransactionNavigation")]
        public ICollection<Iso8583RouterLog> Iso8583RouterLog { get; set; }
    }
}
