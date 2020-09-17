import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    final static int MESSAGE_TIME_INTERVAL = 1000;
    final static int TIMEOUT_INTERVAL = 1200;
    final static int PORT = 6789;
    final static String CODE_WORD = "anime is life";
    final static String IP_ADDR = "224.240.240.240";
    final static String IP6_ADDR = "0:0:0:0:0:ffff:e0f0:f0f0";
    final static int BUFFER_SIZE = 1024;

    public static void main(String args[]) {
        try {
            Map<String, Long> appsOnline = new ConcurrentHashMap<>();

            String ip_addr;
            if (args.length < 2) {
                ip_addr = IP6_ADDR;
            } else {
                ip_addr = args[1];
            }
            ScheduledThreadPoolExecutor threadPool;
            InetAddress group = Inet6Address.getByName(ip_addr);
            MulticastSocket sendSocket = new MulticastSocket();
            MulticastSocket recvSocket = new MulticastSocket(PORT);
            sendSocket.joinGroup(group);
            recvSocket.joinGroup(group);
            threadPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);

            Runnable taskSend = () -> {
                DatagramPacket packet = new DatagramPacket(CODE_WORD.getBytes(), CODE_WORD.length(), group, PORT);
                try {
                    sendSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("\n");
                for (var a : appsOnline.keySet()) {
                    System.out.println(a);
                }
            };
            Runnable taskRecv = () -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket receivedMsg = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    try {
                        recvSocket.receive(receivedMsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String receivedWord = new String(receivedMsg.getData());
                    if ((receivedWord.trim()).equals(CODE_WORD)) {
                        appsOnline.put(receivedMsg.getAddress().toString() + ":" + String.valueOf(receivedMsg.getPort()),
                                (new Date()).getTime());
                    }
                }
            };
            Runnable taskCleaner = () -> {
                long ptime = (new Date()).getTime();
                for (var a : appsOnline.entrySet()) {
                    long time = a.getValue();
                    if (ptime - time > TIMEOUT_INTERVAL) {
                        appsOnline.remove(a.getKey());
                        continue;
                    }
                    appsOnline.put(a.getKey(), time);
                }
            };
            threadPool.scheduleAtFixedRate(taskSend, 0, MESSAGE_TIME_INTERVAL, TimeUnit.MILLISECONDS);
            (new Thread(taskRecv)).start();
            threadPool.scheduleAtFixedRate(taskCleaner, 0, 1, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
