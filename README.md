# Replicated Postgres
This project is written with JAVA using SpringBoot Framework. All dependencies are listed and managed with Maven.

Intellij IDEA is recommended to run the project. MAVEN and JDK can be easily set up with it.

## Requirements
```
Docker
Maven
JAVA > version 17
```

## How to run
You can either use bellow steps or use the saved intellij configuration in the project (from `EditConfiguration` run the `APPLICATION` configuration). 
### Step 1:
running postgres nodes:
```shell
docker-compose up -d
```

Even if you stop services, databases will be saved into a volume. In case you wanted to remove them
run `docker volume prune` after stopping services.

### Step 2:
build the code:
```shell
mvn clean install
```
Start each of the three replication nodes:
```shell
mvn spring-boot:run -Dspring-boot.run.profiles=<leader/node1/node2>
```
Start the client:
```shell
mvn spring-boot:run -Dspring-boot.run.profiles=client
```

### Client
We provide command line interface in client
- Start new transaction with **"normal"** for readwrite transactions or **"readonly"** for readonly transaction
- Read value of x with **"read x"**
- Write value of x with **"write x, y"**
- Commit transaction with **"commit"**
Follow the client log to see how to interact with it.