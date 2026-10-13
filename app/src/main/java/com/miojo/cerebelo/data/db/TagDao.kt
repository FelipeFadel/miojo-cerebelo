package com.miojo.cerebelo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    /** Todas as tags, as mais usadas primeiro (boas sugestões ao escrever). */
    @Query(
        """
        SELECT t.* FROM tags t
        LEFT JOIN note_tags nt ON nt.tagId = t.id
        GROUP BY t.id
        ORDER BY COUNT(nt.noteId) DESC, t.name COLLATE NOCASE
        """
    )
    fun observeByUsage(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun observe(id: Long): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE name = :name AND type = :type LIMIT 1")
    suspend fun find(name: String, type: TagType): TagEntity?

    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Query("UPDATE tags SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: Long)

    // --- Backup ---

    @Query("SELECT * FROM tags")
    suspend fun all(): List<TagEntity>

    @Insert
    suspend fun insertAll(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()
}
