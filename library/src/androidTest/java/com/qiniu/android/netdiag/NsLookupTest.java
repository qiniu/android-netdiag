package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bailong on 16/2/29.
 */
public class NsLookupTest extends AndroidTestCase {
    private NsLookup.Result result;

    public void testOK() {
        final CountDownLatch c = new CountDownLatch(1);

        NsLookup.start("www.baidu.com", new TestLogger(), new NsLookup.Callback() {
            @Override
            public void complete(NsLookup.Result r) {
                result = r;
                c.countDown();
            }
        });
        try {
            c.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, result.code);
        Assert.assertTrue(result.records.length > 0);
        Assert.assertTrue(result.duration > 0);
    }
}
