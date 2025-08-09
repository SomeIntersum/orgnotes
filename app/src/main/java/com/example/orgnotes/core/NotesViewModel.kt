package com.example.orgnotes.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.orgnotes.data.Note
import com.example.orgnotes.data.NotesRepository
import com.example.orgnotes.data.files.FilesNotesRepository
import com.example.orgnotes.data.room.NotesDatabase
import com.example.orgnotes.data.room.RoomNotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotesViewModel(app: Application) : AndroidViewModel(app) {

    private val db by lazy { NotesDatabase.create(app) }
    private val roomRepo by lazy { RoomNotesRepository(db) }
    private val filesRepo by lazy { FilesNotesRepository(app) }

    private var repo: NotesRepository = roomRepo
    private val _items = MutableStateFlow<List<Note>>(emptyList())
    val items: StateFlow<List<Note>> = _items
    private val isoFmt = DateTimeFormatter.ISO_DATE_TIME

    var storageMode: StorageMode = StorageMode.SQLITE
        private set

    init { switchRepo(StorageMode.SQLITE) }

    fun switchRepo(mode: StorageMode) {
        storageMode = mode
        repo = if (mode == StorageMode.SQLITE) roomRepo else filesRepo
        viewModelScope.launch {
            repo.init()
            refresh()
        }
    }

    fun refresh() = viewModelScope.launch {
        _items.value = repo.getAll()
    }

    fun save(id: String?, title: String, dateTime: LocalDateTime, content: String) =
        viewModelScope.launch {
            val note = Note(
                id = id ?: java.util.UUID.randomUUID().toString(),
                title = if (title.isBlank()) "Без названия" else title.trim(),
                dateTimeIso = dateTime.format(isoFmt),
                content = content
            )
            repo.upsert(note); refresh()
        }

    fun delete(id: String) = viewModelScope.launch { repo.delete(id); refresh() }

    fun search(q: String?, from: LocalDateTime?, to: LocalDateTime?) =
        viewModelScope.launch {
            _items.value = repo.search(
                q?.ifBlank { null },
                from?.format(isoFmt),
                to?.format(isoFmt)
            )
        }

    fun getByIdSync(id: String): Note? = _items.value.find { it.id == id }
}
