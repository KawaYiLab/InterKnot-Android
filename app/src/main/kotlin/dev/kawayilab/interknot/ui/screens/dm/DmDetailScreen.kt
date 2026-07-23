@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.dm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.kawayilab.interknot.model.DmConversationDetail
import dev.kawayilab.interknot.model.DmMessage

@Composable
fun DmDetailScreen(
    conversationId: String?,
    targetUserId: Int?,
    targetName: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DmDetailViewModel = hiltViewModel()
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val editingMessage by viewModel.editingMessage.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<DmMessage?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.sendImage(it) }
    }

    LaunchedEffect(editingMessage) {
        inputText = editingMessage?.content ?: ""
    }

    LaunchedEffect(conversationId, targetUserId, targetName) {
        viewModel.load(conversationId, targetUserId, targetName)
    }

    val title = conversation?.let { conv ->
        conv.members.find { !it.isSelf }?.name ?: conv.title ?: targetName ?: "会话"
    } ?: (targetName ?: "会话")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            DmInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                isEditing = editingMessage != null,
                isSending = isSending,
                onSend = { text ->
                    viewModel.sendText(text)
                    inputText = ""
                },
                onImageClick = { imageLauncher.launch("image/*") },
                onCancelEdit = { viewModel.cancelEditing() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            if (isLoading && messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (error != null && messages.isEmpty()) {
                Text(
                    text = error ?: "加载失败",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                DmMessageList(
                    messages = messages,
                    conversation = conversation,
                    onMessageLongPress = { selectedMessage = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    selectedMessage?.let { message ->
        DmMessageActionsDialog(
            message = message,
            onDismiss = { selectedMessage = null },
            onEdit = {
                viewModel.startEditing(message)
                selectedMessage = null
            },
            onWithdraw = {
                viewModel.withdrawMessage(message.documentId)
                selectedMessage = null
            }
        )
    }
}

@Composable
private fun DmMessageList(
    messages: List<DmMessage>,
    conversation: DmConversationDetail?,
    onMessageLongPress: (DmMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val selfUserId = conversation?.members?.find { it.isSelf }?.userId

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        modifier = modifier
    ) {
        items(messages, key = { it.documentId }) { message ->
            DmMessageBubble(
                message = message,
                isSelf = message.isFromSelf || message.sender?.userId == selfUserId,
                onLongPress = { onMessageLongPress(message) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DmMessageBubble(
    message: DmMessage,
    isSelf: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        if (!isSelf) {
            AsyncImage(
                model = message.sender?.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start) {
            if (!isSelf && !message.sender?.name.isNullOrBlank()) {
                Text(
                    text = message.sender?.name ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (message.kind == "image" && message.content != null && message.deletedAt == null) {
                AsyncImage(
                    model = message.content,
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                        .padding(horizontal = 4.dp)
                )
            } else {
                Text(
                    text = message.content ?: if (message.deletedAt != null) "已撤回" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.deletedAt != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textAlign = if (isSelf) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DmInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    isSending: Boolean,
    onSend: (String) -> Unit,
    onImageClick: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("编辑消息", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onCancelEdit) {
                    Icon(Icons.Default.Close, contentDescription = "取消编辑")
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImageClick) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "发送图片",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(if (isEditing) "编辑消息" else "输入消息") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (value.isNotBlank() && !isSending) {
                            onSend(value.trim())
                            onValueChange("")
                        }
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (value.isNotBlank() && !isSending) {
                        onSend(value.trim())
                        onValueChange("")
                    }
                    focusManager.clearFocus()
                },
                enabled = value.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isEditing) "保存" else "发送",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DmMessageActionsDialog(
    message: DmMessage,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onWithdraw: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("消息") },
        text = {
            Column {
                if (message.isFromSelf && message.deletedAt == null) {
                    TextButton(onClick = onEdit) { Text("编辑") }
                    TextButton(onClick = onWithdraw) { Text("撤回") }
                } else {
                    Text("无可用操作")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
