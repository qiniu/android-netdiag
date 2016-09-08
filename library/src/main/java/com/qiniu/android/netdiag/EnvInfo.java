package com.qiniu.android.netdiag;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by bailong on 16/3/14.
 */
public final class EnvInfo {
    public static CpuInfo cpuInfo() {
        BufferedReader reader = null;
        long work1, total1, work2, total2;
        try {
            reader = new BufferedReader(new FileReader("/proc/stat"));
            String[] sa = reader.readLine().split("[ ]+", 9);
            work1 = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
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
            work2 = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
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
        long t = total2 - total1;
        float percent = (work2 - work1) * 100 / (float) t;
        float currentPercent = (workP2 - workP1) * 100 / (float) t;
        if (percent < 0 || percent > 100) {
            return new CpuInfo(0, 0);
        }
        return new CpuInfo(percent, currentPercent);
    }

    public static SystemMemInfo systemMemInfo() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/meminfo"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new SystemMemInfo();
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

                else if (s.startsWith("Cached:")) {
                    cached = Integer.parseInt(s.split("[ ]+", 3)[1]);
                }
                s = reader.readLine();
            }
        } catch (Exception e) {
            return new SystemMemInfo();
        } finally {

            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new SystemMemInfo(total, free, cached);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static AppMemInfo memInfo(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return new AppMemInfo(0, 0, 0);
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return new AppMemInfo(0, 0, 0);
        }
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        return new AppMemInfo(mi.totalMem, mi.totalMem - mi.availMem, mi.threshold);

    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static boolean isBackground(Context context) {
        if (context == null) {
            return true;
        }
        String pkgName = context.getPackageName();
        if (pkgName == null) {
            return true;
        }
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (pkgName.equals(appProcess.processName)) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "Background"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "Foreground"
                            + appProcess.processName);
                    return false;
                }
            }
        }

        return false;
    }

    public static class CpuInfo {
        public final float total;
        public final float current;

        public CpuInfo(float total, float current) {
            this.total = total;
            this.current = current;
        }
    }

    public static class SystemMemInfo {
        public final int total;
        public final int free;
        public final int cached;

        public SystemMemInfo(int total, int free, int cached) {
            this.total = total;
            this.free = free;
            this.cached = cached;
        }

        public SystemMemInfo() {
            this(0, 0, 0);
        }
    }

    public static class AppMemInfo {
        public final long total;
        public final long used;
        public final long threshold;

        public AppMemInfo(long total, long used, long threshold) {
            this.total = total;
            this.used = used;
            this.threshold = threshold;
        }
    }


}
