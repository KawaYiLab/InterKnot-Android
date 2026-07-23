@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun CreateScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExam: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val body by viewModel.body.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isPublishing by viewModel.isPublishing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val examStatus by viewModel.examStatus.collectAsStateWithLifecycle()
    val benefits by viewModel.benefits.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("发布委托") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ExamBanner(
                examStatus = examStatus,
                onNavigateToExam = onNavigateToExam
            )

            OutlinedTextField(
                value = title,
                onValueChange = viewModel::setTitle,
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isPublishing
            )

            OutlinedTextField(
                value = body,
                onValueChange = viewModel::setBody,
                label = { Text("正文") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                minLines = 5,
                enabled = !isPublishing
            )

            Text(
                text = "分类",
                style = MaterialTheme.typography.titleSmall
            )
            CategoryChips(
                categories = categories,
                selected = category,
                onSelected = viewModel::setCategory
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("匿名发布")
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = viewModel::setAnonymous,
                    enabled = !isPublishing
                )
            }

            Text(
                text = "图片",
                style = MaterialTheme.typography.titleSmall
            )
            ImagePickerRow(
                images = selectedImages.map { it.uri },
                onAddClick = { imageLauncher.launch("image/*") },
                onRemove = { viewModel.removeImage(it) },
                isUploading = selectedImages.any { it.isUploading }
            )

            val maxImages = benefits?.benefits?.articleMaxImages ?: 9
            Text(
                text = "${selectedImages.size} / ${maxImages} 张",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    viewModel.publish(
                        onSuccess = { onNavigateBack() },
                        onError = {}
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPublishing
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("发布")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    categories: List<Category>,
    selected: Category?,
    onSelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        categories.forEach { cat ->
            FilterChip(
                selected = cat.documentId == selected?.documentId,
                onClick = { onSelected(if (cat == selected) null else cat) },
                label = { Text(cat.name ?: cat.slug ?: "") }
            )
        }
    }
}

@Composable
private fun ExamBanner(
    examStatus: dev.kawayilab.interknot.model.ExamStatus?,
    onNavigateToExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passed = examStatus?.passed == true
    val color = if (passed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val textColor = if (passed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color, shape = MaterialTheme.shapes.medium)
            .then(if (passed) Modifier else Modifier.clickable { onNavigateToExam() })
            .padding(Spacing.md)
    ) {
        Text(
            text = if (passed) "已通过入站考试，可以发布" else "未通过入站考试，点击前往考试",
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ImagePickerRow(
    images: List<Uri>,
    onAddClick: () -> Unit,
    onRemove: (Uri) -> Unit,
    isUploading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(images, key = { it.toString() }) { uri ->
            Box(modifier = Modifier.size(96.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .size(96.dp)
                    .clickable(enabled = !isUploading, onClick = onAddClick),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加图片",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
