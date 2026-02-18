package com.example.processrecord.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.data.entity.Process
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.ProcessListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessListScreen(
    navigateBack: () -> Unit,
    navigateToProcessEntry: () -> Unit,
    navigateToProcessEdit: (Long) -> Unit,
    viewModel: ProcessListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val processListUiState by viewModel.processListUiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("工序管理") },
                navigationIcon = {
                     IconButton(onClick = navigateBack) {
                         Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                     }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToProcessEntry) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "添加工序")
            }
        }
    ) { innerPadding ->
        ProcessListBody(
            processList = processListUiState.processList,
            onProcessClick = navigateToProcessEdit,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ProcessListBody(
    processList: List<Process>,
    onProcessClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (processList.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "暂无工序，请点击右下角添加",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items = processList, key = { it.id }) { process ->
                ProcessItem(
                    process = process,
                    modifier = Modifier.clickable { onProcessClick(process.id) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ProcessItem(
    process: Process,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(process.name) },
        supportingContent = { Text("${process.defaultPrice}/${process.unit}") },
        trailingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.outline) }, // Visual cue only
        modifier = modifier
    )
}
