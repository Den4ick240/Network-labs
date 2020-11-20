package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

import java.util.HashMap;
import java.util.Map;

public class InvalidHeaderException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid header received";
    }
}
