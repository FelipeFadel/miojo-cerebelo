package com.miojo.cerebelo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.miojo.cerebelo.ui.AppRoot
import com.miojo.cerebelo.ui.Navigator
import com.miojo.cerebelo.ui.theme.CerebeloTheme

class MainActivity : ComponentActivity() {

    private val navigator: Navigator by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Abrir o app já escrevendo uma nota nova.
        if (savedInstanceState == null) navigator.openNewNote()

        setContent {
            CerebeloTheme {
                AppRoot(navigator)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_NEW_NOTE, false)) navigator.openNewNote()
    }

    companion object {
        const val EXTRA_NEW_NOTE = "new_note"

        fun newNoteIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_NEW_NOTE, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
