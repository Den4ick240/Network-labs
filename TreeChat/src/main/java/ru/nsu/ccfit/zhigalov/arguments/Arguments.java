package ru.nsu.ccfit.zhigalov.arguments;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Arguments {
    public final SocketAddress remoteSocketAddress;
    public final int localPort;
    public final String name;
    public final int lossPercent;

    public Arguments(String name, int localPort, int lossPercent) {
        this(name, localPort, lossPercent, null);
    }

    public Arguments(String name,  int localPort, int lossPercent, SocketAddress remoteSocketAddress) {
        this.localPort = localPort;
        this.name = name;
        this.lossPercent = lossPercent;
        this.remoteSocketAddress = remoteSocketAddress;
    }

    public boolean hasRemoteSocketAddress() {
        return remoteSocketAddress != null;
    }
}
