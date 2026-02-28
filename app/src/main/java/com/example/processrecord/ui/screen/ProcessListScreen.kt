package com.example.processrecord.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
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
                title = { Text(stringResource(R.string.process_list_title)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.process_list_back_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToProcessEntry) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.process_list_add_content_description)
                )
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
                text = stringResource(R.string.process_list_empty),
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
                HorizontalDivider()
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
        supportingContent = {
            Text(
                stringResource(
                    R.string.process_price_per_unit,
                    formatProcessPriceValue(process.defaultPrice),
                    process.unit
                )
            )
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.process_list_edit_content_description),
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = modifier
    )
}
