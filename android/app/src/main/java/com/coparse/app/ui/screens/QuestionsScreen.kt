package com.coparse.app.ui.screens

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication

@Composable
fun QuestionsScreen(
    documentId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documentId) {
        try {
            val a = app.repository.getAnalysis(documentId)
            questions = a.questionsToAsk.map { it.question }
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "What to ask", style = MaterialTheme.typography.headlineSmall)
        if (questions.isEmpty() && error == null) {
            CircularProgressIndicator()
            return@Column
        }
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        questions.forEachIndexed { idx, q ->
            Card(onClick = {
                copyToClipboard(clipboard, context, q)
            }) {
                Column(Modifier.padding(12.dp)) {
                    Text(text = "${idx + 1}. $q", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Tap to copy", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}

private fun copyToClipboard(
    context: android.content.Context,
    text: String,
) {
    val clip = ClipData.newPlainText("CoParse question", text)
    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
        .setPrimaryClip(clip)
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
