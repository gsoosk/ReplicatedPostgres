package com.example.replicatedpostgres.client;

import com.example.replicatedpostgres.shared.common.Configuration;
import com.example.replicatedpostgres.shared.common.Serializer;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ClientApplication {

    private enum States {IDLE, INIT, GET_TX_INPUT, WRITE, READ, COMMIT, READ_ONLY_INIT, GET_RO_TX_INPUT, RO_READ, RO_COMMIT};
    private States state = States.IDLE;
    private Map<String, String> writeSet = new HashMap<>();
    private Set<String> readSet = new HashSet<>();
    private String readKey = "";
    private String writeKey = "";
    private String writeValue = "";
    private String transactionID = "";

    private Integer leaderPort = Configuration.LEADER_PORT;

    private Map<String, String> snapshot = new HashMap<>();

    public void run() {
        log.info("Running client application");
        log.info("write 'quit' if you want to exit" );
        while (true) {
            // Get command from user
            String commandMessage = getUserCommandMessage();
            if (commandMessage.equals("quit"))
                return;

            handleNormalTransactionStates(commandMessage);
            handleReadOnlyTransactionsStates(commandMessage);
        }
    }

    private void handleReadOnlyTransactionsStates(String commandMessage) {
        if (state.equals(States.READ_ONLY_INIT)) {
            log.info("sending message:({}) to leader", commandMessage);
            String response = sendToLeader(commandMessage);
            log.info("init response (snapshot): {}", response);
            snapshot = Serializer.deserializeMap(response);

            state = States.GET_RO_TX_INPUT;
        }
        else if (state.equals(States.RO_READ)) {
            if (snapshot.containsKey(readKey))
                log.info("{}={}", readKey, snapshot.get(readKey));
            else
                log.info("{}=<not existed>", readKey);

            state = States.GET_RO_TX_INPUT;
        }
        else if (state.equals(States.RO_COMMIT)) {
            log.info("transaction result: committed");
            state = States.IDLE;
        }
    }

    private void handleNormalTransactionStates(String commandMessage) {
        if (state.equals(States.INIT)) {
            log.info("sending message:({}) to leader", commandMessage);
            String response = sendToLeader(commandMessage);
            log.info("init response (transaction id): {}", response);
            transactionID = response;

            state = States.GET_TX_INPUT;
        }
        else if (state.equals(States.WRITE)) {
            writeSet.put(writeKey, writeValue);
            log.info("buffered {}", commandMessage);

            state = States.GET_TX_INPUT;
        }
        else if (state.equals(States.READ)) {
            if (writeSet.containsKey(readKey))
                log.info("{}={}", readKey, writeSet.get(readKey));
            else {
                log.info("sending message:({}) to leader", commandMessage);
                String response = sendToLeader(transactionID + "," + commandMessage);
                log.info("{}={}", readKey, response);
            }

            readSet.add(readKey);
            state = States.GET_TX_INPUT;
        } else if (state.equals(States.COMMIT)) {
            String commitMessage = commandMessage + " : " + serializeWriteSet() + "|" + serializeReadSet();
            log.info("sending commit to leader: {}", commitMessage);
            String response = sendToLeader(transactionID + "," + commitMessage);
            log.info("transaction result: {}", response);

            state = States.IDLE;
        }
    }

    private String serializeReadSet() {
        return Serializer.serializeSet(readSet);
    }

    private String sendToLeader(String commandMessage) {
        Sender sender = new Sender();
        sender.startConnection("127.0.0.1", leaderPort);
        String response = sender.sendAndReceiveResponse(commandMessage);
        if (response == null) {
            switchLeader();
            sender.stopConnection();
            sender.startConnection("127.0.0.1", leaderPort);
            response = sender.sendAndReceiveResponse(commandMessage);
        }
        sender.stopConnection();
        return response;
    }

    private void switchLeader() {
        leaderPort = Configuration.SECONDARY_LEADER_PORT;
    }

    private String serializeWriteSet() {
        return Serializer.serializeMap(writeSet);
    }

    private String getUserCommandMessage() {
        try {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            if (state.equals(States.IDLE)) {
                log.info("What type of transaction you want to start? (normal/readonly)");
                System.out.print('>');
                String input = reader.readLine();
                if (input.equals("quit"))
                    return "quit";

                if (input.equals("normal")) {
                   state = States.INIT;
                   writeSet = new HashMap<>();
                   readSet = new HashSet<>();
                   return Message.INIT_MESSAGE;
                }
                else if (input.equals("readonly")) {
                    state = States.READ_ONLY_INIT;
                    snapshot = new HashMap<>();
                    return Message.READ_ONLY_INIT;
                }
            }
            else if (state.equals(States.GET_TX_INPUT)) {
                log.info("write input scheme: write <key>,<value>");
                log.info("read input scheme:  read <key>");
                log.info("finishing transaction: commit");
                System.out.print('>');
                String input = reader.readLine();
                if (input.equals("quit"))
                    return "quit";

                if (input.equals("commit")) {
                    state = States.COMMIT;
                    return Message.COMMIT_MESSAGE;
                }
                if (isWrite(input)) {
                    writeKey = input.substring(6, input.indexOf(','));
                    writeValue = input.substring(input.indexOf(',') + 1);
                    state = States.WRITE;
                    return Message.WRITE(writeKey, writeValue);
                }
                if (isRead(input)) {
                    readKey = input.substring(5);
                    state = States.READ;
                    return Message.READ(readKey);
                }
                log.info("Bad input. Try again!");
            }
            else if (state.equals(States.GET_RO_TX_INPUT)) {
                log.info("read input scheme:  read <key>");
                log.info("finishing transaction: commit");
                System.out.print('>');
                String input = reader.readLine();
                if (input.equals("quit"))
                    return "quit";

                if (input.equals("commit")) {
                    state = States.RO_COMMIT;
                    return Message.COMMIT_MESSAGE;
                }
                if (isRead(input)) {
                    readKey = input.substring(5);
                    state = States.RO_READ;
                    return Message.READ(readKey);
                }
                log.info("Bad input. Try again!");
            }
            return "";
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return "quit";
        }
    }

    private static boolean isWrite(final String input) {
        final Pattern pattern = Pattern.compile("write [a-zA-Z0-9]+,[a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private static boolean isRead(final String input) {
        final Pattern pattern = Pattern.compile("read [a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

}
