package ru.nsu.ccfit.zhigalov.packet;

import ru.nsu.ccfit.zhigalov.message.Message;

import java.net.SocketAddress;

public class PacketConstructor {
    final SocketAddress socketAddress;

    public PacketConstructor(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public Packet constructPacket(Message message) {
        return new Packet(socketAddress, message);
    }
}
