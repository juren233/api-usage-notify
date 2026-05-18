package com.juren233.usagenotify.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.Site
import com.juren233.usagenotify.data.model.SiteType

@Composable
fun BalanceCard(
    site: Site,
    balanceResult: BalanceResult?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val siteMeta = listOfNotNull(
        balanceResult.recognitionLabel(site.type),
        site.url,
    ).joinToString(" · ")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = siteMeta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            when (balanceResult) {
                is BalanceResult.Success -> Text(
                    text = "$${String.format("%.2f", balanceResult.balance)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (balanceResult.balance < site.alertThreshold)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                )
                is BalanceResult.Error -> Text(
                    text = "Error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                null -> Text(
                    text = "—",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun BalanceResult?.recognitionLabel(savedType: SiteType): String? {
    val savedDetectedType = savedType.takeUnless { it == SiteType.AUTO }
    return when (this) {
        is BalanceResult.Success -> "识别结果: ${(detectedType ?: savedDetectedType)?.displayName ?: "未知"}"
        is BalanceResult.Error -> "识别失败"
        null -> savedDetectedType?.let { "识别结果: ${it.displayName}" }
    }
}
