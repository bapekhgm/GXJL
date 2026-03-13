package com.example.processrecord.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppActionChip
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.utils.ImageUtils
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import kotlinx.coroutines.launch
import java.io.File

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
        Dialog(onDismissRequest = { showGallery = false }) {
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
                    modifier = Modifier.fillMaxSize()
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

@Composable
private fun ZoomableImage(
    imagePath: String
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange: Float, panChange: Offset, _: Float ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        if (scale > 1f) {
            offset += panChange * scale
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
            contentDescription = stringResource(R.string.work_record_zoomable_image_content_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
