package com.juren233.usagenotify.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import com.juren233.usagenotify.ui.viewmodel.SiteDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: SiteDetailViewModel = hiltViewModel(),
) {
    val site by viewModel.site.collectAsState()
    val balanceResult by viewModel.balanceResult.collectAsState()
    val history by viewModel.history.collectAsState()
    val historyDays by viewModel.historyDays.collectAsState()
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(site?.name ?: "站点详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    site?.let { s ->
                        IconButton(onClick = { onNavigateToEdit(s.id) }) {
                            Icon(Icons.Default.Edit, "编辑")
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            site?.let { s ->
                item {
                    val recognitionText = balanceResult.recognitionText(s.type)
                    Column {
                        Text(s.url, style = MaterialTheme.typography.bodyMedium)
                        if (recognitionText != null) {
                            Text(
                                recognitionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = historyDays == 7,
                        onClick = { viewModel.setHistoryDays(7) },
                        label = { Text("7 天") },
                    )
                    FilterChip(
                        selected = historyDays == 30,
                        onClick = { viewModel.setHistoryDays(30) },
                        label = { Text("30 天") },
                    )
                }
            }

            // TODO: Add Vico chart here once balance data accumulates

            item {
                Text(
                    "余额记录 (${history.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(history.reversed()) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        dateFormat.format(Date(record.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "$${String.format("%.4f", record.balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun BalanceResult?.recognitionText(savedType: SiteType): String? {
    val savedDetectedType = savedType.takeUnless { it == SiteType.AUTO }
    return when (this) {
        is BalanceResult.Success -> "识别结果: ${(detectedType ?: savedDetectedType)?.displayName ?: "未知"}"
        is BalanceResult.Error -> "识别失败"
        null -> savedDetectedType?.let { "识别结果: ${it.displayName}" }
    }
}
