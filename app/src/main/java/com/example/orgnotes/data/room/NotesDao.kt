package com.example.orgnotes.data.room

import androidx.room.*

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY dateTimeIso DESC")
    suspend fun getAll(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id=:id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity)

    @Query("DELETE FROM notes WHERE id=:id")
    suspend fun delete(id: String)

    @Query("""
        SELECT * FROM notes
        WHERE (:q IS NULL OR title LIKE '%'||:q||'%' OR content LIKE '%'||:q||'%')
          AND (:fromIso IS NULL OR dateTimeIso >= :fromIso)
          AND (:toIso   IS NULL OR dateTimeIso <= :toIso)
        ORDER BY dateTimeIso DESC
    """)
    suspend fun search(q: String?, fromIso: String?, toIso: String?): List<NoteEntity>
}
