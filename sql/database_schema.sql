CREATE TABLE crud_service (
    id SERIAL PRIMARY KEY,
    is_on_line boolean,
    menu character varying(255),
    name character varying(255),
    save_and_exit boolean,
    template character varying(255),
    title character varying(255),
    filter_fields character varying(10240),
    fields character varying(10240),
    order_by character varying(512)
);

CREATE TABLE crud_company (
    id SERIAL PRIMARY KEY,
    name character varying(255) NOT NULL
);

CREATE TABLE crud_user (
    company integer references crud_company,
    id SERIAL,
    authctoken character varying(255),
    ip character varying(255),
    menu character varying(10240),
    name character varying(255),
    password character varying(255),
    path character varying(255),
    roles character varying(10240),
    show_system_menu boolean,
    config character varying(10240),
    routes character varying(10240),
    routes_jsonb jsonb,
    PRIMARY KEY(company,id)
);

CREATE TABLE crud_translation (
    id SERIAL PRIMARY KEY,
    locale character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    translation character varying(255)
);

CREATE TABLE category ( -- replace to camex_ncm
    id SERIAL PRIMARY KEY,
    name character varying(100) NOT NULL
);

CREATE TABLE category_company ( -- rename to cnae_ncm
    company integer references crud_company, -- replace to ibge_cnae
    id SERIAL,
    category integer references category, -- replace to camex_ncm
    PRIMARY KEY(company,id) -- replace to cnae,ncm
);

create sequence hibernate_sequence;
