package ru.nsu.ccfit.zhigalov.packet;

import ru.nsu.ccfit.zhigalov.message.MessageConverter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class DatagramSocketPacketStream implements PacketStream {
    final DatagramSocket socket;
    final MessageConverter messageConverter;
    final byte[] buffer;
    final static Logger log = Logger.getLogger(DatagramSocketPacketStream.class.getName());

    public DatagramSocketPacketStream(DatagramSocket socket, MessageConverter messageConverter, int bufferSize) {
        this.socket = socket;
        this.messageConverter = messageConverter;
        buffer = new byte[bufferSize];
    }

    public Packet receivePacket() throws IOException {
        var datagramPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagramPacket);
        var message = messageConverter.bytesToMessageObject(datagramPacket.getData());
        return new Packet(datagramPacket.getSocketAddress(), message);
    }

    public void sendPacket(Packet packet) throws IOException {
        var data = messageConverter.messageObjectToBytes(packet.getMessage());
        var datagramPacket = new DatagramPacket(data, data.length, packet.getAddress());
        socket.send(datagramPacket);
    }
}
