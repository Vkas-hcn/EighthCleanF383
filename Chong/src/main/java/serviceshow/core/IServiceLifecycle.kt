package serviceshow.core

import android.app.Notification
import android.content.Context


interface IServiceLifecycle {

    fun onServiceCreate(context: Context, callback: (Notification?) -> Unit)

    fun onServiceStart(context: Context, notification: Notification?, callback: (Boolean) -> Unit)
    

    fun onServiceDestroy()
}

