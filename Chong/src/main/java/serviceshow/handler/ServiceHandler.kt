package serviceshow.handler

import android.app.Notification
import android.app.Service
import android.content.Context
import android.util.Log
import serviceshow.builder.NotificationBuilder
import serviceshow.channel.ChannelManager
import serviceshow.core.IServiceLifecycle
import serviceshow.state.ServiceState


class ServiceHandler : IServiceLifecycle {
    
    private val tag = "ServiceHandler"
    private var channelId: String? = null
    
    override fun onServiceCreate(context: Context, callback: (Notification?) -> Unit) {
        try {
            // 创建通知渠道
            channelId = ChannelManager.createChannel(context)
            
            // 构建通知
            val notification = NotificationBuilder.buildNotification(context, channelId!!)
            
            // 更新服务状态
            ServiceState.markServiceStarted()
            
            // 回调返回通知
            callback(notification)
            
        } catch (e: Exception) {
            Log.e(tag, "Error creating service: ${e.message}")
            callback(null)
        }
    }
    
    override fun onServiceStart(context: Context, notification: Notification?, callback: (Boolean) -> Unit) {
        if (notification == null) {
            callback(false)
            return
        }
        
        try {
            // 启动前台服务
            if (context is Service) {
                context.startForeground(1000, notification)
                callback(true)
            } else {
                callback(false)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error starting foreground: ${e.message}")
            callback(false)
        }
    }
    
    override fun onServiceDestroy() {
        try {
            // 更新服务状态
            ServiceState.markServiceStopped()
        } catch (e: Exception) {
            Log.e(tag, "Error destroying service: ${e.message}")
        }
    }
}

