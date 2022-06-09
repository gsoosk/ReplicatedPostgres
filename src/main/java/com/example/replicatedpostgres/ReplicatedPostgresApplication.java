package com.example.replicatedpostgres;

import com.example.replicatedpostgres.client.ClientApplication;
import com.example.replicatedpostgres.leader.LeaderApplication;
import com.example.replicatedpostgres.log.ReplicateLogger;
import com.example.replicatedpostgres.replication.ReplicationApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.HashSet;

import static com.example.replicatedpostgres.shared.common.Configuration.REPLICATION_PORTS;

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
    @Autowired
    private LeaderApplication leaderApplication;
    @Autowired
    private ReplicationApplication replicationApplication;
    @Autowired
    private ClientApplication clientApplication;


    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }

        LOG.info("Active profile is {}", this.environment.getActiveProfiles()[0]);
        String activeProfile = this.environment.getActiveProfiles()[0];

        if (activeProfile.equals("client")) {
            clientApplication.run();
        }
        else {
            ReplicateLogger logger = new ReplicateLogger(activeProfile);
            switch (activeProfile) {
                case "leader" -> leaderApplication.run(new HashSet<>(), logger);
                case "node1" -> replicationApplication.run(REPLICATION_PORTS.get(0), logger);
                case "node2" -> replicationApplication.run(REPLICATION_PORTS.get(1), logger);
            }
        }

    }

}
