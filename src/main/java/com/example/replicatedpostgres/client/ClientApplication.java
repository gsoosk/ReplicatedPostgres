package com.example.replicatedpostgres.client;

import com.example.replicatedpostgres.shared.common.Configuration;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ClientApplication {

    private enum States {IDLE, INIT, GET_TX_INPUT, WRITE, READ, COMMIT};
    private States state = States.IDLE;
    private Map<String, String> writeSet = new HashMap<>();
    private String readKey = "";
    private String writeKey = "";
    private String writeValue = "";

    public void run() {
        log.info("Running client application");
        log.info("write 'quit' if you want to exit" );
        while (true) {
            // Get command from user
            String commandMessage = getUserCommandMessage();
            if (commandMessage.equals("quit"))
                return;

            if (state.equals(States.INIT)) {
                state = States.GET_TX_INPUT;
                log.info("sending message:({}) to leader", commandMessage);
                Sender sender = new Sender();
                sender.startConnection("127.0.0.1", Configuration.LEADER_PORT);
                String response = sender.sendAndReceiveResponse(commandMessage);
                log.info("init response: {}", response);
            }
            else if (state.equals(States.WRITE)) {
                // next state
                state = States.GET_TX_INPUT;
                writeSet.put(writeKey, writeValue);
                log.info("buffered {}", commandMessage);
            }
            else if (state.equals(States.READ)) {
                // next state
                state = States.GET_TX_INPUT;
                if (writeSet.containsKey(readKey))
                    log.info("{}:{}", readKey, writeSet.get(readKey));
                else {
                    log.info("sending message:({}) to leader", commandMessage);
                    Sender sender = new Sender();
                    sender.startConnection("127.0.0.1", Configuration.LEADER_PORT);
                    String response = sender.sendAndReceiveResponse(commandMessage);
                    log.info("{}:{}", readKey, response);
                }
            } else if (state.equals(States.COMMIT)) {
                // next state
                state = States.IDLE;
                String commitMessage = commandMessage + " : " + serializeWriteSet();
                log.info("sending commit to leader: {}", commitMessage);
                Sender sender = new Sender();
                sender.startConnection("127.0.0.1", Configuration.LEADER_PORT);
                String response = sender.sendAndReceiveResponse(commitMessage);
                log.info("transaction result: {}", response);
            }
        }
    }

    private String serializeWriteSet() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String,String> entry : writeSet.entrySet()) {
            if (!result.toString().equals(""))
                result.append(",");
            result.append("(")
                .append(entry.getKey())
                .append(",")
                .append(entry.getValue())
                .append(")");
        }
        return result.toString();
    }

    private String getUserCommandMessage() {
        try {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            if (state.equals(States.IDLE)) {
                log.info("Do you want to start a new transaction? (y/n)");
                System.out.print('>');
                String input = reader.readLine();
                if (input.equals("quit"))
                    return "quit";

                if (input.equals("y")) {
                   state = States.INIT;
                   writeSet = new HashMap<>();
                   return Message.INIT_MESSAGE;
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
