package com.example.replicatedpostgres.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataColumn {

    public String key;
    public String value;

    public String getKey() {
        return key;
    }

    public DataColumn setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DataColumn setValue(String value) {
        this.value = value;
        return this;
    }

    public DataColumn(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String toString() {
        return this.key + ":" + this.value;
    }

    public void addToMap(Map<String, String> map) {
        map.put(this.key, this.value);
    }

    public String[] toList() {
        return new String[]{this.key, this.value};
    }
}
