package ru.nsu.ccfit.zhigalov.message;

import java.util.Arrays;
import java.util.UUID;

public class DataMessage extends ConfirmableMessage {
    final byte[] data;

    public DataMessage(String uuid, String data) {
        super(uuid);
        this.data = data.getBytes();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return super.toString() + new String(data) + Message.separator;
    }

    public DataMessage(UUID uuid, byte[] data) {
        super(uuid);
        this.data = data;
    }
}
