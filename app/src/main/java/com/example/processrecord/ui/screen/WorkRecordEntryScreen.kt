package com.example.processrecord.ui.screen
import androidx.compose.material.icons.filled.Check

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.ColorEntryUi
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordUiState
import com.example.processrecord.ui.utils.ImageUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordEntryScreen(
    viewModel: WorkRecordEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
    navigateToColorPresetManage: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val processList by viewModel.processList.collectAsState()
    val styleList by viewModel.styleList.collectAsState()
    val colorGroups by viewModel.colorGroups.collectAsState()
    val colorPresets by viewModel.colorPresets.collectAsState()
    val title = if (viewModel.workRecordUiState.workRecordDetails.id == 0L) "记一笔" else "编辑记录"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        WorkRecordEntryBody(
            workRecordUiState = viewModel.workRecordUiState,
            processList = processList,
            styleList = styleList,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onValueChange = viewModel::updateUiState,
            onProcessSelected = viewModel::onProcessSelected,
            onStyleSelected = viewModel::onStyleSelected,
            onAddProcess = viewModel::addProcess,
            onUpdateProcess = viewModel::updateProcess,
            onDeleteProcess = viewModel::deleteProcess,
            onAddColorEntryFromPreset = viewModel::addColorEntryFromPreset,
            onUpdateColorEntryQuantity = viewModel::updateColorEntryQuantity,
            onRemoveColorEntry = viewModel::removeColorEntry,
            onManageColorPresetsClick = navigateToColorPresetManage,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveWorkRecord()
                    navigateBack()
                }
            },
            onDeleteClick = {
                coroutineScope.launch {
                    viewModel.deleteRecord()
                    navigateBack()
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/** 分组卡片标题行 */
@Composable
private fun SectionHeader(title: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        icon()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 精美分组卡片容器 */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun WorkRecordEntryBody(
    workRecordUiState: WorkRecordUiState,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onStyleSelected: (String) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WorkRecordInputForm(
            workRecordDetails = workRecordUiState.workRecordDetails,
            processList = processList,
            styleList = styleList,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onValueChange = onValueChange,
            onProcessSelected = onProcessSelected,
            onStyleSelected = onStyleSelected,
            onAddProcess = onAddProcess,
            onUpdateProcess = onUpdateProcess,
            onDeleteProcess = onDeleteProcess,
            onAddColorEntryFromPreset = onAddColorEntryFromPreset,
            onUpdateColorEntryQuantity = onUpdateColorEntryQuantity,
            onRemoveColorEntry = onRemoveColorEntry,
            onManageColorPresetsClick = onManageColorPresetsClick
        )

        // ── 保存 / 删除按钮 ──────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSaveClick,
                enabled = workRecordUiState.isEntryValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (workRecordUiState.workRecordDetails.id == 0L) "保存记录" else "更新记录",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            if (workRecordUiState.workRecordDetails.id != 0L) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("删除记录", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordInputForm(
    workRecordDetails: WorkRecordDetails,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onStyleSelected: (String) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedProcess by remember { mutableStateOf(false) }
    var expandedStyle by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var initialPage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // 工序编辑弹窗状态
    var showProcessDialog by remember { mutableStateOf(false) }
    var editingProcess by remember { mutableStateOf<Process?>(null) }
    var processDialogName by remember { mutableStateOf("") }
    var processDialogPrice by remember { mutableStateOf("") }
    var processDialogUnit by remember { mutableStateOf("件") }
    var showDeleteProcessDialog by remember { mutableStateOf<Process?>(null) }
    
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                coroutineScope.launch {
                    val newPaths = workRecordDetails.imagePaths.toMutableList()
                    uris.forEach { uri ->
                        val localPath = ImageUtils.copyImageToPrivateStorage(context, uri)
                        if (localPath != null) newPaths.add(localPath)
                    }
                    onValueChange(workRecordDetails.copy(imagePaths = newPaths))
                }
            }
        }
    )

    // 拍照：先创建临时文件，拍完后复制到私有目录
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val uri = cameraImageUri
                if (uri != null) {
                    coroutineScope.launch {
                        val localPath = ImageUtils.copyImageToPrivateStorage(context, uri)
                        if (localPath != null) {
                            val newPaths = workRecordDetails.imagePaths.toMutableList()
                            newPaths.add(localPath)
                            onValueChange(workRecordDetails.copy(imagePaths = newPaths))
                        }
                    }
                }
            }
        }
    )

    // 实际启动相机（已有权限时调用）
    fun doLaunchCamera() {
        try {
            val imageDir = File(context.cacheDir, "images").also { it.mkdirs() }
            val imageFile = File.createTempFile(
                "photo_${System.currentTimeMillis()}",
                ".jpg",
                imageDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "无法启动相机：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 相机权限申请 launcher：获得授权后立即启动相机
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            doLaunchCamera()
        } else {
            android.widget.Toast.makeText(context, "需要相机权限才能拍照", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 点击拍照：先检查权限，没有则申请
    fun launchCamera() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            doLaunchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Date Picker Logic for Attribution Date
    fun showDatePicker() {
        if (workRecordDetails.date > 0) {
            calendar.timeInMillis = workRecordDetails.date
        } else {
            calendar.timeInMillis = System.currentTimeMillis()
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                // Keep time as is or set to current? Usually Date is just date. 
                // But our DB uses timestamp. Let's keep it simple and just set YMD.
                onValueChange(workRecordDetails.copy(date = calendar.timeInMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Time Picker Logic
    fun showDateTimePicker(isStartTime: Boolean) {
        // If editing existing time, set calendar to that time
        val initialTime = if (isStartTime) workRecordDetails.startTime else workRecordDetails.endTime
        if (initialTime > 0) {
            calendar.timeInMillis = initialTime
        } else {
            // Default to current time if not set
            calendar.timeInMillis = System.currentTimeMillis()
        }

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val timestamp = calendar.timeInMillis
                    if (isStartTime) {
                        onValueChange(workRecordDetails.copy(startTime = timestamp))
                    } else {
                        onValueChange(workRecordDetails.copy(endTime = timestamp))
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            context,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ── 卡片1：归属日期 + 样板图片 ──────────────────────────────
        SectionCard {
            SectionHeader(title = "基本信息") {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 归属日期
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatDate(workRecordDetails.date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("归属日期（统计用）") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 8.dp)
                        .clickable { showDatePicker() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 样板图片
            val thumbnailPagerState = rememberPagerState(initialPage = 0) { workRecordDetails.imagePaths.size }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("样板图片", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    if (workRecordDetails.imagePaths.isNotEmpty()) {
                        Text(
                            text = "(${thumbnailPagerState.currentPage + 1}/${workRecordDetails.imagePaths.size}张)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { launchCamera() },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("📷 拍照", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🖼 相册", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (workRecordDetails.imagePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(state = thumbnailPagerState, modifier = Modifier.fillMaxSize()) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize().clickable {
                                initialPage = page; showGallery = true
                            }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(workRecordDetails.imagePaths[page]).crossfade(true).build(),
                                contentDescription = "样板图片",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // 页码指示器
                    if (workRecordDetails.imagePaths.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "${thumbnailPagerState.currentPage + 1}/${workRecordDetails.imagePaths.size}",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Add, "添加图片", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("点击添加样板图片", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        if (showGallery && workRecordDetails.imagePaths.isNotEmpty()) {
            Dialog(onDismissRequest = { showGallery = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val pagerState = rememberPagerState(initialPage = initialPage) { workRecordDetails.imagePaths.size }
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        ZoomableImage(
                            imagePath = workRecordDetails.imagePaths[page],
                            onDismiss = { showGallery = false }
                        )
                    }
                    
                    // Page Indicator
                    if (workRecordDetails.imagePaths.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${workRecordDetails.imagePaths.size}",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Delete Button (Top Right) - Optional, user said "no close icon ON picture", maybe they mean the thumbnail delete button.
                    // But we still need a way to delete. Let's put a trash icon in the top bar area.
                    // Or just hide it for now as per "clean view" request? 
                    // Let's add it but make it subtle at the top right of the SCREEN, not on the image if zoomed.
                    // Actually, let's respect "don't show close icon" strictly for the picture view itself.
                    // But we should provide a way to delete. 
                    // Let's add a top bar overlay.
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .zIndex(1f)
                    ) {
                        IconButton(
                            onClick = {
                                val currentPath = workRecordDetails.imagePaths[pagerState.currentPage]
                                val newPaths = workRecordDetails.imagePaths.toMutableList()
                                newPaths.remove(currentPath)
                                onValueChange(workRecordDetails.copy(imagePaths = newPaths))
                                // 同步删除私有目录中的本地文件
                                ImageUtils.deleteImageFromPrivateStorage(currentPath)
                                if (newPaths.isEmpty()) {
                                    showGallery = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        
                        IconButton(onClick = { showGallery = false }) {
                             Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── 卡片2：款号 + 工序 + 序号 + 总数量 ─────────────────────
        SectionCard {
            SectionHeader(title = "款号与工序") {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 款号（可直接编辑，右侧箭头展开历史款号）
            OutlinedTextField(
                value = workRecordDetails.style,
                onValueChange = { onValueChange(workRecordDetails.copy(style = it)) },
                label = { Text("款号") },
                leadingIcon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null) },
                trailingIcon = {
                    Box {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "历史款号",
                            modifier = Modifier.clickable { expandedStyle = true }
                        )
                        DropdownMenu(
                            expanded = expandedStyle,
                            onDismissRequest = { expandedStyle = false }
                        ) {
                            if (styleList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("暂无历史款号", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { expandedStyle = false }
                                )
                            } else {
                                styleList.forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text(style.name) },
                                        onClick = {
                                            onStyleSelected(style.name)
                                            expandedStyle = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 工序
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = workRecordDetails.processName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("工序") },
                    leadingIcon = { Icon(imageVector = Icons.Default.List, contentDescription = null) },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Box(modifier = Modifier.matchParentSize().padding(top = 8.dp).clickable { expandedProcess = true })
                DropdownMenu(expanded = expandedProcess, onDismissRequest = { expandedProcess = false },
                    modifier = Modifier.fillMaxWidth(0.9f)) {
                    // 新增工序按钮
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("新增工序", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = {
                            expandedProcess = false
                            editingProcess = null
                            processDialogName = ""
                            processDialogPrice = ""
                            processDialogUnit = "件"
                            showProcessDialog = true
                        }
                    )
                    if (processList.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    processList.forEach { process ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(process.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("${process.defaultPrice}/${process.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                            },
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            expandedProcess = false
                                            editingProcess = process
                                            processDialogName = process.name
                                            processDialogPrice = process.defaultPrice.toString()
                                            processDialogUnit = process.unit
                                            showProcessDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "编辑工序", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            expandedProcess = false
                                            showDeleteProcessDialog = process
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除工序", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            onClick = { onProcessSelected(process); expandedProcess = false }
                        )
                    }
                }
            }

            // 工序新增/编辑弹窗
            if (showProcessDialog) {
                AlertDialog(
                    onDismissRequest = { showProcessDialog = false },
                    title = { Text(if (editingProcess == null) "新增工序" else "编辑工序") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = processDialogName,
                                onValueChange = { processDialogName = it },
                                label = { Text("工序名称") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = processDialogPrice,
                                    onValueChange = { processDialogPrice = it },
                                    label = { Text("默认单价") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = processDialogUnit,
                                    onValueChange = { processDialogUnit = it },
                                    label = { Text("单位") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val name = processDialogName.trim()
                                val price = processDialogPrice.toDoubleOrNull() ?: 0.0
                                val unit = processDialogUnit.trim().ifEmpty { "件" }
                                if (name.isNotEmpty()) {
                                    val ep = editingProcess
                                    if (ep == null) {
                                        onAddProcess(name, price, unit)
                                    } else {
                                        onUpdateProcess(ep.copy(name = name, defaultPrice = price, unit = unit))
                                    }
                                    showProcessDialog = false
                                }
                            },
                            enabled = processDialogName.trim().isNotEmpty()
                        ) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProcessDialog = false }) { Text("取消") }
                    }
                )
            }

            // 工序删除确认弹窗
            showDeleteProcessDialog?.let { process ->
                AlertDialog(
                    onDismissRequest = { showDeleteProcessDialog = null },
                    title = { Text("删除工序") },
                    text = { Text("确定要删除工序「${process.name}」吗？") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteProcess(process)
                            showDeleteProcessDialog = null
                        }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteProcessDialog = null }) { Text("取消") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 序号 + 总数量
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = workRecordDetails.serialNumber,
                    onValueChange = { onValueChange(workRecordDetails.copy(serialNumber = it)) },
                    label = { Text("序号") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = workRecordDetails.totalQuantity,
                    onValueChange = { onValueChange(workRecordDetails.copy(totalQuantity = it)) },
                    label = { Text("总数量") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // ── 卡片3：颜色明细 ──────────────────────────────────────────
        var showAddColorSheet by remember { mutableStateOf(false) }
        val sheetGroupCollapsed = remember { mutableStateMapOf<Long, Boolean>() }
        val selectedColors = remember { androidx.compose.runtime.mutableStateListOf<ColorPreset>() }

        SectionCard {
            SectionHeader(title = "颜色数量明细") {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            brush = Brush.sweepGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color(0xFFE53935),
                                    androidx.compose.ui.graphics.Color(0xFF1E88E5),
                                    androidx.compose.ui.graphics.Color(0xFF43A047),
                                    androidx.compose.ui.graphics.Color(0xFFE53935)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showAddColorSheet = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加颜色", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onManageColorPresetsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("管理颜色库", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (showAddColorSheet) {
                ModalBottomSheet(onDismissRequest = {
                    showAddColorSheet = false
                    selectedColors.clear()
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("选择颜色", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                        // 已选颜色预览区（固定在顶部）
                        if (selectedColors.isNotEmpty()) {
                            Text("已选 ${selectedColors.size} 个颜色", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedColors.forEach { preset ->
                                    Box(
                                        modifier = Modifier
                                            .background(parseColorOrDefault(preset.hexValue), RoundedCornerShape(50))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(preset.name, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Close, contentDescription = "移除",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp).clickable { selectedColors.remove(preset) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 可滚动的颜色列表
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        val groupedFilteredPresets = colorGroups
                            .sortedBy { it.sortOrder }
                            .mapNotNull { group ->
                                val presets = colorPresets
                                    .filter { it.groupId == group.id }
                                    .sortedBy { it.sortOrder }
                                if (presets.isEmpty()) null else group to presets
                            }

                        if (groupedFilteredPresets.isNotEmpty()) {
                            groupedFilteredPresets.forEach { (group, presets) ->
                                val collapsed = sheetGroupCollapsed[group.id] ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = {
                                            sheetGroupCollapsed[group.id] = !collapsed
                                        }
                                    ) {
                                        Text(if (collapsed) "展开" else "收起")
                                    }
                                }

                                if (collapsed) {
                                    return@forEach
                                }

                                presets.forEach { preset ->
                                    val isSelected = selectedColors.any { it.id == preset.id }
                                    OutlinedButton(
                                        onClick = {
                                            if (!isSelected) selectedColors.add(preset)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isSelected
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(
                                                        color = parseColorOrDefault(preset.hexValue),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.size(8.dp))
                                            Text(preset.name)
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(Icons.Default.Check, contentDescription = "已选", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "没有匹配颜色，请先到“管理常用颜色”新增",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        } // end scrollable column

                        // 固定在底部的操作按钮
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedColors.forEach { preset ->
                                        onAddColorEntryFromPreset(preset.name, preset.hexValue)
                                    }
                                    showAddColorSheet = false
                                    selectedColors.clear()
                                },
                                enabled = selectedColors.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("完成")
                            }
                            OutlinedButton(
                                onClick = {
                                    showAddColorSheet = false
                                    selectedColors.clear()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("取消")
                            }
                        }

                        TextButton(
                            onClick = {
                                showAddColorSheet = false
                                selectedColors.clear()
                                onManageColorPresetsClick()
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("去管理常用颜色")
                        }
                    } // end outer column
                }
            }

            if (workRecordDetails.colorEntries.isNotEmpty()) {
                Text(
                    text = "颜色数量明细",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                workRecordDetails.colorEntries.forEach { entry ->
                    ColorQuantityRow(
                        entry = entry,
                        onQuantityChange = { qty -> onUpdateColorEntryQuantity(entry.colorName, qty) },
                        onRemove = { onRemoveColorEntry(entry.colorName) }
                    )
                }
            } else {
                Text(
                    text = "暂无颜色明细，点击上方“添加颜色”开始录入",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── 卡片4：颜色汇总 + 数量 + 单价 + 金额 ───────────────────
        SectionCard {
            SectionHeader(title = "数量与金额") {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 数量 + 单价
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = workRecordDetails.quantity,
                    onValueChange = { onValueChange(workRecordDetails.copy(quantity = it)) },
                    label = { Text("数量") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = workRecordDetails.unitPrice,
                    onValueChange = { onValueChange(workRecordDetails.copy(unitPrice = it)) },
                    label = { Text("单价") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 总金额（高亮显示）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "合计金额",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥ ${workRecordDetails.amount}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── 卡片5：时间记录 ──────────────────────────────────────────
        SectionCard {
            SectionHeader(title = "时间记录（选填）") {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // 开始时间
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = formatTime(workRecordDetails.startTime),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("开始时间") },
                        trailingIcon = {
                            if (workRecordDetails.startTime == 0L) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "开始",
                                    modifier = Modifier.clickable {
                                        onValueChange(workRecordDetails.copy(startTime = System.currentTimeMillis()))
                                    })
                            } else {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(modifier = Modifier.matchParentSize().padding(top = 8.dp, end = 40.dp)
                        .clickable { showDateTimePicker(true) })
                }
                // 结束时间
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = formatTime(workRecordDetails.endTime),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("结束时间") },
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(modifier = Modifier.matchParentSize().padding(top = 8.dp)
                        .clickable { showDateTimePicker(false) })
                }
            }

            // 用时显示
            if (workRecordDetails.startTime > 0 && workRecordDetails.endTime > workRecordDetails.startTime) {
                val ms = workRecordDetails.endTime - workRecordDetails.startTime
                val d = ms / 86400000L; val h = (ms % 86400000L) / 3600000L; val m = (ms % 3600000L) / 60000L
                val durStr = buildString {
                    if (d > 0) append("${d}天 ")
                    if (h > 0) append("${h}小时 ")
                    append("${m}分钟")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("⏱ 用时", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(durStr, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // ── 卡片6：备注 ──────────────────────────────────────────────
        SectionCard {
            SectionHeader(title = "备注（选填）") {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedTextField(
                value = workRecordDetails.remark,
                onValueChange = { onValueChange(workRecordDetails.copy(remark = it)) },
                label = { Text("备注内容") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
private fun ColorQuantityRow(
    entry: ColorEntryUi,
    onQuantityChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(parseColorOrDefault(entry.colorHex), shape = RoundedCornerShape(50))
            )
            Text(text = entry.colorName)
        }
        OutlinedTextField(
            value = entry.quantity,
            onValueChange = onQuantityChange,
            label = { Text("数量") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "删除颜色")
        }
    }
}

private fun parseColorOrDefault(hexValue: String): androidx.compose.ui.graphics.Color {
    return try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hexValue))
    } catch (_: Exception) {
        androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }
}


@Composable
fun ZoomableImage(
    imagePath: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange: Float, panChange: Offset, _: Float ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        if (scale > 1f) {
            val newOffset = offset + panChange * scale // Adjust pan speed by scale
            offset = newOffset
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // transformable handles zoom/pan well and mostly allows parent Pager to scroll if not consuming.
            // But sometimes it conflicts.
            // A common workaround is detecting if scale is 1, let pager handle scroll.
            .transformable(state = state)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = "Zoomable Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
