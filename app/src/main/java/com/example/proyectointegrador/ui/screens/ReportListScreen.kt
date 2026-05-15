package com.example.proyectointegrador.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyectointegrador.data.model.Report
import com.example.proyectointegrador.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: ReportViewModel,
    onAddReportClick: () -> Unit,
    onReportClick: (Report) -> Unit
) {
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchReports()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reportes Geolocalizados") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReportClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Reporte")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(text = "Error: $error", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(reports) { report ->
                        ReportItem(report = report, onClick = { onReportClick(report) })
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = report.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp).padding(end = 16.dp)
            )
            Column {
                Text(text = report.title, style = MaterialTheme.typography.titleMedium)
                Text(text = report.description, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Lat: ${report.latitude}, Lon: ${report.longitude}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
