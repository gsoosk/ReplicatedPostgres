# Replicated Postgres

## How to run
### Step 1:
running postgres nodes:
```shell
docker-compose up -d
```

Even if you stop services, databases will be saved into a volume. In case you wanted to remove them 
run `docker volume prune` after stopping services.

### Step 2:
building code:
```shell
mvn clean install
```
running each node:
```shell
mvn spring-boot:run -Dspring-boot.run.profiles=<leader/node1/node2>
```