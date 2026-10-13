package com.miojo.cerebelo.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.data.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Checklist de hoje. */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    /** null = carregando. */
    val items: StateFlow<List<HabitForDay>?> = _today
        .flatMapLatest { repository.observeDay(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Chamado ao voltar para o app: se virou o dia, o checklist reinicia. */
    fun refreshDate() {
        _today.value = LocalDate.now()
    }

    fun toggle(habitId: Long) {
        viewModelScope.launch { repository.toggle(habitId, _today.value) }
    }
}
