package ru.nsu.ccfit.zhiglaov.networklabs.networklab2.timetracker;

import java.util.concurrent.CopyOnWriteArrayList;

public class ReportPrinter {
    public void printReport(CopyOnWriteArrayList<Timer> timers) {
        if (timers.isEmpty()) {
            System.out.println();
            System.out.println("No connections");
            return;
        }
        String[][] table = new String[1 + timers.size()][3];
        table[0][0] = "Connection";
        table[0][1] = "Instant speed";
        table[0][2] = "Average speed";
        int i = 1;
        for (var timer : timers) {
            table[i][0] = timer.getName();
            table[i][1] = speedToString(timer.getInstantSpeed());
            table[i][2] = speedToString(timer.getAverageSpeed());
            if (timer.isFinished()) {
                timers.remove(timer);
            }
            i++;
        }
        printTable(table);
    }

    String speedToString(long speed) {
        if (speed < 0) {
            return "unknown";
        }
        double bytesPerSec = (double) speed * 1000 / (1024 * 1024);
        return String.format("%.3f Mbyte/s", bytesPerSec);
    }

    void printTable(String[][] table) {
        int[] lens = {20, 20, 20};
        String format = "| %-" + lens[0] + "s | %-" + lens[1] + "s | %-" + lens[2] + "s |%n";
        System.out.println();
        for (var row : table) {
            System.out.printf(format, (String[]) row);
        }
        System.out.println();
    }
}
