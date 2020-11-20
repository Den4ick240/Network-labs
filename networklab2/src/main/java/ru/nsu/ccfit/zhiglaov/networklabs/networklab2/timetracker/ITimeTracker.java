package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker;

public interface ITimeTracker extends Runnable {
    public void run();
    public ITimer getTimer(String name);
}
