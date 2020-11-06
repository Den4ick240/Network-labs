package ru.nsu.ccfit.zhigalov.message;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public static final String separator = "\n";
    @Override
    public String toString() {
        return this.getClass().getName() + separator;
    }
}
