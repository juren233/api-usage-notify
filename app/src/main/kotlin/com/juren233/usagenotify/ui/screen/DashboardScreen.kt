package com.juren233.usagenotify.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juren233.usagenotify.R
import com.juren233.usagenotify.service.BalancePollingService
import com.juren233.usagenotify.ui.component.BalanceCard
import com.juren233.usagenotify.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddSite: () -> Unit,
    onNavigateToSiteDetail: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val sites by viewModel.sites.collectAsState()
    val pollingState by viewModel.pollingState.collectAsState()
    val monitoredSiteId by viewModel.monitoredSiteId.collectAsState()
    val context = LocalContext.current
    var showSitePickerDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, proceed */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UsageNotify") },
                actions = {
                    IconButton(onClick = onNavigateToAddSite) {
                        Icon(Icons.Default.Add, contentDescription = "添加中转站")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (pollingState.isPolling) {
                    context.stopService(Intent(context, BalancePollingService::class.java))
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    context.startForegroundService(
                        Intent(context, BalancePollingService::class.java)
                    )
                }
            }) {
                if (pollingState.isPolling) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stop_24),
                        contentDescription = "停止监控",
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "开始监控")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("总余额", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$${String.format("%.2f", pollingState.totalBalance)}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${sites.size} 个站点",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (pollingState.lastUpdated > 0) {
                                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                Text(
                                    "更新于 ${timeFormat.format(Date(pollingState.lastUpdated))}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            if (sites.isNotEmpty()) {
                item {
                    val selectedName = sites.firstOrNull { it.id == monitoredSiteId }?.name ?: "未选择"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSitePickerDialog = true },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("通知监控站点", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    selectedName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (sites.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("暂无站点", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "点击右下角 + 添加中转站",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(sites, key = { it.id }) { site ->
                BalanceCard(
                    site = site,
                    balanceResult = pollingState.balances[site.id],
                    onClick = { onNavigateToSiteDetail(site.id) },
                )
            }
        }

        if (showSitePickerDialog) {
            AlertDialog(
                onDismissRequest = { showSitePickerDialog = false },
                title = { Text("选择监控站点") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(sites, key = { it.id }) { site ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setMonitoredSite(site.id, site.name)
                                        showSitePickerDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                            ) {
                                RadioButton(
                                    selected = site.id == monitoredSiteId,
                                    onClick = {
                                        viewModel.setMonitoredSite(site.id, site.name)
                                        showSitePickerDialog = false
                                    },
                                )
                                Text(
                                    site.name,
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSitePickerDialog = false }) {
                        Text("取消")
                    }
                },
            )
        }
    }
}
