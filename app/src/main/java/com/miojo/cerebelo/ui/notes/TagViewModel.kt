package com.miojo.cerebelo.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.NoteRepository
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tela de uma pessoa (ou acontecimento): todas as notas em ordem cronológica. */
class TagViewModel(
    private val tagId: Long,
    private val repository: NoteRepository,
) : ViewModel() {

    val tag: StateFlow<TagEntity?> = repository.observeTag(tagId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** null = carregando. */
    val notes: StateFlow<List<NoteWithTags>?> = repository.observeNotesForTag(tagId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _renameError = MutableStateFlow(false)
    val renameError: StateFlow<Boolean> = _renameError.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    /** [onSuccess] é chamado se o nome foi aceito. */
    fun rename(newName: String, onSuccess: () -> Unit) {
        val current = tag.value ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            val ok = repository.renameTag(current, newName)
            _renameError.value = !ok
            if (ok) onSuccess()
        }
    }

    fun clearRenameError() {
        _renameError.value = false
    }

    fun delete() {
        viewModelScope.launch {
            repository.deleteTag(tagId)
            _deleted.value = true
        }
    }
}
