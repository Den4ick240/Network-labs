package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

public class HeaderConverter implements IHeaderConverter {
    static final Logger logger = Logger.getLogger(HeaderConverter.class.getName());
    int pos;
    int headerLength;

    @Override
    public IHeaderConverter getInstance() {
        return this;
    }

    private void check(int len) throws InvalidHeaderException {
        if (len > headerLength + pos) throw new InvalidHeaderException();
    }

    @Override
    public HeaderInfo byteArrayToHeaderInfo(byte[] header) throws InvalidHeaderException {
        pos = 0;
        headerLength = header.length;
        logger.info("Header length: " + headerLength);
        check(Integer.BYTES);
        int fileNameLen = new BigInteger(Arrays.copyOfRange(header, pos, pos += Integer.BYTES)).intValue();
        logger.info("File name length: " + fileNameLen);
        check(fileNameLen);
        String fileName = new String(Arrays.copyOfRange(header, pos, pos += fileNameLen));
        logger.info("File name: " + fileName);
        check(Long.BYTES);
        long fileSize = new BigInteger(Arrays.copyOfRange(header, pos, pos += Long.BYTES)).longValue();
        logger.info("File size: " + fileSize);
        check(Integer.BYTES);
        int checksumLen = new BigInteger(Arrays.copyOfRange(header, pos, pos += Integer.BYTES)).intValue();
        logger.info("File checksum length: " + checksumLen);
        check(checksumLen);
        byte[] fileChecksum = Arrays.copyOfRange(header, pos, pos += checksumLen);
        logger.info("File checksum: " + new String(fileChecksum));
        logger.info("Header is valid");
        return new HeaderInfo(fileName, fileSize, fileChecksum);
    }

    private byte[] integerToByteArray(int n) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(n).array();
    }

    private byte[] longToByteArray(long n) {
        return ByteBuffer.allocate(Long.BYTES).putLong(n).array();
    }

    @Override
    public byte[] headerInfoToByteArray(HeaderInfo info) {
        int headerLen = Integer.BYTES * 2  + Long.BYTES + info.getFileName().length() + info.getFileChecksum().length;
        byte[] header = new byte[headerLen];
        int pos = 0;
        System.arraycopy(integerToByteArray(info.getFileName().length()), 0, header, pos, Integer.BYTES);
        pos += Integer.BYTES;
        System.arraycopy(info.getFileName().getBytes(), 0, header, pos, info.getFileName().length());
        pos += info.getFileName().length();
        System.arraycopy(longToByteArray(info.getFileSize()), 0, header, pos, Long.BYTES);
        pos += Long.BYTES;
        System.arraycopy(integerToByteArray(info.getFileChecksum().length), 0, header, pos, Integer.BYTES);
        pos += Integer.BYTES;
        System.arraycopy(info.getFileChecksum(), 0, header, pos, info.getFileChecksum().length);
        return header;
    }
}
