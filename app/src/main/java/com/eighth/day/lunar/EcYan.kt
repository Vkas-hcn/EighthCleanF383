package com.eighth.day.lunar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eighth.day.lunar.databinding.EcYanBinding
import com.eighth.day.lunar.yan.*


class EcYan : AppCompatActivity(), SettingsInteractionCallback {
    
    private val binding by lazy {
        EcYanBinding.inflate(layoutInflater) 
    }
    
    private val actionHandler by lazy {
        DefaultSettingsActionHandler(
            activity = this,
            onResult = ::onActionExecuted
        )
    }
    
    private val appShareConfig by lazy {
        shareConfig {
            titleFromRes(this@EcYan, R.string.app_name)
            line("Check out this amazing cleaning app: ${getString(R.string.app_name)}")
            line("Download it now!")
            chooserTitle = "Share via"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
        configureSettingsActions()
    }
    


    private fun initializeView() {
        enableEdgeToEdge()
        setContentView(binding.root)
        applySystemWindowInsets()
    }
    

    private fun applySystemWindowInsets() {
        binding.yan.applyWindowInsetsCompat { systemBars ->
            setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
        }
    }
    

    private fun configureSettingsActions() {
        configureSettingsItems {
            item(R.id.btnBack) { 
                handleBackNavigation() 
            }
            item(R.id.itemPrivacySettings) { 
                handlePrivacySettings() 
            }
            item(R.id.itemShare) { 
                handleShareApp() 
            }
        }

    }
    


    private fun handleBackNavigation() {
        safeExecute(
            action = { 
                executeSettingsAction(
                    SettingsAction.NavigateBack,
                    actionHandler
                )
            },
            onSuccess = { onBackNavigationTriggered() }
        )
    }
    

    private fun handlePrivacySettings() {
        safeExecute(
            action = {
                executeSettingsAction(
                    SettingsAction.OpenPrivacy(
                        url = PRIVACY_POLICY_URL
                    ),
                    actionHandler
                )
            },
            onSuccess = { onPrivacySettingsOpened() }
        )
    }
    

    private fun handleShareApp() {
        safeExecute(
            action = {
                executeSettingsAction(
                    SettingsAction.ShareApp(appShareConfig),
                    actionHandler
                )
            },
            onSuccess = { onAppShared() }
        )
    }
    

    override fun onBackNavigationTriggered() {
    }
    
    override fun onPrivacySettingsOpened() {
    }
    
    override fun onAppShared() {
    }
    
    override fun onActionExecuted(success: Boolean, message: String?) {

    }
    
    companion object {
        //TODO 添加隐私政策链接
        private const val PRIVACY_POLICY_URL = "https://www.google.com/privacy"
    }
}


interface SettingsInteractionCallback {
    fun onBackNavigationTriggered()
    fun onPrivacySettingsOpened()
    fun onAppShared()
    fun onActionExecuted(success: Boolean, message: String?)
}


private inline fun android.view.View.applyWindowInsetsCompat(
    crossinline onApply: android.view.View.(androidx.core.graphics.Insets) -> Unit
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
            view.onApply(systemBars)
        }
        insets
    }
}


private inline fun android.view.View.onClick(crossinline action: () -> Unit) {
    setOnClickListener { action() }
}