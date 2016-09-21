package com.qiniu.android.netdiag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Created by bailong on 16/2/24.
 */
public final class Ping implements Task {
    private final String address;
    private final int count;
    private final int size;
    private final Output output;
    private final Callback complete;
    private volatile boolean stopped;
    private int interval;

    private Ping(String address, int count,

                 Output output, Callback complete) {
        this(address, count, 56, 200, output, complete);
    }

    private Ping(String address, int count, int size,
                 int interval, Output output, Callback complete) {
        this.address = address;
        this.count = count;
        this.size = size;
        this.interval = interval;
        this.output = output;
        this.complete = complete;
        this.stopped = false;
    }

    public static Task start(String address, Output output, Callback complete) {
        return start(address, 10, output, complete);
    }

    public static Task start(String address, int count,
                             Output output, Callback complete) {
        final Ping p = new Ping(address, count, output, complete);
        Util.runInBack(new Runnable() {
            @Override
            public void run() {
                p.run();
            }
        });
        return p;
    }

    private static String getIp(String host) throws UnknownHostException {
        InetAddress i = InetAddress.getByName(host);
        return i.getHostAddress();
    }

    private void run() {
        Ping.Result r = pingCmd();
        complete.complete(r);
    }

    private Ping.Result pingCmd() {
        String ip;
        try {
            ip = getIp(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new Result("", "", 0, 0);
        }
        String cmd = String.format(Locale.getDefault(), "ping -n -i %f -s %d -c %d %s", ((double) interval / 1000), size, count, ip);
        Process process = null;
        StringBuilder str = new StringBuilder();
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line;
            errorReader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
                output.write(line);
            }
            while ((line = errorReader.readLine()) != null) {
                str.append(line);
                output.write(line);
            }
            reader.close();
            errorReader.close();
            process.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Result(str.toString(), ip, size, interval);
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public interface Callback {
        void complete(Result r);
    }

    public static class Result {
        public final String result;
        public final String ip;
        public final int size;
        public final int interval;
        private final String lastLinePrefix = "rtt min/avg/max/mdev = ";
        private final String packetWords = " packets transmitted";
        private final String receivedWords = " received";
        public int sent;
        public int dropped;
        public float max;
        public float min;
        public float avg;
        public float stddev;
        public int count;

        Result(String result, String ip, int size, int interval) {
            this.result = result;
            this.ip = ip;
            this.size = size;
            this.interval = interval;
            parseResult();
        }

        private void parseRttLine(String s) {
            String s2 = s.substring(lastLinePrefix.length(), s.length() - 3);
            String[] l = s2.split("/");
            if (l.length != 4) {
                return;
            }
            min = Float.parseFloat(l[0]);
            avg = Float.parseFloat(l[1]);
            max = Float.parseFloat(l[2]);
            stddev = Float.parseFloat(l[3]);
        }

        private void parsePacketLine(String s) {
            String[] l = s.split(",");
            if (l.length != 4) {
                return;
            }
            if (l[0].length() > packetWords.length()) {
                String s2 = l[0].substring(0, l[0].length() - packetWords.length());
                count = Integer.parseInt(s2);
            }
            if (l[1].length() > receivedWords.length()) {
                String s3 = l[1].substring(0, l[1].length() - receivedWords.length());
                sent = Integer.parseInt(s3.trim());
            }
            dropped = count - sent;
        }

        private void parseResult() {
            String[] rs = result.split("\n");
            try {
                for (String s : rs) {
                    if (s.contains(packetWords)) {
                        parsePacketLine(s);
                    } else if (s.contains(lastLinePrefix)) {
                        parseRttLine(s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
