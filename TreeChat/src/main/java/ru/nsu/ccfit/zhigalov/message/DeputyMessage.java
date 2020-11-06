package ru.nsu.ccfit.zhigalov.message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.UUID;

public class DeputyMessage extends ConfirmableMessage {
    static final String NULL_ADDRESS = "null";
    final SocketAddress deputy;

    public DeputyMessage(String uuid, String addr, String port) {
        super(uuid);
        if (addr.equals(NULL_ADDRESS))
            deputy = null;
        else
            deputy = new InetSocketAddress(addr, Integer.parseInt(port));
    }

    @Override
    public int hashCode() {
        if (deputy == null)
            return 0;
        return Arrays.hashCode(deputy.toString().getBytes());
    }

    public SocketAddress getDeputy() {
        return deputy;
    }

    @Override
    public String toString() {
        String str;
        if (deputy == null) {
            str = "null" + separator + "" + separator;
        } else {
            String addr = ((InetSocketAddress) deputy).getHostString();
            int port = ((InetSocketAddress) deputy).getPort();
            str = addr + separator + Integer.toString(port) + separator;
        }
        return super.toString() + str;
    }

    public DeputyMessage(UUID uuid, SocketAddress deputy) {
        super(uuid);
        this.deputy = deputy;
    }
}
