package com.juren233.usagenotify.ui.screen

import android.content.Intent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juren233.usagenotify.service.BalancePollingService
import com.juren233.usagenotify.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val pollingState by viewModel.isPolling.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()
    val isRealtimeMode by viewModel.isRealtimeMode.collectAsState()
    val sufficientThresholdInput by viewModel.liveUpdateSufficientThresholdInput.collectAsState()
    val dangerThresholdInput by viewModel.liveUpdateDangerThresholdInput.collectAsState()
    val insufficientThresholdInput by viewModel.liveUpdateInsufficientThresholdInput.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Monitoring toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("后台监控", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (pollingState.isPolling) "运行中" else "已停止",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = pollingState.isPolling,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            context.startForegroundService(
                                Intent(context, BalancePollingService::class.java)
                            )
                        } else {
                            context.stopService(
                                Intent(context, BalancePollingService::class.java)
                            )
                        }
                    },
                )
            }

            // Realtime mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("实时模式", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isRealtimeMode) "每 10 秒刷新" else "默认模式",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = isRealtimeMode,
                    onCheckedChange = { viewModel.setRealtimeMode(it) },
                )
            }

            // Refresh interval (only when not realtime)
            if (!isRealtimeMode) {
                Column {
                    Text("刷新间隔: ${refreshInterval}s", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = refreshInterval.toFloat(),
                        onValueChange = { viewModel.setRefreshInterval(it.roundToInt()) },
                        valueRange = 15f..120f,
                        steps = 20,
                    )
                }
            }

            // Live update thresholds
            Column {
                Text("实时动态通知", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = sufficientThresholdInput,
                    onValueChange = viewModel::setLiveUpdateSufficientThresholdInput,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("充足") },
                )
                OutlinedTextField(
                    value = dangerThresholdInput,
                    onValueChange = viewModel::setLiveUpdateDangerThresholdInput,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("危险") },
                )
                OutlinedTextField(
                    value = insufficientThresholdInput,
                    onValueChange = viewModel::setLiveUpdateInsufficientThresholdInput,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("不足") },
                )
            }
        }
    }
}
