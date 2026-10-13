package com.miojo.cerebelo.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.HabitRepository
import com.miojo.cerebelo.data.db.HabitEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Histórico de um item do checklist. */
class HabitDetailViewModel(
    private val habitId: Long,
    private val repository: HabitRepository,
) : ViewModel() {

    val habit: StateFlow<HabitEntity?> = repository.observeHabit(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Dias marcados (LocalDate.toEpochDay()). null = carregando. */
    val days: StateFlow<Set<Long>?> = repository.observeDays(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Marca/desmarca um dia (para corrigir dias esquecidos). */
    fun toggle(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return
        viewModelScope.launch { repository.toggle(habitId, date) }
    }
}
