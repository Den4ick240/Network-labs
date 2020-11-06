package ru.nsu.ccfit.zhigalov.packet;

import java.io.IOException;

public interface PacketSender {
    void sendPacket(Packet packet) throws IOException;
}
