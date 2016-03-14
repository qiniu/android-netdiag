package com.qiniu.android.netdiag;

import com.qiniu.android.netdiag.localdns.Record;
import com.qiniu.android.netdiag.localdns.Resolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by bailong on 16/2/24.
 */
public final class NsLookup{
    private final String domain;
    private final String serverIp;
    private final Output output;
    private final Callback complete;
//    private volatile boolean stopped;

    public static void start(String domain, Output output, Callback complete){
        start(domain, null, output, complete);
    }

    public static void start(String domain, String serverIp, Output output, Callback complete){
        if (serverIp == null){
            String[] s = DNS.local();
            if (s != null){
                serverIp = s[0];
            }
        }
        final NsLookup t = new NsLookup(domain, serverIp, output, complete);
        Util.runInBack(new Runnable() {
            @Override
            public void run() {
                t.run();
            }
        });
    }


    private void run(){
        if (serverIp == null){
            Result r = new Result(-1, 0, null);
            complete.complete(r);
            return;
        }

        Resolver r = null;
        output.write("nslookup " + domain + " @" + serverIp);
        try {
            r = new Resolver(InetAddress.getByName(serverIp));
        } catch (UnknownHostException e) {
            Result result = new Result(-1, 0, null);
            output.write("nslookup server invalid");
            complete.complete(result);
            return;
        }

        try {
            long start = System.currentTimeMillis();
            Record[] records = r.resolve(domain);
            long duration = System.currentTimeMillis() - start;
            for (Record record : records) {
                output.write(record.toString());
            }
            complete.complete(new Result(0, (int) duration, records));
        } catch (IOException e) {
            e.printStackTrace();
            complete.complete(new Result(-3, 0, null));
        }

    }

    private NsLookup(String domain, String serverIp, Output output, Callback complete){
        this.domain = domain;
        this.serverIp = serverIp;
        this.output = output;
        this.complete = complete;
    }

    public static class Result{
        public final int code;
        public final int duration;
        public final Record[] records;

        private Result(int code, int duration, Record[] records) {
            this.code = code;
            this.duration = duration;
            this.records = records;
        }
    }

    public interface Callback{
        void complete(Result result);
    }
}
