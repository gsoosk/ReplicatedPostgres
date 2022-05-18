package com.example.replicatedpostgres.shared.network;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;


@Slf4j
public class Receiver {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    public String receive() {
        try {
            String message = in.readLine();
            log.info("Received message: {}", message);
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return null;
        }
    }

    public void respond(String message) {
        out.println(message);
    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            log.debug(e.getMessage());
        }

    }
}