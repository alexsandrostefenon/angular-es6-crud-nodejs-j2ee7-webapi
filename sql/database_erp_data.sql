INSERT INTO crud_service (is_on_line, menu, name, save_and_exit, template, title, filter_fields, fields, order_by) VALUES
(NULL, 'person', 'bacenCountry', true, NULL, 'Códigos de Países', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false},"name":{"required":true}}', NULL),
(NULL, 'person', 'ibgeUf', true, NULL, 'Códigos dos Estados', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false},"name":{},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"}}', NULL),
(NULL, 'person', 'ibgeCity', true, NULL, 'Códigos das Cidades', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false},"name":{},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"}}', NULL),
(NULL, 'person', 'ibgeCnae', true, NULL, 'Classificação Nacional de Atividades Econômicas', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false},"name":{"required":true}}', NULL),
(NULL, 'person', 'person', true, NULL, 'Cadastros de Clientes e Fornecedores', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"hiden":true},"name":{},"phone":{},"cnpjCpf":{},"ieRg":{},"zip":{},"country":{"type":"i","service":"bacenCountry","defaultValue":"1058"},"uf":{"type":"i","service":"ibgeUf","defaultValue":"43"},"city":{"type":"i","service":"ibgeCity","defaultValue":"4304606"},"district":{},"address":{},"addressNumber":{"type":"s","required":true},"fax":{},"email":{},"site":{},"additionalData":{},"credit":{"type":"n3","defaultValue":"0"},"cnae":{"type":"i","service":"ibgeCnae"},"crt":{"type":"i","options":"1,2,3","required":false}}', NULL),
(NULL, 'product', 'camexNcm', true, NULL, 'NCM', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false,"required":true},"name":{"required":true},"unit":{"required":true},"tec":{"type":"i","required":true}}', NULL),
(NULL, 'product', 'confazCest', true, NULL, 'Código Especificador da Substituição Tributária', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false,"required":true},"ncm":{"type":"i","service":"productNcm","required":true},"name":{"required":true}}', NULL),
(NULL, 'product', 'product', true, NULL, 'Produtos, Peças e Componentes', 'id,name,model', '{"id":{"type":"i","primaryKey":true,"hiden":true},"category":{"service":"category"},"name":{},"model":{},"description":{},"additionalData":{},"departament":{"hiden":true},"imageUrl":{"hiden":true},"weight":{"type":"n3"},"ncm":{"type":"i","service":"camexNcm"},"orig":{"type":"i","defaultValue":"0","options":"0,3,4,5,8","required":true}}', NULL),
(NULL, 'product', 'barcode', true, NULL, 'Código de Barras de fornecedores de produtos', 'barcode,product,manufacturer', '{"barcode":{"type":"s","hiden":false,"required":true,"primaryKey":true},"product":{"type":"i","service":"product","title":"Código de Barras de fornecedores de produtos","isClonable":false},"manufacturer":{"type":"s","required":true}}', NULL),
(NULL, 'request', 'requestType', true, NULL, 'Tipo de Requisição', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"description":{}}', NULL),
(NULL, 'request', 'requestState', true, NULL, 'Situação da Requisição', 'id,type,name,stockAction', '{"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","service":"requestType","defaultValue":"1"},"name":{},"stockAction":{"service":"stockAction"},"prev":{"type":"i","service":"requestState","defaultValue":"0"},"next":{"type":"i","service":"requestState","defaultValue":"0"},"description":{}}', NULL),
(NULL, 'request', 'request', false, NULL, 'Requisições de Entrada e Saída', 'id,person,date', '{"id":{"type":"i","hiden":true,"primaryKey":true},"company":{"type":"i","hiden":true,"primaryKey":true},"date":{"type":"datetime-local","required":true},"person":{"type":"i","required":true,"service":"person"},"state":{"type":"i","required":true,"service":"requestState"},"type":{"type":"i","hiden":false,"required":true,"readOnly":true,"service":"requestType"},"additionalData":{},"productsValue":{"defaultValue":"0.0","readOnly":true},"servicesValue":{"defaultValue":"0.0","readOnly":true},"transportValue":{"defaultValue":"0.0","readOnly":true},"sumValue":{"defaultValue":"0.0","readOnly":true},"paymentsValue":{"defaultValue":"0.0","readOnly":true}}', 'date desc,id desc'),
(NULL, 'requestProduct', 'nfeCfop', true, NULL, 'CFOP', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":false,"required":true},"name":{"required":true}}', NULL),
(NULL, 'requestProduct', 'nfeTaxGroup', true, NULL, 'Grupo Tributário', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true,"required":false},"name":{"required":true},"cstIcms":{"type":"i"},"cstPis":{"type":"i"},"cstCofins":{"type":"i"},"taxSimples":{"type":"n2"},"taxIcms":{"type":"n2"},"taxPis":{"type":"n2"},"taxCofins":{"type":"n2"},"taxIssqn":{"type":"n2"}}', NULL),
(NULL, 'requestProduct', 'stockAction', true, NULL, 'Ação sobre o Estoque', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{}}', NULL),
(NULL, 'requestProduct', 'stock', true, NULL, 'Estoque de Produtos', 'id', '{"id":{"type":"i","hiden":false,"required":true,"primaryKey":true,"service":"product"},"company":{"type":"i","hiden":true,"primaryKey":true},"value":{"type":"n"},"stock":{"type":"n"},"stockDefault":{"type":"n"},"stockMinimal":{"type":"n"},"stockSerials":{},"sumValueStock":{"type":"n"},"reservedOut":{"type":"n"},"reservedIn":{"type":"n"},"estimedOut":{"type":"n"},"marginSale":{"type":"n"},"marginWholesale":{"type":"n"},"estimedValue":{"type":"n"},"valueWholesale":{"type":"n"},"countIn":{"type":"n"},"countOut":{"type":"n"},"sumValueIn":{"type":"n"},"sumValueOut":{"type":"n"}}', NULL),
(NULL, 'requestPayment', 'paymentType', true, NULL, 'Tipo de Pagamento', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"description":{}}', NULL),
(NULL, 'requestPayment', 'account', true, NULL, 'Contas Bancárias', 'id,description', '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"service":"crudCompany","hiden":true},"description":{},"bank":{},"agency":{},"account":{}}', NULL),
(NULL, 'report', 'requestProduct', true, NULL, 'Entrada e Saída de Produtos', 'id,product,serial,quantity,value', '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"hiden":true},"request":{"type":"i","service":"request"},"product":{"type":"i","service":"product","required":true},"serials":{},"quantity":{"type":"n3","defaultValue":"1.000","required":true},"value":{"type":"n3","defaultValue":"0.0","required":true},"valueItem":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true},"valueDesc":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"valueFreight":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":false},"cfop":{"type":"i","service":"nfeCfop"},"tax":{"type":"i","service":"nfeTaxGroup"},"valueAllTax":{"type":"n2","defaultValue":"0.0","required":true,"readOnly":true}}', NULL),
(NULL, 'report', 'requestFreight', true, NULL, 'Frete', 'id,person,request,payBy,value', '{"company":{"type":"i","primaryKey":true,"hiden":true},"request":{"type":"i","primaryKey":true,"service":"request"},"person":{"type":"i","service":"person"},"payBy":{"type":"i","options":"0,1,2,9","required":true},"weight":{"type":"n3","hiden":false},"weightFinal":{"type":"n3","hiden":false},"logo":{"hiden":false},"licensePlate":{"hiden":false},"licensePlateUf":{"type":"i","service":"ibgeUf","hiden":false},"containersType":{"hiden":false},"containersCount":{"type":"i"}}', NULL),
(NULL, 'report', 'requestPayment', true, NULL, 'Pagamentos', 'id,type,account,number', '{"id":{"type":"i","hiden":true,"primaryKey":true},"company":{"type":"i","hiden":true,"primaryKey":true},"request":{"type":"i","service":"request"},"type":{"type":"i","required":true,"service":"paymentType"},"account":{"type":"i","required":true,"service":"account"},"number":{},"value":{"type":"n2","required":true},"dueDate":{"type":"datetime-local","defaultValue":"now","required":true},"payday":{"type":"datetime-local"},"balance":{"type":"n2","required":false,"readOnly":true}}', 'due_date,id'),
(NULL, 'report', 'requestNfe', true, NULL, 'Nota Fiscal Eletrônica', 'id,request', '{"company":{"type":"i","primaryKey":true,"hiden":true},"request":{"type":"i","primaryKey":true},"nfeId":{},"natop":{"defaultValue":"Venda","required":true},"indpag":{"type":"i","defaultValue":"0","options":"0,1,2","required":true},"nnf":{"type":"i"},"dhemi":{"type":"datetime-local","defaultValue":"now"},"dhsaient":{"type":"datetime-local","defaultValue":"now"},"iddest":{"type":"i","defaultValue":"1","options":"1,2,3","required":true},"indfinal":{"type":"i","defaultValue":"1","options":"0,1","required":true},"indpres":{"type":"i","defaultValue":"1","options":"0,1,2,3,4,9","required":true},"cdv":{"type":"i","hiden":true}}', NULL);

INSERT INTO crud_company (id, name) VALUES (2, 'USO DOMÉSTICO');

INSERT INTO category (id, name) VALUES (1, 'mercado');

INSERT INTO category_company (company, category) VALUES (2, 1);

INSERT INTO crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (2, '', '', '{"buy":{"menu":"actions","label":"Compra","path":"request/new?type=1&state=10"},"requestPayment":{"menu":"form","label":"Financeiro","path":"request_payment/search"},"stock":{"menu":"form","label":"Estoque","path":"stock/search"},"product":{"menu":"form","label":"Produtos","path":"product/search"},"person":{"menu":"form","label":"Clientes e Fornecedores","path":"person/search"},"requests":{"menu":"form","label":"Requisições","path":"request/search"},"account":{"menu":"form","label":"Contas","path":"account/search"}}', 'spending', '123456', '/app/request/search', '{"crudCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudUser":{"read":true,"query":true,"create":false,"update":false,"delete":false},"category":{"read":true,"query":true,"create":false,"update":false,"delete":false},"categoryCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"account":{"read":true,"query":true,"create":true,"update":false,"delete":false},"requestType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"requestState":{"read":true,"query":true,"create":false,"update":false,"delete":false},"stockAction":{"read":true,"query":true,"create":false,"update":false,"delete":false},"paymentType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"person":{"read":true,"query":true,"create":true,"update":true,"delete":false},"product":{"read":true,"query":true,"create":true,"update":true,"delete":false},"barcode":{"read":true,"query":true,"create":true,"update":true,"delete":false},"request":{"read":true,"query":true,"create":true,"update":true,"delete":false},"requestProduct":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestPayment":{"read":true,"query":true,"create":true,"update":true,"delete":true},"stock":{"read":true,"query":true,"create":true,"update":true,"delete":false},"nfeCfop":{"read":true,"query":true,"create":false,"update":false,"delete":false},"bacenCountry":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeUf":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCity":{"read":true,"query":true,"create":false,"update":false,"delete":false},"ibgeCnae":{"read":true,"query":true,"create":false,"update":false,"delete":false},"nfeTaxGroup":{"read":true,"query":true,"create":false,"update":false,"delete":false},"camexNcm":{"read":true,"query":true,"create":false,"update":false,"delete":false}}', false, NULL, '[{"path":"/app/request/:action","controller":"RequestController"}]', '[{"path": "/app/request/:action", "controller": "RequestController"}]');

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

INSERT INTO account (company, id, account, agency, bank, description) VALUES (2, 1, NULL, NULL, NULL, 'Caixa');
INSERT INTO account (company, id, account, agency, bank, description) VALUES (2, 2, NULL, NULL, NULL, 'Conta Bancária Principal');

