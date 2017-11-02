# angular-es6-crud-nodejs-j2ee7

CRUD Web Application with Angular + ES6 + Bootstrap CSS in browser and Nodejs or J2EE7 server based

## Build

Clone this repo and then `npm install` to download the required dependencies.

## Generate Node https related private key and self-signed certificate

openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem


## WildFly datasource configuration

export JBOSS_HOME=~/wildfly-11.0.0.CR1

$JBOSS_HOME/bin/jboss-cli.sh -c

module add --name=org.postgresql --resources=/usr/share/java/postgresql.jar --dependencies=javax.api,javax.transaction.api

/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)

data-source add --jndi-name=java:/datasources/crud --name=crud --connection-url=jdbc:postgresql://127.0.0.1:5432/crud --driver-name=postgresql --user-name=development --password=123456

/subsystem=ee:list-add(name=global-modules,value={name=org.postgresql,slot=main})

## WildFly build and deploy

mvn clean wildfly:deploy
