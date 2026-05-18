package com.juren233.usagenotify.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.ui.viewmodel.hasCredentialInput
import com.juren233.usagenotify.ui.viewmodel.SiteViewModel
import com.juren233.usagenotify.ui.viewmodel.usesAccountLogin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSiteScreen(
    siteId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: SiteViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsState()
    val saved by viewModel.saved.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val isEditing = siteId != null

    LaunchedEffect(siteId) {
        if (siteId != null) viewModel.loadSite(siteId)
    }

    LaunchedEffect(saved) {
        if (saved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑站点" else "添加站点") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.deleteSite() }) {
                            Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("站点名称") },
                placeholder = { Text("例如：我的中转站") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = form.url,
                onValueChange = { viewModel.updateUrl(it) },
                label = { Text("站点地址") },
                placeholder = { Text("https://api.example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (form.usesAccountLogin()) {
                OutlinedTextField(
                    value = form.account,
                    onValueChange = { viewModel.updateAccount(it) },
                    label = { Text("账号/邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = form.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                            )
                        }
                    },
                )
            } else {
                OutlinedTextField(
                    value = form.credential,
                    onValueChange = { viewModel.updateCredential(it) },
                    label = { Text("Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = form.url.isNotBlank() && form.hasCredentialInput() && !form.isTesting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (form.isTesting) "测试中..." else "测试连接")
                }
                Button(
                    onClick = { viewModel.saveSite() },
                    enabled = form.name.isNotBlank() && form.url.isNotBlank()
                            && form.hasCredentialInput() && !form.isSaving
                            && form.testResult is BalanceResult.Success,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存")
                }
            }

            when (val result = form.testResult) {
                is BalanceResult.Success -> Text(
                    "识别结果: ${result.detectedType?.displayName ?: "未知"} · 余额: $${String.format("%.2f", result.balance)}",
                    color = MaterialTheme.colorScheme.primary,
                )
                is BalanceResult.Error -> Text(
                    "连接失败: ${result.message}",
                    color = MaterialTheme.colorScheme.error,
                )
                null -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
