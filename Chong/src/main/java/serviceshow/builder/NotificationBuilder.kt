package serviceshow.builder

import android.app.Notification
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.september.nine.chong.R

/**
 * 通知构建器
 */
object NotificationBuilder {
    
    /**
     * 构建前台服务通知
     */
    fun buildNotification(context: Context, channelId: String): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setAutoCancel(false)
            .setContentText("")
            .setSmallIcon(R.drawable.bgk_efde)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("")
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(createCustomView(context))
            .build()
    }
    
    /**
     * 创建自定义视图
     */
    private fun createCustomView(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.page_ku)
    }
}

