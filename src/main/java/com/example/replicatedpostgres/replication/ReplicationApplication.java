package com.example.replicatedpostgres.replication;


import com.example.replicatedpostgres.shared.common.Serializer;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class ReplicationApplication {


    private final Receiver receiver;

    private Map<String, String> db = new HashMap<String, String>() {{
        put("x", "12");
        put("y", "13");
    }};

    public ReplicationApplication() {
        this.receiver = new Receiver();
    }


    public void run(int port) {

        while(true) {
            log.info("Waiting for command");
            receiver.start(port);
            String command = receiver.receive();
            if (command.equals("exit") || command == null) {
                receiver.stop();
                break;
            }
            if (Message.isServerMessage(command)) {
                log.info("Processing leader command");
                String commitCommand = command.substring(6);
                String txId = Message.getTXid(commitCommand);
                Map<String, String> writeSet = Serializer.deserializeMap(Message.getWriteSet(commitCommand));
                log.info("client write set is {}", writeSet);
                //TODO: write to log

                for (Map.Entry<String,String> entry : writeSet.entrySet()) {
                    // TODO: should be replaced with real db
                    db.put(entry.getKey(), entry.getValue());
                }
                log.info("new db is {}", db);
                log.info("wrote in db");
                log.info("Response to leader");
                receiver.respond("done"); // TODO: make response here
                receiver.stop();
            }
            else {
                // Leader failed / Switch to leader mode
                receiver.stop();
                log.info("Leader failed!");
                log.info("Running leader application and recovery");
                // TODO
            }
        }

    }
}
