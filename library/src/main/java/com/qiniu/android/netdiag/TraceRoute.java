package com.qiniu.android.netdiag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Runtime.getRuntime;

/**
 * Created by bailong on 16/2/24.
 */
public final class TraceRoute implements Task {
    private static final int MaxHop = 31;
    private static final String Error = "network error";
    private static final String MATCH_TRACE_IP = "(?<=From )(?:[0-9]{1,3}\\.){3}[0-9]{1,3}";
    private static final String MATCH_PING_IP = "(?<=from ).*(?=: icmp_seq=1 ttl=)";
    private static final String MATCH_PING_TIME = "(?<=time=).*?ms";
    private final String address;
    private final Output output;
    private final Callback complete;
    private volatile boolean stopped = false;
    private Result result = null;

    private TraceRoute(String address, Output output, Callback complete) {
        this.address = address;
        this.output = output;
        this.complete = complete;
    }

    static Matcher traceMatcher(String str) {
        Pattern patternTrace = Pattern.compile(MATCH_TRACE_IP);
        return patternTrace.matcher(str);
    }

    static Matcher timeMatcher(String str) {
        Pattern patternTime = Pattern.compile(MATCH_PING_TIME);
        return patternTime.matcher(str);
    }

    static Matcher ipMatcher(String str) {
        Pattern patternIp = Pattern.compile(MATCH_PING_IP);
        return patternIp.matcher(str);
    }

    static String getIpFromTraceMatcher(Matcher m) {
        String pingIp = m.group();
        int start = pingIp.indexOf('(');
        if (start >= 0) {
            pingIp = pingIp.substring(start + 1);
        }
        return pingIp;
    }

    public static Task start(String address, Output output, Callback complete) {
        final TraceRoute t = new TraceRoute(address, output, complete);
        new Thread(new Runnable() {
            @Override
            public void run() {
                t.run();
            }
        }).start();
//        Util.runInBack(new Runnable() {
//            @Override
//            public void run() {
//                t.run();
//            }
//        });
        return t;
    }

    private static String getIp(String host) throws UnknownHostException {
        InetAddress i = InetAddress.getByName(host);
        return i.getHostAddress();
    }

    @Override
    public void stop() {
        stopped = true;
    }

    private Process executePingCmd(String host, int hop) throws IOException {
        String command = "ping -n -c 1 -t " + hop + " " + host;
//        System.out.println("cmd> " + command);
        return getRuntime().exec(command);
    }

    private String getPingtOutput(Process process) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        process.destroy();
        return text.toString();
    }

    private void printNormal(Matcher m, long time, StringBuilder lineBuffer) {
        String pingIp = getIpFromTraceMatcher(m);
        lineBuffer.append("\t");
        lineBuffer.append(pingIp);
        lineBuffer.append("\t\t");
        lineBuffer.append(time); // 近似值
        lineBuffer.append("ms\t");

        if (output != null) {
            output.write(lineBuffer.toString());
        }
        result.append(lineBuffer.toString());
    }

    private void printEnd(Matcher m, String out, StringBuilder lineBuffer) {
        String pingIp = m.group();
        Matcher matcherTime = timeMatcher(out);
        if (matcherTime.find()) {
            String time = matcherTime.group();
            lineBuffer.append("\t\t");
            lineBuffer.append(pingIp);
            lineBuffer.append("\t\t");
            lineBuffer.append(time);
            lineBuffer.append("\t");
            updateOut(lineBuffer.toString());
        }
    }

    private void updateOut(String str) {
        if (str != null) {
            output.write(str);
        }
        result.append(str);
    }

    private void run() {
        int hop = 1;
        String ip = null;
        try {
            ip = getIp(this.address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            result = new Result("");
            updateOut("unknown host " + this.address);
            this.complete.complete(result);
            return;
        }

        result = new Result(ip);
        Process p;
        while (hop < MaxHop && !stopped) {
            long t1 = System.currentTimeMillis();
            try {
                p = executePingCmd(ip, hop);
            } catch (IOException e) {
                e.printStackTrace();
                updateOut("ping cmd error " + e.getMessage());
                break;
            }
            long t2 = System.currentTimeMillis();
            String str = getPingtOutput(p);
//            System.out.println(">" + hop + " " + str.trim());
            if (str.length() == 0) {
                updateOut(Error);
                break;
            }
            Matcher m = traceMatcher(str);

            StringBuilder lineBuffer = new StringBuilder(256);
            lineBuffer.append(hop).append(".");
            if (m.find()) {
                printNormal(m, (t2 - t1) / 2, lineBuffer);
            } else {
                Matcher matchPingIp = ipMatcher(str);
                if (matchPingIp.find()) {
                    printEnd(matchPingIp, str, lineBuffer);
                    break;
                } else {
                    lineBuffer.append("\t\t * \t");
                    updateOut(lineBuffer.toString());
                }
            }
            hop++;
        }
        this.complete.complete(result);
    }

    public interface Callback {
        void complete(Result r);
    }

    public static class Result {
        public final String ip;
        private final StringBuilder builder = new StringBuilder();
        private String allData;

        public Result(String ip) {
            this.ip = ip;
        }

        public String content() {
            if (allData != null) {
                return allData;
            }
            allData = builder.toString();
            return allData;
        }

        private void append(String str) {
            builder.append(str);
        }
    }
}
