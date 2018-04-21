INSERT INTO crud_service (is_on_line, menu, name, save_and_exit, template, title, filter_fields, fields, order_by) VALUES
(NULL, 'admin', 'crudService', NULL, NULL, NULL, 'id,name', '{"id":{"type":"i","hiden":true,"primaryKey":true},"menu":{},"name":{},"template":{},"title":{},"filterFields":{},"isOnLine":{"type":"b"},"fields":{"readOnly":true},"orderBy":{},"saveAndExit":{"type":"b"}}', NULL),
(NULL, 'admin', 'crudCompany', NULL, NULL, NULL, 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"required":true}}', NULL),
(NULL, 'admin', 'crudUser', false, NULL, NULL, 'id,name,company', '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"service":"crudCompany"},"name":{},"path":{},"password":{"type":"p"},"roles":{},"menu":{},"routes":{},"showSystemMenu":{"type":"b","defaultValue":"false"},"authctoken":{},"ip":{}}', NULL),
(NULL, 'admin', 'crudTranslation', true, NULL, NULL, 'id,name,locale,translation', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"translation":{},"locale":{"defaultValue":"pt-br"}}', NULL),
(NULL, 'admin', 'category', false, NULL, 'Controle de Categorias de Produtos e Serviços', 'id,name', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}', NULL),
(NULL, 'admin', 'categoryCompany', true, NULL, 'Categorias de cada Empresa', 'id,category,company', '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"primaryKey":true,"service":"crudCompany","required":true,"title":"Categorias Vinculadas","isClonable":true},"category":{"service":"category","required":true}}', NULL)
;

INSERT INTO crud_company (id, name) VALUES
(1, 'ADMIN')
;


INSERT INTO crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES
(1, '', '', NULL, 'admin', 'admin', '/app/crud_service/search', '{"crudService":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudUser":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudTranslation":{"read":true,"query":true,"create":true,"update":true,"delete":true},"category":{"read":true,"query":true,"create":true,"update":true,"delete":true},"categoryCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]', '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]')
;

INSERT INTO crud_translation (locale, name, translation) VALUES
('pt-br', 'User', 'Usuário'),
('pt-br', 'Exit', 'Sair'),
('pt-br', 'New', 'Novo'),
('pt-br', 'Filter', 'Filtrar'),
('pt-br', 'Search', 'Localizar'),
('pt-br', 'View', 'Visualizar'),
('pt-br', 'Edit', 'Editar'),
('pt-br', 'Delete', 'Apagar'),
('pt-br', 'Actions', 'Ações'),
('pt-br', 'Cancel', 'Cancelar'),
('pt-br', 'Create', 'Criar'),
('pt-br', 'Save', 'Salvar'),
('pt-br', 'Save as New', 'Salvar como Novo'),
('pt-br', 'Name', 'Nome'),
('pt-br', 'Description', 'Descrição'),
('pt-br', 'Category', 'Categoria')
;

