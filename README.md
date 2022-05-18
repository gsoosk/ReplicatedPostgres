# Replicated Postgres

## How to run
### Step 1:
running postgres nodes:
```shell
docker-compose up -d
```

### Step 2:
building code:
```shell
mvn clean install
```
running each node:
```shell
mvn spring-boot:run -Dspring-boot.run.profiles=<leader/node1/node2/node3>
```