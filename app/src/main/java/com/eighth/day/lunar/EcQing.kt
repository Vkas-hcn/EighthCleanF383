package com.eighth.day.lunar

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eighth.day.lunar.databinding.EcQiBinding
import com.eighth.day.lunar.qi.*


class EcQing : AppCompatActivity(), SplashNavigationLifecycle {
    
    private val binding by lazy {
        EcQiBinding.inflate(layoutInflater) 
    }
    
    private val navigator by lazy {
        DefaultSplashNavigator(
            activity = this,
            onComplete = ::onNavigationComplete
        )
    }
    
    private val navConfig by lazy {
        navigationConfig {
            delayMillis = SPLASH_DELAY_MILLIS
            destination = EcChu::class
            enableBackPress = false
            onNavigate = ::onPreNavigate
            onComplete = ::onPostNavigate
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
        setupNavigationBehavior()
        startAutoNavigation()
    }

    private fun initializeView() {
        enableEdgeToEdge()
        setContentView(binding.root)
        applyWindowInsets()
    }
    

    private fun applyWindowInsets() {
        binding.qi.applySystemWindowInsets()
    }
    

    private fun setupNavigationBehavior() {
        // 使用高阶函数禁用返回键
        disableBackPress { 
            // 空实现，完全禁用返回
        }
    }
    

    private fun startAutoNavigation() {
        configureSplashNavigation(navigator, navConfig)

    }
    

    override fun onPreNavigate() {
    }
    
    override fun onPostNavigate() {
    }
    
    override fun onNavigationComplete() {
    }
    


    private inline fun disableBackPress(crossinline onBackPressed: () -> Unit) {
        onBackPressedDispatcher.addCallback(this) { 
            onBackPressed() 
        }
    }
    
    companion object {
        private const val SPLASH_DELAY_MILLIS = 1500L
    }
}


interface SplashNavigationLifecycle {
    fun onPreNavigate()
    fun onPostNavigate()
    fun onNavigationComplete()
}


private fun android.view.View.applySystemWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
        }
        insets
    }
}


