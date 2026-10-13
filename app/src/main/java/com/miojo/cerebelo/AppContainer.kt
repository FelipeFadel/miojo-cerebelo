package com.miojo.cerebelo

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.miojo.cerebelo.data.BackupRepository
import com.miojo.cerebelo.data.HabitRepository
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.db.AppDatabase
import com.miojo.cerebelo.widget.CerebeloWidget

/** Cria e guarda as dependências do app (injeção manual, sem biblioteca). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: AppDatabase = AppDatabase.create(appContext)

    /** Redesenha o widget da tela inicial (depois de mudar o checklist). */
    val updateWidget: suspend () -> Unit = { CerebeloWidget().updateAll(appContext) }

    val noteRepository = NoteRepository(database)
    val habitRepository = HabitRepository(database, onChanged = updateWidget)
    val backupRepository = BackupRepository(database, appContext.contentResolver, onChanged = updateWidget)
}
