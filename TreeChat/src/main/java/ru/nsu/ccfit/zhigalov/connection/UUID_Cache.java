package ru.nsu.ccfit.zhigalov.connection;

import java.util.Arrays;
import java.util.UUID;

public class UUID_Cache {
    final UUID[] buffer;
    int i = 0;
    final int size;

    public UUID_Cache(int size) {
        if (size < 0) throw new IllegalArgumentException("GuidCache size mustn't be lesser than zero: " + size);

        this.size = size;
        buffer = new UUID[size];
    }

    public boolean hasUUID(UUID u) {
        return Arrays.asList(buffer).contains(u);
    }

    public void addUUID(UUID u) {
        buffer[i] = u;
        i++;
        if (i == size) {
            i = 0;
        }
    }
}
