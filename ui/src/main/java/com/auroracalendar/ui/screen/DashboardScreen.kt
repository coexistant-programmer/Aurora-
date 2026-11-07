package com.auroracalendar.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.auroracalendar.ui.navigation.AppRoutes
import androidx.navigation.compose.navController
import com.auroracalendar.ui.viewmodel.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val navController = navController()
    val tasks by dashboardViewModel.todayTasks.collectAsStateWithLifecycle()
    val runningTask by dashboardViewModel.runningTask.collectAsStateWithLifecycle()
    val progress by dashboardViewModel.progress.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Good morning",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = java.time.LocalDate.now().toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Daily progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Today's progress", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${tasks.count { it.status == "completed" }} of ${tasks.size} tasks completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Current running task
        runningTask?.let { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Running now", style = MaterialTheme.typography.labelLarge)
                    Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* timer action */ }) { Text("Pause") }
                        Button(onClick = { /* timer action */ }) { Text("Complete") }
                    }
                }
            }
        }

        // Upcoming tasks
        Text(text = "Upcoming today", style = MaterialTheme.typography.titleMedium)
        if (tasks.isEmpty()) {
            Text(text = "No tasks planned for today. Add one to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(tasks) { task ->
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("${AppRoutes.TaskDetail}/${task.id}") },
                        headlineContent = { Text(task.title) },
                        supportingContent = { Text(task.description.take(60), style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            val color = when (task.status) {
                                "completed" -> MaterialTheme.colorScheme.tertiary
                                "running" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick add
        FloatingActionButton(
            onClick = { navController.navigate(AppRoutes.TaskEditor) },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}
