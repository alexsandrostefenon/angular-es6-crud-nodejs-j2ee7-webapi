# angular-es6-crud-nodejs-j2ee7

CRUD Web Application with Angular + ES6 + Bootstrap CSS in browser and Nodejs or J2EE7 server based

You need NodeJs or WildFly installed, PostgreSql server with database named `crud` created.

## NodeJs based server

### Build

Clone this repository and then `npm install` to download the required dependencies.

### Generate Node HTTPS related private key and self-signed certificate

openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

### Run server application

execute :

`nodejs app.js`

## J2EE7 based server

### WildFly PostgreSql driver and datasource configuration

download or install PostgreSql JDBC driver.

set JBOSS_HOME with your WildFly installation, example :

`export JBOSS_HOME=~/wildfly-11.0.0.Final`

execute WildFly terminal :

`$JBOSS_HOME/bin/jboss-cli.sh -c`

in WildFly terminal execute configurations commands :

`module add --name=org.postgresql --resources=/usr/share/java/postgresql.jar --dependencies=javax.api,javax.transaction.api`

`/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)`

`/subsystem=ee:list-add(name=global-modules,value={name=org.postgresql,slot=main})`

`data-source add --jndi-name=java:/datasources/crud --name=crud --connection-url=jdbc:postgresql://127.0.0.1:5432/crud --driver-name=postgresql --user-name=development --password=123456`

### WildFly build and deploy

Clone this repository and then `mvn clean wildfly:deploy` to build and deploy.

## Web application

in ES6 compliance browser open url `https://localhost:9443/crud` for nodejs server and `https://localhost:8443/angular-es6-crud-nodejs-j2ee7` for WildFly server.

