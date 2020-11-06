package ru.nsu.ccfit.zhigalov.connection.saferebuilding;

import ru.nsu.ccfit.zhigalov.message.ConfirmableMessage;
import ru.nsu.ccfit.zhigalov.message.DeputyMessage;

import java.net.SocketAddress;
import java.util.UUID;

public class UnconfirmedMessagesRequest extends DeputyMessage {
    public UnconfirmedMessagesRequest(String uuid, String addr, String port) {
        super(uuid, addr, port);
    }

    public UnconfirmedMessagesRequest(UUID uuid, SocketAddress deputy) {
        super(uuid, deputy);
    }
}
