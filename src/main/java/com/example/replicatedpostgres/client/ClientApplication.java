package com.example.replicatedpostgres.client;

import com.example.replicatedpostgres.shared.common.Configuration;
import com.example.replicatedpostgres.shared.message.Message;
import com.example.replicatedpostgres.shared.network.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.plaf.nimbus.State;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ClientApplication {

    private enum States {INIT, GET_TX_INPUT, WRITE, READ};
    private States state = States.INIT;

    public void run() {
        log.info("Running client application");
        log.info("write 'quit' if you want to exit" );
        while (true) {
            // Get command from user
            String commandMessage = getUserCommandMessage();
            if (commandMessage.equals("quit"))
                return;
            // send to leader and recv response
            log.info("sending message:({}) to leader", commandMessage);
            Sender sender = new Sender();
            sender.startConnection("127.0.0.1", Configuration.LEADER_PORT);
            String response = sender.sendAndReceiveResponse(commandMessage);
            if (state.equals(States.READ)) {
                //Todo: handle read
                state = States.GET_TX_INPUT;
            }
            if (state.equals(States.WRITE)) {
                //Todo: handle write
                state = States.GET_TX_INPUT;
            }
            log.info("received response from leader:({})", response);
            // process response

        }
    }

    private String getUserCommandMessage() {
        try {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            while (true) {
                if (state.equals(States.INIT)) {
                    log.info("Do you want to start a new transaction? (y/n)");
                    System.out.print('>');
                    String input = reader.readLine();
                    if (input.equals("quit"))
                        return "quit";

                    if (input.equals("y")) {
                       state = States.GET_TX_INPUT;
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
                        state = States.INIT;
                        return Message.COMMIT_MESSAGE;
                    }
                    if (isWrite(input)) {
                        String key = input.substring(6, input.indexOf(','));
                        String value = input.substring(input.indexOf(','));
                        state = States.WRITE;
                        return Message.WRITE(key, value);
                    }
                    if (isRead(input)) {
                        String key = input.substring(5);
                        state = States.READ;
                        return Message.READ(key);
                    }
                    log.info("Bad input. Try again!");
                }
            }
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
