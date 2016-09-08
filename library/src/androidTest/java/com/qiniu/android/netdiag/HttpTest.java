package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bailong on 16/2/28.
 */
public class HttpTest extends AndroidTestCase {
    private HttpPing.Result r;

    public void testOK() throws Exception {
        final CountDownLatch c = new CountDownLatch(1);

        HttpPing.start("http://www.baidu.com", new TestLogger(), new HttpPing.Callback() {
            @Override
            public void complete(HttpPing.Result result) {
                r = result;
                c.countDown();

            }
        });
        c.await(100, TimeUnit.SECONDS);
        Assert.assertEquals(200, r.code);
        Assert.assertTrue(r.duration > 0);
        Assert.assertTrue(r.headers.size() > 0);
        Assert.assertTrue(r.body.length > 0);
    }
}
