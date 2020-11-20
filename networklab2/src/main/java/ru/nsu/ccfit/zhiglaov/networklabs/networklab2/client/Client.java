package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.client;

import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.ClientArguments;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.HeaderInfo;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.ICheckSumCounter;
import ru.nsu.ccfit.zhiglaov.networklabs.networklab2.IHeaderConverter;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    static final Logger logger = Logger.getLogger(Client.class.getName());
    final Socket socket;
    final String fileName;
    final IHeaderConverter headerConverter;
    final ICheckSumCounter checkSumCounter;
    final DataInputStream fromServer;
    final DataOutputStream toServer;
    final File file;
    final static int BUFFER_SIZE = 1024;// * 1024 * 64;
    final static int TIMEOUT = 30 * 1000;

    public Client(ClientArguments arguments,
                  ICheckSumCounter checkSumCounter,
                  IHeaderConverter headerConverter) throws IOException {

        socket = new Socket(arguments.getIp(), arguments.getPort());
        socket.setSoTimeout(TIMEOUT);
        fromServer = new DataInputStream(socket.getInputStream());
        toServer = new DataOutputStream(socket.getOutputStream());

        logger.info("Connected to server: " + socket.getRemoteSocketAddress());
        String filePath = arguments.getFileName();

        var arr = filePath.split("\\\\");
        fileName = arr[arr.length - 1];

        logger.fine("File name: " + fileName);
        file = new File(filePath);
        this.checkSumCounter = checkSumCounter;
        this.headerConverter = headerConverter;
    }

    public void close() {
        try {
            fromServer.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't close DataInputStream from server", e);
        }
        try {
            toServer.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't close DataOutputStream to server", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't close socket", e);
        }
    }

    public void run() throws IOException {
        sendHeader();
        sendFile();
        try {
            System.out.println(receiveReport());
        } catch (IOException e) {
            String msg = "Couldn't receive report from server";
            System.out.println(msg);
            logger.log(Level.SEVERE, msg, e);
        }
    }

    private void sendHeader() throws IOException {
        byte[] checkSum;
        try {
            checkSum = checkSumCounter.getFileCheckSum(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't count file hashcode.\n", e);
            throw e;
        }
        Long fileLen = file.length();
        logger.info("File len = " + fileLen);
        var headerInfo = new HeaderInfo(fileName, fileLen, checkSum);
        byte[] header = headerConverter.headerInfoToByteArray(headerInfo);
        byte[] headerLen = ByteBuffer.allocate(Integer.BYTES).putInt(header.length).array();
        try {
            toServer.write(headerLen);
            toServer.write(header);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't send header to server.\n", e);
            throw e;
        }
        logger.info("Header has been sent to server");
    }

    private String receiveReport() throws IOException {
        var msgLenBuff = fromServer.readInt();
        logger.info("Report length: " + msgLenBuff);
        var buffer = fromServer.readNBytes(msgLenBuff);
        logger.info("Report from server: " + new String(buffer));
        return new String(buffer);
    }


    private void sendFile() throws IOException {
        int bytesRead;
        byte[] byteArray = new byte[BUFFER_SIZE];
        logger.info("Trying to create file input stream...");
        var fis = new FileInputStream(file);
        logger.info("File input stream created successfully");
        logger.info("Starting sending file to server");
        while ((bytesRead = fis.read(byteArray)) != -1) {
            toServer.write(byteArray, 0, bytesRead);
        }
        logger.info("File has been sent to server successfully");
    }
}
