package com.miojo.cerebelo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY position, id")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY position, id")
    suspend fun habits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeHabit(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habit_checks")
    fun observeAllChecks(): Flow<List<HabitCheckEntity>>

    @Query("SELECT day FROM habit_checks WHERE habitId = :habitId")
    fun observeDays(habitId: Long): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM habit_checks WHERE habitId = :habitId AND day = :day)")
    suspend fun isChecked(habitId: Long, day: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheck(check: HabitCheckEntity)

    @Query("DELETE FROM habit_checks WHERE habitId = :habitId AND day = :day")
    suspend fun deleteCheck(habitId: Long, day: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM habits")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Query("UPDATE habits SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("UPDATE habits SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: Long)

    // --- Backup ---

    @Query("SELECT * FROM habit_checks")
    suspend fun allChecks(): List<HabitCheckEntity>

    @Insert
    suspend fun insertAllHabits(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllChecks(checks: List<HabitCheckEntity>)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}
