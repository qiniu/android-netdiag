package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bailong on 16/2/29.
 */
public class PingTest extends AndroidTestCase {
    private Ping.Result result;
    public void testOK() throws InterruptedException {
        final CountDownLatch c = new CountDownLatch(1);
        Task t = Ping.start("www.baidu.com", 10, new TestLogger(), new Ping.Callback() {
            @Override
            public void complete(Ping.Result r) {
                result = r;
                c.countDown();
            }
        });
        c.await(200, TimeUnit.SECONDS);
        Assert.assertNotNull(result.result);
    }
}
