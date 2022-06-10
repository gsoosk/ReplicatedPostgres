package com.example.replicatedpostgres.shared.message;

public class Message {
    public static final String COMMIT_MESSAGE = "COMMIT";
    public static final String RO_COMMIT_MESSAGE = "RO_COMMIT";
    public static String INIT_MESSAGE = "INIT";
    public static String READ_ONLY_INIT = "READ_ONLY_INIT";
    public static String WRITE(String key, String value) {
        return "Write(" + key + "," + value + ")";
    }

    public static String READ(String key) {
        return "Read(" + key +  ")";
    }

    public static String COMMITTED = "COMMITTED";
    public static String ABORTED = "ABORTED";

    public static boolean isReadMessage(String command) {
        try {
            String[] s = command.split(",");
            return s[1].startsWith("Read");
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String getReadKey(String command) {
        try {
            String[] s = command.split(",");
            return s[1].substring(5, s[1].indexOf(')'));
        }
        catch (Exception e) {
            return "";
        }
    }

    public static boolean isCommitMessage(String command) {
        try {
            String[] s = command.split(",");
            return s[1].startsWith(COMMIT_MESSAGE);
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String getWriteKey(String command) {
        try {
            String[] s = command.split(",");
            return s[1].substring(6);
        }
        catch (Exception e) {
            return "";
        }
    }

    public static String getWriteValue(String command) {
        try {
            String[] s = command.split(",");
            return s[2].substring(0, s[2].indexOf(')'));
        }
        catch (Exception e) {
            return "";
        }
    }

    public static String getCommitMesage(String serializeWriteSet, String serializeReadSet) {
        return COMMIT_MESSAGE + "#" + serializeWriteSet + "#" + serializeReadSet;
    }

    public static String getTXid(String command) {
        try {
            String[] s = command.split(",");
            return s[0];
        }
        catch (Exception e) {
          return "";
        }
    }

    public static String getWriteSet(String command) {
        try {
            String[] s = command.split("#");
            return s[1];
        }
        catch (Exception e) {
            return "";
        }
    }

    public static String getReadSet(String command) {
        try {
            String[] s = command.split("#");
            return s[2];
        }
        catch (Exception e) {
            return "";
        }
    }

    public static boolean isServerMessage(String command) {
        return command.startsWith("Server");
    }

    public static String getTXidFromLogEntry(String logEntry) {
        try {
            String[] s = logEntry.split("=");
            return s[0];
        }
        catch (Exception e) {
            return "";
        }
    }

    public static String getWriteSetFromLogEntry(String logEntry) {
        try {
            String[] s = logEntry.split("=");
            return s[1];
        }
        catch (Exception e) {
            return "";
        }
    }
}
