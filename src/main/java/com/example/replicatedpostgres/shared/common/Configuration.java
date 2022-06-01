package com.example.replicatedpostgres.shared.common;

import java.util.List;

public class Configuration {
    public static final Integer LEADER_PORT = 3000;
    public static final Integer SECONDARY_LEADER_PORT = 3001;
    public static final List<Integer> REPLICATION_PORTS = List.of(3001, 3002);
    public static final Integer CLIENT_PORT = 3003;
    public static final List<Integer> POSTGRES_PORTS = List.of(5431, 5432);
    public static final Integer LEADER_POSTGRES_PORT = 5430;
}
