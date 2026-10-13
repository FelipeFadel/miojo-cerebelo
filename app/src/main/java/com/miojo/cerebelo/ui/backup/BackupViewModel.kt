package com.miojo.cerebelo.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miojo.cerebelo.data.BackupException
import com.miojo.cerebelo.data.BackupRepository
import com.miojo.cerebelo.data.BackupSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val working: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

class BackupViewModel(private val repository: BackupRepository) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun exportTo(uri: Uri) = perform("Backup salvo") { repository.exportTo(uri) }

    fun importFrom(uri: Uri) = perform("Backup restaurado") { repository.importFrom(uri) }

    private fun perform(successPrefix: String, action: suspend () -> BackupSummary) {
        if (_state.value.working) return
        _state.value = BackupUiState(working = true)
        viewModelScope.launch {
            _state.value = try {
                val s = action()
                BackupUiState(
                    message = "$successPrefix: ${s.notes} notas, ${s.tags} tags, " +
                        "${s.habits} itens de checklist, ${s.checks} dias marcados.",
                )
            } catch (e: BackupException) {
                BackupUiState(message = e.message, isError = true)
            } catch (e: Exception) {
                BackupUiState(message = "Erro: ${e.message ?: e::class.simpleName}", isError = true)
            }
        }
    }
}
