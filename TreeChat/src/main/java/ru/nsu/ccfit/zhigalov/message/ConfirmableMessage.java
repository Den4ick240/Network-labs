package ru.nsu.ccfit.zhigalov.message;

import java.util.UUID;

public class ConfirmableMessage extends Message {
    private final UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public ConfirmableMessage(UUID uuid) {
        this.uuid = uuid;
    }

    public ConfirmableMessage(String str) {
        this.uuid = UUID.fromString(str);
    }

    @Override
    public String toString() {
        return super.toString() + uuid.toString() + separator;
    }

}
