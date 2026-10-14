package com.miojo.cerebelo.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.miojo.cerebelo.data.db.AppDatabase
import com.miojo.cerebelo.data.db.NoteEntity
import com.miojo.cerebelo.data.db.NoteTagCrossRef
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.data.db.TagWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Tag escolhida na tela. Pode já existir no banco ([id] preenchido)
 * ou ser nova (criada só quando a nota é salva).
 */
data class TagRef(val name: String, val type: TagType, val id: Long? = null) {
    companion object {
        fun of(tag: TagEntity) = TagRef(tag.name, tag.type, tag.id)
    }
}

class NoteRepository(private val db: AppDatabase) {
    private val noteDao = db.noteDao()
    private val tagDao = db.tagDao()

    fun observeNotes(query: String, tagId: Long?): Flow<List<NoteWithTags>> =
        noteDao.observeNotes(query, tagId)

    fun observeNotesForTag(tagId: Long): Flow<List<NoteWithTags>> =
        noteDao.observeNotesForTag(tagId)

    fun observeRecent(limit: Int): Flow<List<NoteWithTags>> = noteDao.observeRecent(limit)

    fun observeTags(): Flow<List<TagEntity>> = tagDao.observeByUsage()

    fun observeTagsWithCounts(): Flow<List<TagWithCount>> = tagDao.observeWithCounts()

    fun observeTag(id: Long): Flow<TagEntity?> = tagDao.observe(id)

    suspend fun getNote(id: Long): NoteWithTags? = noteDao.getNote(id)

    /** Cria (id nulo) ou atualiza uma nota com suas tags. */
    suspend fun saveNote(id: Long?, text: String, tags: List<TagRef>) = db.withTransaction {
        val now = System.currentTimeMillis()
        val noteId = if (id == null) {
            noteDao.insert(NoteEntity(text = text, createdAt = now, updatedAt = now))
        } else {
            val old = noteDao.getNote(id)?.note ?: return@withTransaction
            noteDao.update(old.copy(text = text, updatedAt = now))
            noteDao.clearTags(id)
            id
        }
        val tagIds = tags.map { resolveTagId(it) }.distinct()
        noteDao.insertRefs(tagIds.map { NoteTagCrossRef(noteId, it) })
    }

    suspend fun deleteNote(id: Long) = noteDao.delete(id)

    /** Retorna false se já existir outra tag com esse nome e tipo. */
    suspend fun renameTag(tag: TagEntity, newName: String): Boolean = try {
        tagDao.rename(tag.id, newName.trim())
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    suspend fun deleteTag(id: Long) = tagDao.delete(id)

    private suspend fun resolveTagId(ref: TagRef): Long {
        ref.id?.let { return it }
        val name = ref.name.trim()
        return tagDao.find(name, ref.type)?.id
            ?: tagDao.insert(TagEntity(name = name, type = ref.type))
    }
}
