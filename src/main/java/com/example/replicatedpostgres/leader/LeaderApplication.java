package com.example.replicatedpostgres.leader;


import com.example.replicatedpostgres.database.DatabaseHandler;
import com.example.replicatedpostgres.log.ReplicateLogger;
import com.example.replicatedpostgres.shared.common.Serializer;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Receiver;
import com.example.replicatedpostgres.shared.network.Sender;
import com.example.replicatedpostgres.validation.OptimisticValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.replicatedpostgres.shared.common.Configuration.LEADER_PORT;
import static com.example.replicatedpostgres.shared.common.Configuration.REPLICATION_PORTS;

@Component
@Slf4j
public class LeaderApplication {


    private Integer nextTransactionId = 0;
    private OptimisticValidation validator = new OptimisticValidation();

    private Set<Integer> committedTransactions = new HashSet<>();


    private ReplicateLogger logger = null;
    private Integer port = LEADER_PORT;

    private DatabaseHandler dbHandler;
    public LeaderApplication(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void run(Integer runningPort,  ReplicateLogger initLogger) {
        logger = initLogger;
        port = runningPort;
        committedTransactions = getCommittedTransactionsFromLog();
        nextTransactionId = committedTransactions.size() == 0 ? 0 : Collections.max(committedTransactions) + 1;
        log.info("Leader application is running");
        while (true) {
            log.info("Waiting for client message");
            receiveClientCommand();
        }
    }

    private void dispatchCommandToReplications(String command) {
        for (int i = 0; i < 2; i++) {
            if (REPLICATION_PORTS.get(i).equals(port))
                continue;
            Sender sender = new Sender();
            sender.startConnection("127.0.0.1", REPLICATION_PORTS.get(i));
            String response = sender.sendAndReceiveResponse(command);
            log.info("Received response ({}) from node {}", response, REPLICATION_PORTS.get(i));
            sender.stopConnection();
        }
    }

    private String executeCommand(String command) {
        log.info("Executing command");
        if (command.equals(Message.INIT_MESSAGE)) {
            Integer txId = getNextTXId();
            validator.addTrasaction(txId);
            return Integer.toString(txId);
        }
        else if (command.equals(Message.READ_ONLY_INIT)) {
            return Serializer.serializeMap(dbHandler.readAll());
        }
        else if (Message.isReadMessage(command)) {
            String key = Message.getReadKey(command);
            log.info("reading {} from db", key);
            String value = dbHandler.read(key);
            return value == null ? "<not existed>" : value;
        }
        else if (Message.isCommitMessage(command)) {
            String txId = Message.getTXid(command);
            if (committedTransactions.contains(Integer.parseInt(txId)))
                return Message.COMMITTED;

            Map<String, String> writeSet = Serializer.deserializeMap(Message.getWriteSet(command));
            Set<String> readSet = Serializer.deserializeSet(Message.getReadSet(command));
            log.info("client write set is {}", writeSet);
            log.info("client read set is {}", readSet);
            if (validator.validate(Integer.parseInt(txId), new ArrayList<>(readSet), new ArrayList<>(writeSet.keySet()))) {
                logger.log(Integer.parseInt(txId), writeSet);

                dispatchCommandToReplications("Server" + command); // forward commit message to replications

                dbHandler.write(writeSet);
                validator.CompleteWrite(Integer.parseInt(txId));
                log.info("tx committed");
                committedTransactions.add(Integer.parseInt(txId));
                return Message.COMMITTED;
            }
            log.info("tx aborted");
            return Message.ABORTED;
        }
        return "";
    }

    private Integer getNextTXId() {
        return nextTransactionId++;
    }

    private void receiveClientCommand() {
        Receiver receiver = new Receiver();
        receiver.start(port);
        String command = receiver.receive();
        log.info("received client command: {}", command);
        String response = executeCommand(command);
        log.info("respond to client: {}", response);
        receiver.respond(response);
        receiver.stop();
    }

    private Set<Integer> getCommittedTransactionsFromLog() {
        Set<Integer> result = new HashSet<>();
        String logString = logger.readAllLogs();
        if (logString.equals(""))
            return result;
        String[] ourLogs = logString.split("\n");
        for (String entry : ourLogs) {
            result.add(Integer.parseInt(Message.getTXidFromLogEntry(entry)));
        }
        return result;
    }
}
