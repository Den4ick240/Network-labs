package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;


import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.client.Client;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.server.Server;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker.TimeTracker;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

public class Main {
    final static String checkSumMethod = "SHA-256";
    private static final String UPLOAD_DIRECTORY = "uploads\\";
    private static final int TIME_TRACKER_TIME_INTERVAL = 3000;

    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        ArgumentHandler argHandler = new ArgumentHandler("Main", Locale.ENGLISH);
        IArguments parsedArgs;
        logger.info(Arrays.toString(args));
        try {
            parsedArgs = argHandler.parse(args);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return;
        }
        try {
            if (parsedArgs.getClass().equals(ServerArguments.class)) {
                new Server(
                        (ServerArguments) parsedArgs,
                        new CheckSumCounter(MessageDigest.getInstance(checkSumMethod)),
                        new HeaderConverter(),
                        UPLOAD_DIRECTORY,
                        new TimeTracker(),
                        TIME_TRACKER_TIME_INTERVAL)
                        .run();
            } else {
                var client = new Client(
                        (ClientArguments) parsedArgs,
                        new CheckSumCounter(MessageDigest.getInstance(checkSumMethod)),
                        new HeaderConverter()
                );
                try {
                    client.run();
                } catch (Exception e) {
                    client.close();
                    throw e;
                }
            }
        } catch (IOException | IllegalArgumentException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }
}
