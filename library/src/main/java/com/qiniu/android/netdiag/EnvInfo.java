package com.qiniu.android.netdiag;

import android.os.Process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by bailong on 16/3/14.
 */
public final class EnvInfo {
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

    public static class MemInfo{
        public final int total;
        public final int free;
        public final int cached;

        public MemInfo(int total, int free, int cached) {
            this.total = total;
            this.free = free;
            this.cached = cached;
        }

        public MemInfo(){
            this(0,0,0);
        }
    }
    public static MemInfo memInfo(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/meminfo"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new MemInfo();
        }
        int total = 0;
        int free = 0;
        int cached = 0;
        try {
            String s = reader.readLine();
            while (s != null) {
                if (s.startsWith("MemTotal:")) {
                    total = Integer.parseInt(s.split("[ ]+", 3)[1]);
                } else if (s.startsWith("MemFree:"))
                    free = Integer.parseInt(s.split("[ ]+", 3)[1]);

                else if (s.startsWith("Cached:")){
                    cached = Integer.parseInt(s.split("[ ]+", 3)[1]);
                }
                s = reader.readLine();
            }
        }catch (Exception e){
            return new MemInfo();
        }finally {

            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new MemInfo(total, free, cached);
    }

}
