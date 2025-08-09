package com.example.orgnotes.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        fun create(context: Context) =
            Room.databaseBuilder(context, NotesDatabase::class.java, "org_notes.db").build()
    }
}
