package com.miojo.cerebelo.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.HabitRepository
import com.miojo.cerebelo.data.db.HabitEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Montar o checklist: adicionar, renomear, reordenar e apagar itens. */
class ManageHabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    val habits: StateFlow<List<HabitEntity>> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        viewModelScope.launch { repository.addHabit(name) }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch { repository.renameHabit(id, name) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteHabit(id) }
    }

    fun moveUp(id: Long) {
        viewModelScope.launch { repository.move(id, -1) }
    }

    fun moveDown(id: Long) {
        viewModelScope.launch { repository.move(id, +1) }
    }
}
