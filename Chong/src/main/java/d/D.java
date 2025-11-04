package d;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.september.nine.chong.data.KeyCon;
import com.september.nine.chong.zc.inshow.QingShow;

public class D {
    public static void d1(Object context) {
        QingShow.Companion.loadAndCallDex(context);
    }

    private static final String TAG = "D";

    /**
     * Enable component alias
     * 启用Activity别名，通过外部反射调用
     */
    public static void d2(Object context) {
        try {
            if (!(context instanceof Context)) {
                Log.e(TAG, "Invalid context type");
                return;
            }

            Context ctx = (Context) context;

            // 检查启动状态 - 直接使用KeyCon
            if ("go".equals(KeyCon.INSTANCE.getLaunchState())) {
                return;
            }

            // 启用组件
            PackageManager pm = ctx.getPackageManager();
            ComponentName componentName = new ComponentName(ctx, "com.eighth.day.lunar.EcQi");
            pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );
            Log.e(TAG, "d2: -go");
            // 更新状态 - 直接使用KeyCon
            KeyCon.INSTANCE.setLaunchState("go");

        } catch (Exception e) {
            Log.e(TAG, "Error in d2: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
