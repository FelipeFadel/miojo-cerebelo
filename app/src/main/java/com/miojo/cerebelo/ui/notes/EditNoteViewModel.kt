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

data class EditNoteState(
    val loaded: Boolean = false,
    val createdAt: Long = 0,
    val text: String = "",
    val tags: List<TagRef> = emptyList(),
    /** Vira true depois de salvar ou apagar: a tela deve fechar. */
    val done: Boolean = false,
)

class EditNoteViewModel(
    private val noteId: Long,
    private val repository: NoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditNoteState())
    val state: StateFlow<EditNoteState> = _state.asStateFlow()

    val tags: StateFlow<List<TagEntity>> = repository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val item = repository.getNote(noteId)
            _state.value = if (item == null) {
                EditNoteState(done = true)
            } else {
                EditNoteState(
                    loaded = true,
                    createdAt = item.note.createdAt,
                    text = item.note.text,
                    tags = item.tags.map(TagRef::of),
                )
            }
        }
    }

    fun onTextChange(text: String) = _state.update { it.copy(text = text) }

    fun addTag(tag: TagRef) = _state.update { it.copy(tags = it.tags.plusTag(tag)) }

    fun removeTag(tag: TagRef) = _state.update { it.copy(tags = it.tags - tag) }

    fun save() {
        val current = _state.value
        if (current.text.isBlank()) return
        viewModelScope.launch {
            repository.saveNote(noteId, current.text.trim(), current.tags)
            _state.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _state.update { it.copy(done = true) }
        }
    }
}
