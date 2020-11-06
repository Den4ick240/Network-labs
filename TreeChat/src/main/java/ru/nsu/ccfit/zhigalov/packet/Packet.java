package ru.nsu.ccfit.zhigalov.packet;

import ru.nsu.ccfit.zhigalov.message.Message;

import java.net.SocketAddress;

public class Packet {
    SocketAddress address;
    Message message;

    public SocketAddress getAddress() {
        return address;
    }

    public Message getMessage() {
        return message;
    }

    public Packet(SocketAddress address, Message message) {
        this.address = address;
        this.message = message;
    }
}
