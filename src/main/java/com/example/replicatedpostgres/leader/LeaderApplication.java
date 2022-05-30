package com.example.replicatedpostgres.leader;


import com.example.replicatedpostgres.shared.network.Receiver;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.replicatedpostgres.shared.common.Configuration.LEADER_PORT;
import static com.example.replicatedpostgres.shared.common.Configuration.REPLICATION_PORTS;

@Component
@Slf4j
public class LeaderApplication {



    public LeaderApplication() {
    }

    public void run() {
        log.info("Leader application is running");
        while (true) {
            log.info("Waiting for client message");
            String command = receiveClientCommand();
            log.info("Executing command");
            executeCommand(command);
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

    private void executeCommand(String command) {
        // TODO: Process Command
    }

    private String receiveClientCommand() {
        Receiver receiver = new Receiver();
        receiver.start(LEADER_PORT);
        String command = receiver.receive();
        receiver.respond("ack"); //Respond to client with ack
        receiver.stop();
        return command;
    }
}
