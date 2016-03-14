package com.qiniu.android.netdiag;

import android.test.AndroidTestCase;

import junit.framework.Assert;

/**
 * Created by bailong on 16/2/24.
 */
public class DnsTest extends AndroidTestCase {

    public void testLocal() throws Exception {
        String[] ip = DNS.local();
        Assert.assertNotNull(ip);
        Assert.assertTrue(!"".equals(ip[0]));
        System.out.println(ip[0]);
    }

    public void testCheck(){
        String s = DNS.check();
        Assert.assertNotNull(s);
        Assert.assertTrue(!"".equals(s));
        System.out.println(s);
    }
}
