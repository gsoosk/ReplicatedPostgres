package com.example.replicatedpostgres.leader;


import com.example.replicatedpostgres.shared.common.Serializer;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Receiver;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.replicatedpostgres.shared.common.Configuration.LEADER_PORT;
import static com.example.replicatedpostgres.shared.common.Configuration.REPLICATION_PORTS;

@Component
@Slf4j
public class LeaderApplication {


    private Integer nextTransactionId = 0;

    //TODO: replace with the actual db
    private Map<String, String> db = new HashMap<String, String>() {{
        put("x", "12");
        put("y", "13");
    }};

    public LeaderApplication() {
    }

    public void run() {
        log.info("Leader application is running");
        while (true) {
            log.info("Waiting for client message");
            String command = receiveClientCommand();
            log.info("Dispatch to replications and wait for their response");
            dispatchCommandToReplications(command); // change it to replication commands
        }
    }

    private void dispatchCommandToReplications(String command) {
        for (int i = 0; i < 2; i++) {
            Sender sender = new Sender();
            sender.startConnection("127.0.0.1", REPLICATION_PORTS.get(i));
            String response = sender.sendAndReceiveResponse(command);
            log.info("Received response ({}) from node {}", response, REPLICATION_PORTS.get(i));
            sender.stopConnection();
            // TODO: process the response
        }
    }

    private String executeCommand(String command) {
        log.info("Executing command");
        // TODO: Process Command
        if (command.equals(Message.INIT_MESSAGE)) {
            return getNextTXId();
        }
        else if (command.equals(Message.READ_ONLY_INIT)) {
            return Serializer.serializeMap(db);
        }
        return "";
    }

    private String getNextTXId() {
        return Integer.toString(nextTransactionId++);
    }

    private String receiveClientCommand() {
        Receiver receiver = new Receiver();
        receiver.start(LEADER_PORT);
        String command = receiver.receive();
        log.info("received client command: {}", command);
        String response = executeCommand(command);
        log.info("respond to client: {}", response);
        receiver.respond(response);
        receiver.stop();
        return command;
    }
}
