package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker;

public interface ITimer {
    void start();
    void check(long totalBytesRead);
    void finish();
    long getInstantSpeed();
    long getAverageSpeed();
    boolean isFinished();
    String getName();
}
