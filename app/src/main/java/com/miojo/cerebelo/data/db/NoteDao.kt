package com.miojo.cerebelo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /**
     * Linha do tempo: mais recentes primeiro.
     * [query] vazio = sem busca (procura no texto e no nome das tags).
     * [tagId] nulo = sem filtro de tag.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE (:query = ''
               OR text LIKE '%' || :query || '%'
               OR id IN (SELECT nt.noteId FROM note_tags nt
                         JOIN tags t ON t.id = nt.tagId
                         WHERE t.name LIKE '%' || :query || '%'))
          AND (:tagId IS NULL OR id IN (SELECT noteId FROM note_tags WHERE tagId = :tagId))
        ORDER BY createdAt DESC
        """
    )
    fun observeNotes(query: String, tagId: Long?): Flow<List<NoteWithTags>>

    /** Notas de uma tag, mais recentes primeiro. */
    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE id IN (SELECT noteId FROM note_tags WHERE tagId = :tagId)
        ORDER BY createdAt DESC
        """
    )
    fun observeNotesForTag(tagId: Long): Flow<List<NoteWithTags>>

    /** Últimas notas (tela inicial). */
    @Transaction
    @Query("SELECT * FROM notes ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteWithTags?

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRefs(refs: List<NoteTagCrossRef>)

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun clearTags(noteId: Long)

    // --- Backup ---

    @Query("SELECT * FROM notes")
    suspend fun allNotes(): List<NoteEntity>

    @Query("SELECT * FROM note_tags")
    suspend fun allRefs(): List<NoteTagCrossRef>

    @Insert
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
