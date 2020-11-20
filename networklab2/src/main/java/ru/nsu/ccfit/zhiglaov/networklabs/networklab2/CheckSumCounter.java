package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumCounter implements ICheckSumCounter {
    static final int BUFFER_SIZE = 1024;
    final MessageDigest digest;

    CheckSumCounter(MessageDigest digest) {
        this.digest = digest;
    }

    public ICheckSumCounter getInstance() {
        try {
            return new CheckSumCounter(MessageDigest.getInstance(digest.getAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getFileCheckSum(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[BUFFER_SIZE];
        int bytesCount;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            update(byteArray, bytesCount);
        }
        fis.close();
        return getCheckSum();
    }

    public void update(byte[] byteArray, int bytesCount) {
        digest.update(byteArray, 0, bytesCount);
    }

    public byte[] getCheckSum() {
        return digest.digest();
    }
}
