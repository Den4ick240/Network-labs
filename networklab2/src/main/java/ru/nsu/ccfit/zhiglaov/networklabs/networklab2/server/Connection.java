package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.server;

import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.*;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker.ITimer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection implements Runnable {
    static final Logger logger = Logger.getLogger(Connection.class.getName());
    final String uploadDirectory;
    HeaderInfo headerInfo;
    final IHeaderConverter headerConverter;
    final DataInputStream fromClient;
    final DataOutputStream toClient;
    final ICheckSumCounter checkSumCounter;
    final ITimer timer;
    final Socket clientSocket;
    final static int BUFFER_SIZE = 1024 * 1024 * 64;

    public Connection(Socket clientSocket,
                      String uploadDirectory,
                      IHeaderConverter headerConverter,
                      ICheckSumCounter checkSumCounter,
                      ITimer timer) throws IOException {
        this.uploadDirectory = uploadDirectory;
        this.headerConverter = headerConverter;
        this.checkSumCounter = checkSumCounter;
        this.timer = timer;
        this.clientSocket = clientSocket;
        try {
            toClient = new DataOutputStream(clientSocket.getOutputStream());
            fromClient = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't get input and output streams from socket", e);
            throw e;
        }
        logger.info("New connection: " + clientSocket.getInetAddress());
    }

    @Override
    public void run() {
        try {
            readHeader();
            readFile();
            sendReport(null);
        } catch (IOException | InvalidHeaderException e) {
            sendReport(e);
            logger.warning(e.getLocalizedMessage());
        }
        finally {
            timer.finish();
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning(e.getLocalizedMessage());
            }
        }
    }

    private void sendReport(Exception e) {
        String msg;
        if (e == null) {
            msg = "File received successfully\n";
        } else {
            msg = e.getMessage();
        }
        logger.info("Report to client: " + msg);
        try {
            toClient.writeInt(msg.length());
            toClient.write(msg.getBytes());
        } catch (IOException ioException) {
            logger.log(Level.WARNING, "Couldn't send report to client", ioException);
        }
    }

    private void readFile() throws IOException {
        int bytesRead;
        long bytesReadTotal = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        String fileName = uploadDirectory + headerInfo.getFileName();
        try (FileOutputStream output = new FileOutputStream(new File(fileName))) {
            logger.info("File created successfully");
            timer.start();
            int counter = 0;
            do {
                try {
                    bytesRead = fromClient.read(buffer, 0, BUFFER_SIZE);
                } catch (IOException e) {
                    logger.severe("Couldn't read from socket\n" + e.getLocalizedMessage());
                    throw e;
                }
                try {
                    output.write(buffer, 0, bytesRead);
                } catch (IOException e) {
                    logger.severe("Couldn't write to file\n" + e.getLocalizedMessage());
                    throw e;
                }
                bytesReadTotal += bytesRead;
                if (counter++ == 100) {
                    timer.check(bytesReadTotal);
                    counter = 0;
                }
            } while (bytesReadTotal < headerInfo.getFileSize());
            timer.check(bytesReadTotal);
            timer.finish();
            output.flush();
            logger.info("File transition's been finished");
        } catch (FileNotFoundException e) {
            logger.severe("Couldn't create file.\n" + e.getLocalizedMessage());
            throw e;
        }
        var checkSum = checkSumCounter.getFileCheckSum(new File(fileName));
        if (!Arrays.equals(checkSum, headerInfo.getFileChecksum())) {
            logger.warning("File checksum doesn't match");
        } else {
            logger.info("File checksum matches");
        }
    }

    private void readHeader() throws IOException, InvalidHeaderException {
        int headerLen = fromClient.readInt();
        byte[] headerBuff = fromClient.readNBytes(headerLen);
        logger.info("Header's been received from client");
        headerInfo = headerConverter.byteArrayToHeaderInfo(headerBuff);
    }
}
