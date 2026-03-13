package com.example.processrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.Process
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppFloatingButton
import com.example.processrecord.ui.component.AppSelectableRow
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.component.EmptyStateCard
import com.example.processrecord.ui.viewmodel.ProcessListViewModel

@Composable
fun ProcessListScreen(
    navigateBack: () -> Unit,
    navigateToProcessEntry: () -> Unit,
    navigateToProcessEdit: (Long) -> Unit,
    viewModel: ProcessListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val processListUiState by viewModel.processListUiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.process_list_title),
                subtitle = stringResource(R.string.process_list_subtitle),
                navigationIcon = {
                    ChromeIconButton(
                        onClick = navigateBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.process_list_back_content_description)
                    )
                }
            )
        },
        floatingActionButton = {
            AppFloatingButton(
                onClick = navigateToProcessEntry,
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.process_list_add_content_description)
            )
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
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                title = stringResource(R.string.process_list_title),
                subtitle = stringResource(R.string.process_list_empty),
                icon = Icons.AutoMirrored.Filled.List,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = processList, key = { it.id }) { process ->
                ProcessItem(
                    process = process,
                    onClick = { onProcessClick(process.id) }
                )
            }
        }
    }
}

@Composable
fun ProcessItem(
    process: Process,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppSelectableRow(
        title = process.name,
        subtitle = stringResource(
            R.string.process_price_per_unit,
            formatProcessPriceValue(process.defaultPrice),
            process.unit
        ),
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        emphasized = true,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = process.name.take(1),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.process_list_edit_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
