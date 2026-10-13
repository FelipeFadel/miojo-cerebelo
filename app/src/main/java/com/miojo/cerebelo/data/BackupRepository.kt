package com.miojo.cerebelo.data

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.miojo.cerebelo.data.db.AppDatabase
import com.miojo.cerebelo.data.db.HabitCheckEntity
import com.miojo.cerebelo.data.db.HabitEntity
import com.miojo.cerebelo.data.db.NoteEntity
import com.miojo.cerebelo.data.db.NoteTagCrossRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(val notes: Int, val tags: Int, val habits: Int, val checks: Int)

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exporta/importa tudo num arquivo JSON. Formato (versão 1):
 * { "app": "cerebelo", "version": 1, "exportedAt": ms,
 *   "tags": [{id, name, type}], "notes": [{id, text, createdAt, updatedAt, tagIds: []}],
 *   "habits": [{id, name, position, createdAt}], "checks": [{habitId, day}] }
 */
class BackupRepository(
    private val db: AppDatabase,
    private val contentResolver: ContentResolver,
    private val onChanged: suspend () -> Unit = {},
) {
    suspend fun exportTo(uri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val (json, summary) = db.withTransaction { buildJson() }
        val stream = contentResolver.openOutputStream(uri, "wt")
            ?: throw BackupException("Não foi possível criar o arquivo.")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(json.toString(2)) }
        summary
    }

    /** Substitui TODOS os dados atuais pelos do arquivo. */
    suspend fun importFrom(uri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val text = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: throw BackupException("Não foi possível ler o arquivo.")
        // Lê e valida tudo antes de apagar qualquer coisa.
        val data = try {
            parse(JSONObject(text))
        } catch (e: BackupException) {
            throw e
        } catch (e: Exception) {
            throw BackupException("Arquivo inválido ou corrompido.", e)
        }
        db.withTransaction {
            val noteDao = db.noteDao()
            val tagDao = db.tagDao()
            val habitDao = db.habitDao()
            noteDao.deleteAll()
            tagDao.deleteAll()
            habitDao.deleteAll()
            tagDao.insertAll(data.tags)
            noteDao.insertAll(data.notes)
            noteDao.insertRefs(data.refs)
            habitDao.insertAllHabits(data.habits)
            habitDao.insertAllChecks(data.checks)
        }
        onChanged()
        BackupSummary(data.notes.size, data.tags.size, data.habits.size, data.checks.size)
    }

    private suspend fun buildJson(): Pair<JSONObject, BackupSummary> {
        val tags = db.tagDao().all()
        val notes = db.noteDao().allNotes()
        val tagIdsByNote = db.noteDao().allRefs().groupBy({ it.noteId }, { it.tagId })
        val habits = db.habitDao().habits()
        val checks = db.habitDao().allChecks()

        val json = JSONObject()
            .put("app", APP_ID)
            .put("version", VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("tags", JSONArray(tags.map {
                JSONObject().put("id", it.id).put("name", it.name).put("type", it.type.name)
            }))
            .put("notes", JSONArray(notes.map {
                JSONObject()
                    .put("id", it.id)
                    .put("text", it.text)
                    .put("createdAt", it.createdAt)
                    .put("updatedAt", it.updatedAt)
                    .put("tagIds", JSONArray(tagIdsByNote[it.id].orEmpty()))
            }))
            .put("habits", JSONArray(habits.map {
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("position", it.position)
                    .put("createdAt", it.createdAt)
            }))
            .put("checks", JSONArray(checks.map {
                JSONObject().put("habitId", it.habitId).put("day", it.day)
            }))
        return json to BackupSummary(notes.size, tags.size, habits.size, checks.size)
    }

    private class BackupData(
        val tags: List<TagEntity>,
        val notes: List<NoteEntity>,
        val refs: List<NoteTagCrossRef>,
        val habits: List<HabitEntity>,
        val checks: List<HabitCheckEntity>,
    )

    private fun parse(json: JSONObject): BackupData {
        if (json.optString("app") != APP_ID) throw BackupException("Este arquivo não é um backup do Cerebelo.")
        val version = json.optInt("version", 0)
        if (version > VERSION) throw BackupException("Backup feito por uma versão mais nova do app.")

        val tags = json.getJSONArray("tags").objects().map {
            TagEntity(id = it.getLong("id"), name = it.getString("name"), type = TagType.valueOf(it.getString("type")))
        }
        val tagIds = tags.map { it.id }.toSet()
        val notes = mutableListOf<NoteEntity>()
        val refs = mutableListOf<NoteTagCrossRef>()
        json.getJSONArray("notes").objects().forEach {
            val note = NoteEntity(
                id = it.getLong("id"),
                text = it.getString("text"),
                createdAt = it.getLong("createdAt"),
                updatedAt = it.optLong("updatedAt", it.getLong("createdAt")),
            )
            notes += note
            it.optJSONArray("tagIds")?.let { ids ->
                for (i in 0 until ids.length()) {
                    val tagId = ids.getLong(i)
                    if (tagId in tagIds) refs += NoteTagCrossRef(note.id, tagId)
                }
            }
        }
        val habits = json.optJSONArray("habits")?.objects().orEmpty().map {
            HabitEntity(
                id = it.getLong("id"),
                name = it.getString("name"),
                position = it.optInt("position", 0),
                createdAt = it.optLong("createdAt", 0),
            )
        }
        val habitIds = habits.map { it.id }.toSet()
        val checks = json.optJSONArray("checks")?.objects().orEmpty()
            .map { HabitCheckEntity(habitId = it.getLong("habitId"), day = it.getLong("day")) }
            .filter { it.habitId in habitIds }
        return BackupData(tags, notes, refs, habits, checks)
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }

    private companion object {
        const val APP_ID = "cerebelo"
        const val VERSION = 1
    }
}
