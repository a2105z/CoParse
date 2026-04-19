package com.coparse.app.ui.screens

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.R
import com.coparse.app.ui.components.AppScaffold
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

@Composable
fun QuestionsScreen(
    documentId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailTemplates by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var escalation by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var privacyNote by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documentId) {
        try {
            val a = app.repository.getAnalysis(documentId)
            questions = a.questionsToAsk.map { it.question }
            val next = a.nextSteps
            if (next != null) {
                emailTemplates = next.objectList("email_templates")
                escalation = next.objectList("escalation_resources")
                privacyNote = next.string("privacy_note")
            }
        } catch (e: Exception) {
            error = e.message
        }
    }

    AppScaffold(
        title = stringResource(R.string.screen_questions_title),
        onNavigateBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (questions.isEmpty() && error == null) {
                CircularProgressIndicator()
                return@Column
            }
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                return@Column
            }
            questions.forEachIndexed { idx, q ->
                Card(
                    onClick = { copyToClipboard(context, q) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "${idx + 1}. $q",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.action_copy),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (emailTemplates.isNotEmpty()) {
                Text("Email templates", style = MaterialTheme.typography.titleMedium)
                emailTemplates.forEach { template ->
                    val subject = template.string("subject") ?: "Contract clarifications"
                    val body = template.string("body") ?: ""
                    Card(
                        onClick = { copyToClipboard(context, "Subject: $subject\n\n$body") },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(subject, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text("Tap to copy full email", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (escalation.isNotEmpty()) {
                Text("Escalation path", style = MaterialTheme.typography.titleMedium)
                escalation.forEach { item ->
                    Text("• ${item.string("label")}", style = MaterialTheme.typography.bodyMedium)
                    item.string("why")?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            privacyNote?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.action_back))
            }
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
    Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
}

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.objectList(key: String): List<JsonObject> {
    val arr = this[key] as? JsonArray ?: return emptyList()
    return arr.mapNotNull { runCatching { it.jsonObject }.getOrNull() }
}
