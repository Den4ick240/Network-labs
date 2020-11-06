package ru.nsu.ccfit.zhigalov.client;

import ru.nsu.ccfit.zhigalov.connection.TreeConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalClient implements Runnable {
    private static final String EXIT_MESSAGE = "EXIT";
    private final TreeConnection treeConnection;
    private final BufferedReader reader;
    private static final Logger log = Logger.getLogger(LocalClient.class.getName());
    private final String name;

    public LocalClient(TreeConnection treeConnection, BufferedReader reader, String name) {
        this.treeConnection = treeConnection;
        this.reader = reader;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String string = null;
                string = reader.readLine();
                if (string == null || string.equals(EXIT_MESSAGE))
                    break;
                treeConnection.send(formatMessage(string));
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception in local client loop", e);
        }
    }

    private byte[] formatMessage(String string) {
        var message = name + ": " + string;
        return message.getBytes();
    }
}
