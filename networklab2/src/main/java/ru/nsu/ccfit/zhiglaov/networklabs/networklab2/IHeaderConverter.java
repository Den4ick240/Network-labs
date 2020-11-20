package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

public interface IHeaderConverter {
    HeaderInfo byteArrayToHeaderInfo(byte[] header) throws InvalidHeaderException;
    byte[] headerInfoToByteArray(HeaderInfo info);
    IHeaderConverter getInstance();
}
