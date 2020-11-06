package ru.nsu.ccfit.zhigalov.message;

import java.util.UUID;

public class ConnectionRequestMessage extends ConfirmableMessage {
    public ConnectionRequestMessage(UUID uuid) {
        super(uuid);
    }

    public ConnectionRequestMessage(String arg) {
        super(arg);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
