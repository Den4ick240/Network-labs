package ru.nsu.ccfit.zhigalov;

import ru.nsu.ccfit.zhigalov.arguments.*;
import ru.nsu.ccfit.zhigalov.client.LocalClient;
import ru.nsu.ccfit.zhigalov.connection.*;
import ru.nsu.ccfit.zhigalov.connection.deputies.NodeFactoryWithDeputies;
import ru.nsu.ccfit.zhigalov.connection.saferebuilding.MessageConverter;
import ru.nsu.ccfit.zhigalov.connection.saferebuilding.TreeConnectionWithSafeRebuilding;
import ru.nsu.ccfit.zhigalov.packet.DatagramSocketPacketStream;
import ru.nsu.ccfit.zhigalov.packet.PacketStreamWithLosses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static final int DATAGRAM_SOCKET_BUFFER_SIZE = 1024;
    private static final int THREAD_POOL_SIZE = 16;
    private static final int UUID_CACHE_SIZE = 256;
    private static final long TIMEOUT_MILLIS = 5000;
    private static final long PING_TIME_INTERVAL_MILLIS = TIMEOUT_MILLIS / 5;
    private static final long RESEND_MESSAGE_TIME_INTERVAL_MILLIS = 1000;
    private static final long CONNECTION_CLEANER_TIME_INTERVAL_MILLIS = TIMEOUT_MILLIS;

    public static void main(String[] args) {
        Arguments arguments;
        try {
            arguments = new ArgumentHandler().parse(args);
        } catch (ArgumentException e) {
            System.out.println(e.getMessage());
            log.log(Level.SEVERE, "Exception in parsing arguments", e);
            return;
        }

        var scheduledThreadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        LocalClient localClient;
        TreeConnection treeConnection = null;

        try (DatagramSocket socket = new DatagramSocket(arguments.localPort);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            treeConnection = constructTreeConnection(socket, scheduledThreadPool, arguments);

            if (arguments.hasRemoteSocketAddress())
                treeConnection.requestConnection(arguments.remoteSocketAddress);

            localClient = new LocalClient(treeConnection, consoleReader, arguments.name);

            scheduledThreadPool.execute(treeConnection);
            localClient.run();
            log.info("console reading is finished");
            treeConnection.stopRunning();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduledThreadPool.shutdown();
            if (treeConnection != null) {
                treeConnection.close();
            }
        }
    }

    public static TreeConnection constructTreeConnection(
            DatagramSocket socket, ScheduledExecutorService scheduledThreadPool, Arguments arguments) {

        var packetStream = new DatagramSocketPacketStream(socket, new MessageConverter(), DATAGRAM_SOCKET_BUFFER_SIZE);
        var packetStreamWithLosses = new PacketStreamWithLosses(
                arguments.lossPercent, arguments.lossPercent, new Random(), packetStream);
        var concurrentHashMap = new ConcurrentHashMap<SocketAddress, RemoteNode>();
        var uuidCache = new UUID_Cache(UUID_CACHE_SIZE);
        TreeConnection.DataHandler dataHandler = (byte[] data) -> System.out.println(new String(data));
        var nodeFactory = new NodeFactoryWithDeputies(
                packetStream,
                scheduledThreadPool,
                RESEND_MESSAGE_TIME_INTERVAL_MILLIS,
                TIMEOUT_MILLIS,
                PING_TIME_INTERVAL_MILLIS
        );
        return new TreeConnectionWithSafeRebuilding(
                dataHandler,
                packetStreamWithLosses,
                concurrentHashMap,
                scheduledThreadPool,
                uuidCache,
                CONNECTION_CLEANER_TIME_INTERVAL_MILLIS,
                nodeFactory
                , new HashMap<>()
        );
    }
}
