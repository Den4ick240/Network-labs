package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker;

import java.util.concurrent.CopyOnWriteArrayList;

public class TimeTracker implements ITimeTracker {
    private final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<>();
    private final ReportPrinter reportPrinter = new ReportPrinter();

    @Override
    public void run() {
        reportPrinter.printReport(timers);
    }

    @Override
    public ITimer getTimer(String name) {
        Timer timer = new Timer(name);
        timers.add(timer);
        return timer;
    }
}
