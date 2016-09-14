package com.qiniu.android.netdiag;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Created by bailong on 16/9/14.
 */
public class TraceRouteTest extends TestCase {

    public void testTrace() throws Exception {
        final ArrayList<TraceRoute.Result> l = new ArrayList<>();
        final CountDownLatch c = new CountDownLatch(1);
        Task t = TraceRoute.start("www.baidu.com", new Output() {
            @Override
            public void write(String line) {
                System.out.println("test> " + line);
            }
        }, new TraceRoute.Callback() {
            @Override
            public void complete(TraceRoute.Result r) {
                System.out.println(r.content());
                l.add(r);
                c.countDown();
            }
        });
        c.await(200, TimeUnit.SECONDS);
        assertEquals(l.size(), 1);
        assertNotNull(l.get(0).content());
    }
}