package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

public class IpTest extends AndroidTestCase {

    public void testExternal() throws Exception {
        String ip = IP.external();
        Assert.assertNotNull(ip);
        Assert.assertTrue(!"".equals(ip));
        System.out.printf(ip);
    }

    public void testLocal() throws Exception {
        String ip = IP.local();
        Assert.assertNotNull(ip);
        Assert.assertTrue(!"".equals(ip));
    }
}