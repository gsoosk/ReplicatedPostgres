package com.example.replicatedpostgres.shared.common;

import java.util.List;

public class Configuration {
    public static final Integer LEADER_PORT = 3000;
    public static final List<Integer> REPLICATION_PORTS = List.of(3001, 3002, 3003);
    public static final List<Integer> POSTGRES_PORTS = List.of(5431, 5432, 5433);
}
