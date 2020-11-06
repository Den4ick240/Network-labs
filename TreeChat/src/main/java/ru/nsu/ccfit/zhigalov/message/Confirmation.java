package ru.nsu.ccfit.zhigalov.message;

import java.util.Objects;
import java.util.UUID;

public class Confirmation extends Message {
    private final UUID uuid;
    private final int messageHash;

    public Confirmation(ConfirmableMessage message) {
        this.uuid = message.getUuid();
        this.messageHash = message.hashCode();
    }

    public Confirmation(String uuid, String hashCode) {
        this.uuid = UUID.fromString(uuid);
        this.messageHash = Integer.parseInt(hashCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Confirmation that = (Confirmation) o;
        return messageHash == that.messageHash &&
                uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return messageHash;
    }

    @Override
    public String toString() {
        return super.toString() + uuid.toString() + separator + Integer.toString(messageHash) + separator;
    }
}
