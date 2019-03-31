INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterMessageAdapter', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Adaptador de protocolo de mensagens financeiras', '{"name":{"type":"s","primaryKey":true,"required":true},"parent":{"type":"s"},"adapterClass":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterMessageAdapterItem', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Configuração de adaptador de mensagens financeiras', '{"messageAdapter":{"type":"s","service":"iso8583RouterMessageAdapter","fieldNameForeign":"name","primaryKey":true,"required":true,"title":"Configuração dos campos"},"orderIndex":{"type":"i"},"rootPattern":{"type":"s","primaryKey":true,"required":true},"tag":{"type":"s","primaryKey":true,"required":true},"fieldName":{"type":"s"},"dataType":{"type":"i","flags":"decimal,hexadecimal,alfanumérico,caracteres especiais,caracteres mascarados"},"sizeHeader":{"type":"i","defaultValue":"-1","options":"-1,0,1,2,3","optionsStr":"UNDEFINED SIZE,FIXED,LVAR,LLVAR,LLLVAR"},"minLength":{"type":"i"},"maxLength":{"type":"i"},"alignment":{"type":"i","defaultValue":"0","options":"0,1,2,3,4","optionsStr":"NONE,ZERO_LEFT,ZERO_RIGHT,SPACE_LEFT,SPACE_RIGHT"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterComm', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Gateways de Transações Financeiras', '{"session":{"type":"s"},"name":{"type":"s","primaryKey":true,"required":true},"enabled":{"type":"b"},"listen":{"type":"b"},"ip":{"type":"s"},"port":{"type":"i"},"permanent":{"type":"b"},"sizeAscii":{"type":"b"},"adapter":{"type":"s"},"backlog":{"type":"i"},"maxOpenedConnections":{"type":"i"},"messageAdapter":{"type":"s","service":"iso8583RouterMessageAdapter","fieldNameForeign":"name","required":true,"defaultValue":"iso8583default","title":"Padrão de mensagem"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterLog', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Registro de tráfego de transações financeiras', '{"timeId":{"type":"s","primaryKey":true,"required":true},"transactionId":{"type":"i"},"loglevel":{"type":"s","required":true},"modules":{"type":"s","required":true},"root":{"type":"s"},"header":{"type":"s","required":true},"message":{"type":"s","required":true},"transaction":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterTransaction', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Transações financeiras', '{"id":{"type":"i","primaryKey":true,"hiden":true},"authNsu":{"type":"s"},"captureEc":{"type":"s"},"captureEquipamentType":{"type":"s"},"captureNsu":{"type":"s"},"captureProtocol":{"type":"s"},"captureTablesVersionsIn":{"type":"s"},"captureTablesVersionsOut":{"type":"s"},"captureType":{"type":"s"},"cardExpiration":{"type":"i"},"channelConn":{"type":"s"},"codeProcess":{"type":"s"},"codeResponse":{"type":"s"},"connDirection":{"type":"s"},"connId":{"type":"s"},"countryCode":{"type":"s"},"data":{"type":"s"},"dataComplement":{"type":"s"},"dateLocal":{"type":"s"},"dateTimeGmt":{"type":"s"},"emvData":{"type":"s"},"emvPanSequence":{"type":"s"},"equipamentId":{"type":"s"},"financialDate":{"type":"i"},"hourLocal":{"type":"s"},"lastOkDate":{"type":"s"},"lastOkNsu":{"type":"s"},"merchantType":{"type":"s"},"module":{"type":"s"},"moduleIn":{"type":"s"},"moduleOut":{"type":"s"},"msgType":{"type":"s"},"numPayments":{"type":"s"},"pan":{"type":"s"},"password":{"type":"s"},"providerEc":{"type":"s"},"providerId":{"type":"s"},"providerName":{"type":"s"},"providerNsu":{"type":"s"},"replyEspected":{"type":"s"},"root":{"type":"s"},"route":{"type":"s"},"sendResponse":{"type":"b"},"sequenceIndex":{"type":"s"},"systemDateTime":{"type":"s"},"terminalSerialNumber":{"type":"s"},"timeExec":{"type":"i"},"timeout":{"type":"i"},"trackI":{"type":"s"},"trackIi":{"type":"s"},"transactionValue":{"type":"s"},"transportData":{"type":"s"},"uniqueCaptureNsu":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterChipApplicationIdentifier', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"applicationIdentifierCodeTag9f06":{"type":"s","primaryKey":true,"required":true},"dynamicDataAuthenticationDataObjectList":{"type":"s"},"maxTargetPercentage":{"type":"i"},"merchantCategoryCodeTag9f15":{"type":"i"},"product":{"type":"i"},"responseCodeOfflineAproved":{"type":"s"},"responseCodeOfflineDeclined":{"type":"s"},"responseCodeOnlineApproved":{"type":"s"},"responseCodeOnlineDeclined":{"type":"s"},"tags":{"type":"s"},"targetPercentage":{"type":"i"},"label":{"type":"s"},"terminalActionCodeDefault":{"type":"s"},"terminalActionCodeDenial":{"type":"s"},"terminalActionCodeOnline":{"type":"s"},"terminalCapabilitiesAditionalTag9f40":{"type":"s"},"terminalCapabilitiesTag9f33":{"type":"s"},"terminalCountryCodeTag9f1a":{"type":"i"},"terminalFloorLimitTag9f1b":{"type":"s"},"terminalReferenceCurrencyCodeTag9f3c":{"type":"i"},"terminalTypeTag9f35":{"type":"i"},"thresholdAmount":{"type":"s"},"transactionCategoryCodeTag9f53":{"type":"s"},"transactionCertificateDataObjectList":{"type":"s"},"transactionCurrencyCodeTag5f2a":{"type":"i"},"transactionCurrencyExponentTag5f36":{"type":"i"},"versionIiiTag9f09":{"type":"i"},"versionIiTag9f09":{"type":"i"},"versionITag9f09":{"type":"i"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterChipPublicKey', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"publicKeyCheckSum":{"type":"s","primaryKey":true,"required":true},"expSize":{"type":"i"},"hashStatus":{"type":"i"},"publicKeyCheckExponentTag9f2e":{"type":"s"},"publicKeyIndexTag9f22":{"type":"s"},"publicKeyModulusTag9f2d":{"type":"s"},"registeredApplicationProviderIdentifier":{"type":"s"}}');

INSERT INTO crud_user (crud_group_owner, name, password, path, menu, roles, show_system_menu, config, routes, routes_jsonb) VALUES
(1, 'iso8583router', '123456', 'iso8583_router_comm/search', NULL, '{"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"iso8583RouterComm":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterMessageAdapter":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterMessageAdapterItem":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterLog":{"read":true,"query":true,"create":false,"update":false,"delete":false},"iso8583RouterTransaction":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '', '[{"path": "/app/crud_service/:action", "controller": "crud/CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "crud/UserController"}]')
;

INSERT INTO iso8583_router_message_adapter (name, parent, adapter_class) VALUES
('iso8583default', NULL, 'org.domain.financial.messages.MessageAdapterISO8583')
;

INSERT INTO iso8583_router_comm (name, message_adapter, adapter, backlog, direction, enabled, endian_type, ip, listen, max_opened_connections, permanent, port, size_ascii) VALUES
('POS', 'iso8583default', 'org.domain.financial.messages.comm.CommAdapterPayload', 50, 0, true, 1, 'localhost', true, 1, true, 2001, false),
('TEF', 'iso8583default', 'org.domain.financial.messages.comm.CommAdapterSizePayload', 50, 0, true, 1, 'localhost', true, 1, true, 2002, true),
('MASTERCARD', 'iso8583default', 'org.domain.financial.messages.comm.CommAdapterSizePayload', 50, 2, true, 1, 'localhost', false, 1, true, 3001, true)
;

INSERT INTO iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES 
('iso8583default', 1, 'XXXX_XXXXXX', '000', 'msgType', 1, NULL, 1, 0, 4, 4),
('iso8583default', 2, '0000_XXXXXX', '001', NULL, 1, NULL, 1, 0, 1, 0),
('iso8583default', 3, 'XXXX_XXXXXX', '002', 'pan', 0, NULL, 17, 2, 19, 32),
('iso8583default', 4, 'XXXX_XXXXXX', '003', 'codeProcess', 1, NULL, 1, 0, 6, 6),
('iso8583default', 5, 'XXXX_XXXXXX', '004', 'transactionValue', 1, NULL, 1, 0, 12, 12),
('iso8583default', 6, 'XXXX_XXXXXX', '005', NULL, 1, NULL, 1, 0, 12, 12),
('iso8583default', 7, '0000_XXXXXX', '006', NULL, 1, NULL, 1, 0, 12, 12),
('iso8583default', 8, 'XXXX_XXXXXX', '007', 'dateTimeGmt', 1, NULL, 1, 0, 10, 10),
('iso8583default', 9, 'XXXX_XXXXXX', '011', 'captureNsu', 1, NULL, 1, 0, 6, 6),
('iso8583default', 10, 'XXXX_XXXXXX', '012', 'hourLocal', 1, NULL, 1, 0, 6, 6),
('iso8583default', 11, 'XXXX_XXXXXX', '013', 'dateLocal', 1, NULL, 1, 0, 4, 4),
('iso8583default', 12, 'XX00_XXXXXX', '014', 'cardExpiration', 1, NULL, 1, 0, 4, 4),
('iso8583default', 13, 'XXXX_XXXXXX', '015', 'financialDate', 1, NULL, 1, 0, 4, 4),
('iso8583default', 14, 'XXXX_XXXXXX', '018', 'merchantType', 1, NULL, 15, 0, 4, 4),
('iso8583default', 15, 'XXXX_XXXXXX', '022', 'captureType', 1, NULL, 1, 0, 3, 3),
('iso8583default', 16, 'XX00_XXXXXX', '023', 'emvPanSequence', 1, NULL, 17, 0, 3, 3),
('iso8583default', 17, '0420_XXXXXX', '023', 'emvPanSequence', 1, NULL, 17, 0, 3, 3),
('iso8583default', 18, 'XX00_XXXXXX', '024', 'nii', 1, NULL, 1, 0, 3, 3),
('iso8583default', 19, '0420_XXXXXX', '024', 'nii', 1, NULL, 1, 0, 3, 3),
('iso8583default', 21, '0000_XXXXXX', '025', NULL, 1, NULL, 1, 0, 2, 2),
('iso8583default', 22, 'XXXX_XXXXXX', '026', NULL, 1, NULL, 1, 0, 1, 1),
('iso8583default', 23, 'X210_XXXXXX', '027', 'replyEspected', 1, NULL, 1, 0, 1, 1),
('iso8583default', 25, 'XX00_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6),
('iso8583default', 26, 'XX10_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6),
('iso8583default', 30, 'XXXX_XXXXXX', '032', 'providerId', 1, NULL, 1, 2, 11, 15),
('iso8583default', 31, 'XXXX_XXXXXX', '033', 'channelConn', 1, NULL, 1, 2, 2, 4),
('iso8583default', 32, 'XX00_XXXXXX', '035', 'trackIi', 0, NULL, 15, 2, 1, 80),
('iso8583default', 34, 'XXXX_XXXXXX', '037', NULL, 0, NULL, 15, 0, 12, 12),
('iso8583default', 35, 'XXXX_XXXXXX', '038', NULL, 0, NULL, 7, 0, 6, 6),
('iso8583default', 36, 'XXXX_XXXXXX', '039', 'codeResponse', 1, NULL, 7, 0, 2, 2),
('iso8583default', 37, 'XX00_XXXXXX', '040', 'messageVersion', 1, NULL, 7, 0, 3, 3),
('iso8583default', 39, 'XX10_XXXXXX', '040', NULL, 0, NULL, 7, 0, 3, 3),
('iso8583default', 40, 'XX02_XXXXXX', '040', NULL, 0, NULL, 7, 0, 3, 3),
('iso8583default', 41, 'XXXX_XXXXXX', '041', 'equipamentId', 0, NULL, 15, 0, 8, 8),
('iso8583default', 42, 'XXXX_XXXXXX', '042', 'captureEc', 0, NULL, 15, 0, 15, 15),
('iso8583default', 43, 'XXXX_XXXXXX', '043', 'terminalSerialNumber', 0, NULL, 15, 2, 1, 99),
('iso8583default', 44, 'XXXX_XXXXXX', '044', NULL, 0, NULL, 15, 2, 1, 99),
('iso8583default', 45, 'XX00_XXXXXX', '045', 'trackI', 0, NULL, 15, 2, 1, 80),
('iso8583default', 47, '0000_XXXXXX', '047', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 48, 'XXXX_XXXXXX', '048', 'captureTablesVersionsIn', 0, NULL, 15, 3, 1, 999),
('iso8583default', 49, 'XX10_XXXXXX', '048', 'captureTablesVersionsOut', 0, NULL, 15, 3, 1, 999),
('iso8583default', 51, 'XXXX_XXXXXX', '049', 'countryCode', 1, NULL, 1, 0, 3, 3),
('iso8583default', 52, 'XX00_XXXXXX', '052', 'password', 0, NULL, 19, 0, 16, 16),
('iso8583default', 54, 'XX00_XXXXXX', '053', 'passwordSize', 1, NULL, 1, 0, 16, 16),
('iso8583default', 55, 'XX00_XXXXXX', '054', NULL, 1, NULL, 1, 3, 1, 255),
('iso8583default', 56, 'XX00_XXXXXX', '055', 'emvData', 0, NULL, 3, 3, 1, 255),
('iso8583default', 57, 'XX10_XXXXXX', '055', 'emvData', 0, NULL, 3, 3, 1, 255),
('iso8583default', 58, 'XXXX_XXXXXX', '057', NULL, 0, NULL, 15, 0, 1, 3),
('iso8583default', 59, 'XX00_XXXXXX', '057', 'sequenceIndex', 1, NULL, 1, 0, 3, 3),
('iso8583default', 60, 'XX10_XXXXXX', '057', 'sequenceIndex', 1, NULL, 1, 0, 3, 3),
('iso8583default', 61, 'XXXX_XXXXXX', '058', NULL, 0, NULL, 15, 3, 8, 8),
('iso8583default', 62, 'XX00_XXXXXX', '059', 'transportData', 0, NULL, 15, 3, 1, 999),
('iso8583default', 63, 'XXXX_XXXXXX', '060', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 64, 'XX00_XXXXXX', '061', 'captureEquipamentType', 0, NULL, 15, 3, 1, 999),
('iso8583default', 66, 'XX02_XXXXXX', '061', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 67, 'XX00_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999),
('iso8583default', 68, 'XX10_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999),
('iso8583default', 71, 'XX00_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999),
('iso8583default', 73, 'XX10_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999),
('iso8583default', 76, 'XXXX_XXXXXX', '067', 'numPayments', 1, NULL, 1, 0, 2, 2),
('iso8583default', 77, 'XX00_XXXXXX', '070', NULL, 0, NULL, 15, 0, 3, 3),
('iso8583default', 78, 'XX10_XXXXXX', '070', NULL, 0, NULL, 15, 0, 3, 3),
('iso8583default', 79, 'XXXX_XXXXXX', '071', 'captureProtocol', 0, NULL, 15, 0, 8, 8),
('iso8583default', 80, '0000_XXXXXX', '086', NULL, 0, NULL, 15, 0, 1, 16),
('iso8583default', 81, 'XXXX_XXXXXX', '090', 'transactionReference', 0, NULL, 15, 0, 42, 42),
('iso8583default', 82, 'XXXX_XXXXXX', '100', NULL, 1, NULL, 1, 2, 11, 11),
('iso8583default', 83, '0000_XXXXXX', '118', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 84, 'XXXX_XXXXXX', '120', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 85, '0000_XXXXXX', '121', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 86, 'XXXX_XXXXXX', '123', NULL, 0, NULL, 15, 3, 1, 999),
('iso8583default', 87, '0000_XXXXXX', '124', NULL, 0, NULL, 15, 3, 1, 99),
('iso8583default', 88, 'XXXX_XXXXXX', '125', 'lastOkNsu', 1, NULL, 1, 3, 9, 999),
('iso8583default', 89, 'XXXX_XXXXXX', '127', 'authNsu', 0, NULL, 15, 3, 6, 10),
('iso8583default', 90, 'XXXX_XXXXXX', '128', NULL, 0, NULL, 15, 3, 1, 999)
;
