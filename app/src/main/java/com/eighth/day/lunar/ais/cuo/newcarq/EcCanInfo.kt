package com.eighth.day.lunar.ais.cuo.newcarq

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eighth.day.lunar.EcQing

class EcCanInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, EcQing::class.java))
        finish()
    }
}