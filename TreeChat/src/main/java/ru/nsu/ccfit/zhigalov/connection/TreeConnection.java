package ru.nsu.ccfit.zhigalov.connection;

import ru.nsu.ccfit.zhigalov.message.*;
import ru.nsu.ccfit.zhigalov.packet.Packet;
import ru.nsu.ccfit.zhigalov.packet.PacketReceiver;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TreeConnection implements Runnable {

    public interface DataHandler {
        void handle(byte[] data);
    }

    private final DataHandler dataHandler;
    private final PacketReceiver packetReceiver;
    protected final ConcurrentHashMap<SocketAddress, RemoteNode> remoteNodes;
    protected final UUID_Cache uuidCache;
    protected static final Logger log = Logger.getLogger(TreeConnection.class.getName());
    private final ScheduledFuture<?> connectionCleanerScheduledFuture;
    protected volatile boolean isRunning = true;
    private final NodeFactory nodeFactory;

    public void stopRunning() {
        isRunning = false;
    }

    public TreeConnection(DataHandler dataHandler,
                          PacketReceiver packetReceiver,
                          ConcurrentHashMap<SocketAddress, RemoteNode> remoteNodes,
                          ScheduledExecutorService scheduler,
                          UUID_Cache uuidCache,
                          long connectionCleanerTimeIntervalMillis,
                          NodeFactory nodeFactory) {
        this.dataHandler = dataHandler;
        this.packetReceiver = packetReceiver;
        this.remoteNodes = remoteNodes;
        this.uuidCache = uuidCache;
        this.nodeFactory = nodeFactory;

        connectionCleanerScheduledFuture = scheduler.scheduleAtFixedRate(this::removeTimedOutNodes,
                connectionCleanerTimeIntervalMillis, connectionCleanerTimeIntervalMillis, TimeUnit.MILLISECONDS);
    }

    protected synchronized void removeTimedOutNodes() {
        var iter = remoteNodes.entrySet().iterator();
        while (iter.hasNext()) {
            var node = iter.next();
            if (node.getValue().isTimedOut()) {
                log.info("Connection timed out " + node.getKey().toString());
                iter.remove();
                nodeDisconnection(node.getKey(), node.getValue());
            }
        }
    }

    public void send(byte[] data) {
        var uuid = UUID.randomUUID();
        var message = new DataMessage(uuid, data);
        uuidCache.addUUID(uuid);
        sendMessageToAllNodes(message);
    }

    protected void sendMessageToAllNodes(Message message) {
        sendMessageToAllNodesExceptOne(message, null);
    }

    protected synchronized void sendMessageToAllNodesExceptOne(Message message, RemoteNode excludedNode) {
        for (var remoteNode : remoteNodes.values()) {
            boolean thisNodeIsExcluded = remoteNode.equals(excludedNode);
            if (!thisNodeIsExcluded) {
                remoteNode.sendMessage(message);
            }
        }
    }

    public void requestConnection(SocketAddress remoteAddress) {
        var node = getRemoteNode(remoteAddress);
        node.sendMessage(new ConnectionRequestMessage(UUID.randomUUID()));
    }

    protected synchronized RemoteNode addRemoteNode(SocketAddress remoteAddress) {
        var remoteNode = nodeFactory.getNode(remoteAddress);
        remoteNodes.put(remoteAddress, remoteNode);
        log.info("new node " + remoteAddress.toString());
        return remoteNode;
    }

    protected synchronized RemoteNode getRemoteNode(SocketAddress remoteAddress) {
        RemoteNode remoteNode;
        remoteNode = remoteNodes.get(remoteAddress);
        if (remoteNode == null)
            remoteNode = addRemoteNode(remoteAddress);
        return remoteNode;
    }

    protected synchronized void nodeDisconnection(SocketAddress nodeAddress, RemoteNode remoteNode) {
        log.info("removing node " + nodeAddress);
        remoteNode.close();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                var packet = packetReceiver.receivePacket();
                handlePacket(packet);
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "packetStream threw exception", e);
        }
    }

    public void close() {
        for (var node : remoteNodes.values())
            node.close();
        connectionCleanerScheduledFuture.cancel(false);
    }

    protected void handlePacket(Packet packet) {
        var remoteNode = getRemoteNode(packet.getAddress());
        remoteNode.updateLastReceivedMessageTime();
        handlePacket(packet, remoteNode);
    }

    protected void handlePacket(Packet packet, RemoteNode remoteNode) {
        var message = packet.getMessage();
        logPacket(packet);

        if (message instanceof ConfirmableMessage)
            remoteNode.sendMessage(new Confirmation((ConfirmableMessage) message));

        if (message instanceof DataMessage)
            handleDataMessage((DataMessage) message, remoteNode);

        if (message.getClass().equals(Confirmation.class))
            remoteNode.confirmDelivery((Confirmation) message);

        if (message.getClass().equals(Disconnect.class)) {
            remoteNodes.remove(packet.getAddress());
            nodeDisconnection(packet.getAddress(), remoteNode);
        }
    }

    private void logPacket(Packet packet) {
        var message = packet.getMessage();
        if (!(message instanceof Ping))
            log.info("Message received from " + packet.getAddress().toString() + " " +
                    message.getClass().getName());
    }

    private void handleDataMessage(DataMessage message, RemoteNode remoteNode) {
        boolean isRepeatedMessage = uuidCache.hasUUID(message.getUuid());
        if (!isRepeatedMessage) {
            uuidCache.addUUID(message.getUuid());
            sendMessageToAllNodesExceptOne(message, remoteNode);
            dataHandler.handle(message.getData());
        }
    }

}
