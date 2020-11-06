package ru.nsu.ccfit.zhigalov.connection.deputies;

import ru.nsu.ccfit.zhigalov.connection.NodeFactory;
import ru.nsu.ccfit.zhigalov.connection.RemoteNode;
import ru.nsu.ccfit.zhigalov.packet.PacketConstructor;
import ru.nsu.ccfit.zhigalov.packet.PacketSender;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

public class NodeFactoryWithDeputies extends NodeFactory {


    public NodeFactoryWithDeputies(PacketSender packetSender,
                                   ScheduledExecutorService scheduler,
                                   long resend_message_time_interval_millis,
                                   long timeout_millis,
                                   long ping_time_millis) {
        super(packetSender, scheduler, resend_message_time_interval_millis, timeout_millis, ping_time_millis);
    }

    @Override
    public RemoteNode getNode(SocketAddress remoteAddress) {
        return new RemoteNodeWithDeputies(
                resend_message_time_interval_millis,
                timeout_millis,
                ping_time_millis,
                new PacketConstructor(remoteAddress),
                packetSender,
                scheduler,
                new HashMap<>());
    }
}
