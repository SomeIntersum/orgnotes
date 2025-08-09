package com.example.orgnotes.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.orgnotes.data.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dateTimeIso: String,
    val content: String
) {
    fun toDomain() = Note(id, title, dateTimeIso, content)
    companion object {
        fun from(n: Note) = NoteEntity(n.id, n.title, n.dateTimeIso, n.content)
    }
}
