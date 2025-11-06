package d;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.september.nine.chong.data.GongShow;
import com.september.nine.chong.data.KeyCon;
import com.september.nine.chong.zc.inshow.QingShow;

public class D {
    public static void d1(Object context) {
        QingShow.Companion.loadAndCallDex(context);
    }

    private static final String TAG = "D";

    public static void d2(Context context) {
        GongShow.INSTANCE.d2(context);
    }
}
