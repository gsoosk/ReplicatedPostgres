package com.example.replicatedpostgres.shared.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Serializer {
    public static String serializeMap(Map<String, String> map) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String,String> entry : map.entrySet()) {
            if (!result.toString().equals(""))
                result.append(",");
            result.append(entry.getKey())
                    .append(":")
                    .append(entry.getValue());
        }
        return result.toString();
    }

    public static String serializeSet(Set<String> set) {
        StringBuilder result = new StringBuilder();
        for (String key : set) {
            if (!result.toString().equals(""))
                result.append(",");
            result.append(key);
        }
        return result.toString();
    }

    public static Map<String, String> deserializeMap(String mapString) {
        Map<String, String> result = new HashMap<>();
        try {
            String[] pairs = mapString.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                result.put(keyValue[0], keyValue[1]);
            }
        }
        catch (Exception e)
        {
            return new HashMap<>();
        }
        return result;
    }
}
