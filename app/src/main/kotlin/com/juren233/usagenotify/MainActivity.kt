package com.juren233.usagenotify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.juren233.usagenotify.ui.navigation.AppNavigation
import com.juren233.usagenotify.ui.theme.UsageNotifyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UsageNotifyTheme {
                AppNavigation()
            }
        }
    }
}
