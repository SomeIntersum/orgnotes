package com.example.orgnotes.data.files

import android.content.Context
import com.example.orgnotes.data.Note
import com.example.orgnotes.data.NotesRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class FilesNotesRepository(private val context: Context) : NotesRepository {
    private lateinit var dir: File
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    override suspend fun init() {
        dir = File(context.filesDir, "notes")
        if (!dir.exists()) dir.mkdirs()
    }

    private fun fileOf(id: String) = File(dir, "$id.json")

    override suspend fun getAll(): List<Note> =
        dir.listFiles { f -> f.extension == "json" }?.mapNotNull { f ->
            runCatching { json.decodeFromString<Note>(f.readText()) }.getOrNull()
        }?.sortedByDescending { it.dateTimeIso } ?: emptyList()

    override suspend fun getById(id: String): Note? =
        fileOf(id).takeIf { it.exists() }?.let { f ->
            runCatching { json.decodeFromString<Note>(f.readText()) }.getOrNull()
        }

    override suspend fun upsert(note: Note) {
        fileOf(note.id).writeText(json.encodeToString(note))
    }

    override suspend fun delete(id: String) {
        fileOf(id).takeIf { it.exists() }?.delete()
    }

    override suspend fun search(query: String?, fromIso: String?, toIso: String?): List<Note> {
        val all = getAll()
        return all.filter { n ->
            val qOk = query.isNullOrBlank() ||
                    n.title.contains(query!!, true) || n.content.contains(query, true)
            val fromOk = fromIso == null || n.dateTimeIso >= fromIso
            val toOk = toIso == null || n.dateTimeIso <= toIso
            qOk && fromOk && toOk
        }
    }
}
