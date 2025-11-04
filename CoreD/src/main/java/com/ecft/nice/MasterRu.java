package com.ecft.nice;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.tencent.mmkv.MMKV;


public class MasterRu {

    public static String ver = "1.0.1"; //appVersion
    public static long insAppTime = 0L; //installAppTime
    private static final MMKV mmkv = MMKV.defaultMMKV();
    public static Application mApp;



    // 入口方法
    public static void jkks(Object context) {
        try {
            mApp = (Application) context;


            pE("test_d_load");
            inIf(mApp);

            AdminGo aa = new AdminGo();
            aa.refreshLastConfigure();
        } catch (Exception e) {
            Log.e("Core", "jkks-error: " + e.getMessage(), e);
        }
    }


    public static void pE(String name, String value) {
        boolean canRetry;
        switch (name) {
            case "config_G":
            case "cf_fail":
            case "pop_fail":
            case "advertise_limit":
                canRetry = true;
                break;
            default:
                canRetry = false;
                break;
        }
        b.B.b(canRetry,name, "string",value);
    }

    public static void pE(String string) {
        pE(string, null);
    }


    public static void postAd(String string) {
        a.A.a(string);
    }


    public static long finishAllActivities() {
        c.C.c();
        return 0L;
    }

    // 以下方法保持不变
    public static String getStr(String key) {
        return mmkv.decodeString(key, "");
    }

    public static void saveC(String ke, String con) {
        mmkv.encode(ke, con);
    }

    public static int getInt(String key) {
        return mmkv.decodeInt(key, 0);
    }

    public static void saveInt(String key, int i) {
        mmkv.encode(key, i);
    }

    private static void inIf(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ver = pi.versionName;
            insAppTime = pi.firstInstallTime;
        } catch (Exception ignored) {
        }
    }
}
