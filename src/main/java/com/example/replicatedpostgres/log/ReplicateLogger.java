package com.example.replicatedpostgres.log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.example.replicatedpostgres.shared.common.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;


@Slf4j
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
                log.info("File created: " + myObj.getName());
            } else {
                log.info("File already exists.");
            }
        } catch (IOException e) {
            log.error("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeFile(String writeString){
        try {
            FileWriter myWriter = new FileWriter(this.loggerName, true);
            myWriter.write(writeString);
            myWriter.close();
            log.info("Successfully wrote to the file.");
        } catch (IOException e) {
            log.info("An error Occurred.");
            e.printStackTrace();
        }
    }

    public void log(int transactionId, Map<String, String> writeSet) {
        writeFile(transactionId + "=" + Serializer.serializeMap(writeSet) + "\n");
    }

    public String readAllLogs() {
        try {
            return Files.readString(Path.of(loggerName)) ;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
