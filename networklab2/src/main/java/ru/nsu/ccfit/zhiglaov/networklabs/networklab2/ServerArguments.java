package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

public class ServerArguments implements IArguments {
    private final Integer port;

    public ServerArguments(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }
}
