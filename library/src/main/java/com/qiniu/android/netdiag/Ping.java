package com.qiniu.android.netdiag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by bailong on 16/2/24.
 */
public final class Ping implements Task {
    private final String address;
    private final int count;
    private final Output output;
    private final Callback complete;
    private volatile boolean stopped;

    private Ping(String address, int count,
                 Output output, Callback complete) {

        this.address = address;
        this.count = count;
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

    private void run() {
        String r = pingCmd();
        complete.complete(new Result(r));
    }

    private String pingCmd() {
        String cmd = String.format(Locale.getDefault(), "ping -n -c %d %s", count, address);
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
                str.append(line);
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
        return str.toString();
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

        private Result(String result) {
            this.result = result;
        }
    }
}
