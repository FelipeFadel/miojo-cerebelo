package com.miojo.cerebelo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.miojo.cerebelo.MainActivity
import com.miojo.cerebelo.appContainer
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.ui.weekdayShort
import com.miojo.cerebelo.ui.theme.InkAssets
import com.miojo.cerebelo.ui.theme.WidgetTokens
import com.miojo.cerebelo.ui.theme.inkMaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Cores do widget = mesmas cores do app (DesignTokens.kt). */
private val WidgetColors = ColorProviders(light = inkMaterialColors(false), dark = inkMaterialColors(true))

/** Widget: botão "Nova nota" + checklist de hoje + ilustração do dia da semana. */
class CerebeloWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.appContainer.habitRepository
        val today = LocalDate.now()
        val initial = withContext(Dispatchers.IO) { repository.loadDay(today) }

        provideContent {
            val items by remember { repository.observeDay(today) }.collectAsState(initial)
            GlanceTheme(colors = WidgetColors) {
                WidgetContent(items, today)
            }
        }
    }
}

@Composable
private fun WidgetContent(items: List<HabitForDay>, today: LocalDate) {
    val context = LocalContext.current
    val ink = GlanceTheme.colors.onSurface
    val inkSoft = GlanceTheme.colors.onSurfaceVariant

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(WidgetTokens.cornerRadius)
            .background(GlanceTheme.colors.surface),
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(start = WidgetTokens.padding, top = WidgetTokens.padding, bottom = WidgetTokens.padding),
        ) {
            // Cabeçalho: dia + quanto falta à esquerda, "+" (nova nota) numa moldura de tinta à direita.
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${weekdayShort(today)} ${today.dayOfMonth}".uppercase() + remainingLabel(items),
                    style = TextStyle(color = inkSoft, fontSize = WidgetTokens.labelSize, fontFamily = FontFamily.Serif),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                )
                Box(
                    modifier = GlanceModifier
                        .size(WidgetTokens.buttonHeight)
                        .background(ImageProvider(InkAssets.frameSingle), ContentScale.FillBounds)
                        .clickable(actionStartActivity(MainActivity.newNoteIntent(context))),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(InkAssets.plus),
                        contentDescription = "Nova nota",
                        modifier = GlanceModifier.size(WidgetTokens.plusIcon),
                    )
                }
            }
            Image(
                provider = ImageProvider(InkAssets.rule),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.fillMaxWidth().height(WidgetTokens.ruleHeight).padding(top = 2.dp),
            )

            if (items.isEmpty()) {
                Text(
                    text = "Toque para montar seu checklist no app.",
                    style = TextStyle(color = inkSoft, fontSize = WidgetTokens.bodySize, fontFamily = FontFamily.Serif),
                    modifier = GlanceModifier
                        .padding(top = WidgetTokens.itemSpacing)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                )
            } else {
                LazyColumn {
                    items(items, itemId = { it.habit.id }) { item -> HabitRow(item, ink) }
                }
            }
        }

        // Ilustração lateral: uma para cada dia da semana.
        Image(
            provider = ImageProvider(InkAssets.widgetVerticals[today.dayOfWeek.value - 1]),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier
                .width(WidgetTokens.sideStripWidth)
                .fillMaxHeight()
                .padding(vertical = WidgetTokens.padding, horizontal = 4.dp),
        )
    }
}

/** " · FALTA 1", " · FALTAM 2", " · TUDO FEITO" (vazio se não há afazeres). */
private fun remainingLabel(items: List<HabitForDay>): String {
    if (items.isEmpty()) return ""
    return when (val left = items.count { !it.done }) {
        0 -> " · TUDO FEITO"
        1 -> " · FALTA 1"
        else -> " · FALTAM $left"
    }
}

@Composable
private fun HabitRow(item: HabitForDay, ink: ColorProvider) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = WidgetTokens.itemSpacing)
            .clickable(
                actionRunCallback<ToggleHabitAction>(actionParametersOf(ToggleHabitAction.HabitId to item.habit.id))
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = GlanceModifier.size(WidgetTokens.checkbox)) {
            Image(ImageProvider(InkAssets.box), contentDescription = null, modifier = GlanceModifier.size(WidgetTokens.checkbox))
            if (item.done) {
                Image(ImageProvider(InkAssets.xmark), contentDescription = "Feito", modifier = GlanceModifier.size(WidgetTokens.checkbox))
            }
        }
        Spacer(GlanceModifier.width(WidgetTokens.itemSpacing + 2.dp))
        Text(
            text = item.habit.name,
            style = TextStyle(color = ink, fontSize = WidgetTokens.itemSize, fontFamily = FontFamily.Serif),
            maxLines = 2,
        )
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
