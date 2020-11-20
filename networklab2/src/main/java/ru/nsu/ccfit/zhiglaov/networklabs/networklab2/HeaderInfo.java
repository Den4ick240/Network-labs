package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;

public class HeaderInfo {
    public HeaderInfo(String fileName, Long fileSize, byte[] fileChecksum) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileChecksum = fileChecksum;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public byte[] getFileChecksum() {
        return fileChecksum;
    }

    private final String fileName;
    private final Long fileSize;
    private final byte[] fileChecksum;
}
