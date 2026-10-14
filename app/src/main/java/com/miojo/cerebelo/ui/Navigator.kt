package com.miojo.cerebelo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/** Telas do app. */
sealed interface Screen {
    data object Home : Screen
    /** [noteId] nulo = nota nova; [withTagId] = tag já marcada na nota nova. */
    data class EditNote(val noteId: Long? = null, val withTagId: Long? = null) : Screen
    data class Tag(val tagId: Long) : Screen
    data class HabitDetail(val habitId: Long) : Screen
    data object ManageHabits : Screen
    data object Backup : Screen
}

/** Abas da navegação inferior. */
enum class HomeTab(val label: String) {
    NOTES("Notas"),
    TIMELINE("Tempo"),
    PEOPLE("Pessoas"),
    TODOS("Afazeres"),
}

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

    var homeTab by mutableStateOf(HomeTab.NOTES)
        private set

    fun push(screen: Screen) {
        backStack.add(BackStackEntry(screen, nextKey++))
    }

    fun pop() {
        if (canPop) backStack.removeAt(backStack.lastIndex).viewModelStore.clear()
    }

    fun popToHome() {
        while (canPop) pop()
    }

    /** Troca de aba (fecha telas abertas por cima). */
    fun selectTab(tab: HomeTab) {
        popToHome()
        homeTab = tab
    }

    /** Abre a escrita de uma nota nova, com o teclado aberto. Voltar leva às Notas. */
    fun openNewNote(withTagId: Long? = null) {
        popToHome()
        homeTab = HomeTab.NOTES
        push(Screen.EditNote(noteId = null, withTagId = withTagId))
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
