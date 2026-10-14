package com.miojo.cerebelo.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkBackButton
import com.miojo.cerebelo.ui.components.InkDialog
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.theme.Ink
import java.time.LocalDate

@Composable
fun BackupScreen(onBack: () -> Unit) {
    val vm = appViewModel { BackupViewModel(it.backupRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmImport by rememberSaveable { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(vm::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(vm::importFrom) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.colors.page)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Ink.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Ink.spacing.lg),
    ) {
        Column {
            InkBackButton(onClick = onBack, modifier = Modifier.padding(top = Ink.spacing.sm))
            InkScreenTitle("Backup", Modifier.padding(top = Ink.spacing.sm))
        }
        Text(
            "Suas notas ficam só neste celular. De vez em quando, guarde uma cópia em outro lugar (Google Drive, e-mail, computador).",
            style = Ink.type.item,
            color = Ink.colors.inkSoft,
        )

        BackupCard(
            title = "Exportar",
            description = "Salva notas, tags e afazeres (com histórico) num arquivo .json.",
            action = {
                InkTextButton(
                    "Exportar backup",
                    onClick = { exportLauncher.launch("cerebelo-backup-${LocalDate.now()}.json") },
                    frame = InkFrameStyle.Double,
                    enabled = !state.working,
                )
            },
        )

        BackupCard(
            title = "Importar",
            description = "Restaura um arquivo de backup. Atenção: substitui tudo o que está no app agora.",
            action = {
                InkTextButton(
                    "Importar backup",
                    onClick = { confirmImport = true },
                    frame = InkFrameStyle.Single,
                    enabled = !state.working,
                )
            },
        )

        if (state.working) InkLabel("Trabalhando…")
        state.message?.let {
            Text(it, style = Ink.type.item, color = if (state.isError) Ink.colors.seal else Ink.colors.ink)
        }
    }

    if (confirmImport) {
        InkDialog(
            title = "Substituir tudo?",
            onDismiss = { confirmImport = false },
            confirmLabel = "Escolher arquivo",
            onConfirm = {
                confirmImport = false
                importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/*"))
            },
        ) {
            Text(
                "As notas e afazeres atuais serão trocados pelos do arquivo. Se quiser, exporte um backup antes.",
                style = Ink.type.item,
                color = Ink.colors.inkSoft,
            )
        }
    }
}

@Composable
private fun BackupCard(title: String, description: String, action: @Composable () -> Unit) {
    InkFrame(Modifier.fillMaxWidth(), style = InkFrameStyle.Single) {
        Column(verticalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            Text(title, style = Ink.type.section, color = Ink.colors.ink)
            Text(description, style = Ink.type.item, color = Ink.colors.ink)
            action()
        }
    }
}
