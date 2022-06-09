package com.example.replicatedpostgres.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;


public class ReplicateLogger{
    private String loggerName = "";

    public ReplicateLogger(String loggerName){
        this.loggerName = loggerName + ".log";
        createFile();
    }

    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public String toString() {
        return "ReplicateLogger{" +
                "loggerName='" + loggerName + '\'' +
                '}';
    }

    public void createFile() {
        try {
            File myObj = new File(this.loggerName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeFile(String writeString){
        try {
            FileWriter myWriter = new FileWriter(this.loggerName, true);
            myWriter.write(writeString);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error Occurred.");
            e.printStackTrace();
        }
    }

    public void log(Map<String, String> writeSet) {
        // prepare write string
        StringBuilder writeString = new StringBuilder();
        for (Map.Entry<String, String> entry : writeSet.entrySet()){
            writeString.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        // write with writeFile function
        writeFile(writeString.toString());
    }
}
