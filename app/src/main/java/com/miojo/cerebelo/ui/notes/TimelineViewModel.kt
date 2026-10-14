package com.miojo.cerebelo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.millisToDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Notas de um mesmo dia na linha do tempo. */
data class DayGroup(val date: LocalDate, val notes: List<NoteWithTags>)

/** [groups] null = ainda carregando. */
data class TimelineState(
    val groups: List<DayGroup>? = null,
    val isFiltering: Boolean = false,
)

/** Linha do tempo: busca + filtro por uma tag. */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class TimelineViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val filterTagId = MutableStateFlow<Long?>(null)

    val tags: StateFlow<List<TagEntity>> = repository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Tag do filtro atual (some sozinha se a tag for apagada). */
    val filterTag: StateFlow<TagEntity?> = combine(filterTagId, tags) { id, all ->
        all.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val timeline: StateFlow<TimelineState> =
        combine(_query.debounce(200), filterTagId) { q, tagId -> q.trim() to tagId }
            .flatMapLatest { (q, tagId) ->
                repository.observeNotes(q, tagId).map { notes ->
                    TimelineState(
                        groups = notes.groupBy { millisToDate(it.note.createdAt) }
                            .map { (date, items) -> DayGroup(date, items) },
                        isFiltering = q.isNotEmpty() || tagId != null,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun setFilter(tagId: Long?) {
        filterTagId.value = tagId
    }
}
