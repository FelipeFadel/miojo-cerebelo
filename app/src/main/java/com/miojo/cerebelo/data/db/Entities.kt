package com.miojo.cerebelo.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class TagType { PERSON, EVENT }

@Entity(tableName = "notes", indices = [Index("createdAt")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name", "type"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val type: TagType,
)

@Entity(
    tableName = "note_tags",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tagId")],
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long,
)

/** Item do checklist diário (ex.: "Exercício"). */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Ordem na lista (menor primeiro). */
    val position: Int,
    val createdAt: Long,
)

/** Marca de "feito" de um hábito num dia. [day] = LocalDate.toEpochDay(). */
@Entity(
    tableName = "habit_checks",
    primaryKeys = ["habitId", "day"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitCheckEntity(
    val habitId: Long,
    val day: Long,
)

data class NoteWithTags(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
)
