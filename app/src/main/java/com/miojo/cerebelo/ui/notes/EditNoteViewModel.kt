package com.miojo.cerebelo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Adiciona a tag se ainda não estiver na lista (mesmo nome e tipo). */
fun List<TagRef>.plusTag(tag: TagRef): List<TagRef> =
    if (any { it.type == tag.type && it.name.equals(tag.name, ignoreCase = true) }) this else this + tag

data class EditNoteState(
    val loaded: Boolean = false,
    val isNew: Boolean = true,
    val createdAt: Long = 0,
    val text: String = "",
    val tags: List<TagRef> = emptyList(),
    /** Houve mudança desde que a tela abriu. */
    val dirty: Boolean = false,
    /** Vira true depois de salvar ou apagar: a tela deve fechar. */
    val done: Boolean = false,
) {
    val canSave: Boolean get() = text.isNotBlank()
}

/** Escrever uma nota nova ([noteId] nulo) ou editar uma existente. */
class EditNoteViewModel(
    private val noteId: Long?,
    private val withTagId: Long?,
    private val repository: NoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditNoteState())
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

    val tags: StateFlow<List<TagEntity>> = repository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _state.value = if (noteId == null) {
                val tag = withTagId?.let { repository.getTag(it) }
                EditNoteState(
                    loaded = true,
                    isNew = true,
                    createdAt = System.currentTimeMillis(),
                    tags = listOfNotNull(tag?.let(TagRef::of)),
                )
            } else {
                val item = repository.getNote(noteId)
                if (item == null) {
                    EditNoteState(done = true)
                } else {
                    EditNoteState(
                        loaded = true,
                        isNew = false,
                        createdAt = item.note.createdAt,
                        text = item.note.text,
                        tags = item.tags.map(TagRef::of),
                    )
                }
            }
        }
    }

    fun onTextChange(text: String) = _state.update { it.copy(text = text, dirty = true) }

    fun addTag(tag: TagRef) = _state.update { it.copy(tags = it.tags.plusTag(tag), dirty = true) }

    fun removeTag(tag: TagRef) = _state.update { it.copy(tags = it.tags - tag, dirty = true) }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        viewModelScope.launch {
            repository.saveNote(noteId, current.text.trim(), current.tags)
            _state.update { it.copy(done = true) }
        }
    }

    fun delete() {
        val id = noteId ?: return
        viewModelScope.launch {
            repository.deleteNote(id)
            _state.update { it.copy(done = true) }
        }
    }
}
