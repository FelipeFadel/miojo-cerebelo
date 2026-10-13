package com.miojo.cerebelo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miojo.cerebelo.AppContainer
import com.miojo.cerebelo.appContainer
import com.miojo.cerebelo.data.TagRef

/** Telas do app. */
sealed interface Screen {
    data object Home : Screen
    data class EditNote(val noteId: Long) : Screen
    data class Tag(val tagId: Long) : Screen
    data class HabitDetail(val habitId: Long) : Screen
    data object ManageHabits : Screen
    data object Backup : Screen
}

/** Abas da tela inicial. */
enum class HomeTab { NOTES, TODAY }

/**
 * Uma tela na pilha. Cada uma tem seus próprios ViewModels,
 * que são descartados quando a tela é fechada.
 */
class BackStackEntry(val screen: Screen, val key: Long) : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

/**
 * Pilha de telas mínima (evita a dependência navigation-compose).
 * É um ViewModel para sobreviver a rotação de tela.
 */
class Navigator : ViewModel() {
    private var nextKey = 0L
    val backStack = mutableStateListOf(BackStackEntry(Screen.Home, nextKey++))

    val current: BackStackEntry get() = backStack.last()
    val canPop: Boolean get() = backStack.size > 1

    /** Cada incremento pede para focar o campo de nova nota. */
    var newNoteRequest by mutableIntStateOf(0)
        private set

    /** Tag a pré-marcar na nova nota (ex.: "Nova nota" na tela da Ana). */
    var pendingTag by mutableStateOf<TagRef?>(null)
        private set

    var homeTab by mutableStateOf(HomeTab.NOTES)

    /** Volta para a tela inicial com o cursor no campo de escrita. */
    fun requestNewNote(withTag: TagRef? = null) {
        popToHome()
        homeTab = HomeTab.NOTES
        pendingTag = withTag
        newNoteRequest++
    }

    fun consumePendingTag() {
        pendingTag = null
    }

    fun push(screen: Screen) {
        backStack.add(BackStackEntry(screen, nextKey++))
    }

    fun pop() {
        if (canPop) backStack.removeAt(backStack.lastIndex).viewModelStore.clear()
    }

    fun popToHome() {
        while (canPop) pop()
    }

    override fun onCleared() {
        backStack.forEach { it.viewModelStore.clear() }
    }
}

/** Cria um ViewModel com acesso às dependências do app. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = LocalContext.current.appContainer
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}
