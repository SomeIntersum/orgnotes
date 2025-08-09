@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.orgnotes.ui.edit

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.orgnotes.core.NotesViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DISPLAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH.mm")

@Composable
fun NoteEditorScreen(
    noteId: String?,
    onDone: () -> Unit,
    vm: NotesViewModel
) {
    val items by vm.items.collectAsState()
    val editing = remember(items, noteId) { items.find { it.id == noteId } }

    var title by remember(editing) { mutableStateOf(editing?.title ?: "") }
    var content by remember(editing) { mutableStateOf(editing?.content ?: "") }
    var dt by remember(editing) {
        mutableStateOf(
            editing?.dateTimeIso?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text(if (noteId == null) "Новая заметка" else "Редактирование") }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Содержимое") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            minLines = 6
        )

        Spacer(Modifier.height(12.dp))

        Text("Дата/время: ${dt.format(DISPLAY_FMT)}", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))

        Button(onClick = { showDatePicker = true }) {
            Text("Выбрать дату и время")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            vm.save(noteId, title, dt, content)
            onDone()
        }) {
            Text("Сохранить")
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(millis),
                            ZoneId.systemDefault()
                        )
                        // сначала применим выбранную дату
                        dt = dt.withYear(picked.year)
                            .withMonth(picked.monthValue)
                            .withDayOfMonth(picked.dayOfMonth)
                        // затем сразу покажем системный TimePicker
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                dt = dt.withHour(hour).withMinute(minute)
                            },
                            dt.hour,
                            dt.minute,
                            true
                        ).show()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
