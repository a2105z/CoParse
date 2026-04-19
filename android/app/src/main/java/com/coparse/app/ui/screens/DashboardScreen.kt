package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.data.remote.AnalysisResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Composable
fun DashboardScreen(
    documentId: String,
    onClause: (clauseId: String) -> Unit,
    onQuestions: () -> Unit,
    onHome: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val scope = rememberCoroutineScope()
    var analysis by remember { mutableStateOf<AnalysisResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documentId) {
        try {
            analysis = app.repository.getAnalysis(documentId)
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
        Text(text = "Summary", style = MaterialTheme.typography.headlineSmall)
        if (analysis == null && error == null) {
            CircularProgressIndicator()
            return@Column
        }
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        val a = analysis ?: return@Column

        Text(
            text = "Type: ${a.contractType} · Role: ${a.role}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Signature readiness: ${a.overallScore}/100",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = recommendationText(a.signatureReadiness),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(text = "Category signals", style = MaterialTheme.typography.titleMedium)
        a.categoryScores.forEach { (k, v) ->
            Text(text = "· $k: $v")
        }

        Text(text = "Top items to review", style = MaterialTheme.typography.titleMedium)
        a.topIssues.take(5).forEach { issue ->
            val theme = (issue["theme"] as? JsonPrimitive)?.contentOrNull ?: "item"
            Text(text = "· $theme")
        }

        Text(text = "Missing protections (signals)", style = MaterialTheme.typography.titleMedium)
        a.missingProtections.take(5).forEach { m ->
            Text(text = "· ${m.label}: ${m.detail}")
        }

        Button(onClick = onQuestions) {
            Text("Questions to ask")
        }

        Text(text = "Clauses", style = MaterialTheme.typography.titleMedium)
        a.clauses.take(12).forEach { c ->
            Card(onClick = { onClause(c.id) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(text = c.theme.uppercase(), style = MaterialTheme.typography.labelMedium)
                    Text(text = c.text.take(220) + if (c.text.length > 220) "…" else "")
                    Text(text = "Risk: ${c.riskLevel}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        app.repository.saveToLocal(a)
                    } catch (_: Exception) {
                    }
                }
            },
        ) {
            Text("Save summary locally")
        }

        OutlinedButton(onClick = onHome) {
            Text("Back to home")
        }
    }
}

private fun recommendationText(obj: JsonObject): String {
    val text = (obj["recommendation_text"] as? JsonPrimitive)?.contentOrNull
    if (text != null) return text
    val key = (obj["recommendation_key"] as? JsonPrimitive)?.contentOrNull
    return when (key) {
        "mostly_standard" -> "Looks mostly standard—still review flagged items."
        "worth_clarifying" -> "Worth clarifying several items before signing."
        "caution" -> "Several terms may be unusually restrictive or unclear."
        "strongly_consider_review" -> "Strongly consider reviewing with legal aid or counsel."
        else -> "Review details carefully before signing."
    }
}
