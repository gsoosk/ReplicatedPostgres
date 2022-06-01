package com.example.replicatedpostgres.shared.message;

public class Message {
    public static final String COMMIT_MESSAGE = "COMMIT";
    public static String INIT_MESSAGE = "INIT";
    public static String READ_ONLY_INIT = "READ_ONLY_INIT";
    public static String WRITE(String key, String value) {
        return "Write(" + key + "," + value + ")";
    }

    public static String READ(String key) {
        return "Read(" + key +  ")";
    }
}
