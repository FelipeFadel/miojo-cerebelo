package com.miojo.cerebelo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.miojo.cerebelo.MainActivity
import com.miojo.cerebelo.appContainer
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.ui.theme.WidgetTokens
import com.miojo.cerebelo.ui.theme.inkMaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Cores do widget = mesmas cores do app (DesignTokens.kt). */
private val WidgetColors = ColorProviders(light = inkMaterialColors(false), dark = inkMaterialColors(true))

/** Widget: botão "Nova nota" + checklist de hoje. */
class CerebeloWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.appContainer.habitRepository
        val today = LocalDate.now()
        val initial = withContext(Dispatchers.IO) { repository.loadDay(today) }

        provideContent {
            val items by remember { repository.observeDay(today) }.collectAsState(initial)
            GlanceTheme(colors = WidgetColors) {
                WidgetContent(items)
            }
        }
    }
}

@Composable
private fun WidgetContent(items: List<HabitForDay>) {
    val context = LocalContext.current
    val textColor = GlanceTheme.colors.onSurface

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(WidgetTokens.padding),
    ) {
        Button(
            text = "+ Nova nota",
            onClick = actionStartActivity(MainActivity.newNoteIntent(context)),
            modifier = GlanceModifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(),
        )
        Spacer(GlanceModifier.height(WidgetTokens.padding))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Hoje",
                style = TextStyle(fontWeight = FontWeight.Bold, color = textColor),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (items.isNotEmpty()) {
                Text(
                    text = "${items.count { it.done }}/${items.size}",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                text = "Toque para montar seu checklist no app.",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier
                    .padding(top = WidgetTokens.itemSpacing)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            )
        } else {
            LazyColumn {
                items(items, itemId = { it.habit.id }) { item ->
                    CheckBox(
                        checked = item.done,
                        onCheckedChange = actionRunCallback<ToggleHabitAction>(
                            actionParametersOf(ToggleHabitAction.HabitId to item.habit.id)
                        ),
                        text = item.habit.name,
                        style = TextStyle(color = textColor),
                        modifier = GlanceModifier.fillMaxWidth().padding(vertical = WidgetTokens.itemSpacing),
                    )
                }
            }
        }
    }
}

/** Marca/desmarca um item de hoje direto pelo widget, sem abrir o app. */
class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HabitId] ?: return
        context.appContainer.habitRepository.toggle(habitId, LocalDate.now())
    }

    companion object {
        val HabitId = ActionParameters.Key<Long>("habitId")
    }
}

class CerebeloWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CerebeloWidget()
}
