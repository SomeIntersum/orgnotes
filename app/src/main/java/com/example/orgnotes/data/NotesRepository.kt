package com.example.orgnotes.data

interface NotesRepository {
    suspend fun init()
    suspend fun getAll(): List<Note>
    suspend fun getById(id: String): Note?
    suspend fun upsert(note: Note)
    suspend fun delete(id: String)
    suspend fun search(query: String?, fromIso: String?, toIso: String?): List<Note>
}
