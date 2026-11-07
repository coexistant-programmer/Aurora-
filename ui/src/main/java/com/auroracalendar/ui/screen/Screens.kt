package com.auroracalendar.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.navController
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.model.Task
import com.auroracalendar.ui.navigation.AppRoutes
import com.auroracalendar.ui.theme.*
import com.auroracalendar.ui.viewmodel.AgendaViewModel
import com.auroracalendar.ui.viewmodel.CalendarDay
import com.auroracalendar.ui.viewmodel.DayScheduleViewModel
import com.auroracalendar.ui.viewmodel.DayScheduleItem
import com.auroracalendar.ui.viewmodel.MonthViewModel
import com.auroracalendar.ui.viewmodel.WeekViewModel
import com.auroracalendar.ui.viewmodel.YearViewModel
import java.time.*
import java.time.format.DateTimeFormatter

// ======================================================================================
// Month calendar view
// ======================================================================================

@Composable
fun MonthCalendarScreen(
    monthViewModel: MonthViewModel = hiltViewModel()
) {
    val navController = navController()
    val selectedDate by monthViewModel.selectedDate.collectAsStateWithLifecycle()
    val monthDays by monthViewModel.monthDays.collectAsStateWithLifecycle()
    val categories by monthViewModel.categories.collectAsStateWithLifecycle()
    val selectedDayEvents by monthViewModel.selectedDayEvents.collectAsStateWithLifecycle()
    val selectedDayTasks by monthViewModel.selectedDayTasks.collectAsStateWithLifecycle()
    val today by monthViewModel.today.collectAsStateWithLifecycle()

    val monthLabel = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar: month navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = monthViewModel::previousMonth) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        monthViewModel.jumpToToday()
                        navController.navigate("${AppRoutes.Day}/${today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()}")
                    }
                ) {
                    Icon(imageVector = Icons.Default.Today, contentDescription = null)
                }
                IconButton(onClick = monthViewModel::nextMonth) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }

        // Weekday header row
        WeekdayHeader()

        // Month grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(monthDays) { day ->
                MonthDayCell(
                    day = day,
                    categories = categories,
                    onClick = {
                        monthViewModel.selectDate(day.date)
                        val ms = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        navController.navigate("${AppRoutes.Day}/${ms}")
                    },
                    isSelected = day.isSelected
                )
            }
        }

        // Selected day summary
        if (selectedDayEvents.isNotEmpty() || selectedDayTasks.isNotEmpty()) {
            SelectedDaySummary(
                date = selectedDate,
                events = selectedDayEvents,
                tasks = selectedDayTasks,
                onEventClick = { eventId ->
                    navController.navigate("${AppRoutes.EventDetail}/$eventId")
                },
                onTaskClick = { taskId ->
                    navController.navigate("${AppRoutes.TaskDetail}/$taskId")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WeekdayHeader() {
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(weekdays) { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    day: CalendarDay,
    categories: List<com.auroracalendar.domain.model.TaskCategory>,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val categoryColorById = categories.associateBy { it.id } { it.color }
    val allColors = (day.eventCategoryColors + day.taskCategoryColors).distinct()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clipToBounds()
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Day number
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    day.isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // Selected ring
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .clipToBounds()
            )
        }

        // Today highlight
        if (day.isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clipToBounds()
            )
        }

        // Event/task dots
        if (allColors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                allColors.take(3).forEach { color ->
                    val actualColor = categoryColorById[color] ?: color
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(actualColor))
                            .clipToBounds()
                            .clip(CircleShape)
                    )
                }
                if (allColors.size > 3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            .clipToBounds()
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Background for today / selected
        if (day.isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .clipToBounds()
                    .clip(CircleShape)
                    .padding(4.dp)
            )
        } else if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clipToBounds()
                    .clip(CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun SelectedDaySummary(
    date: LocalDate,
    events: List<CalendarEvent>,
    tasks: List<Task>,
    onEventClick: (Int) -> Unit,
    onTaskClick: (Int) -> Unit
) {
    val dateLabel = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        if (events.isNotEmpty()) {
            Text(
                text = "Events",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(events) { event ->
                    AgendaListItem(
                        title = event.title,
                        subtitle = event.description.takeIf { it.isNotEmpty() } ?: null,
                        timeLabel = if (event.allDay) "All day" else event.startTime.atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                        color = event.color,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }

        if (tasks.isNotEmpty()) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(tasks) { task ->
                    AgendaListItem(
                        title = task.title,
                        subtitle = task.notes.takeIf { it.isNotEmpty() } ?: null,
                        timeLabel = if (task.startTimeMs == null) "All day" else Instant.ofEpochMilli(task.startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                        color = task.color,
                        status = task.status,
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

// ======================================================================================
// Week calendar view
// ======================================================================================

@Composable
fun WeekCalendarScreen(
    weekViewModel: WeekViewModel = hiltViewModel()
) {
    val navController = navController()
    val weekStart by weekViewModel.weekStart.collectAsStateWithLifecycle()
    val weekDays by weekViewModel.weekDays.collectAsStateWithLifecycle()
    val selectedDate by weekViewModel.selectedDate.collectAsStateWithLifecycle()

    val weekLabel = "${weekStart.format(DateTimeFormatter.ofPattern("MMM d"))} – ${weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = weekViewModel::previousWeek) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = weekViewModel::jumpToToday) {
                    Icon(imageVector = Icons.Default.Today, contentDescription = null)
                }
                IconButton(onClick = weekViewModel::nextWeek) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(weekDays) { weekDay ->
                WeekDayColumn(
                    day = weekDay,
                    isSelected = weekDay.isSelected,
                    onClick = {
                        weekViewModel.selectDate(weekDay.date)
                        val ms = weekDay.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        navController.navigate("${AppRoutes.Day}/${ms}")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WeekDayColumn(
    day: com.auroracalendar.ui.viewmodel.WeekDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val hasItems = day.events.isNotEmpty() || day.tasks.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day name + number
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfWeek.displayName(TextStyle.SHORT, java.util.Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    day.isSelected -> MaterialTheme.colorScheme.primary
                    !day.date.isCurrentMonth() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.bold,
                color = when {
                    day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    day.isSelected -> MaterialTheme.colorScheme.primary
                    !day.date.isCurrentMonth() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // Today pill
        if (day.isToday) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clip(CircleShape)
                    .padding(top = 4.dp)
            )
        }

        // Event/task dots
        val allColors = (day.events.map { it.color } + day.tasks.map { it.color }).distinct()
        if (allColors.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                allColors.take(3).forEach { color ->
                    Box(
                        modifier = Modifier.size(6.dp).background(Color(color)).clip(CircleShape)
                    )
                }
                if (allColors.size > 3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Item list preview
        if (hasItems) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val preview = mutableListOf<String>()
                day.events.take(1).forEach { preview.add(it.title) }
                day.tasks.take(1).forEach { preview.add(it.title) }
                preview.take(2).forEach { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun LocalDate.isCurrentMonth(): Boolean {
    val now = LocalDate.now()
    return this.month == now.month && this.year == now.year
}

// ======================================================================================
// Day schedule view
// ======================================================================================

@Composable
fun DayScheduleScreen(
    dateMs: Long = System.currentTimeMillis(),
    dayScheduleViewModel: DayScheduleViewModel = hiltViewModel()
) {
    val navController = navController()
    val dateLabel by dayScheduleViewModel.dateLabel.collectAsStateWithLifecycle()
    val items by dayScheduleViewModel.items.collectAsStateWithLifecycle()

    dayScheduleViewModel.setDate(dateMs)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = dateLabel) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = { /* add event/task */ }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        )

        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Default.EventNote,
                title = "No items scheduled",
                body = "Everything is clear for this day."
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items) { item ->
                    DayScheduleItemRow(
                        item = item,
                        onClick = {
                            when (item.type) {
                                "event" -> navController.navigate("${AppRoutes.EventDetail}/${item.id}")
                                "task" -> navController.navigate("${AppRoutes.TaskDetail}/${item.id}")
                                else -> {}
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // FAB offset
    }

    FloatingActionButton(
        onClick = { /* create */ },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun DayScheduleItemRow(
    item: DayScheduleItem,
    onClick: () -> Unit
) {
    val timeLabel = if (item.allDay) "All day" else {
        item.startTimeMs?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))
        } ?: "—"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(item.color))
                .clip(CircleShape)
        )

        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.timeLabel != null || item.subtitle != null) {
                    Text(
                        text = listOfNotNull(item.subtitle, item.timeLabel).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val statusColor = when (item.status) {
                    "completed" -> MaterialTheme.colorScheme.tertiary
                    "running" -> MaterialTheme.colorScheme.primary
                    "overdue" -> MaterialTheme.colorScheme.error
                    else -> null
                }
                if (statusColor != null) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
}

// ======================================================================================
// Year overview
// ======================================================================================

@Composable
fun YearCalendarScreen(
    yearViewModel: YearViewModel = hiltViewModel()
) {
    val navController = navController()
    val yearMonths by yearViewModel.yearMonths.collectAsStateWithLifecycle()
    val currentYear by yearViewModel.currentYear.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = yearViewModel::previousYear) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = currentYear.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = yearViewModel::nextYear) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(yearMonths) { month ->
                YearMonthCard(
                    month = month,
                    onClick = {
                        // Navigate to month view for this month
                        val firstOfMonth = LocalDate.of(month.year, month.month.value, 1)
                        val ms = firstOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        navController.navigate("${AppRoutes.Day}/${ms}")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun YearMonthCard(
    month: YearViewModel.YearMonth,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = month.monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spaceBetween) {
                Text(
                    text = "${month.eventCount} events",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${month.taskCount} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ======================================================================================
// Agenda view
// ======================================================================================

@Composable
fun AgendaScreen(
    agendaViewModel: AgendaViewModel = hiltViewModel()
) {
    val navController = navController()
    val itemsByDate by agendaViewModel.itemsByDate.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Agenda") },
            actions = {
                IconButton(onClick = agendaViewModel::refresh) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                }
            }
        )

        if (itemsByDate.isEmpty()) {
            EmptyState(
                icon = Icons.Default.EventNote,
                title = "Nothing upcoming",
                body = "Your agenda is empty. Add a task or event to get started."
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(itemsByDate) { day ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(
                            text = day.dateLabel,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        day.items.forEach { item ->
                            AgendaItemRow(
                                item = item,
                                onClick = {
                                    when (item.type) {
                                        "event" -> navController.navigate("${AppRoutes.EventDetail}/${item.id}")
                                        "task" -> navController.navigate("${AppRoutes.TaskDetail}/${item.id}")
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    FloatingActionButton(
        onClick = { /* create */ },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun AgendaItemRow(
    item: AgendaViewModel.AgendaItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(item.color))
                .clip(CircleShape)
        )

        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.timeLabel != null) {
                    Text(
                        text = item.timeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.allDay) {
                    Text(
                        text = "All day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
}

// ======================================================================================
// Shared components
// ======================================================================================

@Composable
private fun AgendaListItem(
    title: String,
    subtitle: String?,
    timeLabel: String?,
    color: Int,
    status: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(color))
                .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (timeLabel != null) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
}

@Composable
private fun SectionHeader(
    text: String,
    color: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(color).copy(alpha = 0.15f))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, horizontal = 32.dp)
        )
    }
}

// ======================================================================================
// Extension helpers
// ======================================================================================

private fun TextStyle.bold: TextStyle = this.copy(fontWeight = FontWeight.Bold)
