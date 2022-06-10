package com.example.replicatedpostgres.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class DatabaseHandler {

    private String tableName = "data";
//    private boolean debug = false;  // for debug use

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void write(Map<String, String> writeSet) {
        // construct write sql // TODO check duplicate
        StringBuilder pre_sql = new StringBuilder("INSERT INTO " + this.tableName + "(key, value)" + " VALUES ");
        for (Map.Entry<String, String> entry : writeSet.entrySet()) {
            pre_sql.append("('").append(entry.getKey()).append("','").append(entry.getValue()).append("'),");
        }
        StringBuilder sql = pre_sql.delete(pre_sql.length()-1, pre_sql.length()); // remove last char
        sql.append(" On CONFLICT(key) DO UPDATE SET value = EXCLUDED.value");
        sql.append(";");

        // write
        int rows = jdbcTemplate.update(sql.toString());
        if (rows > 0) {
            log.info("insert success");
        } else {
            log.info("insert failed");
        }
    }

    public String read(String key) {
        // construct read sql
        StringBuilder sql = new StringBuilder("SELECT key, value FROM " + this.tableName + " WHERE key='" + key + "';");

        // read
        List<DataColumn> result = jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
                new DataColumn(
                        rs.getString("key"),
                        rs.getString("value")
                ));

        // return
        if (result.size() == 0) {
            return null;
        }
        return result.get(0).value;  //TODO
    }

    public Map<String, String> readAll() {
        // get all data from tableName
        StringBuilder sql = new StringBuilder("SELECT key, value FROM " + this.tableName + ";");

        // read
        List<DataColumn> result = jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
                new DataColumn(
                        rs.getString("key"),
                        rs.getString("value")
                ));

        // convert into Map // hm.put(key, value)
        Map<String, String> map = new HashMap<>();
        for (DataColumn col : result) {
            map.put(col.key, col.value);
        }
        // return
        return map;
    }
}
