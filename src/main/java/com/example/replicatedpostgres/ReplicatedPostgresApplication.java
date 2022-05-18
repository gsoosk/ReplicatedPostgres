package com.example.replicatedpostgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ReplicatedPostgresApplication implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(ReplicatedPostgresApplication.class);

    public static void main(String[] args) {
        LOG.info("Starting the application");
        SpringApplication.run(ReplicatedPostgresApplication.class, args);
        LOG.info("Application finished");
    }


    @Autowired
    private Environment environment;


    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }

        LOG.info("Active profile is {}", this.environment.getActiveProfiles()[0]);
        String activeProfile = this.environment.getActiveProfiles()[0];
        if (activeProfile.equals("leader")) {
            // TODO: run leader
        }
        else if (activeProfile.equals("node1")) {
            // TODO: run node1
        }
        else if (activeProfile.equals("node2")) {
            // TODO: run node2
        }
        else if (activeProfile.equals("node3")) {
            // TODO: run node3
        }

    }

}
