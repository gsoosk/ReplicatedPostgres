package com.example.replicatedpostgres.replication;


import com.example.replicatedpostgres.shared.network.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReplicationApplication {


    private final Receiver receiver;

    public ReplicationApplication() {
        this.receiver = new Receiver();
    }


    public void run(int port) {

        while(true) {
            log.info("Waiting for command from leader");
            receiver.start(port);
            String command = receiver.receive();
            if (command.equals("exit") || command == null) {
                receiver.stop();
                break;
            }
            log.info("Processing leader command");
            // TODO: process the command
            log.info("Response to leader");
            receiver.respond("OK"); // TODO: make response here
            receiver.stop();
        }

    }
}
