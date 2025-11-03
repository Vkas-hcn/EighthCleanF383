package com.september.nine.chong.data

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * MMKV本地持久化工具类
 * 使用属性委托简化使用流程
 */
object CunUtils {

    private lateinit var mmkv: MMKV

    /**
     * 初始化MMKV
     * 在Application中调用
     */
    fun init(app: Application) {
        KeyCon.openEc = app
        MMKV.initialize(app)
        mmkv = MMKV.defaultMMKV()
    }

    /**
     * 获取MMKV实例
     */
    fun getMMKV(): MMKV = mmkv

    /**
     * 清除所有数据
     */
    fun clearAll() {
        mmkv.clearAll()
    }

    /**
     * 移除指定key的数据
     */
    fun remove(key: String) {
        mmkv.removeValueForKey(key)
    }

    /**
     * 检查是否包含某个key
     */
    fun contains(key: String): Boolean {
        return mmkv.containsKey(key)
    }

    // ========== 属性委托 ==========

    /**
     * String类型属性委托
     * 用法: var name by CunUtils.string("user_name", "默认值")
     */
    fun string(key: String? = null, defaultValue: String = ""): ReadWriteProperty<Any?, String> {
        return object : ReadWriteProperty<Any?, String> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                val k = key ?: property.name
                return mmkv.decodeString(k, defaultValue) ?: defaultValue
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * Int类型属性委托
     * 用法: var age by CunUtils.int("user_age", 0)
     */
    fun int(key: String? = null, defaultValue: Int = 0): ReadWriteProperty<Any?, Int> {
        return object : ReadWriteProperty<Any?, Int> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
                val k = key ?: property.name
                return mmkv.decodeInt(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * Long类型属性委托
     * 用法: var timestamp by CunUtils.long("last_time", 0L)
     */
    fun long(key: String? = null, defaultValue: Long = 0L): ReadWriteProperty<Any?, Long> {
        return object : ReadWriteProperty<Any?, Long> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
                val k = key ?: property.name
                return mmkv.decodeLong(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * Float类型属性委托
     * 用法: var score by CunUtils.float("user_score", 0f)
     */
    fun float(key: String? = null, defaultValue: Float = 0f): ReadWriteProperty<Any?, Float> {
        return object : ReadWriteProperty<Any?, Float> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
                val k = key ?: property.name
                return mmkv.decodeFloat(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * Double类型属性委托
     * 用法: var price by CunUtils.double("product_price", 0.0)
     */
    fun double(key: String? = null, defaultValue: Double = 0.0): ReadWriteProperty<Any?, Double> {
        return object : ReadWriteProperty<Any?, Double> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
                val k = key ?: property.name
                return mmkv.decodeDouble(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * Boolean类型属性委托
     * 用法: var isLogin by CunUtils.boolean("is_login", false)
     */
    fun boolean(key: String? = null, defaultValue: Boolean = false): ReadWriteProperty<Any?, Boolean> {
        return object : ReadWriteProperty<Any?, Boolean> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
                val k = key ?: property.name
                return mmkv.decodeBool(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
                val k = key ?: property.name
                mmkv.encode(k, value)
            }
        }
    }

    /**
     * ByteArray类型属性委托
     * 用法: var data by CunUtils.byteArray("user_data")
     */
    fun byteArray(key: String? = null, defaultValue: ByteArray? = null): ReadWriteProperty<Any?, ByteArray?> {
        return object : ReadWriteProperty<Any?, ByteArray?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): ByteArray? {
                val k = key ?: property.name
                return mmkv.decodeBytes(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: ByteArray?) {
                val k = key ?: property.name
                if (value != null) {
                    mmkv.encode(k, value)
                } else {
                    mmkv.removeValueForKey(k)
                }
            }
        }
    }

    /**
     * Set<String>类型属性委托
     * 用法: var tags by CunUtils.stringSet("user_tags")
     */
    fun stringSet(key: String? = null, defaultValue: Set<String>? = null): ReadWriteProperty<Any?, Set<String>?> {
        return object : ReadWriteProperty<Any?, Set<String>?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Set<String>? {
                val k = key ?: property.name
                return mmkv.decodeStringSet(k, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>?) {
                val k = key ?: property.name
                if (value != null) {
                    mmkv.encode(k, value)
                } else {
                    mmkv.removeValueForKey(k)
                }
            }
        }
    }

    /**
     * 可空String类型属性委托
     * 用法: var token by CunUtils.stringNullable("user_token")
     */
    fun stringNullable(key: String? = null): ReadWriteProperty<Any?, String?> {
        return object : ReadWriteProperty<Any?, String?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
                val k = key ?: property.name
                return mmkv.decodeString(k, null)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
                val k = key ?: property.name
                if (value != null) {
                    mmkv.encode(k, value)
                } else {
                    mmkv.removeValueForKey(k)
                }
            }
        }
    }
}