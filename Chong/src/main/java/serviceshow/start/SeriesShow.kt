package serviceshow.start

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import serviceshow.handler.ServiceHandler

class SeriesShow : Service() {
    
    private var mNotification: Notification? = null
    private val serviceHandler = ServiceHandler()
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        
        // 使用服务处理器创建通知
        serviceHandler.onServiceCreate(this) { notification ->
            mNotification = notification
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            // 使用服务处理器启动前台服务
            serviceHandler.onServiceStart(this, mNotification) { success ->
                // 启动结果回调（可选处理）
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        // 使用服务处理器处理销毁
        serviceHandler.onServiceDestroy()
        super.onDestroy()
    }
}