package com.example.processrecord.ui.screen

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppActionChip
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.utils.ImageUtils
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

private const val IMAGE_PREVIEW_MIN_SCALE = 1f
private const val IMAGE_PREVIEW_MAX_SCALE = 10f

@Composable
fun RecordImageSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit
) {
    var showGallery by remember { mutableStateOf(false) }
    var initialPage by remember { mutableStateOf(0) }
    val context = LocalContext.current
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

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val uri = cameraImageUri ?: return@rememberLauncherForActivityResult
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
    )

    fun launchCameraInternal() {
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
            Toast.makeText(
                context,
                context.getString(
                    R.string.work_record_toast_camera_launch_failed,
                    e.message ?: context.getString(R.string.common_unknown_error)
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraInternal()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.work_record_toast_camera_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun launchCamera() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            launchCameraInternal()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val thumbnailPagerState = rememberPagerState(initialPage = 0) {
        workRecordDetails.imagePaths.size
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.work_record_image_title),
                style = MaterialTheme.typography.bodyMedium
            )
            if (workRecordDetails.imagePaths.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.work_record_image_count,
                        thumbnailPagerState.currentPage + 1,
                        workRecordDetails.imagePaths.size
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AppActionChip(
                text = stringResource(R.string.work_record_button_camera),
                onClick = { launchCamera() }
            )
            AppActionChip(
                text = stringResource(R.string.work_record_button_gallery),
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    }

    if (workRecordDetails.imagePaths.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        ) {
            HorizontalPager(
                state = thumbnailPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            initialPage = page
                            showGallery = true
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(workRecordDetails.imagePaths[page])
                            .allowHardware(false)
                            .bitmapConfig(Bitmap.Config.ARGB_8888)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.work_record_image_content_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            if (workRecordDetails.imagePaths.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${thumbnailPagerState.currentPage + 1}/${workRecordDetails.imagePaths.size}",
                        color = Color.White,
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
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.work_record_add_image_content_description),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.work_record_add_image_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showGallery && workRecordDetails.imagePaths.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showGallery = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val pagerState = rememberPagerState(initialPage = initialPage) {
                    workRecordDetails.imagePaths.size
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 0
                ) { page ->
                    ZoomableImage(imagePath = workRecordDetails.imagePaths[page])
                }

                if (workRecordDetails.imagePaths.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${workRecordDetails.imagePaths.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(1f)
                ) {
                    AppIconActionButton(
                        onClick = {
                            val currentPath = workRecordDetails.imagePaths[pagerState.currentPage]
                            val newPaths = workRecordDetails.imagePaths.toMutableList()
                            newPaths.remove(currentPath)
                            onValueChange(workRecordDetails.copy(imagePaths = newPaths))
                            ImageUtils.deleteImageFromPrivateStorage(currentPath)
                            if (newPaths.isEmpty()) {
                                showGallery = false
                            }
                        },
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(
                            R.string.work_record_delete_image_content_description
                        ),
                        modifier = Modifier.background(Color.Transparent),
                        tint = Color.White,
                        containerColor = Color.Black.copy(alpha = 0.42f),
                        borderColor = Color.White.copy(alpha = 0.12f),
                        size = 40.dp
                    )

                    AppIconActionButton(
                        onClick = { showGallery = false },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.Transparent),
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = Color.White,
                        containerColor = Color.Black.copy(alpha = 0.42f),
                        borderColor = Color.White.copy(alpha = 0.12f),
                        size = 40.dp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoomableImage(
    imagePath: String
) {
    var scale by remember(imagePath) { mutableStateOf(IMAGE_PREVIEW_MIN_SCALE) }
    var offset by remember(imagePath) { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            val newScale =
                (scale * zoomChange).coerceIn(IMAGE_PREVIEW_MIN_SCALE, IMAGE_PREVIEW_MAX_SCALE)
            val newOffset = if (newScale > IMAGE_PREVIEW_MIN_SCALE) {
                offset + panChange
            } else {
                Offset.Zero
            }
            scale = newScale
            offset = clampPreviewOffset(
                offset = newOffset,
                containerWidthPx = containerWidthPx,
                containerHeightPx = containerHeightPx,
                scale = newScale
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(
                    state = state,
                    canPan = { scale > IMAGE_PREVIEW_MIN_SCALE }
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .allowHardware(false)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.work_record_zoomable_image_content_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .requiredSize(
                        width = containerWidth * scale,
                        height = containerHeight * scale
                    )
                    .offset {
                        IntOffset(
                            x = offset.x.roundToInt(),
                            y = offset.y.roundToInt()
                        )
                    },
                filterQuality = FilterQuality.Medium
            )
        }
    }
}

private fun clampPreviewOffset(
    offset: Offset,
    containerWidthPx: Float,
    containerHeightPx: Float,
    scale: Float
): Offset {
    if (scale <= IMAGE_PREVIEW_MIN_SCALE) {
        return Offset.Zero
    }

    val maxOffsetX = max(0f, (containerWidthPx * scale - containerWidthPx) / 2f)
    val maxOffsetY = max(0f, (containerHeightPx * scale - containerHeightPx) / 2f)

    return Offset(
        x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
        y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
    )
}
