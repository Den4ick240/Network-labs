package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker;

public class Timer implements ITimer {
    private boolean finished = false;
    private volatile long startTime;
    private volatile long lastTime;
    private volatile long timeDifference;
    private volatile long bytesReadDifference;
    private volatile long totalBytesRead;
    private final String name;

    public Timer(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        startTime = lastTime = getTime();
        check(0);
    }

    @Override
    public void check(long totalBytesRead) {
        long newTime = getTime();
        timeDifference = newTime - lastTime;
        lastTime = newTime;
        bytesReadDifference = totalBytesRead - this.totalBytesRead;
        this.totalBytesRead = totalBytesRead;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public long getInstantSpeed() {
        return (timeDifference > 0) ? (bytesReadDifference / timeDifference) : -1;
    }

    @Override
    public long getAverageSpeed() {
        var diff = lastTime - startTime;
        return (diff > 0) ? (totalBytesRead / diff) : -1;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public String getName() {
        return name;
    }

    private long getTime() {
        return System.currentTimeMillis();
    }
}