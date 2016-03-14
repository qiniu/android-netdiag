package com.qiniu.android.netdiag;

import android.os.Process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by bailong on 16/3/14.
 */
public class EnvInfo {
    public static class CpuInfo{
        public final float total;
        public final float current;

        public CpuInfo(float total, float current) {
            this.total = total;
            this.current = current;
        }
    }

    public static CpuInfo cpuInfo() {
        BufferedReader reader = null;
        long work1, total1, work2, total2;
        try {
            reader = new BufferedReader(new FileReader("/proc/stat"));
            String[] sa = reader.readLine().split("[ ]+", 9);
            work1  = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
            total1 = work1 + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
        } catch (IOException e) {
            e.printStackTrace();
            return new CpuInfo(0, 0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        reader = null;
        long workP1;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/stat"));
            String[] sa = reader.readLine().split("[ ]+", 18);
            workP1 = Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new CpuInfo(0, 0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/stat"));
            String[] sa = reader.readLine().split("[ ]+", 9);
            work2  = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
            total2 = work2 + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
        } catch (IOException e) {
            e.printStackTrace();
            return new CpuInfo(0, 0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        reader = null;
        long workP2;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/stat"));
            String[] sa = reader.readLine().split("[ ]+", 18);
            workP2 = Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new CpuInfo(0, 0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long t = total2 -total1;
        float percent = (work2 - work1)*100/(float)t;
        float currentPercent = (workP2-workP1)*100/(float)t;
        if (percent < 0 || percent> 100){
            return new CpuInfo(0, 0);
        }
        return new CpuInfo(percent, currentPercent);
    }

    public static int totalMem(){
        return 0;
    }

    public static int processMem(int pid){
        return 0;
    }
}
