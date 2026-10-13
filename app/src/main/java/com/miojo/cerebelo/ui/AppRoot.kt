package com.miojo.cerebelo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.backup.BackupScreen
import com.miojo.cerebelo.ui.habits.HabitDetailScreen
import com.miojo.cerebelo.ui.habits.ManageHabitsScreen
import com.miojo.cerebelo.ui.habits.TodayScreen
import com.miojo.cerebelo.ui.notes.EditNoteScreen
import com.miojo.cerebelo.ui.notes.NotesScreen
import com.miojo.cerebelo.ui.notes.TagScreen

/** Mostra a tela do topo da pilha. */
@Composable
fun AppRoot(navigator: Navigator) {
    val entry = navigator.current
    val stateHolder = rememberSaveableStateHolder()
    val openTag = { tag: TagEntity -> navigator.push(Screen.Tag(tag.id)) }
    val openNote = { id: Long -> navigator.push(Screen.EditNote(id)) }

    BackHandler(enabled = navigator.canPop) { navigator.pop() }

    stateHolder.SaveableStateProvider(entry.key) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
            when (val screen = entry.screen) {
                Screen.Home -> HomeScreen(navigator, openNote, openTag)
                is Screen.EditNote -> EditNoteScreen(screen.noteId, onClose = navigator::pop)
                is Screen.Tag -> TagScreen(
                    tagId = screen.tagId,
                    onBack = navigator::pop,
                    onOpenNote = openNote,
                    onTagClick = openTag,
                    onNewNote = { navigator.requestNewNote(TagRef.of(it)) },
                )
                is Screen.HabitDetail -> HabitDetailScreen(screen.habitId, onBack = navigator::pop)
                Screen.ManageHabits -> ManageHabitsScreen(onBack = navigator::pop)
                Screen.Backup -> BackupScreen(onBack = navigator::pop)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    navigator: Navigator,
    openNote: (Long) -> Unit,
    openTag: (TagEntity) -> Unit,
) {
    val tab = navigator.homeTab
    val tabStateHolder = rememberSaveableStateHolder()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tab == HomeTab.NOTES) "Notas" else "Hoje") },
                actions = {
                    if (tab == HomeTab.TODAY) {
                        IconButton(onClick = { navigator.push(Screen.ManageHabits) }) {
                            Icon(Icons.Default.Edit, "Editar checklist")
                        }
                    }
                    IconButton(onClick = { navigator.push(Screen.Backup) }) {
                        Icon(Icons.Default.Settings, "Backup")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == HomeTab.NOTES,
                    onClick = { navigator.homeTab = HomeTab.NOTES },
                    icon = { Icon(Icons.Default.Edit, null) },
                    label = { Text("Notas") },
                )
                NavigationBarItem(
                    selected = tab == HomeTab.TODAY,
                    onClick = { navigator.homeTab = HomeTab.TODAY },
                    icon = { Icon(Icons.Default.CheckCircle, null) },
                    label = { Text("Hoje") },
                )
            }
        },
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()
        tabStateHolder.SaveableStateProvider(tab.name) {
            when (tab) {
                HomeTab.NOTES -> NotesScreen(
                    focusRequest = navigator.newNoteRequest,
                    pendingTag = navigator.pendingTag,
                    onPendingTagConsumed = navigator::consumePendingTag,
                    onOpenNote = openNote,
                    onTagClick = openTag,
                    modifier = contentModifier,
                )
                HomeTab.TODAY -> TodayScreen(
                    onOpenHabit = { navigator.push(Screen.HabitDetail(it)) },
                    onManageHabits = { navigator.push(Screen.ManageHabits) },
                    modifier = contentModifier,
                )
            }
        }
    }
}
