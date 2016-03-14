package com.qiniu.android.netdiag;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by bailong on 16/2/24.
 */
public final class HttpPing implements Task {
    private static final int MAX = 64 * 1024;

    private final Output out;
    private final String url;
    private final Callback complete;
    private volatile boolean stopped;

    private HttpPing(String url, Output out, final Callback complete) {
        this.out = out;
        this.url = url;
        this.complete = complete;
        this.stopped = false;
    }

    public static Task start(String url, Output out, Callback complete) {
        final HttpPing h = new HttpPing(url, out, complete);
        Util.runInBack(new Runnable() {
            @Override
            public void run() {
                h.run();
            }
        });
        return h;
    }

    private void run() {
        long start = System.currentTimeMillis();
        try {
            out.write("Get " + url);
            URL u = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(20000);
            int responseCode = httpConn.getResponseCode();
            out.write("status " + responseCode);

            Map<String, List<String>> headers = httpConn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                out.write(entry.getKey() + ":" + entry.getValue().get(0));
            }
            InputStream is = httpConn.getInputStream();
            int len = httpConn.getContentLength();
            len = len > MAX || len < 0 ? MAX : len;
            byte[] data = new byte[len];
            int read = is.read(data);
            long duration = System.currentTimeMillis() - start;
            out.write("Done, duration " + duration + "ms");
            is.close();
            if (read <= 0) {
                Result r = new Result(responseCode, headers, null, (int) duration, "no body");
                this.complete.complete(r);
                return;
            }
            if (read < data.length) {
                byte[] b = new byte[read];
                System.arraycopy(data, 0, b, 0, read);
                Result r = new Result(responseCode, headers, b, (int) duration, "no body");
                this.complete.complete(r);

            }
        } catch (IOException e) {
            e.printStackTrace();
            long duration = System.currentTimeMillis() - start;
            Result r = new Result(-1, null, null, (int) duration, e.getMessage());
            out.write("error : " + e.getMessage());
            this.complete.complete(r);

        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public static class Result {
        public final int code;
        public final Map<String, List<String>> headers;
        public final byte[] body;
        public final int duration;
        public final String errorMessage;

        private Result(int code,
                       Map<String, List<String>> headers, byte[] body, int duration, String errorMessage) {
            this.code = code;
            this.headers = headers;
            this.body = body;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
    }

    public interface Callback {
        void complete(Result result);
    }
}
