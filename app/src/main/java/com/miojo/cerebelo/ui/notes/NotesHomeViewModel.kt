package com.miojo.cerebelo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.ChecklistProgress
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.data.HabitRepository
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.db.NoteWithTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Tela inicial: sequência, últimas notas e afazeres de hoje. */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesHomeViewModel(
    notes: NoteRepository,
    private val habits: HabitRepository,
) : ViewModel() {

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    /** null = carregando. */
    val recent: StateFlow<List<NoteWithTags>?> = notes.observeRecent(RECENT_COUNT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todos: StateFlow<List<HabitForDay>> = _today
        .flatMapLatest { habits.observeDay(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val progress: StateFlow<ChecklistProgress?> = _today
        .flatMapLatest { habits.observeProgress(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Chamado ao voltar para o app: se virou o dia, tudo se atualiza. */
    fun refreshDate() {
        _today.value = LocalDate.now()
    }

    fun toggle(habitId: Long) {
        viewModelScope.launch { habits.toggle(habitId, _today.value) }
    }

    private companion object {
        const val RECENT_COUNT = 3
    }
}
