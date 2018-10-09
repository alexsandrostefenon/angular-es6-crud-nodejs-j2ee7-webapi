INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('bacenCountry', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos de Países', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"namePt":{"type":"s","required":true},"abr":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeUf', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos dos Estados', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"},"name":{"type":"s","required":true},"abr":{"type":"s","required":true},"ddd":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeCity', 'person', NULL, true, 'id,name', NULL, NULL, 'Códigos das Cidades', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"},"name":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('ibgeCnae', 'person', NULL, true, 'id,name', NULL, NULL, 'Classificação Nacional de Atividades Econômicas', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('person', 'person', NULL, true, 'id,name', NULL, NULL, 'Cadastros de Clientes e Fornecedores', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true},"fantasy":{"type":"s"},"cnpjCpf":{"type":"s"},"ieRg":{"type":"s"},"suframa":{"type":"s"},"im":{"type":"s"},"cnae":{"type":"i","service":"ibgeCnae"},"crt":{"type":"i","defaultValue":"1","options":"1 - Simples Nacional,2 - Simples Nacional (excesso sublimite de receita bruta),3 - Regime Normal","required":false},"zip":{"type":"s"},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"},"city":{"type":"i","service":"ibgeCity","defaultValue":"4304606"},"district":{"type":"s"},"address":{"type":"s"},"addressNumber":{"type":"s","required":false},"complement":{"type":"s"},"email":{"type":"s"},"site":{"type":"s"},"phone":{"type":"s"},"fax":{"type":"s"},"credit":{"type":"n3","defaultValue":"0"},"additionalData":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('camexNcm', 'product', NULL, true, 'id,name', NULL, NULL, 'NCM', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"unit":{"type":"s","required":true},"tec":{"type":"i","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('confazCest', 'product', NULL, true, 'id,name', NULL, NULL, 'Código Especificador da Substituição Tributária', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"ncm":{"type":"i","required":true,"service":"productNcm"},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('product', 'product', NULL, true, 'id,name,model', NULL, NULL, 'Produtos, Peças e Componentes', '{"id":{"type":"i","primaryKey":true,"hiden":true},"category":{"type":"i","service":"category"},"ncm":{"type":"i","service":"camexNcm"},"orig":{"type":"i","defaultValue":"0","options":"0,3,4,5,8","required":true},"name":{"type":"s"},"departament":{"type":"s","hiden":true},"model":{"type":"s"},"description":{"type":"s"},"weight":{"type":"n3"},"imageUrl":{"type":"s","hiden":true},"additionalData":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('barcode', 'product', NULL, true, 'barcode,product,manufacturer', NULL, NULL, 'Código de Barras de fornecedores de produtos', '{"number":{"type":"s","primaryKey":true,"required":true},"manufacturer":{"type":"s","required":true},"product":{"type":"i","service":"product","title":"Código de Barras de fornecedores de produtos","isClonable":false}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestType', 'request', NULL, true, 'id,name', NULL, NULL, 'Tipo de Requisição', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestState', 'request', NULL, true, 'id,type,name,stockAction', NULL, NULL, 'Situação da Requisição', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true},"next":{"type":"i","service":"requestState","defaultValue":"0"},"prev":{"type":"i","service":"requestState","defaultValue":"0"},"stockAction":{"type":"i","service":"stockAction"},"type":{"type":"i","service":"requestType","defaultValue":"1"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('request', 'request', NULL, false, 'id,person,date', 'date desc,id desc', NULL, 'Requisições de Entrada e Saída', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","required":true,"service":"requestType","readOnly":true,"hiden":false},"state":{"type":"i","required":true,"service":"requestState"},"person":{"type":"i","required":true,"service":"person"},"date":{"type":"datetime-local","required":true},"additionalData":{"type":"s"},"productsValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"servicesValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"transportValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"sumValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"paymentsValue":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeCfop', 'requestProduct', NULL, true, 'id,name', NULL, NULL, 'CFOP', '{"id":{"type":"i","primaryKey":true,"required":true,"hiden":false},"name":{"type":"s","required":true},"indNfe":{"type":"i"},"indComunica":{"type":"i"},"indTransp":{"type":"i"},"indDevol":{"type":"i"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('nfeTaxGroup', 'requestProduct', NULL, true, 'id,name', NULL, NULL, 'Grupo Tributário', '{"id":{"type":"i","primaryKey":true,"hiden":true,"required":false},"name":{"type":"s","required":true},"cstIpi":{"type":"i"},"cstIcms":{"type":"i"},"cstPis":{"type":"i"},"cstCofins":{"type":"i"},"taxSimples":{"type":"n2"},"taxIpi":{"type":"n3"},"taxIcms":{"type":"n2"},"taxPis":{"type":"n2"},"taxCofins":{"type":"n2"},"taxIssqn":{"type":"n2"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('stockAction', 'requestProduct', NULL, true, 'id,name', NULL, NULL, 'Ação sobre o Estoque', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('stock', 'requestProduct', NULL, true, 'id', NULL, NULL, 'Estoque de Produtos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"service":"product","required":true,"hiden":false},"countIn":{"type":"n"},"countOut":{"type":"n"},"estimedIn":{"type":"n3"},"estimedOut":{"type":"n"},"estimedValue":{"type":"n"},"marginSale":{"type":"n"},"marginWholesale":{"type":"n"},"reservedIn":{"type":"n"},"reservedOut":{"type":"n"},"stockValue":{"type":"n3"},"stockDefault":{"type":"n"},"stockMinimal":{"type":"n"},"stockSerials":{"type":"s"},"sumValueIn":{"type":"n"},"sumValueOut":{"type":"n"},"sumValueStock":{"type":"n"},"value":{"type":"n"},"valueWholesale":{"type":"n"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('paymentType', 'requestPayment', NULL, true, 'id,name', NULL, NULL, 'Tipo de Pagamento', '{"id":{"type":"i","primaryKey":true,"hiden":true},"description":{"type":"s"},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('account', 'requestPayment', NULL, true, 'id,description', NULL, NULL, 'Contas Bancárias', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"number":{"type":"s"},"agency":{"type":"s"},"bank":{"type":"s"},"description":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestProduct', 'report', NULL, true, 'id,product,serial,quantity,value', NULL, NULL, 'Entrada e Saída de Produtos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"product":{"type":"i","required":true,"service":"product"},"quantity":{"type":"n3","defaultValue":"1.000","required":true},"value":{"type":"n3","defaultValue":"0.0","required":true},"valueItem":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true},"valueDesc":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"valueFreight":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"cfop":{"type":"i","service":"nfeCfop"},"tax":{"type":"i","service":"nfeTaxGroup"},"valueAllTax":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true},"serials":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestFreight', 'report', NULL, true, 'id,person,request,payBy,value', NULL, NULL, 'Frete', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"person":{"type":"i","service":"person"},"payBy":{"type":"i","options":"0,1,2,9","required":true},"licensePlate":{"type":"s","hiden":false},"licensePlateUf":{"type":"i","service":"ibgeUf","hiden":false},"containersType":{"type":"s","hiden":false},"containersCount":{"type":"i"},"weight":{"type":"n3","hiden":false},"weightFinal":{"type":"n3","hiden":false},"logo":{"type":"s","hiden":false},"value":{"type":"n2"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestPayment', 'report', NULL, true, 'id,type,account,number', 'due_date,id', NULL, 'Pagamentos', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","required":true,"service":"paymentType"},"value":{"type":"n2","required":true},"account":{"type":"i","required":true,"service":"account"},"number":{"type":"s"},"dueDate":{"type":"datetime-local","defaultValue":"now","required":true},"payday":{"type":"datetime-local"},"balance":{"type":"n2","readOnly":true,"required":false}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('requestNfe', 'report', NULL, true, 'id,request', NULL, NULL, 'Nota Fiscal Eletrônica', '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"request":{"type":"i","primaryKey":true,"service":"request","required":true},"person":{"type":"i"},"versao":{"type":"s"},"nfeId":{"type":"s"},"natop":{"type":"s","defaultValue":"Venda","required":true},"indpag":{"type":"i","defaultValue":"0","options":"0,1,2","required":true},"mod":{"type":"i"},"serie":{"type":"i"},"nnf":{"type":"i"},"dhemi":{"type":"datetime-local","defaultValue":"now"},"dhsaient":{"type":"datetime-local","defaultValue":"now"},"tpnf":{"type":"i"},"iddest":{"type":"i","defaultValue":"1","options":"1,2,3","required":true},"tpimp":{"type":"i"},"tpemis":{"type":"i"},"cdv":{"type":"i","hiden":true},"tpamb":{"type":"i"},"finnfe":{"type":"i"},"indfinal":{"type":"i","defaultValue":"1","options":"0,1","required":true},"indpres":{"type":"i","defaultValue":"1","options":"0,1,2,3,4,9","required":true},"procemi":{"type":"i"},"verproc":{"type":"s"},"indiedest":{"type":"i"},"valueIi":{"type":"n2"},"valueIpi":{"type":"n2"},"valuePis":{"type":"n2"},"valueCofins":{"type":"n2"},"valueIcms":{"type":"n2"},"valueIcmsSt":{"type":"n2"},"valueIssqn":{"type":"n2"},"valueTax":{"type":"n2"}}');

INSERT INTO crud_company (id, name) VALUES (2, 'USO DOMÉSTICO');

INSERT INTO category (id, name) VALUES (1, 'mercado');

INSERT INTO category_company (company, category) VALUES (2, 1);

INSERT INTO crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (2, '', '', '{"buy":{"menu":"actions","label":"Compra","path":"request/new?type=1&state=10"},"requestPayment":{"menu":"form","label":"Financeiro","path":"request_payment/search"},"stock":{"menu":"form","label":"Estoque","path":"stock/search"},"product":{"menu":"form","label":"Produtos","path":"product/search"},"person":{"menu":"form","label":"Clientes e Fornecedores","path":"person/search"},"requests":{"menu":"form","label":"Requisições","path":"request/search"},"account":{"menu":"form","label":"Contas","path":"account/search"}}', 'spending', '123456', 'request/search', '{"crudCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudUser":{"read":true,"query":true,"create":false,"update":false,"delete":false},"category":{"read":true,"query":true,"create":false,"update":false,"delete":false},"categoryCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"account":{"read":true,"query":true,"create":true,"update":false,"delete":false},"requestType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"requestState":{"read":true,"query":true,"create":false,"update":false,"delete":false},"stockAction":{"read":true,"query":true,"create":false,"update":false,"delete":false},"paymentType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"person":{"read":true,"query":true,"create":true,"update":true,"delete":false},"product":{"read":true,"query":true,"create":true,"update":true,"delete":false},"barcode":{"read":true,"query":true,"create":true,"update":true,"delete":false},"request":{"read":true,"query":true,"create":true,"update":true,"delete":false},"requestProduct":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestPayment":{"read":true,"query":true,"create":true,"update":true,"delete":true},"stock":{"read":true,"query":true,"create":true,"update":true,"delete":false},"nfeCfop":{"read":true,"query":true,"create":false,"update":false,"delete":false},"bacenCountry":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeUf":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCity":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCnae":{"read":true,"query":true,"create":false,"update":false,"delete":false},"nfeTaxGroup":{"read":true,"query":true,"create":false,"update":false,"delete":false},"camexNcm":{"read":true,"query":true,"create":false,"update":false,"delete":false}}', false, NULL, '[{"path":"/app/request/:action","controller":"erp/RequestController"}]', '[{"path": "/app/request/:action", "controller": "erp/RequestController"}]');

INSERT INTO request_type (id, description, name) VALUES (1, NULL, 'Compra');
INSERT INTO request_type (id, description, name) VALUES (2, NULL, 'Venda');

INSERT INTO stock_action (id, name) VALUES (1, 'countIn');
INSERT INTO stock_action (id, name) VALUES (2, 'countOut');
INSERT INTO stock_action (id, name) VALUES (4, 'reservedIn');
INSERT INTO stock_action (id, name) VALUES (8, 'reservedOut');
INSERT INTO stock_action (id, name) VALUES (16, 'estimedIn');
INSERT INTO stock_action (id, name) VALUES (32, 'estimedOut');

INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES
(10, NULL, 'Solicitar Orçamento', NULL, NULL, 16, 1),
(20, NULL, 'Aguardando Resposta', NULL, 10, 16, 1),
(30, NULL, 'Recusado, orçar em outro fornecedor', NULL, 20, NULL, 1),
(40, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 30, NULL, 1),
(50, NULL, 'Aprovado, enviar resposta', NULL, 10, 4, 1),
(60, NULL, 'Aprovado, aguardando entrega', 80, 50, 4, 1),
(70, NULL, 'Aprovado, aguardando retirada', 80, 50, 4, 1),
(80, NULL, 'Aprovado e Concluído', NULL, NULL, 1, 1),

(210, NULL, 'Montar Orçamento', NULL, NULL, NULL, 2),
(220, NULL, 'Montando Orçamento', NULL, 210, NULL, 2),
(230, NULL, 'Enviar Orçamento', NULL, 220, NULL, 2),
(240, NULL, 'Aguardando Resposta', NULL, 230, NULL, 2),
(250, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 240, NULL, 2),
(260, NULL, 'Aprovado, dar andamento', NULL, 240, 8, 2),
(270, NULL, 'Aprovado, aguardando peças', NULL, 260, 8, 2),
(280, NULL, 'Aprovado, efetuar serviços', NULL, 260, 8, 2),
(290, NULL, 'Aprovado, efetuando serviços', NULL, 280, 8, 2),
(300, NULL, 'Aprovado, aguardando entrega', 320, 290, 8, 2),
(310, NULL, 'Aprovado, aguardando retirada', 320, 290, 8, 2),
(320, NULL, 'Aprovado e Concluído', NULL, NULL, 2, 2);

INSERT INTO crud_translation (locale, name, translation) VALUES
('pt-br', 'Date', 'Data'),
('pt-br', 'Unit', 'Unidade'),
('pt-br', 'Quantity', 'Quantidade'),
('pt-br', 'Value', 'Valor'),
('pt-br', 'Type', 'Tipo'),
('pt-br', 'State', 'Situação'),
('pt-br', 'Person', 'Cliente/Fornecedor'),
('pt-br', 'Additional Data', 'Dados Adicionais'),
('pt-br', 'Products Value', 'Valor Produtos'),
('pt-br', 'Services Value', 'Valor Serviços'),
('pt-br', 'Transport Value', 'Valor Transporte'),
('pt-br', 'Sum Value', 'Valor Total'),
('pt-br', 'Payments Value', 'Valor Faturas'),
('pt-br', 'Product', 'Produto'),
('pt-br', 'Serial', 'N. Série'),
('pt-br', 'Defect', 'Defeito'),
('pt-br', 'Barcode', 'Código de Barras'),
('pt-br', 'Manufacturer', 'Fabricante'),
('pt-br', 'Model', 'Modelo'),
('pt-br', 'Weight', 'Peso'),
('pt-br', 'Phone', 'Telefone'),
('pt-br', 'Zip', 'CEP'),
('pt-br', 'City', 'Cidade'),
('pt-br', 'District', 'Bairro'),
('pt-br', 'Address', 'Endereço'),
('pt-br', 'Credit', 'Credito'),
('pt-br', 'Number', 'Numero'),
('pt-br', 'Number Form', 'Numero do Formulário'),
('pt-br', 'Date Fiscal', 'Data Fiscal'),
('pt-br', 'Request', 'Requisição'),
('pt-br', 'Account', 'Conta'),
('pt-br', 'Due Date', 'Data Vencimento'),
('pt-br', 'Payday', 'Data do Pagamento'),
('pt-br', 'Pay By', 'Pago Por'),
('pt-br', 'Bank', 'Banco'),
('pt-br', 'Agency', 'Agência'),
('pt-br', 'Stock', 'Estoque'),
('pt-br', 'Count In', 'Quantidade Entrada'),
('pt-br', 'Count Out', 'Quantidade Saída'),
('pt-br', 'Stock Default', 'Estoque Ideal'),
('pt-br', 'Stock Minimal', 'Estoque Mínimo'),
('pt-br', 'Reserved Out', 'Saída Reservada'),
('pt-br', 'Reserved In', 'Entrada Reservada'),
('pt-br', 'Estimed Out', 'Estimativa de Saída'),
('pt-br', 'Sum Value In', 'Soma dos Valores de Entrada'),
('pt-br', 'Sum Value Out', 'Soma dos Valores de Saída'),
('pt-br', 'Sum Value Stock', 'Soma dos Valores em Estoque'),
('pt-br', 'Margin Sale', 'Margem de Venda'),
('pt-br', 'Margin Wholesale', 'Margem de Revenda'),
('pt-br', 'Estimed Value', 'Valor Estimado'),
('pt-br', 'Value Wholesale', 'Valor Revenda'),
('pt-br', 'Stock Serials', 'Seriais em Estoque');
