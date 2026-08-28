package com.masamunr.trailcharter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.masamunr.trailcharter.data.adventure.AdventureEntity
import com.masamunr.trailcharter.data.adventure.AdventureRepository
import com.masamunr.trailcharter.data.adventure.AdventureSummaryRow
import com.masamunr.trailcharter.data.adventure.ItineraryItemEntity
import com.masamunr.trailcharter.data.adventure.StageEntity
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ukDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK)

@Composable
fun TrailCharterApp(
    repository: AdventureRepository,
) {
    var selectedAdventureId by rememberSaveable { mutableStateOf<Long?>(null) }
    var creatingAdventure by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = creatingAdventure || selectedAdventureId != null) {
        if (creatingAdventure) {
            creatingAdventure = false
        } else {
            selectedAdventureId = null
        }
    }

    when {
        creatingAdventure -> NewAdventureScreen(
            repository = repository,
            onBack = { creatingAdventure = false },
            onCreated = { adventureId ->
                creatingAdventure = false
                selectedAdventureId = adventureId
            },
        )

        selectedAdventureId != null -> AdventureEditorScreen(
            repository = repository,
            adventureId = requireNotNull(selectedAdventureId),
            onBack = { selectedAdventureId = null },
            onSaved = { selectedAdventureId = null },
            onSavedAndNew = {
                selectedAdventureId = null
                creatingAdventure = true
            },
            onDeleted = { selectedAdventureId = null },
        )

        else -> AdventuresHomeScreen(
            repository = repository,
            onNewAdventure = { creatingAdventure = true },
            onOpenAdventure = { selectedAdventureId = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventuresHomeScreen(
    repository: AdventureRepository,
    onNewAdventure: () -> Unit,
    onOpenAdventure: (Long) -> Unit,
) {
    val adventures by repository.observeAdventureSummaries().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TrailCharter", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Your adventures",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onNewAdventure,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("New adventure")
                }
            }

            if (adventures.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "No adventures yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "A title is all you need to start. Everything else can be added later.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(adventures, key = { it.id }) { adventure ->
                    AdventureCard(
                        adventure = adventure,
                        onClick = { onOpenAdventure(adventure.id) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun AdventureCard(
    adventure: AdventureSummaryRow,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = adventure.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            formatDateRange(adventure.startDateEpochDay, adventure.endDateEpochDay)?.let { dateRange ->
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (adventure.summary.isNotBlank()) {
                Text(
                    text = adventure.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (adventure.itineraryCount > 0) {
                val progress = adventure.completedCount.toFloat() / adventure.itineraryCount.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${adventure.completedCount} of ${adventure.itineraryCount} milestones complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "No milestones yet",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewAdventureScreen(
    repository: AdventureRepository,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var endDate by rememberSaveable { mutableStateOf<Long?>(null) }

    val dateOrderInvalid = startDate != null && endDate != null && requireNotNull(endDate) < requireNotNull(startDate)
    val canCreate = title.isNotBlank() && !dateOrderInvalid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New adventure") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }
            item {
                Text(
                    text = "Start simple",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                Text(
                    text = "Only the title is required. Dates and detail are optional.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary (optional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                DateInput(
                    valueEpochDay = startDate,
                    onValueChange = { startDate = it },
                    label = "Start date (optional)",
                )
            }
            item {
                DateInput(
                    valueEpochDay = endDate,
                    onValueChange = { endDate = it },
                    label = "End date (optional)",
                    supportingText = if (dateOrderInvalid) "End date cannot be before the start date" else null,
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            val adventureId = repository.createAdventure(
                                title = title,
                                summary = summary,
                                startDateEpochDay = startDate,
                                endDateEpochDay = endDate,
                            )
                            onCreated(adventureId)
                        }
                    },
                    enabled = canCreate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create adventure")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventureEditorScreen(
    repository: AdventureRepository,
    adventureId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSavedAndNew: () -> Unit,
    onDeleted: () -> Unit,
) {
    val adventure by repository.observeAdventure(adventureId).collectAsState(initial = null)
    val stages by repository.observeStages(adventureId).collectAsState(initial = emptyList())
    val itineraryItems by repository.observeItineraryItems(adventureId).collectAsState(initial = emptyList())

    val currentAdventure = adventure
    if (currentAdventure == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AdventureEditorContent(
        repository = repository,
        adventure = currentAdventure,
        stages = stages,
        itineraryItems = itineraryItems,
        onBack = onBack,
        onSaved = onSaved,
        onSavedAndNew = onSavedAndNew,
        onDeleted = onDeleted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventureEditorContent(
    repository: AdventureRepository,
    adventure: AdventureEntity,
    stages: List<StageEntity>,
    itineraryItems: List<ItineraryItemEntity>,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSavedAndNew: () -> Unit,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by rememberSaveable(adventure.id) { mutableStateOf(adventure.title) }
    var summary by rememberSaveable(adventure.id) { mutableStateOf(adventure.summary) }
    var startDate by rememberSaveable(adventure.id) { mutableStateOf(adventure.startDateEpochDay) }
    var endDate by rememberSaveable(adventure.id) { mutableStateOf(adventure.endDateEpochDay) }
    var newStageTitle by rememberSaveable(adventure.id) { mutableStateOf("") }
    var newItemTitle by rememberSaveable(adventure.id) { mutableStateOf("") }
    var selectedStageId by rememberSaveable(adventure.id) { mutableStateOf<Long?>(null) }
    var stageMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable(adventure.id) { mutableStateOf(false) }

    val dateOrderInvalid = startDate != null && endDate != null && requireNotNull(endDate) < requireNotNull(startDate)
    val canSave = title.isNotBlank() && !dateOrderInvalid
    val stageNames = remember(stages) { stages.associate { it.id to it.title } }
    val completeCount = itineraryItems.count { it.isComplete }

    LaunchedEffect(stages, selectedStageId) {
        if (selectedStageId != null && stages.none { it.id == selectedStageId }) {
            selectedStageId = null
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete adventure?") },
            text = { Text("This removes the adventure, its stages and its milestones from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            repository.deleteAdventure(adventure.id)
                            onDeleted()
                        }
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = adventure.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }
            item {
                Text(
                    text = "Adventure details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                DateInput(
                    valueEpochDay = startDate,
                    onValueChange = { startDate = it },
                    label = "Start date",
                )
            }
            item {
                DateInput(
                    valueEpochDay = endDate,
                    onValueChange = { endDate = it },
                    label = "End date",
                    supportingText = if (dateOrderInvalid) "End date cannot be before the start date" else null,
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateAdventure(
                                adventure = adventure,
                                title = title,
                                summary = summary,
                                startDateEpochDay = startDate,
                                endDateEpochDay = endDate,
                            )
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save details")
                }
            }

            item { SectionDivider(title = "Stages") }
            item {
                Text(
                    text = "Stages are optional. Use them for days, legs or other useful sections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items(stages, key = { "stage-${it.id}" }) { stage ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stage.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        TextButton(onClick = { scope.launch { repository.deleteStage(stage) } }) {
                            Text("Remove")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newStageTitle,
                        onValueChange = { newStageTitle = it },
                        label = { Text("New stage") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val stageTitle = newStageTitle
                            newStageTitle = ""
                            scope.launch { repository.addStage(adventure.id, stageTitle) }
                        },
                        enabled = newStageTitle.isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }
            }

            item { SectionDivider(title = "Progress") }
            item {
                if (itineraryItems.isEmpty()) {
                    Text(
                        text = "Add milestones or itinerary items, then tick them off as you achieve them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val progress = completeCount.toFloat() / itineraryItems.size.toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "$completeCount of ${itineraryItems.size} complete",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(itineraryItems, key = { "item-${it.id}" }) { item ->
                ItineraryItemRow(
                    item = item,
                    stageName = item.stageId?.let(stageNames::get),
                    onCheckedChange = { checked ->
                        scope.launch { repository.setItineraryItemComplete(item, checked) }
                    },
                    onRemove = { scope.launch { repository.deleteItineraryItem(item) } },
                )
            }

            item {
                OutlinedTextField(
                    value = newItemTitle,
                    onValueChange = { newItemTitle = it },
                    label = { Text("New milestone or itinerary item") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { stageMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = selectedStageId?.let(stageNames::get) ?: "Adventure-wide",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        DropdownMenu(
                            expanded = stageMenuExpanded,
                            onDismissRequest = { stageMenuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Adventure-wide") },
                                onClick = {
                                    selectedStageId = null
                                    stageMenuExpanded = false
                                },
                            )
                            stages.forEach { stage ->
                                DropdownMenuItem(
                                    text = { Text(stage.title) },
                                    onClick = {
                                        selectedStageId = stage.id
                                        stageMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val itemTitle = newItemTitle
                            val stageId = selectedStageId
                            newItemTitle = ""
                            scope.launch {
                                repository.addItineraryItem(
                                    adventureId = adventure.id,
                                    title = itemTitle,
                                    stageId = stageId,
                                )
                            }
                        },
                        enabled = newItemTitle.isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }
            }
            item {
                Text(
                    text = "Completion is manual in this first pass. Automatic location-based completion remains optional future behaviour.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionDivider(title = "Adventure actions") }
            item {
                Text(
                    text = "Stages and milestones are stored as you add them. Save adventure stores the current details and finishes this planning session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateAdventure(
                                adventure = adventure,
                                title = title,
                                summary = summary,
                                startDateEpochDay = startDate,
                                endDateEpochDay = endDate,
                            )
                            onSaved()
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save adventure")
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            repository.updateAdventure(
                                adventure = adventure,
                                title = title,
                                summary = summary,
                                startDateEpochDay = startDate,
                                endDateEpochDay = endDate,
                            )
                            onSavedAndNew()
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save & new adventure")
                }
            }
            item {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete adventure")
                }
            }
        }
    }
}

@Composable
private fun ItineraryItemRow(
    item: ItineraryItemEntity,
    stageName: String?,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = item.isComplete,
                    onCheckedChange = onCheckedChange,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.isComplete) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    stageName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = onRemove) { Text("Remove") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateInput(
    valueEpochDay: Long?,
    onValueChange: (Long?) -> Unit,
    label: String,
    supportingText: String? = null,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "$label: ${formatEpochDay(valueEpochDay).ifBlank { "Choose date" }}",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(if (valueEpochDay == null) "Choose" else "Change")
        }

        if (valueEpochDay != null) {
            TextButton(onClick = { onValueChange(null) }) {
                Text("Clear date")
            }
        }

        supportingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = valueEpochDay?.let(::epochDayToUtcMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(pickerState.selectedDateMillis?.let(::utcMillisToEpochDay))
                        showDatePicker = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SectionDivider(title: String) {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun formatEpochDay(epochDay: Long?): String =
    epochDay?.let { LocalDate.ofEpochDay(it).format(ukDateFormatter) }.orEmpty()

internal fun formatDateRange(startEpochDay: Long?, endEpochDay: Long?): String? {
    val start = formatEpochDay(startEpochDay).ifBlank { null }
    val end = formatEpochDay(endEpochDay).ifBlank { null }
    return when {
        start != null && end != null -> "$start to $end"
        start != null -> "Starts $start"
        end != null -> "Ends $end"
        else -> null
    }
}

internal fun epochDayToUtcMillis(epochDay: Long): Long =
    LocalDate.ofEpochDay(epochDay)
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

internal fun utcMillisToEpochDay(epochMillis: Long): Long =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
