package com.two.smooth

import org.junit.Test

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val soName = "libncku.so"
    private val progetName = "T714"
    private val name64 = "kuang.txt"
    private val name32 = "kun.zip"

    // h5
//    private val soName = "libncan.so"
//    private val progetName = "T714"
//    private val name64 = "huang.txt"
//    private val name32 = "hua.zip"

    @Test
    fun addition_isCorrect() {
        val inputFile = "/Users/vkas/Desktop/soencode/$progetName/arm64-v8a/$soName"
        // 加密后文件路径 64
        val encryptedFile = "/Users/vkas/Desktop/soencode/$progetName/$name64"

        encrypt(File(inputFile).inputStream(), File(encryptedFile))

        val inputFile2 = "/Users/vkas/Desktop/soencode/$progetName/armeabi-v7a/$soName"
        // 加密后文件路径
        val encryptedFile2 = "/Users/vkas/Desktop/soencode/$progetName/$name32"
        encrypt(File(inputFile2).inputStream(), File(encryptedFile2))
    }

    private val ALGORITHM = "AES"
    private val SECRET_KEY = "dfgdh3gtg56rfrg6".toByteArray() // 16, 24, or 32 bytes
    // 加密
    fun encrypt(inputStream: InputStream, outputFile: File) {
        val key = SecretKeySpec(
            SECRET_KEY, ALGORITHM
        )
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputStream.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputStream.close()
    }

    // 解密
    fun decrypt(inputFile: InputStream, outputFile: File) {
        val key = SecretKeySpec(
            SECRET_KEY, ALGORITHM
        )
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }


    private val pathBASE = "/Users/vkas/AndroidStudioProjects/NewCleanF378/CoreD/"
    @Test
    fun addition_dex() {
        val sourceFilePath = "${pathBASE}makejar/dex/classes.dex" // 源文件路径，可按需修改
        val outputFolderPath = "${pathBASE}output" // 目标文件路径，可按需修改
        val sourceFile = File(sourceFilePath)
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
        }

        val file3 = File("$outputFolderPath/mast.zip")
        val string = dexToAesText(sourceFile)
        println("文件重写并保存成功")
        // 验证
        file3.writeText(string)
        // aes+iv 加密
        val restoredDex = File(outputFolderPath, "dexMy2.dex")
        val dexBytes = decryptDex(DEX_AES_KEY, file3.readText())
        FileOutputStream(restoredDex).use { it.write(dexBytes) }
    }

    private val DEX_AES_KEY = "m6khiy86thg6rf4d".toByteArray() // 16, 24, or 32 bytes


    // DEX -> AES加密文本
    fun dexToAesText(dexFile: File): String {
        val dexBytes = dexFile.readBytes()
        val encrypted = encrypt(dexBytes)
        return Base64.getEncoder().encodeToString(encrypted)
    }


    // 加密
    fun encrypt(inputBytes: ByteArray): ByteArray {
        val key = SecretKeySpec(DEX_AES_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val outputBytes = cipher.doFinal(inputBytes)
        return outputBytes
    }

    // 解密
    private fun decryptDex(keyAes: ByteArray, inStr: String): ByteArray {
        val inputBytes = Base64.getDecoder().decode(inStr)
        val key = SecretKeySpec(keyAes, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputBytes = cipher.doFinal(inputBytes)
        return outputBytes
    }

}