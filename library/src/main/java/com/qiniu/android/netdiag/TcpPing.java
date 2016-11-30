package com.qiniu.android.netdiag;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by bailong on 16/2/24.
 */
public final class TcpPing implements Task {

    public static final int TimeOut = -3;
    public static final int NotReach = -2;
    public static final int UnkownHost = -4;
    public static final int Stopped = -1;
    private final String host;
    private final int port;
    private final int count;
    private final Callback complete;
    private boolean stopped;
    private Output output;

    private TcpPing(String host, int port, int count, Output output, Callback complete) {
        this.host = host;
        this.port = port;
        this.count = count;
        this.complete = complete;
        this.output = output;
    }

    public static Task start(String host, Output output, Callback complete) {
        return start(host, 80, 3, output, complete);
    }

    public static Task start(String host, int port, int count
            , Output output, Callback complete) {
        final TcpPing t = new TcpPing(host, port, count, output, complete);
        Util.runInBack(new Runnable() {
            @Override
            public void run() {
                t.run();
            }
        });
        return t;
    }

    private void run() {
        InetAddress[] addrs = null;
        try {
            addrs = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            output.write("Unknown host: " + host);
            Util.runInMain(new Runnable() {
                @Override
                public void run() {
                    complete.complete(new Result(UnkownHost, "", 0, 0, 0, 0, 0, 0));
                }
            });
            return;
        }

        final String ip = addrs[0].getHostAddress();
        InetSocketAddress server = new InetSocketAddress(ip, port);
        output.write("connect to " + ip + ":" + port);
        int[] times = new int[count];
        int index = -1;
        int dropped = 0;
        for (int i = 0; i < count && !stopped; i++) {
            long start = System.currentTimeMillis();
            try {
                connect(server, 20 * 1000);
            } catch (IOException e) {
                e.printStackTrace();
                output.write(e.getMessage());
                int code = NotReach;
                if (e instanceof SocketTimeoutException) {
                    code = TimeOut;
                }
                final int code2 = code;
                if (i == 0) {
                    Util.runInMain(new Runnable() {
                        @Override
                        public void run() {
                            complete.complete(new Result(code2, ip, 0, 0, 0, 0, 1, 1));
                        }
                    });
                    return;
                } else {
                    dropped++;
                }
            }
            long end = System.currentTimeMillis();
            int t = (int) (end - start);
            times[i] = t;
            index = i;
            try {
                if (!stopped && 100 > t && t > 0) {
                    Thread.sleep(100 - t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (index == -1) {
            complete.complete(new Result(Stopped, ip, 0, 0, 0, 0, 0, 0));
            return;
        }

        complete.complete(buildResult(times, index, ip, dropped));
    }

    private Result buildResult(int[] times, int index, String ip, int dropped) {
        int sum = 0;
        int min = 1000000;
        int max = 0;
        for (int i = 0; i <= index; i++) {
            int t = times[i];
            if (t > max) {
                max = t;
            }
            if (t < min) {
                min = t;
            }
            sum += t;
        }
        return new Result(0, ip, max, min, sum / (index + 1), 0, index + 1, dropped);
    }

    private void connect(InetSocketAddress socketAddress, int timeOut) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(socketAddress, timeOut);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public interface Callback {
        void complete(Result r);
    }

    public static final class Result {
        public final int code;
        public final String ip;
        public final int maxTime;
        public final int minTime;
        public final int avgTime;
        public final int stddevTime;
        public final int count;
        public final int dropped;

        public Result(int code, String ip, int maxTime, int minTime, int avgTime,
                      int stddevTime, int count, int dropped) {
            this.code = code;
            this.ip = ip;
            this.maxTime = maxTime;
            this.minTime = minTime;
            this.avgTime = avgTime;
            this.stddevTime = stddevTime;
            this.count = count;
            this.dropped = dropped;
        }
    }
}
