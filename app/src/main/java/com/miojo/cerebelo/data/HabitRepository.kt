package com.miojo.cerebelo.data

import com.miojo.cerebelo.data.db.AppDatabase
import com.miojo.cerebelo.data.db.HabitCheckEntity
import com.miojo.cerebelo.data.db.HabitEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Um hábito como aparece no checklist de um dia. */
data class HabitForDay(
    val habit: HabitEntity,
    val done: Boolean,
    val streak: Int,
)

/**
 * @param onChanged chamado depois de qualquer mudança (usado para atualizar o widget).
 */
class HabitRepository(
    private val db: AppDatabase,
    private val onChanged: suspend () -> Unit = {},
) {
    private val dao = db.habitDao()

    fun observeHabits(): Flow<List<HabitEntity>> = dao.observeHabits()

    fun observeHabit(id: Long): Flow<HabitEntity?> = dao.observeHabit(id)

    /** Dias marcados de um hábito. */
    fun observeDays(habitId: Long): Flow<Set<Long>> = dao.observeDays(habitId).map { it.toSet() }

    /** Checklist do dia [date], com a sequência atual de cada hábito. */
    fun observeDay(date: LocalDate): Flow<List<HabitForDay>> =
        combine(dao.observeHabits(), dao.observeAllChecks()) { habits, checks ->
            val daysByHabit = checks.groupBy({ it.habitId }, { it.day }).mapValues { it.value.toSet() }
            habits.map { habit ->
                val days = daysByHabit[habit.id].orEmpty()
                HabitForDay(
                    habit = habit,
                    done = date.toEpochDay() in days,
                    streak = Streaks.current(days, date),
                )
            }
        }

    suspend fun loadDay(date: LocalDate): List<HabitForDay> = observeDay(date).first()

    suspend fun toggle(habitId: Long, date: LocalDate) {
        val day = date.toEpochDay()
        setChecked(habitId, date, checked = !dao.isChecked(habitId, day))
    }

    suspend fun setChecked(habitId: Long, date: LocalDate, checked: Boolean) {
        val day = date.toEpochDay()
        if (checked) dao.insertCheck(HabitCheckEntity(habitId, day)) else dao.deleteCheck(habitId, day)
        onChanged()
    }

    suspend fun addHabit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        db.withTransaction {
            dao.insertHabit(
                HabitEntity(
                    name = trimmed,
                    position = dao.maxPosition() + 1,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
        onChanged()
    }

    suspend fun renameHabit(id: Long, name: String) {
        if (name.isBlank()) return
        dao.rename(id, name.trim())
        onChanged()
    }

    suspend fun deleteHabit(id: Long) {
        dao.delete(id)
        onChanged()
    }

    /** Move o hábito uma posição para cima (-1) ou para baixo (+1). */
    suspend fun move(id: Long, direction: Int) {
        db.withTransaction {
            val list = dao.habits().toMutableList()
            val from = list.indexOfFirst { it.id == id }
            val to = from + direction
            if (from < 0 || to !in list.indices) return@withTransaction
            list.add(to, list.removeAt(from))
            list.forEachIndexed { index, habit -> dao.setPosition(habit.id, index) }
        }
        onChanged()
    }
}
