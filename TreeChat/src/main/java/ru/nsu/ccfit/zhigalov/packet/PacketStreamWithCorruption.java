package ru.nsu.ccfit.zhigalov.packet;

import java.io.IOException;
import java.util.Random;

public class PacketStreamWithCorruption extends PacketStreamWithLosses {
    private final int corruptionPercent;

    public PacketStreamWithCorruption(int corruptionPercent, int lossPercentReceive, int lossPercentSend, Random randomizer, PacketStream packetStream) {
        super(lossPercentReceive, lossPercentSend, randomizer, packetStream);
        this.corruptionPercent = corruptionPercent;
    }

    @Override
    public Packet receivePacket() throws IOException {
        var packet = super.receivePacket();
        if (chance(corruptionPercent))
            packet = corrupt(packet);
        return packet;
    }

    private Packet corrupt(Packet packet) {
        return packet;
    }

    @Override
    public void sendPacket(Packet packet) throws IOException {
        super.sendPacket(packet);
    }
}
