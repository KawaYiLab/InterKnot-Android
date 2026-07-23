@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.dm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.R
import dev.kawayilab.interknot.model.DmConversationDetail
import dev.kawayilab.interknot.model.DmMessage
import dev.kawayilab.interknot.ui.components.common.InterknotImage
import dev.kawayilab.interknot.ui.theme.LocalInterknotColors

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

    val peer = conversation?.members?.find { !it.isSelf }
    val title = peer?.name ?: conversation?.title ?: targetName ?: "会话"
    val avatarUrl = conversation?.avatar ?: peer?.avatar
    val defaultAvatar = painterResource(R.drawable.default_avatar)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InterknotImage(
                            model = avatarUrl,
                            contentDescription = "头像",
                            placeholderPainter = defaultAvatar,
                            errorPainter = defaultAvatar,
                            fallbackPainter = defaultAvatar,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
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
    val extendedColors = LocalInterknotColors.current
    val bubbleColor = if (isSelf) extendedColors.messageOutgoing else extendedColors.messageIncoming
    val contentColor = if (message.deletedAt != null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        extendedColors.messageText
    }
    val defaultAvatar = painterResource(R.drawable.default_avatar)
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = if (isSelf) 4.dp else 18.dp,
        bottomStart = if (isSelf) 18.dp else 4.dp
    )

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
            InterknotImage(
                model = message.sender?.avatar,
                contentDescription = "头像",
                placeholderPainter = defaultAvatar,
                errorPainter = defaultAvatar,
                fallbackPainter = defaultAvatar,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier.fillMaxWidth(0.76f),
            contentAlignment = if (isSelf) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Column(horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start) {
                if (!isSelf && !message.sender?.name.isNullOrBlank()) {
                    Text(
                        text = message.sender?.name ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                if (message.kind == "image" && message.content != null && message.deletedAt == null) {
                    Box(
                        modifier = Modifier
                            .clip(bubbleShape)
                            .background(bubbleColor)
                            .sizeIn(maxWidth = 240.dp, maxHeight = 240.dp)
                    ) {
                        InterknotImage(
                            model = message.content,
                            contentDescription = "图片",
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = formatMessageTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .background(bubbleColor, bubbleShape)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = message.content ?: if (message.deletedAt != null) "已撤回" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatMessageTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.72f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMessageTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(iso)
        val zdt = instant.atZone(java.time.ZoneId.systemDefault())
        java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.getDefault())
            .format(zdt)
    } catch (_: Exception) {
        iso
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
            .padding(horizontal = 8.dp, vertical = 6.dp)
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImageClick) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "发送图片",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
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
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (value.isBlank()) {
                                Text(
                                    text = if (isEditing) "编辑消息" else "输入消息",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

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
                        tint = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
