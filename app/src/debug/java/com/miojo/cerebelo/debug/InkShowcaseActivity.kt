package com.miojo.cerebelo.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.components.DayMark
import com.miojo.cerebelo.ui.components.InkBottomNav
import com.miojo.cerebelo.ui.components.InkCheckbox
import com.miojo.cerebelo.ui.components.InkDayBox
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkItemText
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkPlusButton
import com.miojo.cerebelo.ui.components.InkRule
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.components.InkTag
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.InkTextField
import com.miojo.cerebelo.ui.components.InkVRule
import com.miojo.cerebelo.ui.components.LedgerText
import com.miojo.cerebelo.ui.components.LedgerTextField
import com.miojo.cerebelo.ui.components.OrnamentOfDay
import com.miojo.cerebelo.ui.theme.CerebeloTheme
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkAssets
import com.miojo.cerebelo.ui.theme.InkSizes

/**
 * Vitrine dos componentes de tinta (só no build de debug).
 * Abrir: adb shell am start -n com.miojo.cerebelo/.debug.InkShowcaseActivity
 */
class InkShowcaseActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CerebeloTheme {
                var search by remember { mutableStateOf("") }
                var draft by remember { mutableStateOf("Texto escrito sobre a linha de caderno, que se repete a cada 28dp.") }
                var vitaminas by remember { mutableStateOf(true) }
                var exercicio by remember { mutableStateOf(false) }
                var tab by remember { mutableIntStateOf(0) }

                Box(Modifier.fillMaxSize().background(Ink.colors.page)) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .padding(horizontal = Ink.spacing.screen),
                        verticalArrangement = Arrangement.spacedBy(Ink.spacing.md),
                    ) {
                        Spacer(Modifier.height(Ink.spacing.dragonClearance))
                        InkScreenTitle("Componentes")
                        InkLabel("Terça, 13 de outubro")

                        InkFrame(Modifier.fillMaxWidth(), style = InkFrameStyle.Single, contentPadding = PaddingValues(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("5", style = Ink.type.bigNumber, color = Ink.colors.seal)
                                Spacer(Modifier.width(Ink.spacing.sm))
                                InkLabel("Dias\nseguidos", color = Ink.colors.ink)
                                Spacer(Modifier.weight(1f))
                                listOf("Q" to DayMark.MISSED, "Q" to DayMark.DONE, "S" to DayMark.DONE, "S" to DayMark.DONE, "D" to DayMark.DONE)
                                    .forEach { (l, m) -> InkDayBox(l, m, isToday = false, modifier = Modifier.width(30.dp)) }
                                InkDayBox("T", DayMark.TODAY, isToday = true, modifier = Modifier.width(30.dp))
                            }
                        }

                        InkFrame(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
                                LedgerText("Ana vai fazer a entrevista na Orion no dia 20. A mãe dela operou o joelho e está se recuperando bem.")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
                                        InkTag("Ana", TagType.PERSON, onClick = {})
                                        InkTag("Entrevista", TagType.EVENT, onClick = {})
                                    }
                                    InkLabel("19:04")
                                }
                            }
                        }

                        InkTextField(search, { search = it }, placeholder = "Buscar por nome, tag ou palavra", showSearchIcon = true)

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
                            InkTextButton("Tudo", {}, frame = InkFrameStyle.Double)
                            InkTextButton("Pessoas", {}, frame = InkFrameStyle.Person, color = Ink.colors.person)
                            InkTextButton("Acontecimentos", {}, frame = InkFrameStyle.Event, color = Ink.colors.seal, italic = true)
                        }

                        InkSectionHeader("Afazeres")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InkCheckbox(vitaminas, { vitaminas = it })
                            Spacer(Modifier.width(Ink.spacing.sm))
                            InkItemText("Vitaminas", vitaminas)
                        }
                        InkRule()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InkCheckbox(exercicio, { exercicio = it })
                            Spacer(Modifier.width(Ink.spacing.sm))
                            InkItemText("Exercício", exercicio)
                        }

                        InkSectionHeader("Escrita")
                        InkFrame(Modifier.fillMaxWidth()) {
                            LedgerTextField(draft, { draft = it }, placeholder = "O que te contaram?", minRows = 4)
                        }

                        Row(Modifier.height(120.dp), horizontalArrangement = Arrangement.spacedBy(Ink.spacing.md)) {
                            InkVRule()
                            Column {
                                Text("13", style = Ink.type.dateNumber, color = Ink.colors.ink)
                                InkLabel("Out")
                            }
                            Spacer(Modifier.weight(1f))
                            InkPlusButton(onClick = {})
                        }

                        OrnamentOfDay()
                        Text("Ana", style = Ink.type.personName, color = Ink.colors.ink, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(Modifier.height(Ink.spacing.xl))
                    }

                    // Dragão por cima do conteúdo, sem receber toques.
                    Image(
                        painter = painterResource(InkAssets.dragon),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(InkSizes.dragonWidth)
                            .offset(x = InkSizes.dragonOffsetX, y = InkSizes.dragonOffsetY),
                    )

                    InkBottomNav(
                        items = listOf("Notas", "Tempo", "Pessoas", "Afazeres"),
                        selectedIndex = tab,
                        onSelect = { tab = it },
                        modifier = Modifier.align(Alignment.BottomCenter).background(Ink.colors.page),
                    )
                }
            }
        }
    }
}

