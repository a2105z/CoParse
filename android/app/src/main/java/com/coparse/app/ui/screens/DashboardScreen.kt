package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.R
import com.coparse.app.data.remote.AnalysisResponse
import com.coparse.app.ui.components.AppScaffold
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

@Composable
fun DashboardScreen(
    documentId: String,
    onClause: (clauseId: String) -> Unit,
    onOpenDocumentReview: (focusClauseId: String?) -> Unit,
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

    AppScaffold(
        title = stringResource(R.string.screen_dashboard_title),
        onNavigateBack = onHome,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (analysis == null && error == null) {
                CircularProgressIndicator()
                return@Column
            }
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                return@Column
            }
            val a = analysis ?: return@Column

            if (a.limitations.isNotEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Limitations", style = MaterialTheme.typography.titleSmall)
                        a.limitations.take(2).forEach { limitation ->
                            Text("• $limitation", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Text(
                text = "Type: ${a.contractType} · Role: ${a.role}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Signature readiness: ${a.overallScore}/100",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = recommendationText(a.signatureReadiness),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            a.analysisConfidence?.let { confidence ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val level = confidence.string("level")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                        Text("Analysis confidence: $level", style = MaterialTheme.typography.titleMedium)
                        confidence.string("summary")?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        confidence.stringList("reasons").take(2).forEach { reason ->
                            Text("• $reason", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            a.changesSinceLastRun?.let { changes ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("What changed", style = MaterialTheme.typography.titleMedium)
                        changes.string("summary")?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        changes.int("score_delta")?.let { delta ->
                            val sign = if (delta > 0) "+" else ""
                            Text(
                                text = "Score delta: $sign$delta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            a.studentJourney?.let { journey ->
                Text(text = journey.string("title") ?: "Student checklist", style = MaterialTheme.typography.titleMedium)
                journey.stringList("checklist").forEach {
                    Text("☑ $it", style = MaterialTheme.typography.bodyMedium)
                }
                journey.stringList("verification_prompts").take(2).forEach {
                    Text("Check: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(text = "Category signals", style = MaterialTheme.typography.titleMedium)
            a.categoryScores.forEach { (k, v) ->
                Text(text = "· $k: $v", style = MaterialTheme.typography.bodyMedium)
            }

            Text(text = "Top items to review", style = MaterialTheme.typography.titleMedium)
            a.topIssues.take(5).forEach { issue ->
                val theme = (issue["theme"] as? JsonPrimitive)?.contentOrNull ?: "item"
                Card(
                    onClick = { issue.string("id")?.let(onOpenDocumentReview) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = theme.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleSmall)
                        issue.string("flag_reason")?.let { reason ->
                            Text(
                                text = "Flagged because: $reason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        issue.string("plain_english")?.let { pe ->
                            Text(
                                text = pe.take(180) + if (pe.length > 180) "…" else "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = "Tap to open in document review",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Text(text = "Missing protections (signals)", style = MaterialTheme.typography.titleMedium)
            a.missingProtections.take(5).forEach { m ->
                Text(text = "· ${m.label}: ${m.detail}", style = MaterialTheme.typography.bodyMedium)
            }

            if (a.timeline.isNotEmpty()) {
                Text(text = "When this matters", style = MaterialTheme.typography.titleMedium)
                a.timeline.forEach { item ->
                    val firstClauseId = item.stringList("clause_ids").firstOrNull()
                    Card(
                        onClick = { firstClauseId?.let(onOpenDocumentReview) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.string("title") ?: "Timeline item", style = MaterialTheme.typography.titleSmall)
                            item.string("when")?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            item.string("watch_for")?.let {
                                Text("Watch for: $it", style = MaterialTheme.typography.bodySmall)
                            }
                            if (firstClauseId != null) {
                                Text(
                                    "Tap to open related section",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            a.nextSteps?.let { next ->
                Text(text = "What to do Monday", style = MaterialTheme.typography.titleMedium)
                next.objectList("if_then_nudges").take(4).forEach { nudge ->
                    Text("If: ${nudge.string("if")}", style = MaterialTheme.typography.bodySmall)
                    Text("Then: ${nudge.string("then")}", style = MaterialTheme.typography.bodyMedium)
                }
                next.string("privacy_note")?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(onClick = onQuestions) {
                Text("Questions to ask")
            }
            OutlinedButton(onClick = { onOpenDocumentReview(null) }) {
                Text("Open document review")
            }

            Text(text = "Clauses", style = MaterialTheme.typography.titleMedium)
            a.clauses.take(12).forEach { c ->
                Card(
                    onClick = { onClause(c.id) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text = c.theme.uppercase(), style = MaterialTheme.typography.labelMedium)
                        Text(text = c.text.take(220) + if (c.text.length > 220) "…" else "")
                        Text(text = "Risk: ${c.riskLevel}", style = MaterialTheme.typography.bodySmall)
                        c.flagReason?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.stringList(key: String): List<String> {
    val arr = this[key] as? JsonArray ?: return emptyList()
    return arr.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
}

private fun JsonObject.objectList(key: String): List<JsonObject> {
    val arr = this[key] as? JsonArray ?: return emptyList()
    return arr.mapNotNull { runCatching { it.jsonObject }.getOrNull() }
}

private fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
