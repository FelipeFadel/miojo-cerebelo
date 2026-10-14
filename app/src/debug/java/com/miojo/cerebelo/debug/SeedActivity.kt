package com.miojo.cerebelo.debug

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.miojo.cerebelo.MainActivity
import com.miojo.cerebelo.appContainer
import com.miojo.cerebelo.data.db.HabitCheckEntity
import com.miojo.cerebelo.data.db.HabitEntity
import com.miojo.cerebelo.data.db.NoteEntity
import com.miojo.cerebelo.data.db.NoteTagCrossRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Preenche o app com dados de exemplo (APAGA os dados atuais). Só existe no build de debug.
 * Abrir: adb shell am start -n com.miojo.cerebelo/.debug.SeedActivity
 */
class SeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            seed()
            startActivity(Intent(this@SeedActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
    }

    private suspend fun seed() {
        val db = appContainer.database
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        fun at(daysAgo: Long, h: Int, m: Int) =
            today.minusDays(daysAgo).atTime(LocalTime.of(h, m)).atZone(zone).toInstant().toEpochMilli()

        val tags = listOf(
            TagEntity(1, "Ana", TagType.PERSON), TagEntity(2, "Bruno", TagType.PERSON), TagEntity(3, "Carla", TagType.PERSON),
            TagEntity(10, "Entrevista", TagType.EVENT), TagEntity(11, "Família", TagType.EVENT), TagEntity(12, "Mudança", TagType.EVENT),
            TagEntity(13, "Natação", TagType.EVENT), TagEntity(14, "Trabalho", TagType.EVENT), TagEntity(15, "Aniversário", TagType.EVENT),
            TagEntity(16, "Pet", TagType.EVENT),
        )
        val notes = listOf(
            NoteEntity(1, "Ana vai fazer a entrevista na Orion no dia 20. A mãe dela operou o joelho e está se recuperando bem.", at(0, 19, 4), at(0, 19, 4)),
            NoteEntity(2, "Bruno mudou de apartamento no sábado. Pediu ajuda com a mudança em dezembro.", at(1, 20, 31), at(1, 20, 31)),
            NoteEntity(3, "Carla comentou que começou a fazer natação às terças.", at(1, 12, 10), at(1, 12, 10)),
            NoteEntity(4, "Ana está pensando em trocar de emprego antes do fim do ano.", at(3, 16, 45), at(3, 16, 45)),
            NoteEntity(5, "Aniversário dela foi no domingo. Gostou do livro que dei de presente.", at(17, 21, 0), at(17, 21, 0)),
            NoteEntity(6, "Contou que adotou um gato chamado Pimenta.", at(41, 18, 20), at(41, 18, 20)),
        )
        val refs = listOf(1L to 1L, 1L to 10L, 1L to 11L, 2L to 2L, 2L to 12L, 3L to 3L, 3L to 13L, 4L to 1L, 4L to 14L, 5L to 1L, 5L to 15L, 6L to 1L, 6L to 16L)
            .map { (n, t) -> NoteTagCrossRef(n, t) }
        val habits = listOf("Vitaminas", "Exercício", "Anotar as conversas do dia", "Dormir antes das 23h")
            .mapIndexed { i, name -> HabitEntity(i + 1L, name, i, at(30, 8, 0)) }
        // 5 dias seguidos cumpridos até ontem; 6 dias atrás incompleto; hoje só Vitaminas.
        val checks = buildList {
            for (back in 1L..5L) habits.forEach { add(HabitCheckEntity(it.id, today.minusDays(back).toEpochDay())) }
            add(HabitCheckEntity(1, today.minusDays(6).toEpochDay()))
            for (back in 7L..12L) habits.forEach { add(HabitCheckEntity(it.id, today.minusDays(back).toEpochDay())) }
            add(HabitCheckEntity(1, today.toEpochDay()))
        }

        db.withTransaction {
            db.noteDao().deleteAll(); db.tagDao().deleteAll(); db.habitDao().deleteAll()
            db.tagDao().insertAll(tags)
            db.noteDao().insertAll(notes)
            db.noteDao().insertRefs(refs)
            db.habitDao().insertAllHabits(habits)
            db.habitDao().insertAllChecks(checks)
        }
        appContainer.updateWidget()
    }
}
