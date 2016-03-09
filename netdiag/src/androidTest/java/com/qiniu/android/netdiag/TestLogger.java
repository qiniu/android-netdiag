package com.qiniu.android.netdiag;

/**
 * Created by bailong on 16/2/28.
 */
public class TestLogger implements Output{

    @Override
    public void write(String line) {
        System.out.println(line);
    }
}
