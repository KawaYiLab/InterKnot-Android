@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.level

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.model.DailyExp
import dev.kawayilab.interknot.model.ExpInfo
import dev.kawayilab.interknot.ui.components.common.EmptyState
import dev.kawayilab.interknot.ui.components.common.ErrorState

@Composable
fun LevelScreen(
    modifier: Modifier = Modifier,
    viewModel: LevelViewModel = hiltViewModel()
) {
    val expInfo by viewModel.expInfo.collectAsStateWithLifecycle()
    val dailyExp by viewModel.dailyExp.collectAsStateWithLifecycle()
    val checkInStatus by viewModel.checkInStatus.collectAsStateWithLifecycle()
    val benefits by viewModel.benefits.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isCheckingIn by viewModel.isCheckingIn.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val checkInResult by viewModel.checkInResult.collectAsStateWithLifecycle()

    var showCheckInMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(checkInResult) {
        checkInResult?.let {
            showCheckInMessage = "签到成功：${it.reward} 经验，第 ${it.consecutiveDays} 天"
            viewModel.consumeCheckInResult()
        }
    }

    LaunchedEffect(error) {
        error?.let { showCheckInMessage = it }
    }

    if (showCheckInMessage != null) {
        AlertDialog(
            onDismissRequest = { showCheckInMessage = null },
            title = { Text(if (error != null) "提示" else "签到") },
            text = { Text(showCheckInMessage ?: "") },
            confirmButton = { TextButton(onClick = { showCheckInMessage = null }) { Text("确定") } }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("绳网等级") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (expInfo == null && !isLoading && error != null) {
                ErrorState(
                    message = error ?: "加载失败",
                    onRetry = { viewModel.load() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { LevelCard(expInfo, benefits) }

                    item {
                        val canCheckIn = checkInStatus?.canCheckIn == true
                        Button(
                            onClick = { viewModel.checkIn() },
                            enabled = canCheckIn && !isCheckingIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isCheckingIn) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    if (canCheckIn) "签到" else "今日已签到"
                                )
                            }
                        }
                        if (checkInStatus != null) {
                            Text(
                                text = "累计签到 ${checkInStatus?.totalDays ?: 0} 天，连续 ${checkInStatus?.consecutiveDays ?: 0} 天",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    item { DailyExpCard(dailyExp) }

                    item { BenefitsCard(benefits) }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(expInfo: ExpInfo?, benefits: dev.kawayilab.interknot.model.Benefits?) {
    val level = expInfo?.level ?: benefits?.level ?: 1
    val exp = expInfo?.exp ?: 0
    val maxLevel = benefits?.maxLevel ?: level
    val progress = (exp % 100) / 100f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Lv.$level",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "经验值：$exp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "最高等级：$maxLevel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyExpCard(dailyExp: DailyExp?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "今日经验",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val gained = dailyExp?.todaySelfGained ?: 0
            val cap = dailyExp?.todaySelfCap ?: 0
            Text(
                text = "$gained / $cap",
                style = MaterialTheme.typography.bodyMedium
            )
            if (cap > 0) {
                LinearProgressIndicator(
                    progress = { (gained.toFloat() / cap).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val sources = dailyExp?.sources
            if (sources != null) {
                DailySourceRow("签到", sources.checkIn)
                DailySourceRow("发委托", sources.createArticle)
                DailySourceRow("发评论", sources.createComment)
                DailySourceRow("点赞", sources.likeGive)
            }
        }
    }
}

@Composable
private fun DailySourceRow(label: String, source: dev.kawayilab.interknot.model.DailyExpSource) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${if (source.done) "已完成" else "未完成"} · ${source.exp} 经验",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BenefitsCard(benefits: dev.kawayilab.interknot.model.Benefits?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "等级权益",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (benefits == null) {
                EmptyState(message = "暂无权益数据", modifier = Modifier.fillMaxWidth())
            } else {
                val b = benefits.benefits
                Text("委托字数上限：${b.articleMaxBody ?: "-"}")
                Text("评论字数上限：${b.commentMaxBody ?: "-"}")
                Text("委托图片上限：${b.articleMaxImages ?: "-"}")
                Text("私信图片上限：${b.dmMaxImages ?: "-"}")
                Text("置顶槽位：${b.pinSlots ?: "-"}")
            }
        }
    }
}
