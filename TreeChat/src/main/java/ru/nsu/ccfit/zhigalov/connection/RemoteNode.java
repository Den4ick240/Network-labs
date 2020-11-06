package ru.nsu.ccfit.zhigalov.connection;

import ru.nsu.ccfit.zhigalov.message.*;
import ru.nsu.ccfit.zhigalov.packet.PacketConstructor;
import ru.nsu.ccfit.zhigalov.packet.PacketSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteNode {
    private final long RESEND_MESSAGE_TIME_INTERVAL_MILLIS;
    private final long TIMEOUT_MILLIS;
    private final long PING_TIME_MILLIS;
    protected final PacketConstructor packetConstructor;
    private final PacketSender packetSender;
    private final Map<Confirmation, ScheduledFuture<?>> resendingMessageMap;
    private final ScheduledExecutorService scheduler;
    protected static final Logger log = Logger.getLogger(RemoteNode.class.getName());
    private ScheduledFuture<?> pingScheduledFuture;
    private long lastReceivedMessageTime;

    public RemoteNode(long resend_message_time_interval_millis, long timeout_millis, long ping_time_millis, PacketConstructor packetConstructor,
                      PacketSender packetSender,
                      ScheduledExecutorService scheduler) {
        RESEND_MESSAGE_TIME_INTERVAL_MILLIS = resend_message_time_interval_millis;
        TIMEOUT_MILLIS = timeout_millis;
        PING_TIME_MILLIS = ping_time_millis;
        this.packetConstructor = packetConstructor;
        this.packetSender = packetSender;
        this.scheduler = scheduler;
        resendingMessageMap = new HashMap<>();
        startSendingPing();
    }

    public void sendMessage(Message message) {
        log.info("Sending message " + message);
        if (message instanceof ConfirmableMessage) {
            resendMessageUntilDeliveryConfirmed((ConfirmableMessage) message);
        } else {
            sendMessageOnce(message);
        }
    }

    public synchronized void confirmDelivery(Confirmation confirmation) {
        var scheduledFuture = resendingMessageMap.get(confirmation);
        if (scheduledFuture != null) {
            log.info("Confirming delivery " + confirmation);
            scheduledFuture.cancel(false);
            resendingMessageMap.remove(confirmation);
        } else {
            log.warning("Received confirmation is unexpected " + confirmation);
        }
    }

    public synchronized void close() {
        pingScheduledFuture.cancel(false);
        for (var task : resendingMessageMap.values()) {
            task.cancel(false);
        }
        sendMessageOnce(new Disconnect());
    }

    public void updateLastReceivedMessageTime() {
        lastReceivedMessageTime = getCurrentTime();
    }

    public boolean isTimedOut() {
        long timeWithoutReceivingMessage = getCurrentTime() - lastReceivedMessageTime;
        return timeWithoutReceivingMessage > TIMEOUT_MILLIS;
    }

    private void startSendingPing() {
        pingScheduledFuture = scheduler.scheduleAtFixedRate(
                () -> sendMessageOnce(new Ping()),
                PING_TIME_MILLIS, PING_TIME_MILLIS, TimeUnit.MILLISECONDS);
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    protected synchronized void resendMessageUntilDeliveryConfirmed(ConfirmableMessage message) {
        var expectedConfirmation = new Confirmation(message);
        log.info("Scheduling message resending with expected confirmation: " + expectedConfirmation + "\n" +
                message);
        resendingMessageMap.put(expectedConfirmation, scheduleMessageResending(message));
    }

    private ScheduledFuture<?> scheduleMessageResending(ConfirmableMessage message) {
        return scheduler.scheduleAtFixedRate(() -> {
                    sendMessageOnce(message);
                    log.info("Resending message " + message.toString());
                },
                0, RESEND_MESSAGE_TIME_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void sendMessageOnce(Message message) {
        var packet = packetConstructor.constructPacket(message);
        try {
            packetSender.sendPacket(packet);
        } catch (IOException e) {
            log.log(Level.WARNING, "Couldn't send message", e);
        }
    }

}
