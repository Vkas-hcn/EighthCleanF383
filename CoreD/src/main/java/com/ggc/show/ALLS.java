package com.ggc.show;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import gg.GgUtils;

/**
 * Date：2025/9/26
 * Describe:
 */
public class ALLS implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.e("TAG", "onActivityCreated: "+activity.getClass().getSimpleName());
        String name = activity.getClass().getSimpleName();
        if (name.equals(DataCc.AC_NAME)) {
            GgUtils.getMAdC().showAd(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 使用 Builder 创建 TaskDescription
            ActivityManager.TaskDescription taskDescription = (new ActivityManager.TaskDescription.Builder()).setLabel("\t\n").build();
            activity.setTaskDescription(taskDescription);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        String name = activity.getClass().getSimpleName();
        if (name.equals(DataCc.AC_NAME)) {
            View view = activity.getWindow().getDecorView();
            ((ViewGroup) view).removeAllViews();
        }
    }
}
