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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
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
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.masamunr.trailcharter.data.adventure.AdventurePlanningSnapshot
import com.masamunr.trailcharter.data.adventure.AdventureRepository
import com.masamunr.trailcharter.data.adventure.AdventureSummaryRow
import com.masamunr.trailcharter.data.adventure.PlanningStageDraft
import com.masamunr.trailcharter.data.adventure.StageEntity
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ukDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK)

internal data class DraftStage(
    val key: String,
    val persistedId: Long?,
    val title: String,
    val isComplete: Boolean,
    val completedAtEpochMillis: Long?,
)

@Composable
fun TrailCharterApp(
    repository: AdventureRepository,
) {
    var selectedAdventureId by rememberSaveable { mutableStateOf<Long?>(null) }
    var creatingAdventure by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = creatingAdventure) {
        creatingAdventure = false
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

            if (adventure.stageCount > 0) {
                val progress = adventure.completedStageCount.toFloat() / adventure.stageCount.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${adventure.completedStageCount} of ${adventure.stageCount} stages complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "No stages yet",
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

    val canCreate = title.isNotBlank()

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
                DateRangeInput(
                    startEpochDay = startDate,
                    endEpochDay = endDate,
                    onRangeChange = { newStart, newEnd ->
                        startDate = newStart
                        endDate = newEnd
                    },
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

@Composable
private fun AdventureEditorScreen(
    repository: AdventureRepository,
    adventureId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSavedAndNew: () -> Unit,
    onDeleted: () -> Unit,
) {
    val planningFlow = remember(repository, adventureId) {
        repository.observePlanningSession(adventureId)
    }
    val snapshot by planningFlow.collectAsState(initial = null)
    val currentSnapshot = snapshot

    if (currentSnapshot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AdventureEditorContent(
        repository = repository,
        snapshot = currentSnapshot,
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
    snapshot: AdventurePlanningSnapshot,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSavedAndNew: () -> Unit,
    onDeleted: () -> Unit,
) {
    val adventure = snapshot.adventure
    val persistedStages = snapshot.stages
    val scope = rememberCoroutineScope()

    var title by rememberSaveable(adventure.id) { mutableStateOf(adventure.title) }
    var summary by rememberSaveable(adventure.id) { mutableStateOf(adventure.summary) }
    var startDate by rememberSaveable(adventure.id) { mutableStateOf(adventure.startDateEpochDay) }
    var endDate by rememberSaveable(adventure.id) { mutableStateOf(adventure.endDateEpochDay) }
    var newStageTitle by rememberSaveable(adventure.id) { mutableStateOf("") }
    var nextDraftStageNumber by rememberSaveable(adventure.id) { mutableStateOf(1) }
    var showDeleteDialog by rememberSaveable(adventure.id) { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable(adventure.id) { mutableStateOf(false) }

    val draftStages = remember(adventure.id) {
        mutableStateListOf<DraftStage>().apply {
            addAll(
                persistedStages.map { stage ->
                    DraftStage(
                        key = "stage-${stage.id}",
                        persistedId = stage.id,
                        title = stage.title,
                        isComplete = stage.isComplete,
                        completedAtEpochMillis = stage.completedAtEpochMillis,
                    )
                },
            )
        }
    }

    val canSave = title.isNotBlank()
    val completedStages = draftStages.count { it.isComplete }
    val hasChanges = planningSessionHasChanges(
        adventure = adventure,
        persistedStages = persistedStages,
        title = title,
        summary = summary,
        startDateEpochDay = startDate,
        endDateEpochDay = endDate,
        draftStages = draftStages,
    )

    fun saveAndThen(afterSave: () -> Unit) {
        scope.launch {
            repository.savePlanningSession(
                snapshot = snapshot,
                title = title,
                summary = summary,
                startDateEpochDay = startDate,
                endDateEpochDay = endDate,
                stages = draftStages.map { stage ->
                    PlanningStageDraft(
                        persistedId = stage.persistedId,
                        title = stage.title,
                        isComplete = stage.isComplete,
                        completedAtEpochMillis = stage.completedAtEpochMillis,
                    )
                },
            )
            afterSave()
        }
    }

    fun requestExit() {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler { requestExit() }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Save changes?") },
            text = { Text("You have unsaved changes in this planning session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        saveAndThen(onBack)
                    },
                    enabled = canSave,
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            onBack()
                        },
                    ) {
                        Text("Don't save")
                    }
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete adventure?") },
            text = { Text("This removes the adventure and its planning data from this device.") },
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
                    TextButton(onClick = ::requestExit) { Text("Back") }
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
                DateRangeInput(
                    startEpochDay = startDate,
                    endEpochDay = endDate,
                    onRangeChange = { newStart, newEnd ->
                        startDate = newStart
                        endDate = newEnd
                    },
                )
            }

            item { SectionDivider(title = "Stages") }
            item {
                Text(
                    text = "Plan the Adventure as stages, then tick each stage when you complete it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                if (draftStages.isEmpty()) {
                    Text(
                        text = "Stages are optional. Use them for days, legs or other useful sections.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val progress = completedStages.toFloat() / draftStages.size.toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "$completedStages of ${draftStages.size} stages complete",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(draftStages, key = { it.key }) { stage ->
                StageRow(
                    stage = stage,
                    onCheckedChange = { checked ->
                        val index = draftStages.indexOfFirst { it.key == stage.key }
                        if (index >= 0) {
                            draftStages[index] = stage.copy(
                                isComplete = checked,
                                completedAtEpochMillis = if (checked) System.currentTimeMillis() else null,
                            )
                        }
                    },
                    onRemove = {
                        draftStages.removeAll { it.key == stage.key }
                    },
                )
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
                            val stageTitle = newStageTitle.trim()
                            if (stageTitle.isNotEmpty()) {
                                draftStages.add(
                                    DraftStage(
                                        key = "draft-${nextDraftStageNumber++}",
                                        persistedId = null,
                                        title = stageTitle,
                                        isComplete = false,
                                        completedAtEpochMillis = null,
                                    ),
                                )
                                newStageTitle = ""
                            }
                        },
                        enabled = newStageTitle.isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }
            }
            item {
                Text(
                    text = "Changes stay in this planning session until you save. Stage completion is manual in this pass.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionDivider(title = "Adventure actions") }
            item {
                Button(
                    onClick = { saveAndThen(onSaved) },
                    enabled = canSave && hasChanges,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save adventure")
                }
            }
            item {
                OutlinedButton(
                    onClick = { saveAndThen(onSavedAndNew) },
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
private fun StageRow(
    stage: DraftStage,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = stage.isComplete,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = stage.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (stage.isComplete) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            TextButton(onClick = onRemove) { Text("Remove") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeInput(
    startEpochDay: Long?,
    endEpochDay: Long?,
    onRangeChange: (Long?, Long?) -> Unit,
) {
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactDateButton(
            label = "Start",
            valueEpochDay = startEpochDay,
            onClick = { showDateRangePicker = true },
            modifier = Modifier.weight(1f),
        )
        CompactDateButton(
            label = "End",
            valueEpochDay = endEpochDay,
            onClick = { showDateRangePicker = true },
            modifier = Modifier.weight(1f),
        )
    }

    if (showDateRangePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startEpochDay?.let(::epochDayToUtcMillis),
            initialSelectedEndDateMillis = endEpochDay?.let(::epochDayToUtcMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRangeChange(
                            pickerState.selectedStartDateMillis?.let(::utcMillisToEpochDay),
                            pickerState.selectedEndDateMillis?.let(::utcMillisToEpochDay),
                        )
                        showDateRangePicker = false
                    },
                    enabled = pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Row {
                    if (startEpochDay != null || endEpochDay != null) {
                        TextButton(
                            onClick = {
                                onRangeChange(null, null)
                                showDateRangePicker = false
                            },
                        ) {
                            Text("Clear")
                        }
                    }
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text("Cancel")
                    }
                }
            },
        ) {
            DateRangePicker(
                state = pickerState,
                showModeToggle = false,
            )
        }
    }
}

@Composable
private fun CompactDateButton(
    label: String,
    valueEpochDay: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatEpochDay(valueEpochDay).ifBlank { "Choose date" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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

internal fun planningSessionHasChanges(
    adventure: AdventureEntity,
    persistedStages: List<StageEntity>,
    title: String,
    summary: String,
    startDateEpochDay: Long?,
    endDateEpochDay: Long?,
    draftStages: List<DraftStage>,
): Boolean {
    if (title != adventure.title) return true
    if (summary != adventure.summary) return true
    if (startDateEpochDay != adventure.startDateEpochDay) return true
    if (endDateEpochDay != adventure.endDateEpochDay) return true
    if (persistedStages.size != draftStages.size) return true

    return persistedStages.zip(draftStages).any { (persisted, draft) ->
        draft.persistedId != persisted.id ||
            draft.title != persisted.title ||
            draft.isComplete != persisted.isComplete ||
            draft.completedAtEpochMillis != persisted.completedAtEpochMillis
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
