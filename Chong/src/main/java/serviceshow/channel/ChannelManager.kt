package serviceshow.channel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


object ChannelManager {
    
    private const val DEFAULT_CHANNEL_ID = "Notification"
    private const val DEFAULT_CHANNEL_NAME = "Notification Channel"
    

    fun createChannel(context: Context): String {
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            DEFAULT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        
        return DEFAULT_CHANNEL_ID
    }
    

    fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}

