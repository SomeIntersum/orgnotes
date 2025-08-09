package com.example.orgnotes.data.room

import com.example.orgnotes.data.Note
import com.example.orgnotes.data.NotesRepository

class RoomNotesRepository(private val db: NotesDatabase) : NotesRepository {
    private val dao get() = db.notesDao()
    override suspend fun init() { /* no-op */ }
    override suspend fun getAll() = dao.getAll().map { it.toDomain() }
    override suspend fun getById(id: String) = dao.getById(id)?.toDomain()
    override suspend fun upsert(note: Note) = dao.upsert(NoteEntity.from(note))
    override suspend fun delete(id: String) = dao.delete(id)
    override suspend fun search(query: String?, fromIso: String?, toIso: String?) =
        dao.search(query, fromIso, toIso).map { it.toDomain() }
}
