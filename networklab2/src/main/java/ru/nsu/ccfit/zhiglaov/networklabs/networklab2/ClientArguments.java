package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;


public class ClientArguments implements IArguments {
    private final Integer port;
    private final String ip;
    private final String fileName;

    public ClientArguments(String ip, Integer port, String fileName) {
        this.port = port;
        this.ip = ip;
        this.fileName = fileName;
    }

    public String getFileName() { return fileName; }

    public String getIp() {
        return ip;
    }

    public Integer getPort() {
        return port;
    }
}
