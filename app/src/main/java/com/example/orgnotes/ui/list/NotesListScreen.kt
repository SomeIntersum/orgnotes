@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.orgnotes.ui.list

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.orgnotes.core.NotesViewModel
import com.example.orgnotes.core.StorageMode
import com.example.orgnotes.data.Note
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val DISPLAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH.mm")

@Composable
fun NotesListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    vm: NotesViewModel
) {
    val items by vm.items.collectAsState()
    var query by remember { mutableStateOf("") }

    // Общий диапазон
    var fromDt by remember { mutableStateOf<LocalDateTime?>(null) }
    var toDt by remember { mutableStateOf<LocalDateTime?>(null) }

    // BottomSheet с пресетами
    var showFilterSheet by remember { mutableStateOf(false) }

    // Состояние ручного выбора диапазона (одна кнопка → 4 шага)
    var showManualRange by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf<RangeStep>(RangeStep.FROM_DATE) }
    var tempFrom by remember { mutableStateOf(LocalDateTime.now()) }
    var tempTo by remember { mutableStateOf(LocalDateTime.now()) }

    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заметки") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(if (vm.storageMode == StorageMode.SQLITE) "SQLite" else "Файлы")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("SQLite") }, onClick = {
                                vm.switchRepo(StorageMode.SQLITE); expanded = false
                            })
                            DropdownMenuItem(text = { Text("Файлы") }, onClick = {
                                vm.switchRepo(StorageMode.FILES); expanded = false
                            })
                        }
                    }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Поиск (слова)") },
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                singleLine = true
            )

            Row(
                Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { showFilterSheet = true }) { Text("Фильтр даты") }
                Spacer(Modifier.width(12.dp))
                Text(rangeLabel(fromDt, toDt), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.padding(horizontal = 12.dp)) {
                Button(onClick = { vm.search(query, fromDt, toDt) }) { Text("Искать") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    query = ""
                    fromDt = null
                    toDt = null
                    vm.refresh()
                }) { Text("Сбросить") }
            }

            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет заметок")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(items) { n -> NoteRow(n, onOpen, onDelete = { vm.delete(n.id) }) }
                }
            }
        }
    }

    // Нижний лист с пресетами и запуском ручного диапазона
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Быстрые фильтры", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                FilterItem("Сегодня") {
                    val now = LocalDate.now()
                    fromDt = now.atTime(0, 0)
                    toDt = now.atTime(23, 59)
                    vm.search(query, fromDt, toDt)
                    showFilterSheet = false
                }

                FilterItem("Эта неделя") {
                    val today = LocalDate.now()
                    val start = today.with(java.time.DayOfWeek.MONDAY)
                    val end = start.plusDays(6)
                    fromDt = start.atTime(0, 0)
                    toDt = end.atTime(23, 59)
                    vm.search(query, fromDt, toDt)
                    showFilterSheet = false
                }

                FilterItem("Этот месяц") {
                    val today = LocalDate.now()
                    val start = today.with(TemporalAdjusters.firstDayOfMonth())
                    val end = today.with(TemporalAdjusters.lastDayOfMonth())
                    fromDt = start.atTime(0, 0)
                    toDt = end.atTime(23, 59)
                    vm.search(query, fromDt, toDt)
                    showFilterSheet = false
                }

                Divider(Modifier.padding(vertical = 12.dp))

                Text("Ручной диапазон", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    // стартуем последовательность: дата «С» → время «С» → дата «По» → время «По»
                    tempFrom = fromDt ?: LocalDateTime.now()
                    tempTo = toDt ?: tempFrom
                    step = RangeStep.FROM_DATE
                    showManualRange = true
                    showFilterSheet = false
                }) { Text("Указать диапазон…") }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = {
                    fromDt = null; toDt = null
                    vm.search(query, null, null)
                    showFilterSheet = false
                }) { Text("Очистить фильтр") }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // Хост пошагового ручного выбора (композабельно, без вызовов из onClick)
    ManualRangePickerHost(
        visible = showManualRange,
        step = step,
        tempFrom = tempFrom,
        tempTo = tempTo,
        onUpdateStep = { step = it },
        onUpdateTempFrom = { tempFrom = it },
        onUpdateTempTo = { tempTo = it },
        onClose = { canceled ->
            showManualRange = false
            if (!canceled) {
                fromDt = tempFrom
                toDt = tempTo
                vm.search(query, fromDt, toDt)
            }
        }
    )
}

/* ----------------- Вспомогательные композаблы/утилиты ----------------- */

@Composable
private fun FilterItem(text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    )
}

private fun rangeLabel(from: LocalDateTime?, to: LocalDateTime?): String =
    when {
        from == null && to == null -> "Фильтр не задан"
        from != null && to == null -> "С ${from.format(DISPLAY_FMT)}"
        from == null && to != null -> "До ${to.format(DISPLAY_FMT)}"
        else -> "${from!!.format(DISPLAY_FMT)} — ${to!!.format(DISPLAY_FMT)}"
    }

@Composable
private fun NoteRow(note: Note, onOpen: (String) -> Unit, onDelete: () -> Unit) {
    val dt = remember(note.dateTimeIso) {
        runCatching { LocalDateTime.parse(note.dateTimeIso).format(DISPLAY_FMT) }
            .getOrElse { note.dateTimeIso }
    }

    Card(
        Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .clickable { onOpen(note.id) }
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(dt, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text(note.content, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onDelete) { Text("Удалить") }
        }
    }
}

/* ----------------- Реализация ручного диапазона одной кнопкой ----------------- */

private enum class RangeStep { FROM_DATE, FROM_TIME, TO_DATE, TO_TIME }

@Composable
private fun ManualRangePickerHost(
    visible: Boolean,
    step: RangeStep,
    tempFrom: LocalDateTime,
    tempTo: LocalDateTime,
    onUpdateStep: (RangeStep) -> Unit,
    onUpdateTempFrom: (LocalDateTime) -> Unit,
    onUpdateTempTo: (LocalDateTime) -> Unit,
    onClose: (canceled: Boolean) -> Unit
) {
    if (!visible) return

    val ctx = LocalContext.current

    when (step) {
        RangeStep.FROM_DATE -> {
            val millis = tempFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val state = rememberDatePickerState(initialSelectedDateMillis = millis)
            DatePickerDialog(
                onDismissRequest = { onClose(true) },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val picked = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                            onUpdateTempFrom(
                                tempFrom.withYear(picked.year).withMonth(picked.monthValue).withDayOfMonth(picked.dayOfMonth)
                            )
                            onUpdateStep(RangeStep.FROM_TIME)
                        } ?: onClose(true)
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { onClose(true) }) { Text("Отмена") } }
            ) { DatePicker(state = state) }
        }

        RangeStep.FROM_TIME -> {
            // показать системный TimePicker один раз на входе в этот step
            LaunchedEffect(step) {
                TimePickerDialog(
                    ctx,
                    { _, h, m ->
                        onUpdateTempFrom(tempFrom.withHour(h).withMinute(m))
                        onUpdateStep(RangeStep.TO_DATE)
                    },
                    tempFrom.hour,
                    tempFrom.minute,
                    true
                ).show()
            }
        }

        RangeStep.TO_DATE -> {
            val millis = tempTo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val state = rememberDatePickerState(initialSelectedDateMillis = millis)
            DatePickerDialog(
                onDismissRequest = { onClose(true) },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val picked = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                            onUpdateTempTo(
                                tempTo.withYear(picked.year).withMonth(picked.monthValue).withDayOfMonth(picked.dayOfMonth)
                            )
                            onUpdateStep(RangeStep.TO_TIME)
                        } ?: onClose(true)
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { onClose(true) }) { Text("Отмена") } }
            ) { DatePicker(state = state) }
        }

        RangeStep.TO_TIME -> {
            LaunchedEffect(step) {
                TimePickerDialog(
                    ctx,
                    { _, h, m ->
                        onUpdateTempTo(tempTo.withHour(h).withMinute(m))
                        onClose(false) // завершили успешно
                    },
                    tempTo.hour,
                    tempTo.minute,
                    true
                ).show()
            }
        }
    }
}
