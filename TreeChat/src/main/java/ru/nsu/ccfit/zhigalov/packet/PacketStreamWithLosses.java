package ru.nsu.ccfit.zhigalov.packet;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

public class PacketStreamWithLosses implements PacketStream {
    private final int lossPercentReceive;
    private final int lossPercentSend;
    private final Random randomizer;
    private final PacketStream packetStream;
    private static final Logger log = Logger.getLogger(PacketStreamWithLosses.class.getName());

    public PacketStreamWithLosses(int lossPercentReceive, int lossPercentSend, Random randomizer, PacketStream packetStream) {
        this.lossPercentReceive = lossPercentReceive;
        this.lossPercentSend = lossPercentSend;
        this.randomizer = randomizer;
        this.packetStream = packetStream;


        boolean lossPercentSendValid = lossPercentSend >= 0 && lossPercentSend <= 100;
        boolean lossPercentReceiveValid = lossPercentReceive >= 0 && lossPercentReceive <= 100;
        if (!lossPercentReceiveValid || !lossPercentSendValid)
            throw new IllegalArgumentException("loss percent must be between 0 and 100");
    }

    @Override
    public void sendPacket(Packet packet) throws IOException {
        if (chance(lossPercentSend))
            packetStream.sendPacket(packet);
        else
            log.info("Packet lost to " + packet.getAddress().toString());
    }

    @Override
    public Packet receivePacket() throws IOException {
         while (true) {
             var out = packetStream.receivePacket();
             if (chance(lossPercentReceive))
                 return out;
             else
                 log.info("Packet lost from " + out.getAddress().toString());
         }
    }

    protected boolean chance(int lossPercent) {
        int n = randomizer.nextInt(99);
        return n >= lossPercent;
    }
}
