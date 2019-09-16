# angular-es6-crud-nodejs-j2ee-aspnetcore

CRUD Web Application with Angular + ES6 + Bootstrap CSS in browser and NodeJs, J2EE or AspNetCore server based.

You need NodeJs or WildFly or mono/dotnet installed and PostgreSql server.

Requires browser with support to dynamic ES6 modules (tested with Chrome versions >= 64)

## First Step

Clone this repository and open terminal, changing path to local repository folder.

git clone https://github.com/alexsandrostefenon/angular-es6-crud-nodejs-j2ee7-webapi.git;

cd angular-es6-crud-nodejs-j2ee7-webapi

## PostgreSql setup

in terminal, create database :

sudo su postgres;
export PGDATABASE=postgres;
psql -c 'CREATE USER development WITH CREATEDB LOGIN PASSWORD '\''123456'\''';
psql -c 'CREATE DATABASE crud WITH OWNER development';
psql -c 'CREATE DATABASE crud_dev WITH OWNER development';
exit;

Note, database "crud_dev" is only for testing purposes.

import default configuration data with commands :

export PGHOST=localhost;
export PPORT=5432;
export PGPASSWORD=123456;
export PGUSER=development;
export PGDATABASE=crud;

psql < ./sql/crud/database_schema.sql;
psql < ./sql/crud/database_first_data.sql;

psql < ./sql/nfe/database_schema.sql;
psql < ./sql/nfe/database_first_data.sql;

psql < ./sql/erp/database_schema.sql;
psql < ./sql/erp/database_first_data.sql;

psql < ./sql/iso8583router/database_schema.sql;
psql < ./sql/iso8583router/database_first_data.sql;

## NodeJs based server

Requires NodeJs version >= 9.1

### Build

then `npm install` to download the required dependencies.

### Generate Node HTTPS related private key and self-signed certificate

openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

### Run server application

execute :

`nodejs --inspect --experimental-modules --loader ./src/main/es6/custom-loader.mjs ./src/main/es6/app.js --name=crud --modules="./crud/rest/CrudServiceEndPoint.js"`

## J2EE7 based server

### WildFly PostgreSql driver and datasource configuration

download or install PostgreSql JDBC driver.

set JBOSS_HOME with your WildFly installation, example :

`export JBOSS_HOME=~/wildfly-14.0.1.Final`

start WildFly server :

`$JBOSS_HOME/bin/standalone.sh -b=0.0.0.0 -Djboss.https.port=8443 --debug &`

execute WildFly terminal :

`$JBOSS_HOME/bin/jboss-cli.sh -c`

At terminal prompt ([standalone@localhost:9990 /]) execute one to one of configurations commands :

`module add --name=org.postgresql --resources=/usr/share/java/postgresql.jar --dependencies=javax.api,javax.transaction.api`

`/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)`

`/subsystem=ee:list-add(name=global-modules,value={name=org.postgresql,slot=main})`

`data-source add --jndi-name=java:/datasources/crud --name=crud --connection-url=jdbc:postgresql://127.0.0.1:5432/crud --driver-name=postgresql --user-name=development --password=123456`

`exit`

### WildFly build and deploy

in terminal then command `mvn -f pom-wildfly.xml clean wildfly:deploy` to build and deploy.

## ASP.NET CORE WebApi + EF CORE based server

Requires mono or dotnet

### Build

`nuget restore && msbuild /t:Clean && msbuild`

or

`dotnet build -f netcoreapp2.1`

### Run server application

Execute :

`mono ./bin/Debug/net471/AspNetCoreWebApi.exe`

or

`dotnet run --no-build -f netcoreapp2.1 --environment Development --urls "https://*:7443"`

## Web application

In ES6 compliance browser open url

`https://localhost:9443/crud` for nodejs server or
`https://localhost:8443/angular-es6-crud-nodejs-j2ee7` for WildFly server or
`http://localhost:5000/` for mono server or
`https://localhost:7443/` for dotnet server

For already configured services, use user 'spending' with password '123456'.

For custom service configuration or user edition, use user 'admin' with password 'admin'.

## Automated tests

npm install selenium-side-runner &&
./node_modules/.bin/selenium-side-runner ./src/test/*.side

python3 ./src/test/test.py "http://localhost:9080/crud"

