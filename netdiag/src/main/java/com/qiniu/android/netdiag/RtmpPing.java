package com.qiniu.android.netdiag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Created by bailong on 16/2/24.
 */
public final class RtmpPing implements Task{
    public static final int TimeOut = -3;
    public static final int NotReach = -2;
    public static final int UnkownHost = -4;
    public static final int HandshakeFail = -5;
    public static final int Stopped = -1;

    public static final int ServerVersionError = -20001;
    public static final int ServerSignatureError = -20002;
    public static final int ServerTimeError = -20003;

    public static final int RTMP_SIG_SIZE = 1536;

    private final String host;
    private final int port;
    private final int count;
    private final Callback complete;
    private boolean stopped;
    private Output output;

    public RtmpPing(String host, int port, int count, Output output, Callback complete) {
        this.host = host;
        this.port = port;
        this.count = count;
        this.complete = complete;
        this.output = output;
    }

    public static Task start(String host, Output output, Callback complete){
        return start(host, 1935, 2, output, complete);
    }

    public static Task start(String host, int port, int count
            , Output output, Callback complete){
        final RtmpPing t = new RtmpPing(host, port, count,output, complete);
        Util.runInBack(new Runnable() {
            @Override
            public void run() {
                t.run();
            }
        });
        return t;
    }

    private void run(){
        InetAddress[] addrs = null;
        try {
            addrs = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            output.write("Unknown host: " + host);
            Util.runInMain(new Runnable() {
                @Override
                public void run() {
                    complete.complete(new Result(UnkownHost, "", 0, 0, 0, 0));
                }
            });
            return;
        }

        final String ip = addrs[0].getHostAddress();
        InetSocketAddress server = new InetSocketAddress(ip, port);
        output.write("connect to " + ip + ":" + port);
        int[] times = new int[count];
        int index = -1;
        for (int i = 0; i < count && !stopped; i++) {
            long start = System.currentTimeMillis();
            Socket sock;
            try {
                sock = connect(server, 20*1000);
            } catch (IOException e) {
                e.printStackTrace();
                int code = NotReach;
                if (e instanceof SocketTimeoutException){
                    code = TimeOut;
                }
                final int code2 = code;
                Util.runInMain(new Runnable() {
                    @Override
                    public void run() {
                        complete.complete(new Result(code2, ip, 0, 0, 0, 0));
                    }
                });
                return;
            }

            long connEnd = System.currentTimeMillis();
            int connect_time = (int)(connEnd -start);

            long end = System.currentTimeMillis();

            try {
                handshake(sock);
            } catch (IOException e) {
                e.printStackTrace();
                Util.runInMain(new Runnable() {
                    @Override
                    public void run() {
                        complete.complete(new Result(HandshakeFail, ip, 0, 0, 0, 0));
                    }
                });
                return;
            }finally {
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            times[i] = (int)(end -start);
            index = i;
            output.write(String.format(Locale.getDefault(), "%d: conn:%d handshake:%d",
                    index, connect_time, (int)(end-start)));
            try {
                if (!stopped && i!=count-1 && 100>(end - start)){
                    Thread.sleep(100 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (index == -1){
            complete.complete(new Result(Stopped, ip, 0, 0, 0, 0));
            return;
        }

        complete.complete(buildResult(times, index, ip));
    }

    private Result buildResult(int[] times, int index, String ip){
        int sum = 0;
        int min = 1000000;
        int max = 0;
        for (int i = 0; i <= index; i++) {
            int t = times[i];
            if (t > max){
                max = t;
            }
            if (t< min){
                min = t;
            }
            sum += t;
        }
        return new Result(0, ip, max, min, sum/(index+1), index+1);
    }

    private Socket connect(InetSocketAddress socketAddress, int timeOut) throws IOException{
        Socket socket  = new Socket();
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(30*1000);

        try {
            socket.connect(socketAddress, timeOut);
        } catch (Exception e){
            socket.close();
            throw e;
        }
        return socket;
    }

    private int handshake(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        InputStream input = socket.getInputStream();

        byte[] c0_c1 = c0_c1();

        send_c0_c1(out, c0_c1);

        byte[] s0_s1 = new byte[RTMP_SIG_SIZE+1];

        boolean b = verify_s0_s1(input, s0_s1);
        if (!b){
            return ServerVersionError;
        }

        send_c2(out, s0_s1, 1);


        b = verify_s2(input, s0_s1, c0_c1);
        if (!b){
            return ServerSignatureError;
        }
        return 0;
    }

    private static int readAll(InputStream in, byte[]buffer, int offset, int size) throws IOException {
        int pos = 0;
        while (pos < size) {
            int ret = in.read(buffer, offset+pos, size-pos);
            if (ret < 0){
                return pos;
            }
            pos += ret;
        }
        return pos;
    }

    private static void writeAll(OutputStream outputStream, byte[] buffer, int offset, int n) throws IOException {
        outputStream.write(buffer, offset, n);
        outputStream.flush();
    }

    private static byte[] c0_c1() throws IOException {
        byte[] data = new byte[RTMP_SIG_SIZE+1];
        int i = 0;
        data[i++] = 0x03; /* not encrypted */
        //time
        for (;i<5;i++){
            data[i] = 0;
        }
        //zero
        for (;i<9;i++){
            data[i] = 0;
        }

        Random r = new Random();
        for (; i < data.length; i++) {
            data[i] = (byte)r.nextInt(256);
        }
        return data;
    }

    private static void send_c0_c1(OutputStream outputStream, byte[]c0_c1) throws IOException {
        writeAll(outputStream, c0_c1, 0, c0_c1.length);
    }

    private static void send_c2(OutputStream outputStream, byte[] c2, int offset) throws IOException {
        writeAll(outputStream, c2, offset, c2.length-offset);
    }

    private static boolean verify_s0_s1(InputStream inputStream, byte[]s0_s1) throws IOException {
        int r = readAll(inputStream, s0_s1, 0, s0_s1.length);
        if (r != s0_s1.length){
            throw new IOException("read not complete, read "+ r);
        }
        byte s0 = s0_s1[0];

        return s0 == 0x03;
    }

    private static boolean verify_s2(InputStream inputStream,
                                     byte[] server_sig, byte[] client_sig) throws IOException {
        int n = readAll(inputStream, server_sig, 1, server_sig.length-1);
        if (n != server_sig.length-1) {
            throw new IOException("read not complete");
        }

        for (int i = 1; i < server_sig.length; i++) {
            if (server_sig[i] != client_sig[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public static final class Result{
        public final int code;
        public final String ip;
        public final int maxTime;
        public final int minTime;
        public final int avgTime;
        public final int count;

        public Result(int code, String ip, int maxTime, int minTime, int avgTime,
                      int count) {
            this.code = code;
            this.ip = ip;
            this.maxTime = maxTime;
            this.minTime = minTime;
            this.avgTime = avgTime;
            this.count = count;
        }
    }

    public interface Callback{
        void complete(Result r);
    }
}
