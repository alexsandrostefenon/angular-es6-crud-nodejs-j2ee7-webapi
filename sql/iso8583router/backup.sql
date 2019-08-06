--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: crud_service; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudService', 'admin', NULL, NULL, 'name', NULL, NULL, NULL, '{"name":{"type":"s","primaryKey":true,"required":true},"title":{"type":"s"},"template":{"type":"s"},"menu":{"type":"s"},"saveAndExit":{"type":"b"},"filterFields":{"type":"s"},"orderBy":{"type":"s"},"isOnLine":{"type":"b"},"fields":{"type":"s","readOnly":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudCompany', 'admin', NULL, NULL, 'id,name', NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudUser', 'admin', NULL, false, 'name,company', NULL, NULL, NULL, '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"name":{"type":"s","primaryKey":true},"path":{"type":"s"},"menu":{"type":"s"},"roles":{"type":"s"},"showSystemMenu":{"type":"b","defaultValue":"false"},"routes":{"type":"s"},"authctoken":{"type":"s"},"ip":{"type":"s"},"config":{"type":"s"},"password":{"type":"p"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudTranslation', 'admin', NULL, true, 'id,name,locale,translation', NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"locale":{"type":"s","defaultValue":"pt-br","required":true},"name":{"type":"s","required":true},"translation":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('category', 'admin', NULL, false, 'name', NULL, NULL, 'Controle de Categorias de Produtos e Serviços', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('categoryCompany', 'admin', NULL, true, 'id,category,company', NULL, NULL, 'Categorias de cada Empresa', '{"company":{"type":"i","hiden":true,"primaryKey":true,"isClonable":true,"service":"crudCompany","title":"Categorias Vinculadas","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"category":{"type":"i","required":true,"service":"category"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStCofins', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStCsosn', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"},"description":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIcms', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIcmsDesoneracao', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIcmsModalidadeBc', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIcmsModalidadeSt', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIcmsOrigem', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIpi', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIpiEnquadramento', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"},"ipiOperacao":{"type":"i"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStIpiOperacao', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeStPis', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('bacenCountry', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos de Países', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"namePt":{"type":"s","required":true},"abr":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeUf', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos dos Estados', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"},"name":{"type":"s","required":true},"abr":{"type":"s","required":true},"ddd":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeCity', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos das Cidades', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"},"name":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeCnae', 'person', NULL, true, 'id,name', NULL, NULL, 'Classificação Nacional de Atividades Econômicas', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('person', 'person', NULL, true, 'name,cnpjCpf', NULL, NULL, 'Cadastros de Clientes e Fornecedores', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true},"fantasy":{"type":"s"},"cnpjCpf":{"type":"s"},"ieRg":{"type":"s"},"suframa":{"type":"s"},"im":{"type":"s"},"cnae":{"type":"i","service":"ibgeCnae"},"crt":{"type":"i","defaultValue":"1","options":"1 - Simples Nacional,2 - Simples Nacional (excesso sublimite de receita bruta),3 - Regime Normal","required":false},"zip":{"type":"s"},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"},"city":{"type":"i","service":"ibgeCity","defaultValue":"4304606"},"district":{"type":"s"},"address":{"type":"s"},"addressNumber":{"type":"s","required":false},"complement":{"type":"s"},"email":{"type":"s"},"site":{"type":"s"},"phone":{"type":"s"},"fax":{"type":"s"},"credit":{"type":"n3","defaultValue":"0"},"additionalData":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('camexNcm', 'product', NULL, true, 'id,name', NULL, NULL, 'NCM', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"unit":{"type":"s","required":true},"tec":{"type":"i","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('confazCest', 'product', NULL, true, 'id,name', NULL, NULL, 'Código Especificador da Substituição Tributária', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"ncm":{"type":"i","required":true,"service":"productNcm"},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('product', 'product', NULL, true, 'id,name,model', NULL, NULL, 'Produtos, Peças e Componentes', '{"id":{"type":"i","primaryKey":true,"hiden":true},"category":{"type":"i","service":"category"},"ncm":{"type":"i","service":"camexNcm"},"orig":{"type":"i","defaultValue":"0","options":"0,3,4,5,8","required":true},"name":{"type":"s"},"departament":{"type":"s","hiden":true},"model":{"type":"s"},"description":{"type":"s"},"weight":{"type":"n3"},"imageUrl":{"type":"s","hiden":true},"additionalData":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('barcode', 'product', NULL, true, 'barcode,product,manufacturer', NULL, NULL, 'Código de Barras de fornecedores de produtos', '{"number":{"type":"s","primaryKey":true,"required":true},"manufacturer":{"type":"s","required":true},"product":{"type":"i","service":"product","title":"Código de Barras de fornecedores de produtos","isClonable":false}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestType', 'request', NULL, true, 'name', NULL, NULL, 'Tipo de Requisição', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestState', 'request', NULL, true, 'type,name', NULL, NULL, 'Situação da Requisição', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true},"next":{"type":"i","service":"requestType","defaultValue":"0"},"prev":{"type":"i","service":"requestState","defaultValue":"0"},"stockAction":{"type":"i","service":"stockAction"},"type":{"type":"i","service":"requestType","defaultValue":"1"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterComm', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Gateways de Transações Financeiras', '{"session":{"type":"s"},"name":{"type":"s","primaryKey":true,"required":true},"enabled":{"type":"b"},"listen":{"type":"b"},"ip":{"type":"s"},"port":{"type":"i"},"permanent":{"type":"b"},"sizeAscii":{"type":"b"},"adapter":{"type":"s"},"backlog":{"type":"i"},"maxOpenedConnections":{"type":"i"},"messageAdapter":{"type":"s","service":"iso8583RouterMessageAdapter","fieldNameForeign":"name","required":true,"defaultValue":"iso8583default","title":"Padrão de mensagem"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('request', 'request', NULL, false, 'person,date', 'date desc,id desc', NULL, 'Requisições de Entrada e Saída', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","required":true,"service":"requestType","readOnly":true,"hiden":false},"state":{"type":"i","required":true,"service":"requestState"},"person":{"type":"i","required":true,"service":"person"},"date":{"type":"datetime-local","required":true},"additionalData":{"type":"s"},"productsValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"servicesValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"transportValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"descValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"sumValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"paymentsValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeCfop', 'requestProduct', NULL, true, 'id,name', NULL, NULL, 'CFOP', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"indNfe":{"type":"i"},"indComunica":{"type":"i"},"indTransp":{"type":"i"},"indDevol":{"type":"i"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeTaxGroup', 'requestProduct', NULL, true, 'name', NULL, NULL, 'Grupo Tributário', '{"id":{"type":"i","primaryKey":true,"hiden":true,"required":false},"name":{"type":"s","required":true},"cstIpi":{"type":"i"},"cstIcms":{"type":"i"},"cstPis":{"type":"i"},"cstCofins":{"type":"i"},"taxSimples":{"type":"n2"},"taxIpi":{"type":"n3"},"taxIcms":{"type":"n2"},"taxPis":{"type":"n2"},"taxCofins":{"type":"n2"},"taxIssqn":{"type":"n2"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('stockAction', 'requestProduct', NULL, true, 'name', NULL, NULL, 'Ação sobre o Estoque', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('stock', 'requestProduct', NULL, true, 'product', NULL, NULL, 'Estoque de Produtos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"service":"product","required":true,"hiden":false},"countIn":{"type":"n"},"countOut":{"type":"n"},"estimedIn":{"type":"n3"},"estimedOut":{"type":"n"},"estimedValue":{"type":"n"},"marginSale":{"type":"n"},"marginWholesale":{"type":"n"},"reservedIn":{"type":"n"},"reservedOut":{"type":"n"},"stockValue":{"type":"n3"},"stockDefault":{"type":"n"},"stockMinimal":{"type":"n"},"stockSerials":{"type":"s"},"sumValueIn":{"type":"n"},"sumValueOut":{"type":"n"},"sumValueStock":{"type":"n"},"value":{"type":"n"},"valueWholesale":{"type":"n"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('paymentType', 'requestPayment', NULL, true, 'name', NULL, NULL, 'Tipo de Pagamento', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('account', 'requestPayment', NULL, true, 'description', NULL, NULL, 'Contas Bancárias', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"number":{"type":"s"},"agency":{"type":"s"},"bank":{"type":"s"},"description":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestProduct', 'report', NULL, true, 'request,product,serials,quantity,value', NULL, NULL, 'Entrada e Saída de Produtos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"product":{"type":"i","required":true,"service":"product"},"quantity":{"type":"n3","defaultValue":"1.000","required":true},"value":{"type":"n3","defaultValue":"0.0","required":true},"valueItem":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true},"valueDesc":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"valueFreight":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"cfop":{"type":"i","service":"nfeCfop"},"tax":{"type":"i","service":"nfeTaxGroup"},"valueAllTax":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true},"serials":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestFreight', 'report', NULL, true, 'request,person,payBy,value', NULL, NULL, 'Frete', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"person":{"type":"i","service":"person"},"payBy":{"type":"i","options":"0,1,2,9","required":true},"licensePlate":{"type":"s","hiden":false},"licensePlateUf":{"type":"i","service":"ibgeUf","hiden":false},"containersType":{"type":"s","hiden":false},"containersCount":{"type":"i"},"weight":{"type":"n3","hiden":false},"weightFinal":{"type":"n3","hiden":false},"logo":{"type":"s","hiden":false},"value":{"type":"n2"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestPayment', 'report', NULL, true, 'request,type,account,number', 'due_date,id', NULL, 'Pagamentos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","required":true,"service":"paymentType"},"value":{"type":"n2","required":true},"account":{"type":"i","required":true,"service":"account"},"number":{"type":"s"},"dueDate":{"type":"datetime-local","defaultValue":"now","required":true},"payday":{"type":"datetime-local"},"balance":{"type":"n2","readOnly":true,"required":false}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestNfe', 'report', NULL, true, 'request', NULL, NULL, 'Nota Fiscal Eletrônica', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"person":{"type":"i"},"versao":{"type":"s"},"nfeId":{"type":"s"},"natop":{"type":"s","defaultValue":"Venda","required":true},"indpag":{"type":"i","defaultValue":"0","options":"0,1,2","required":true},"mod":{"type":"i"},"serie":{"type":"i"},"nnf":{"type":"i"},"dhemi":{"type":"datetime-local","defaultValue":"now"},"dhsaient":{"type":"datetime-local","defaultValue":"now"},"tpnf":{"type":"i"},"iddest":{"type":"i","defaultValue":"1","options":"1,2,3","required":true},"tpimp":{"type":"i"},"tpemis":{"type":"i"},"cdv":{"type":"i","hiden":true},"tpamb":{"type":"i"},"finnfe":{"type":"i"},"indfinal":{"type":"i","defaultValue":"1","options":"0,1","required":true},"indpres":{"type":"i","defaultValue":"1","options":"0,1,2,3,4,9","required":true},"procemi":{"type":"i"},"verproc":{"type":"s"},"indiedest":{"type":"i"},"valueIi":{"type":"n2"},"valueIpi":{"type":"n2"},"valuePis":{"type":"n2"},"valueCofins":{"type":"n2"},"valueIcms":{"type":"n2"},"valueIcmsSt":{"type":"n2"},"valueIssqn":{"type":"n2"},"valueTax":{"type":"n2"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterMessageAdapter', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Adaptador de protocolo de mensagens financeiras', '{"name":{"type":"s","primaryKey":true,"required":true},"parent":{"type":"s"},"adapterClass":{"type":"s","required":true}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterLog', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Registro de tráfego de transações financeiras', '{"timeId":{"type":"s","primaryKey":true,"required":true},"transactionId":{"type":"i"},"loglevel":{"type":"s","required":true},"modules":{"type":"s","required":true},"root":{"type":"s"},"header":{"type":"s","required":true},"message":{"type":"s","required":true},"transaction":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterTransaction', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Transações financeiras', '{"id":{"type":"i","primaryKey":true,"hiden":true},"authNsu":{"type":"s"},"captureEc":{"type":"s"},"captureEquipamentType":{"type":"s"},"captureNsu":{"type":"s"},"captureProtocol":{"type":"s"},"captureTablesVersionsIn":{"type":"s"},"captureTablesVersionsOut":{"type":"s"},"captureType":{"type":"s"},"cardExpiration":{"type":"i"},"channelConn":{"type":"s"},"codeProcess":{"type":"s"},"codeResponse":{"type":"s"},"connDirection":{"type":"s"},"connId":{"type":"s"},"countryCode":{"type":"s"},"data":{"type":"s"},"dataComplement":{"type":"s"},"dateLocal":{"type":"s"},"dateTimeGmt":{"type":"s"},"emvData":{"type":"s"},"emvPanSequence":{"type":"s"},"equipamentId":{"type":"s"},"financialDate":{"type":"i"},"hourLocal":{"type":"s"},"lastOkDate":{"type":"s"},"lastOkNsu":{"type":"s"},"merchantType":{"type":"s"},"module":{"type":"s"},"moduleIn":{"type":"s"},"moduleOut":{"type":"s"},"msgType":{"type":"s"},"numPayments":{"type":"s"},"pan":{"type":"s"},"password":{"type":"s"},"providerEc":{"type":"s"},"providerId":{"type":"s"},"providerName":{"type":"s"},"providerNsu":{"type":"s"},"replyEspected":{"type":"s"},"root":{"type":"s"},"route":{"type":"s"},"sendResponse":{"type":"b"},"sequenceIndex":{"type":"s"},"systemDateTime":{"type":"s"},"terminalSerialNumber":{"type":"s"},"timeExec":{"type":"i"},"timeout":{"type":"i"},"trackI":{"type":"s"},"trackIi":{"type":"s"},"transactionValue":{"type":"s"},"transportData":{"type":"s"},"uniqueCaptureNsu":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterChipApplicationIdentifier', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"applicationIdentifierCodeTag9f06":{"type":"s","primaryKey":true,"required":true},"dynamicDataAuthenticationDataObjectList":{"type":"s"},"maxTargetPercentage":{"type":"i"},"merchantCategoryCodeTag9f15":{"type":"i"},"product":{"type":"i"},"responseCodeOfflineAproved":{"type":"s"},"responseCodeOfflineDeclined":{"type":"s"},"responseCodeOnlineApproved":{"type":"s"},"responseCodeOnlineDeclined":{"type":"s"},"tags":{"type":"s"},"targetPercentage":{"type":"i"},"label":{"type":"s"},"terminalActionCodeDefault":{"type":"s"},"terminalActionCodeDenial":{"type":"s"},"terminalActionCodeOnline":{"type":"s"},"terminalCapabilitiesAditionalTag9f40":{"type":"s"},"terminalCapabilitiesTag9f33":{"type":"s"},"terminalCountryCodeTag9f1a":{"type":"i"},"terminalFloorLimitTag9f1b":{"type":"s"},"terminalReferenceCurrencyCodeTag9f3c":{"type":"i"},"terminalTypeTag9f35":{"type":"i"},"thresholdAmount":{"type":"s"},"transactionCategoryCodeTag9f53":{"type":"s"},"transactionCertificateDataObjectList":{"type":"s"},"transactionCurrencyCodeTag5f2a":{"type":"i"},"transactionCurrencyExponentTag5f36":{"type":"i"},"versionIiiTag9f09":{"type":"i"},"versionIiTag9f09":{"type":"i"},"versionITag9f09":{"type":"i"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterChipPublicKey', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{"publicKeyCheckSum":{"type":"s","primaryKey":true,"required":true},"expSize":{"type":"i"},"hashStatus":{"type":"i"},"publicKeyCheckExponentTag9f2e":{"type":"s"},"publicKeyIndexTag9f22":{"type":"s"},"publicKeyModulusTag9f2d":{"type":"s"},"registeredApplicationProviderIdentifier":{"type":"s"}}');
INSERT INTO public.crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('iso8583RouterMessageAdapterItem', 'iso8583Router', NULL, true, NULL, NULL, NULL, 'Configuração de adaptador de mensagens financeiras', '{"messageAdapter":{"type":"s","service":"iso8583RouterMessageAdapter","fieldNameForeign":"name","primaryKey":true,"required":true,"title":"Configuração dos campos"},"orderIndex":{"type":"i"},"rootPattern":{"type":"s","primaryKey":true,"required":true},"tag":{"type":"s","primaryKey":true,"required":true},"fieldName":{"type":"s"},"dataType":{"type":"i","flags":"decimal,hexadecimal,alfanumérico,caracteres especiais,caracteres mascarados"},"sizeHeader":{"type":"i","defaultValue":"-1","options":"-1,0,1,2,3","optionsStr":"UNDEFINED SIZE,FIXED,LVAR,LLVAR,LLLVAR"},"minLength":{"type":"i"},"maxLength":{"type":"i"},"alignment":{"type":"i","defaultValue":"0","options":"0,1,2,3,4","optionsStr":"NONE,ZERO_LEFT,ZERO_RIGHT,SPACE_LEFT,SPACE_RIGHT"}}');


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: crud_user; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (1, '03ce76b5-b2ed-42ff-783e-54b8fa87d844', NULL, NULL, 'iso8583router', '123456', 'iso8583_router_comm/search', '{"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"iso8583RouterComm":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterMessageAdapter":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterMessageAdapterItem":{"read":true,"query":true,"create":true,"update":true,"delete":true},"iso8583RouterLog":{"read":true,"query":true,"create":false,"update":false,"delete":false},"iso8583RouterTransaction":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '', '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]');
INSERT INTO public.crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (1, '5e2ee98a-d1b5-b8d6-559b-1457bfe5b9d0', NULL, NULL, 'admin', 'admin', 'crud_service/search', '{"crudService":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudUser":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudTranslation":{"read":true,"query":true,"create":true,"update":true,"delete":true},"category":{"read":true,"query":true,"create":true,"update":true,"delete":true},"categoryCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]', '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]');
INSERT INTO public.crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (2, '3c9bcca1-d808-0d33-85a6-1de0f6f9f19c', NULL, '{"buy":{"menu":"actions","label":"Compra","path":"request/new?type=1&state=10"},"requestPayment":{"menu":"form","label":"Financeiro","path":"request_payment/search"},"stock":{"menu":"form","label":"Estoque","path":"stock/search"},"product":{"menu":"form","label":"Produtos","path":"product/search"},"person":{"menu":"form","label":"Clientes e Fornecedores","path":"person/search"},"requests":{"menu":"form","label":"Requisições","path":"request/search"},"account":{"menu":"form","label":"Contas","path":"account/search"}}', 'spending', '123456', 'request/search', '{"crudCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudUser":{"read":true,"query":true,"create":false,"update":false,"delete":false},"category":{"read":true,"query":true,"create":false,"update":false,"delete":false},"categoryCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"account":{"read":true,"query":true,"create":true,"update":false,"delete":true},"requestType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"requestState":{"read":true,"query":true,"create":false,"update":false,"delete":false},"stockAction":{"read":true,"query":true,"create":false,"update":false,"delete":false},"paymentType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"person":{"read":true,"query":true,"create":true,"update":true,"delete":true},"product":{"read":true,"query":true,"create":true,"update":true,"delete":true},"barcode":{"read":true,"query":true,"create":true,"update":true,"delete":false},"request":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestProduct":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestPayment":{"read":true,"query":true,"create":true,"update":true,"delete":true},"stock":{"read":true,"query":true,"create":true,"update":true,"delete":false},"nfeCfop":{"read":true,"query":true,"create":false,"update":false,"delete":false},"bacenCountry":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeUf":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCity":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCnae":{"read":true,"query":true,"create":false,"update":false,"delete":false},"nfeTaxGroup":{"read":true,"query":true,"create":false,"update":false,"delete":false},"camexNcm":{"read":true,"query":true,"create":false,"update":false,"delete":false}}', false, NULL, '[{"path":"/app/request/:action","controller":"erp/RequestController"}]', '[{"path": "/app/request/:action", "controller": "erp/RequestController"}]');


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: crud_translation; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (1, 'pt-br', 'User', 'Usuário');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (2, 'pt-br', 'Exit', 'Sair');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (3, 'pt-br', 'New', 'Novo');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (4, 'pt-br', 'Filter', 'Filtrar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (5, 'pt-br', 'Search', 'Localizar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (6, 'pt-br', 'View', 'Visualizar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (7, 'pt-br', 'Edit', 'Editar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (8, 'pt-br', 'Delete', 'Apagar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (9, 'pt-br', 'Actions', 'Ações');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (10, 'pt-br', 'Cancel', 'Cancelar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (11, 'pt-br', 'Create', 'Criar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (12, 'pt-br', 'Save', 'Salvar');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (13, 'pt-br', 'Save as New', 'Salvar como Novo');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (14, 'pt-br', 'Name', 'Nome');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (15, 'pt-br', 'Description', 'Descrição');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (16, 'pt-br', 'Category', 'Categoria');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (17, 'pt-br', 'Date', 'Data');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (18, 'pt-br', 'Unit', 'Unidade');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (19, 'pt-br', 'Quantity', 'Quantidade');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (20, 'pt-br', 'Value', 'Valor');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (21, 'pt-br', 'Type', 'Tipo');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (22, 'pt-br', 'State', 'Situação');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (23, 'pt-br', 'Person', 'Cliente/Fornecedor');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (24, 'pt-br', 'Additional Data', 'Dados Adicionais');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (25, 'pt-br', 'Products Value', 'Valor Produtos');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (26, 'pt-br', 'Services Value', 'Valor Serviços');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (27, 'pt-br', 'Transport Value', 'Valor Transporte');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (28, 'pt-br', 'Sum Value', 'Valor Total');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (29, 'pt-br', 'Payments Value', 'Valor Faturas');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (30, 'pt-br', 'Product', 'Produto');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (31, 'pt-br', 'Serial', 'N. Série');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (32, 'pt-br', 'Defect', 'Defeito');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (33, 'pt-br', 'Barcode', 'Código de Barras');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (34, 'pt-br', 'Manufacturer', 'Fabricante');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (35, 'pt-br', 'Model', 'Modelo');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (36, 'pt-br', 'Weight', 'Peso');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (37, 'pt-br', 'Phone', 'Telefone');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (38, 'pt-br', 'Zip', 'CEP');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (39, 'pt-br', 'City', 'Cidade');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (40, 'pt-br', 'District', 'Bairro');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (41, 'pt-br', 'Address', 'Endereço');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (42, 'pt-br', 'Credit', 'Credito');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (43, 'pt-br', 'Number', 'Numero');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (44, 'pt-br', 'Number Form', 'Numero do Formulário');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (45, 'pt-br', 'Date Fiscal', 'Data Fiscal');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (46, 'pt-br', 'Request', 'Requisição');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (47, 'pt-br', 'Account', 'Conta');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (48, 'pt-br', 'Due Date', 'Data Vencimento');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (49, 'pt-br', 'Payday', 'Data do Pagamento');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (50, 'pt-br', 'Pay By', 'Pago Por');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (51, 'pt-br', 'Bank', 'Banco');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (52, 'pt-br', 'Agency', 'Agência');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (53, 'pt-br', 'Stock', 'Estoque');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (54, 'pt-br', 'Count In', 'Quantidade Entrada');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (55, 'pt-br', 'Count Out', 'Quantidade Saída');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (56, 'pt-br', 'Stock Default', 'Estoque Ideal');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (57, 'pt-br', 'Stock Minimal', 'Estoque Mínimo');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (58, 'pt-br', 'Reserved Out', 'Saída Reservada');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (59, 'pt-br', 'Reserved In', 'Entrada Reservada');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (60, 'pt-br', 'Estimed Out', 'Estimativa de Saída');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (61, 'pt-br', 'Sum Value In', 'Soma dos Valores de Entrada');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (62, 'pt-br', 'Sum Value Out', 'Soma dos Valores de Saída');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (63, 'pt-br', 'Sum Value Stock', 'Soma dos Valores em Estoque');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (64, 'pt-br', 'Margin Sale', 'Margem de Venda');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (65, 'pt-br', 'Margin Wholesale', 'Margem de Revenda');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (66, 'pt-br', 'Estimed Value', 'Valor Estimado');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (67, 'pt-br', 'Value Wholesale', 'Valor Revenda');
INSERT INTO public.crud_translation (id, locale, name, translation) VALUES (68, 'pt-br', 'Stock Serials', 'Seriais em Estoque');


--
-- Name: crud_translation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: development
--

SELECT pg_catalog.setval('public.crud_translation_id_seq', 68, true);


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: iso8583_router_comm; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.iso8583_router_comm (session, name, enabled, listen, ip, port, permanent, size_ascii, adapter, backlog, direction, endian_type, max_opened_connections, message_adapter) VALUES (NULL, 'POS', true, true, 'localhost', 2001, true, false, 'org.domain.financial.messages.comm.CommAdapterPayload', 50, 0, 1, 1, 'iso8583default');
INSERT INTO public.iso8583_router_comm (session, name, enabled, listen, ip, port, permanent, size_ascii, adapter, backlog, direction, endian_type, max_opened_connections, message_adapter) VALUES (NULL, 'TEF', true, true, 'localhost', 2002, true, true, 'org.domain.financial.messages.comm.CommAdapterSizePayload', 50, 0, 1, 1, 'iso8583default');
INSERT INTO public.iso8583_router_comm (session, name, enabled, listen, ip, port, permanent, size_ascii, adapter, backlog, direction, endian_type, max_opened_connections, message_adapter) VALUES (NULL, 'MASTERCARD', true, false, 'localhost', 3001, true, true, 'org.domain.financial.messages.comm.CommAdapterSizePayload', 50, 2, 1, 1, 'iso8583default');


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: iso8583_router_message_adapter; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.iso8583_router_message_adapter (name, parent, adapter_class) VALUES ('iso8583default', NULL, 'org.domain.financial.messages.MessageAdapterISO8583');


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: iso8583_router_message_adapter_item; Type: TABLE DATA; Schema: public; Owner: development
--

INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 1, 'XXXX_XXXXXX', '000', 'msgType', 1, NULL, 1, 0, 4, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 2, '0000_XXXXXX', '001', NULL, 1, NULL, 1, 0, 1, 0);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 3, 'XXXX_XXXXXX', '002', 'pan', 0, NULL, 17, 2, 19, 32);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 4, 'XXXX_XXXXXX', '003', 'codeProcess', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 5, 'XXXX_XXXXXX', '004', 'transactionValue', 1, NULL, 1, 0, 12, 12);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 6, 'XXXX_XXXXXX', '005', NULL, 1, NULL, 1, 0, 12, 12);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 7, '0000_XXXXXX', '006', NULL, 1, NULL, 1, 0, 12, 12);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 8, 'XXXX_XXXXXX', '007', 'dateTimeGmt', 1, NULL, 1, 0, 10, 10);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 9, 'XXXX_XXXXXX', '011', 'captureNsu', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 10, 'XXXX_XXXXXX', '012', 'hourLocal', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 11, 'XXXX_XXXXXX', '013', 'dateLocal', 1, NULL, 1, 0, 4, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 12, 'XX00_XXXXXX', '014', 'cardExpiration', 1, NULL, 1, 0, 4, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 13, 'XXXX_XXXXXX', '015', 'financialDate', 1, NULL, 1, 0, 4, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 14, 'XXXX_XXXXXX', '018', 'merchantType', 1, NULL, 15, 0, 4, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 15, 'XXXX_XXXXXX', '022', 'captureType', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 16, 'XX00_XXXXXX', '023', 'emvPanSequence', 1, NULL, 17, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 17, '0420_XXXXXX', '023', 'emvPanSequence', 1, NULL, 17, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 18, 'XX00_XXXXXX', '024', 'nii', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 19, '0420_XXXXXX', '024', 'nii', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 20, '0802_XXXXXX', '024', 'nii', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 21, '0000_XXXXXX', '025', NULL, 1, NULL, 1, 0, 2, 2);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 22, 'XXXX_XXXXXX', '026', NULL, 1, NULL, 1, 0, 1, 1);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 23, 'X210_XXXXXX', '027', 'replyEspected', 1, NULL, 1, 0, 1, 1);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 24, 'X910_XXXXXX', '027', 'replyEspected', 1, NULL, 1, 0, 1, 1);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 25, 'XX00_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 26, 'XX10_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 27, 'XX02_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 28, '0420_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 29, '0430_XXXXXX', '028', 'lastOkDate', 1, NULL, 1, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 30, 'XXXX_XXXXXX', '032', 'providerId', 1, NULL, 1, 2, 11, 15);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 31, 'XXXX_XXXXXX', '033', 'channelConn', 1, NULL, 1, 2, 2, 4);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 32, 'XX00_XXXXXX', '035', 'trackIi', 0, NULL, 15, 2, 1, 80);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 33, '0420_XXXXXX', '035', 'trackIi', 0, NULL, 15, 2, 1, 80);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 34, 'XXXX_XXXXXX', '037', NULL, 0, NULL, 15, 0, 12, 12);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 35, 'XXXX_XXXXXX', '038', NULL, 0, NULL, 7, 0, 6, 6);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 36, 'XXXX_XXXXXX', '039', 'codeResponse', 1, NULL, 7, 0, 2, 2);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 37, 'XX00_XXXXXX', '040', 'messageVersion', 1, NULL, 7, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 38, '0420_XXXXXX', '040', 'messageVersion', 0, NULL, 7, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 39, 'XX10_XXXXXX', '040', NULL, 0, NULL, 7, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 40, 'XX02_XXXXXX', '040', NULL, 0, NULL, 7, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 41, 'XXXX_XXXXXX', '041', 'equipamentId', 0, NULL, 15, 0, 8, 8);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 42, 'XXXX_XXXXXX', '042', 'captureEc', 0, NULL, 15, 0, 15, 15);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 43, 'XXXX_XXXXXX', '043', 'terminalSerialNumber', 0, NULL, 15, 2, 1, 99);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 44, 'XXXX_XXXXXX', '044', NULL, 0, NULL, 15, 2, 1, 99);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 45, 'XX00_XXXXXX', '045', 'trackI', 0, NULL, 15, 2, 1, 80);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 46, '0420_XXXXXX', '045', 'trackI', 0, NULL, 15, 2, 1, 80);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 47, '0000_XXXXXX', '047', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 48, 'XXXX_XXXXXX', '048', 'captureTablesVersionsIn', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 49, 'XX10_XXXXXX', '048', 'captureTablesVersionsOut', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 50, 'XX30_XXXXXX', '048', 'captureTablesVersionsOut', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 51, 'XXXX_XXXXXX', '049', 'countryCode', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 52, 'XX00_XXXXXX', '052', 'password', 0, NULL, 19, 0, 16, 16);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 53, 'X810_XXXXXX', '052', 'password', 0, NULL, 19, 0, 16, 16);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 54, 'XX00_XXXXXX', '053', 'passwordSize', 1, NULL, 1, 0, 16, 16);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 55, 'XX00_XXXXXX', '054', NULL, 1, NULL, 1, 3, 1, 255);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 56, 'XX00_XXXXXX', '055', 'emvData', 0, NULL, 3, 3, 1, 255);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 57, 'XX10_XXXXXX', '055', 'emvData', 0, NULL, 3, 3, 1, 255);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 58, 'XXXX_XXXXXX', '057', NULL, 0, NULL, 15, 0, 1, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 59, 'XX00_XXXXXX', '057', 'sequenceIndex', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 60, 'XX10_XXXXXX', '057', 'sequenceIndex', 1, NULL, 1, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 61, 'XXXX_XXXXXX', '058', NULL, 0, NULL, 15, 3, 8, 8);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 62, 'XX00_XXXXXX', '059', 'transportData', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 63, 'XXXX_XXXXXX', '060', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 64, 'XX00_XXXXXX', '061', 'captureEquipamentType', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 65, 'XX10_XXXXXX', '061', 'captureEquipamentType', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 66, 'XX02_XXXXXX', '061', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 67, 'XX00_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 68, 'XX10_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 69, 'XX20_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 70, 'XX30_XXXXXX', '062', 'data', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 71, 'XX00_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 72, 'XX20_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 73, 'XX10_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 74, 'XX30_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 75, 'XX02_XXXXXX', '063', 'dataComplement', 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 76, 'XXXX_XXXXXX', '067', 'numPayments', 1, NULL, 1, 0, 2, 2);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 77, 'XX00_XXXXXX', '070', NULL, 0, NULL, 15, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 78, 'XX10_XXXXXX', '070', NULL, 0, NULL, 15, 0, 3, 3);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 79, 'XXXX_XXXXXX', '071', 'captureProtocol', 0, NULL, 15, 0, 8, 8);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 80, '0000_XXXXXX', '086', NULL, 0, NULL, 15, 0, 1, 16);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 81, 'XXXX_XXXXXX', '090', 'transactionReference', 0, NULL, 15, 0, 42, 42);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 82, 'XXXX_XXXXXX', '100', NULL, 1, NULL, 1, 2, 11, 11);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 83, '0000_XXXXXX', '118', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 84, 'XXXX_XXXXXX', '120', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 85, '0000_XXXXXX', '121', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 86, 'XXXX_XXXXXX', '123', NULL, 0, NULL, 15, 3, 1, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 87, '0000_XXXXXX', '124', NULL, 0, NULL, 15, 3, 1, 99);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 88, 'XXXX_XXXXXX', '125', 'lastOkNsu', 1, NULL, 1, 3, 9, 999);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 89, 'XXXX_XXXXXX', '127', 'authNsu', 0, NULL, 15, 3, 6, 10);
INSERT INTO public.iso8583_router_message_adapter_item (message_adapter, order_index, root_pattern, tag, field_name, alignment, data_format, data_type, size_header, min_length, max_length) VALUES ('iso8583default', 90, 'XXXX_XXXXXX', '128', NULL, 0, NULL, 15, 3, 1, 999);


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: iso8583_router_log; Type: TABLE DATA; Schema: public; Owner: development
--



--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11rc1 (Debian 11~rc1-1)
-- Dumped by pg_dump version 11rc1 (Debian 11~rc1-1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: iso8583_router_transaction; Type: TABLE DATA; Schema: public; Owner: development
--



--
-- Name: iso8583_router_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: development
--

SELECT pg_catalog.setval('public.iso8583_router_transaction_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

