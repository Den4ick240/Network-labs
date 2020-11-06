package ru.nsu.ccfit.zhigalov.connection.deputies;

import ru.nsu.ccfit.zhigalov.connection.RemoteNode;
import ru.nsu.ccfit.zhigalov.message.ConfirmableMessage;
import ru.nsu.ccfit.zhigalov.message.Confirmation;
import ru.nsu.ccfit.zhigalov.message.DataMessage;
import ru.nsu.ccfit.zhigalov.message.Ping;
import ru.nsu.ccfit.zhigalov.packet.PacketConstructor;
import ru.nsu.ccfit.zhigalov.packet.PacketSender;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

public class RemoteNodeWithDeputies extends RemoteNode {
    private final Map<Confirmation, ConfirmableMessage> unconfirmedDataMessages;
    private SocketAddress deputy = null;

    public RemoteNodeWithDeputies(long resend_message_time_interval_millis,
                                  long timeout_millis,
                                  long ping_time_millis,
                                  PacketConstructor packetConstructor,
                                  PacketSender packetSender,
                                  ScheduledExecutorService scheduler,
                                  Map<Confirmation, ConfirmableMessage> unconfirmedDataMessages) {
        super(resend_message_time_interval_millis, timeout_millis, ping_time_millis, packetConstructor, packetSender, scheduler);
        this.unconfirmedDataMessages = unconfirmedDataMessages;
    }

    public boolean hasDeputy() {
        return deputy != null;
    }

    public void setDeputy(SocketAddress deputy) {
        log.info("New deputy is set for " + packetConstructor.constructPacket(new Ping()).getAddress() +
                "\n" + deputy);
        this.deputy = deputy;
    }

    public SocketAddress getDeputy() {
        return deputy;
    }


    public void sendUnconfirmedMessages(Collection<ConfirmableMessage> messages) {
        for (var message : messages) {
            resendMessageUntilDeliveryConfirmed(message);
        }
    }

    public synchronized Collection<ConfirmableMessage> getUnconfirmedDataMessages() {
        var out = new ArrayList<>(unconfirmedDataMessages.values());
        return out;
    }

    @Override
    public synchronized void confirmDelivery(Confirmation confirmation) {
        super.confirmDelivery(confirmation);
        unconfirmedDataMessages.remove(confirmation);
    }

    @Override
    protected synchronized void resendMessageUntilDeliveryConfirmed(ConfirmableMessage message) {
        super.resendMessageUntilDeliveryConfirmed(message);
        if (message instanceof DataMessage)
            unconfirmedDataMessages.put(new Confirmation(message), message);
    }
}
