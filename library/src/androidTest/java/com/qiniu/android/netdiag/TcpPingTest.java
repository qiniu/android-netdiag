package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bailong on 16/3/8.
 */
public class TcpPingTest extends AndroidTestCase{
    private TcpPing.Result result;
    public void testOK() throws InterruptedException {
        final CountDownLatch c = new CountDownLatch(1);
        TcpPing.start("www.baidu.com", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                result = r;
                c.countDown();
            }
        });
        c.await(200, TimeUnit.SECONDS);
        Assert.assertEquals(0, result.code);
        Assert.assertTrue(result.avgTime >= result.minTime &&
                result.maxTime>= result.avgTime);
        Assert.assertEquals(3, result.count);
        Assert.assertTrue(result.ip.length()>=8);
    }

    public void testStop() throws InterruptedException {
        final CountDownLatch c = new CountDownLatch(1);
        Task t = TcpPing.start("www.baidu.com", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                result = r;
                c.countDown();
            }
        });
        t.stop();
        c.await(200, TimeUnit.SECONDS);
        Assert.assertEquals(TcpPing.Stopped, result.code);
        Assert.assertTrue(result.avgTime >= result.minTime &&
                result.maxTime>= result.avgTime);
        Assert.assertTrue(result.count >= 0);
    }

    public void testUnknown() throws InterruptedException {
        final CountDownLatch c = new CountDownLatch(1);
        TcpPing.start("unknown.qiniu.com", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                result = r;
                c.countDown();
            }
        });
        c.await(200, TimeUnit.SECONDS);
        Assert.assertEquals(TcpPing.UnkownHost, result.code);
    }

    public void testNotReach() throws InterruptedException {
        final CountDownLatch c = new CountDownLatch(1);
        TcpPing.start("1.1.1.10", new TestLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                result = r;
                c.countDown();
            }
        });
        c.await(200, TimeUnit.SECONDS);
        Assert.assertTrue(result.code == TcpPing.NotReach ||
                result.code == TcpPing.TimeOut);
    }
}
