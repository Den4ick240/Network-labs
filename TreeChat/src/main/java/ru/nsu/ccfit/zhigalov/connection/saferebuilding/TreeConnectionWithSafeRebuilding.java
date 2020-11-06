package ru.nsu.ccfit.zhigalov.connection.saferebuilding;

import ru.nsu.ccfit.zhigalov.connection.NodeFactory;
import ru.nsu.ccfit.zhigalov.connection.RemoteNode;
import ru.nsu.ccfit.zhigalov.connection.UUID_Cache;
import ru.nsu.ccfit.zhigalov.connection.deputies.RemoteNodeWithDeputies;
import ru.nsu.ccfit.zhigalov.connection.deputies.TreeConnectionWithDeputies;
import ru.nsu.ccfit.zhigalov.message.ConfirmableMessage;
import ru.nsu.ccfit.zhigalov.packet.Packet;
import ru.nsu.ccfit.zhigalov.packet.PacketStream;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class TreeConnectionWithSafeRebuilding extends TreeConnectionWithDeputies {

    final Map<SocketAddress, Collection<ConfirmableMessage>> unconfirmedMessagesBuffer;

    public TreeConnectionWithSafeRebuilding(DataHandler dataHandler,
                                            PacketStream packetReceiver,
                                            ConcurrentHashMap<SocketAddress, RemoteNode> remoteNodes,
                                            ScheduledExecutorService scheduler,
                                            UUID_Cache uuidCache,
                                            long connectionCleanerTimeIntervalMillis,
                                            NodeFactory nodeFactory,
                                            Map<SocketAddress, Collection<ConfirmableMessage>> unconfirmedMessagesBuffer) {
        super(dataHandler, packetReceiver, remoteNodes, scheduler, uuidCache, connectionCleanerTimeIntervalMillis,
                nodeFactory);
        this.unconfirmedMessagesBuffer = unconfirmedMessagesBuffer;
    }

    @Override
    protected void handlePacket(Packet packet, RemoteNode remoteNode) {
        super.handlePacket(packet, remoteNode);
        var message = packet.getMessage();

        if (message.getClass().equals(UnconfirmedMessagesRequest.class)) {
            handleUnconfirmedMessagesRequest(remoteNode, (UnconfirmedMessagesRequest) message);
        }
    }

    protected void handleUnconfirmedMessagesRequest(RemoteNode remoteNode, UnconfirmedMessagesRequest request) {
        var uuid = request.getUuid();
        if (uuidCache.hasUUID(uuid)) {
            log.info("Repeated uuid for unconfirmed message request");
            return;
        }
        uuidCache.addUUID(uuid);

        Collection<ConfirmableMessage> unconfirmedMessages = unconfirmedMessagesBuffer.get(request.getDeputy());
        var unconfirmedNode = (RemoteNodeWithDeputies) remoteNodes.get(request.getDeputy());
        if (unconfirmedNode != null) {
            unconfirmedMessages = unconfirmedNode.getUnconfirmedDataMessages();
        } else if (unconfirmedMessages == null) {
            log.info("Unconfirmed messages for " + request.getDeputy() + " not found");
            return;
        }

        log.info("Sending unconfirmed messages");
        ((RemoteNodeWithDeputies) remoteNode).sendUnconfirmedMessages(unconfirmedMessages);

    }

    @Override
    protected synchronized void nodeDisconnection(SocketAddress nodeAddress, RemoteNode node) {
        super.nodeDisconnection(nodeAddress, node);
        var remoteNode = (RemoteNodeWithDeputies) node;

        if (nodeAddress == deputy)
            unconfirmedMessagesBuffer.put(nodeAddress, remoteNode.getUnconfirmedDataMessages());

        if (remoteNode.hasDeputy()) {
            getRemoteNode(remoteNode.getDeputy())
                    .sendMessage(new UnconfirmedMessagesRequest(UUID.randomUUID(), nodeAddress));
            log.info("Sending request to " + remoteNode.getDeputy());
        }
    }


}
