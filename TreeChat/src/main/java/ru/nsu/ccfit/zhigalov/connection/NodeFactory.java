package ru.nsu.ccfit.zhigalov.connection;

import ru.nsu.ccfit.zhigalov.packet.PacketConstructor;
import ru.nsu.ccfit.zhigalov.packet.PacketSender;

import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;

public class NodeFactory {
    protected final PacketSender packetSender;
    protected final ScheduledExecutorService scheduler;
    protected final long resend_message_time_interval_millis;
    protected final long timeout_millis;
    protected final long ping_time_millis;

    public NodeFactory(PacketSender packetSender,
                       ScheduledExecutorService scheduler,
                       long resend_message_time_interval_millis,
                       long timeout_millis,
                       long ping_time_millis) {
        this.packetSender = packetSender;
        this.scheduler = scheduler;
        this.resend_message_time_interval_millis = resend_message_time_interval_millis;
        this.timeout_millis = timeout_millis;
        this.ping_time_millis = ping_time_millis;
    }


    public RemoteNode getNode(SocketAddress remoteAddress) {
        return new RemoteNode(
                resend_message_time_interval_millis,
                timeout_millis,
                ping_time_millis,
                new PacketConstructor(remoteAddress),
                packetSender,
                scheduler);
    }
}
