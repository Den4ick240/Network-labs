package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

import java.io.File;
import java.io.IOException;

public interface ICheckSumCounter {
    byte[] getFileCheckSum(File file) throws IOException;
    ICheckSumCounter getInstance();
}
