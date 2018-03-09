# angular-es6-crud-nodejs-j2ee7

CRUD Web Application with Angular + ES6 + Bootstrap CSS in browser and NodeJs or J2EE7 server based

You need NodeJs or WildFly installed and PostgreSql server.

Requires browser with support to dynamic ES6 modules (tested with Chrome versions >= 64)

## First Step

Clone this repository and open terminal, changing path to local repository folder.

## PostgreSql setup

execute psql terminal :

`$sudo su postgres -c psql`

in psql terminal execute configurations commands :

`CREATE USER development LOGIN PASSWORD '123456';`
`CREATE DATABASE crud WITH OWNER development;`

exit psql terminal and import default configuration data with command :

`psql -U development -h localhost crud < first_run.sql;`

## NodeJs based server

Requires NodeJs version >= 9.1

### Build

then `npm install` to download the required dependencies.

### Generate Node HTTPS related private key and self-signed certificate

openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

### Run server application

execute :

`nodejs --inspect --experimental-modules --loader ./custom-loader.mjs app.js`

## J2EE7 based server

### WildFly PostgreSql driver and datasource configuration

download or install PostgreSql JDBC driver.

set JBOSS_HOME with your WildFly installation, example :

`export JBOSS_HOME=~/wildfly-12.0.0.Final`

start WildFly server :

`sudo $JBOSS_HOME/bin/standalone.sh -b=0.0.0.0 -Djboss.https.port=8443 &`

execute WildFly terminal :

`$JBOSS_HOME/bin/jboss-cli.sh -c`

in WildFly terminal execute configurations commands :

`module add --name=org.postgresql --resources=/usr/share/java/postgresql.jar --dependencies=javax.api,javax.transaction.api`

`/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)`

`/subsystem=ee:list-add(name=global-modules,value={name=org.postgresql,slot=main})`

`data-source add --jndi-name=java:/datasources/crud --name=crud --connection-url=jdbc:postgresql://127.0.0.1:5432/crud --driver-name=postgresql --user-name=development --password=123456`

close WildFly terminal.

### WildFly build and deploy

Clone this repository and then `mvn clean wildfly:deploy` to build and deploy.

## Web application

in ES6 compliance browser open url `https://localhost:9443/crud` for nodejs server and `https://localhost:8443/angular-es6-crud-nodejs-j2ee7` for WildFly server.

