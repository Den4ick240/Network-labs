package ru.nsu.ccfit.zhigalov.packet;

import java.io.IOException;

public interface PacketReceiver {
    Packet receivePacket() throws IOException;
}
