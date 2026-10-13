package com.miojo.cerebelo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.millisToDateTime
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ComposerState(
    val text: String = "",
    val tags: List<TagRef> = emptyList(),
) {
    val canSave: Boolean get() = text.isNotBlank()
}

/** Notas de um mesmo dia na linha do tempo. */
data class DayGroup(val date: LocalDate, val notes: List<NoteWithTags>)

/** null = ainda carregando. */
data class TimelineState(
    val groups: List<DayGroup>? = null,
    val isFiltering: Boolean = false,
)

/** Adiciona a tag se ainda não estiver na lista (mesmo nome e tipo). */
fun List<TagRef>.plusTag(tag: TagRef): List<TagRef> =
    if (any { it.type == tag.type && it.name.equals(tag.name, ignoreCase = true) }) this else this + tag

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _composer = MutableStateFlow(ComposerState())
    val composer: StateFlow<ComposerState> = _composer.asStateFlow()

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
                        groups = groupByDay(notes),
                        isFiltering = q.isNotEmpty() || tagId != null,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineState())

    /** Último pedido de foco no campo de escrita já atendido. */
    var handledFocusRequest = 0
        private set

    fun markFocusHandled(request: Int) {
        handledFocusRequest = request
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun setFilter(tagId: Long?) {
        filterTagId.value = tagId
    }

    fun onTextChange(text: String) = _composer.update { it.copy(text = text) }

    fun addTag(tag: TagRef) = _composer.update { it.copy(tags = it.tags.plusTag(tag)) }

    fun removeTag(tag: TagRef) = _composer.update { it.copy(tags = it.tags - tag) }

    fun save() {
        val state = _composer.value
        if (!state.canSave) return
        _composer.value = ComposerState()
        viewModelScope.launch {
            repository.saveNote(id = null, text = state.text.trim(), tags = state.tags)
        }
    }

    private fun groupByDay(notes: List<NoteWithTags>): List<DayGroup> =
        notes.groupBy { millisToDateTime(it.note.createdAt).toLocalDate() }
            .map { (date, items) -> DayGroup(date, items) }
}
