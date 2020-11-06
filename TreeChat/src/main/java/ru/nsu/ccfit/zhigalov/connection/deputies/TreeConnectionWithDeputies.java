package ru.nsu.ccfit.zhigalov.connection.deputies;

import ru.nsu.ccfit.zhigalov.connection.NodeFactory;
import ru.nsu.ccfit.zhigalov.connection.RemoteNode;
import ru.nsu.ccfit.zhigalov.connection.TreeConnection;
import ru.nsu.ccfit.zhigalov.connection.UUID_Cache;
import ru.nsu.ccfit.zhigalov.message.DeputyMessage;
import ru.nsu.ccfit.zhigalov.packet.Packet;
import ru.nsu.ccfit.zhigalov.packet.PacketStream;

import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class TreeConnectionWithDeputies extends TreeConnection {
    protected volatile SocketAddress deputy;

    public TreeConnectionWithDeputies(DataHandler dataHandler,
                                      PacketStream packetReceiver,
                                      ConcurrentHashMap<SocketAddress, RemoteNode> remoteNodes,
                                      ScheduledExecutorService scheduler,
                                      UUID_Cache uuidCache,
                                      long connectionCleanerTimeIntervalMillis,
                                      NodeFactory nodeFactory) {
        super(dataHandler, packetReceiver, remoteNodes, scheduler, uuidCache, connectionCleanerTimeIntervalMillis, nodeFactory);
    }

    @Override
    protected void handlePacket(Packet packet, RemoteNode remoteNode) {
        super.handlePacket(packet, remoteNode);

        if (packet.getMessage().getClass().equals(DeputyMessage.class)) {
            var remoteDeputy = ((DeputyMessage) packet.getMessage()).getDeputy();
            ((RemoteNodeWithDeputies) remoteNode).setDeputy(remoteDeputy);
        }
    }

    @Override
    protected synchronized RemoteNode addRemoteNode(SocketAddress remoteAddress) {
        var newNode = super.addRemoteNode(remoteAddress);

        if (deputy == null) {
            deputy = remoteAddress;
            log.info("Setting deputy " + deputy.toString());
        } else {
            log.info("Sending deputy " + deputy.toString() + " to " + remoteAddress.toString());
            var message = new DeputyMessage(UUID.randomUUID(), deputy);
            newNode.sendMessage(message);
        }

        return newNode;
    }

    @Override
    protected synchronized void nodeDisconnection(SocketAddress nodeAddress, RemoteNode node) {
        super.nodeDisconnection(nodeAddress, node);
        connectToDeputyIfThereIsOne((RemoteNodeWithDeputies) node);

        boolean myDeputyWasRemoved = nodeAddress == deputy;
        if (myDeputyWasRemoved)
            setNewDeputy();
    }


    protected void connectToDeputyIfThereIsOne(RemoteNodeWithDeputies remoteNode) {
        if (!remoteNode.hasDeputy()) return;

        var deputyAddr = remoteNode.getDeputy();
        var newNode = (RemoteNodeWithDeputies) getRemoteNode(deputyAddr);
        requestConnection(deputyAddr);
        var dataMessages = remoteNode.getUnconfirmedDataMessages();
        newNode.sendUnconfirmedMessages(dataMessages);
    }

    private synchronized void setNewDeputy() {
        log.info("Choosing new deputy");
        RemoteNode newDeputyRemoteNode = null;
        deputy = null;

        if (remoteNodes.size() > 0) {
            var entry = remoteNodes.entrySet().iterator().next();
            deputy = entry.getKey();
            newDeputyRemoteNode = entry.getValue();
            var msg = new DeputyMessage(UUID.randomUUID(), null);
            newDeputyRemoteNode.sendMessage(msg);
        }

        log.info("New deputy " + deputy);
        var message = new DeputyMessage(UUID.randomUUID(), deputy);
        sendMessageToAllNodesExceptOne(message, newDeputyRemoteNode);
    }
}
