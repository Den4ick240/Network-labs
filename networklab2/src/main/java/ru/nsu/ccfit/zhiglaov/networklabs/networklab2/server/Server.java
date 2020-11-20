package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.server;

import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.ICheckSumCounter;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.IHeaderConverter;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.ServerArguments;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker.ITimeTracker;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker.ITimer;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker.Timer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    static final Logger logger = Logger.getLogger(Server.class.getName());
    final ServerSocket serverSocket;
    final ExecutorService executorService = Executors.newFixedThreadPool(2);
    final ScheduledExecutorService timeTrackerExecutor = Executors.newSingleThreadScheduledExecutor();
    final ICheckSumCounter checkSumCounter;
    final IHeaderConverter headerConverter;
    final String uploadDirectory;
    final ITimeTracker timeTracker;
    final int timeTrackerTimeInterval;
    final static int TIMEOUT = 30 * 1000;

    public Server(ServerArguments args,
                  ICheckSumCounter checkSumCounter,
                  IHeaderConverter headerConverter,
                  String uploadDirectory,
                  ITimeTracker timeTracker,
                  int timeTrackerTimeInterval) throws IOException {
        serverSocket = new ServerSocket(args.getPort());
        this.checkSumCounter = checkSumCounter;
        this.headerConverter = headerConverter;
        this.uploadDirectory = uploadDirectory;
        this.timeTracker = timeTracker;
        this.timeTrackerTimeInterval = timeTrackerTimeInterval;
        logger.info("Created server object, ip address: " + serverSocket.getLocalSocketAddress());
    }

    public void run() {
        logger.info("Starting server...");
        timeTrackerExecutor.scheduleAtFixedRate(timeTracker, timeTrackerTimeInterval, timeTrackerTimeInterval,
                TimeUnit.MILLISECONDS);
        while (!serverSocket.isClosed()) {
            ITimer timer;
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(TIMEOUT);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Couldn't accept connection", e);
                continue;
            }
            String connectionTitle = getConnectionTitle(clientSocket);
            timer = timeTracker.getTimer(connectionTitle);
            var headerConverterInstance = headerConverter.getInstance();
            var checkSumCounterInstance = checkSumCounter.getInstance();
            try {
                var connection = new Connection(clientSocket, uploadDirectory, headerConverterInstance, checkSumCounterInstance, timer);
                executorService.execute(connection);
            } catch (IOException e) {
                logger.warning("Failed to transmit file from " + clientSocket.getRemoteSocketAddress());
                timer.finish();
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                    logger.log(Level.WARNING, "Couldn't close connection with " + clientSocket.getRemoteSocketAddress(), ioException);
                }
            }
        }
    }

    private String getConnectionTitle(Socket socket) {
        return socket.getRemoteSocketAddress().toString();
    }
}
