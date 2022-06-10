package com.example.replicatedpostgres.replication;


import com.example.replicatedpostgres.leader.LeaderApplication;
import com.example.replicatedpostgres.log.ReplicateLogger;
import com.example.replicatedpostgres.shared.common.Serializer;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Receiver;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.replicatedpostgres.shared.common.Configuration.REPLICATION_PORTS;

@Component
@Slf4j
public class ReplicationApplication {


    private final Receiver receiver;

    private Map<String, String> db = new HashMap<String, String>() {{
        put("x", "12");
        put("y", "13");
    }};

    private LeaderApplication backUpLeaderApplication;

    private ReplicateLogger logger = null;

    public ReplicationApplication(LeaderApplication leaderApplication) {
        this.receiver = new Receiver();
        backUpLeaderApplication = leaderApplication;
    }


    public void run(int port, ReplicateLogger initLogger) {
        this.logger = initLogger;
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
                this.logger.log(Integer.parseInt(txId), writeSet);

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
            else if (command.equals("GetLogs")) {
                String allLogs = String.join("@",logger.readAllLogs().split("\n"));
                log.info("sending logs {} ", allLogs);
                receiver.respond(allLogs);
                receiver.stop();
            }
            else if (command.startsWith("UNCOMMITTED:")) {
                log.info("received transactions to write from new leader: {}", command.substring(12));
                List<String> allLogs = Arrays.asList(command.substring(12).split("@"));
                writeTransactionsForLogEntries(new HashSet<>(allLogs));
            }
            else {
                // Leader failed / Switch to leader mode
                receiver.stop();
                log.info("Leader failed!");
                log.info("Recovery");
                Set<String> theirLogs = getOtherReplicationLogs();
                Set<String> ourLogs = getMyLogs();
                log.info("received other replica logs: {}", theirLogs);
                writeUncommittedTransactions(ourLogs, theirLogs);
                sendUncommittedTransactions(ourLogs, theirLogs);

                log.info("Running leader application");
                backUpLeaderApplication.run(port, logger);
            }
        }

    }

    private void sendUncommittedTransactions(Set<String> ourLogs, Set<String> theirLogs) {
        Set<String> diff = new HashSet<>(ourLogs);
        diff.removeAll(theirLogs);
        List<String> diffList = new ArrayList<>(diff);
        String message = "UNCOMMITTED:" +  String.join("@", diffList);
        if (diff.size() > 0) {
            log.info("sending logs to other replica to persist: {}", diff);
            sendMessageToOtherReplica(message);
        }
    }

    private String sendMessageToOtherReplica(String message) {
        Sender sender = new Sender();
        sender.startConnection("127.0.0.1", REPLICATION_PORTS.get(1));
        String response = sender.sendAndReceiveResponse(message);
        log.info("Received response ({}) from node {}", response, REPLICATION_PORTS.get(1));
        sender.stopConnection();
        return response;
    }

    private void writeUncommittedTransactions(Set<String> ourLogs, Set<String> theirLogs) {
        Set<String> diff = new HashSet<>(theirLogs);
        diff.removeAll(ourLogs);
        log.info("Writing uncommitted transaction from the other node: {}", diff);
        writeTransactionsForLogEntries(diff);
    }

    private void writeTransactionsForLogEntries(Set<String> diff) {
        for (String logEntry : diff) {
            String txId = Message.getTXidFromLogEntry(logEntry);
            Map<String, String> writeSet = Serializer.deserializeMap(Message.getWriteSetFromLogEntry(logEntry));
            log.info("write set is {}", writeSet);
            this.logger.log(Integer.parseInt(txId), writeSet);
            for (Map.Entry<String,String> entry : writeSet.entrySet()) {
                // TODO: should be replaced with real db
                db.put(entry.getKey(), entry.getValue());
            }
            log.info("new db is {}", db);
            log.info("wrote in db");
        }
    }

    private Set<String> getMyLogs() {
        String logString = logger.readAllLogs();
        List<String> logsList = Arrays.asList(logString.split("\n"));
        return new HashSet<>(logsList);
    }

    private Set<String> getOtherReplicationLogs() {
        String response = sendMessageToOtherReplica("GetLogs");
        List<String> logsList = Arrays.asList(response.split("@"));
        return new HashSet<>(logsList);
    }
}
