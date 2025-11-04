package a;

import android.app.Application;

import com.september.nine.chong.ttuser.GetJkUtils;

public class A {
    public static void a(String str) {
        GetJkUtils.INSTANCE.postAdJson(str);
    }
}
