# quarkus-data-reactive-and-nosql project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `quarkus-data-reactive-and-nosql-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/quarkus-data-reactive-and-nosql-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/quarkus-data-reactive-and-nosql-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Start docker for mysql and execute the script

```
docker run --name quarkus-db -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql
docker exec -it quarkus-db bash
mysql -uroot -proot
```

### Start docker for mongodb

```
docker run -it --name quarkus_mongo_db --rm -p 27017:27017 mongo
```

### Start docker for Neo4j

```
docker run --publish=7474:7474 --publish=7687:7687 -e NEO4J_AUTH=neo4j/neo4j neo4j:4.0.0
```
Or without authentication
```
docker run --publish=7474:7474 --publish=7687:7687 -e NEO4J_AUTH=none neo4j:4.0.0
```

### Start docker for Amazon DB

```
docker run --publish 8000:8000 amazon/dynamodb-local:1.11.477 -jar DynamoDBLocal.jar -inMemory -sharedDb
```

### Setup Amazon DB

Console
```
aws dynamodb create-table --table-name Books \
--attribute-definitions AttributeName=title,AttributeType=S \
--key-schema AttributeName=title,KeyType=HASH \
--provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1
```
Or in the shell (http://localhost:8000/shell) you can put the code below: 
```
var params = {
    TableName: 'Persons',
    KeySchema: [{ AttributeName: 'name', KeyType: 'HASH' }],
    AttributeDefinitions: [{ AttributeName: 'name', AttributeType: 'S', },],
    ProvisionedThroughput: { ReadCapacityUnits: 1, WriteCapacityUnits: 1, }
};

dynamodb.createTable(params, function(err, data) {
    if (err) ppJson(err);
    else ppJson(data);
});
```
