package com.miojo.cerebelo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.backup.BackupScreen
import com.miojo.cerebelo.ui.components.InkBottomNav
import com.miojo.cerebelo.ui.habits.HabitDetailScreen
import com.miojo.cerebelo.ui.habits.ManageHabitsScreen
import com.miojo.cerebelo.ui.habits.TodayScreen
import com.miojo.cerebelo.ui.notes.EditNoteScreen
import com.miojo.cerebelo.ui.notes.NotesHomeScreen
import com.miojo.cerebelo.ui.notes.TagScreen
import com.miojo.cerebelo.ui.notes.TimelineScreen
import com.miojo.cerebelo.ui.people.PeopleScreen
import com.miojo.cerebelo.ui.theme.Ink

/** Mostra a tela do topo da pilha. */
@Composable
fun AppRoot(navigator: Navigator) {
    val entry = navigator.current
    val stateHolder = rememberSaveableStateHolder()
    val openTag = { tag: TagEntity -> navigator.push(Screen.Tag(tag.id)) }
    val openNote = { id: Long -> navigator.push(Screen.EditNote(noteId = id)) }

    BackHandler(enabled = navigator.canPop) { navigator.pop() }

    Box(Modifier.fillMaxSize().background(Ink.colors.page)) {
        stateHolder.SaveableStateProvider(entry.key) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
                when (val screen = entry.screen) {
                    Screen.Home -> WithBottomNav(navigator, navigator.homeTab) { modifier ->
                        HomeTabs(navigator, openNote, openTag, modifier)
                    }
                    is Screen.Tag -> WithBottomNav(navigator, HomeTab.PEOPLE) { modifier ->
                        TagScreen(
                            tagId = screen.tagId,
                            onBack = navigator::pop,
                            onOpenNote = openNote,
                            onTagClick = openTag,
                            onNewNote = { navigator.openNewNote(withTagId = it.id) },
                            modifier = modifier,
                        )
                    }
                    is Screen.EditNote -> EditNoteScreen(screen.noteId, screen.withTagId, onClose = navigator::pop)
                    is Screen.HabitDetail -> HabitDetailScreen(screen.habitId, onBack = navigator::pop)
                    Screen.ManageHabits -> ManageHabitsScreen(onBack = navigator::pop)
                    Screen.Backup -> BackupScreen(onBack = navigator::pop)
                }
            }
        }
    }
}

/** Conteúdo em cima, navegação de tinta embaixo. */
@Composable
private fun WithBottomNav(
    navigator: Navigator,
    selected: HomeTab,
    content: @Composable (Modifier) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        content(Modifier.weight(1f))
        InkBottomNav(
            items = HomeTab.entries.map { it.label },
            selectedIndex = selected.ordinal,
            onSelect = { navigator.selectTab(HomeTab.entries[it]) },
        )
    }
}

@Composable
private fun HomeTabs(
    navigator: Navigator,
    openNote: (Long) -> Unit,
    openTag: (TagEntity) -> Unit,
    modifier: Modifier,
) {
    val tabStateHolder = rememberSaveableStateHolder()
    val tab = navigator.homeTab
    tabStateHolder.SaveableStateProvider(tab.name) {
        when (tab) {
            HomeTab.NOTES -> NotesHomeScreen(
                onNewNote = { navigator.openNewNote() },
                onOpenNote = openNote,
                onTagClick = openTag,
                onSeeAllNotes = { navigator.selectTab(HomeTab.TIMELINE) },
                onEditTodos = { navigator.push(Screen.ManageHabits) },
                onBackup = { navigator.push(Screen.Backup) },
                modifier = modifier,
            )
            HomeTab.TIMELINE -> TimelineScreen(onOpenNote = openNote, onTagClick = openTag, modifier = modifier)
            HomeTab.PEOPLE -> PeopleScreen(onOpenTag = openTag, modifier = modifier)
            HomeTab.TODOS -> TodayScreen(
                onOpenHabit = { navigator.push(Screen.HabitDetail(it)) },
                onEditList = { navigator.push(Screen.ManageHabits) },
                modifier = modifier,
            )
        }
    }
}
