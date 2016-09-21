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

    public void testParseResult() {
        String r = " ping -c 10 www.baidu.com\n" +
                "PING www.a.shifen.com (115.239.211.112) 56(84) bytes of data.\n" +
                "64 bytes from 115.239.211.112: icmp_seq=1 ttl=52 time=112 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=2 ttl=52 time=25.9 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=3 ttl=52 time=26.0 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=4 ttl=52 time=32.8 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=5 ttl=52 time=26.2 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=6 ttl=52 time=39.5 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=7 ttl=52 time=22.7 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=8 ttl=52 time=28.5 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=9 ttl=52 time=43.6 ms\n" +
                "64 bytes from 115.239.211.112: icmp_seq=10 ttl=52 time=73.8 ms\n" +
                "\n" +
                "--- www.a.shifen.com ping statistics ---\n" +
                "10 packets transmitted, 10 received, 0% packet loss, time 9013ms\n" +
                "rtt min/avg/max/mdev = 22.720/43.159/112.111/27.063 ms";

        Ping.Result pr = new Ping.Result(r, "115.239.211.112", 56, 200);
        Assert.assertEquals(pr.ip, "115.239.211.112");
        Assert.assertEquals(pr.result, r);
        Assert.assertEquals((int) pr.min, 22);
        Assert.assertEquals((int) pr.avg, 43);
        Assert.assertEquals((int) pr.max, 112);
        Assert.assertEquals((int) pr.stddev, 27);
    }

}
