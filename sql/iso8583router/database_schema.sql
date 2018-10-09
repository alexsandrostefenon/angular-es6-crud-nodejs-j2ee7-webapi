CREATE TABLE iso8583_router_message_adapter (
    name character varying(64) primary key,
    parent character varying(64) references iso8583_router_message_adapter, -- iso8583default
    adapter_class character varying(255) NOT NULL, -- MessageAdapterISO8583
    compress boolean,
    tag_prefix character varying(32)
);

CREATE TABLE iso8583_router_message_adapter_item (
    id SERIAL primary key,
    message_adapter character varying(64) references iso8583_router_message_adapter, -- iso8583default
    alignment integer,  -- ZERO_LEFT
    data_format character varying(255), -- 
    data_type integer NOT NULL, -- Utils.DATA_TYPE_DECIMAL | Utils.DATA_TYPE_ALPHA | Utils.DATA_TYPE_SPECIAL;
    field_name character varying(255),
    max_length integer NOT NULL, -- 2 * 999;
    min_length integer NOT NULL, -- 1
    root_pattern character varying(255) NOT NULL, -- "\\d\\d\\d\\d";
    size_header integer NOT NULL, -- 0
    tag character varying(255) NOT NULL
);

CREATE TABLE iso8583_router_comm (
    session character varying(64),
    name character varying(64) primary key,
    enabled boolean,
    listen boolean,
    ip character varying(64),
    port integer,
    permanent boolean,
    size_ascii boolean,
    adapter character varying(64),
    backlog integer,
    direction integer,
    endian_type integer,
    max_opened_connections integer,
    message_adapter character varying(255)
);

CREATE TABLE iso8583_router_transaction (
    id SERIAL primary key,
    auth_nsu character varying(20),
    capture_ec character varying(15),
    capture_equipament_type character varying(999),
    capture_nsu character varying(255),
    capture_protocol character varying(64),
    capture_tables_versions_in character varying(64),
    capture_tables_versions_out character varying(64),
    capture_type character varying(255),
    card_expiration integer,
    channel_conn character varying(255),
    code_process character varying(6),
    code_response character varying(3),
    conn_direction character varying(4),
    conn_id character varying(64),
    country_code character varying(255),
    data character varying(5994),
    data_complement character varying(5994),
    date_local character varying(255),
    date_time_gmt character varying(255),
    dinamic_fields character varying(5994),
    emv_data character varying(999),
    emv_pan_sequence character varying(999),
    equipament_id character varying(8),
    financial_date integer,
    hour_local character varying(255),
    last_ok_date character varying(255),
    last_ok_nsu character varying(20),
    merchant_type character varying(4),
    module character varying(64),
    module_in character varying(64),
    module_out character varying(64),
    msg_type character varying(64),
    num_payments character varying(2),
    pan character varying(32),
    password character varying(64),
    provider_ec character varying(20),
    provider_id character varying(255),
    provider_name character varying(64),
    provider_nsu character varying(12),
    reply_espected character varying(1),
    root character varying(64),
    route character varying(4096),
    send_response boolean,
    sequence_index character varying(3),
    system_date_time character varying(14),
    terminal_serial_number character varying(64),
    time_exec integer,
    time_stamp bigint,
    time_stamp_off bigint,
    time_stamp_on bigint,
    timeout integer,
    track_i character varying(160),
    track_ii character varying(104),
    transaction_id bigint,
    transaction_value character varying(255),
    transport_data character varying(999),
    unique_capture_nsu character varying(64)
);

CREATE TABLE iso8583_router_log (
    time_id char(19) primary key, -- yyyyMMdd-HHmmss.SSS
    transaction_id integer references iso8583_router_transaction,
    log_level character varying(64) NOT NULL,
    modules character varying(128) NOT NULL,
    root character varying(128),
    header character varying(128) NOT NULL,
    message character varying(1024) NOT NULL,
    transaction character varying(10240)
);

CREATE TABLE iso8583_router_chip_application_identifier (
    application_identifier_code_tag_9f06 character varying(32) primary key,
    dynamic_data_authentication_data_object_list character varying(40),
    max_target_percentage integer,
    merchant_category_code_tag_9f15 integer,
    product integer,
    response_code_offline_aproved character varying(2),
    response_code_offline_declined character varying(2),
    response_code_online_approved character varying(2),
    response_code_online_declined character varying(2),
    tags character varying(255),
    target_percentage integer,
    label character varying(24),
    terminal_action_code_default character varying(10),
    terminal_action_code_denial character varying(10),
    terminal_action_code_online character varying(10),
    terminal_capabilities_aditional_tag_9f40 character varying(10),
    terminal_capabilities_tag_9f33 character varying(6),
    terminal_country_code_tag_9f1a integer,
    terminal_floor_limit_tag_9f1b character varying(8),
    terminal_reference_currency_code_tag_9f3c integer,
    terminal_type_tag_9f35 integer,
    threshold_amount character varying(255),
    transaction_category_code_tag_9f53 character varying(2),
    transaction_certificate_data_object_list character varying(40),
    transaction_currency_code_tag_5f2a integer,
    transaction_currency_exponent_tag_5f36 integer,
    version_iii_tag_9f09 integer,
    version_ii_tag_9f09 integer,
    version_i_tag_9f09 integer
);

CREATE TABLE iso8583_router_chip_public_key (
    public_key_check_sum character varying(40) primary key,
    exp_size integer,
    hash_status integer,
    public_key_check_exponent_tag_9f2e character varying(6),
    public_key_index_tag_9f22 character varying(2),
    public_key_modulus_tag_9f2d character varying(496),
    registered_application_provider_identifier character varying(10)
);
