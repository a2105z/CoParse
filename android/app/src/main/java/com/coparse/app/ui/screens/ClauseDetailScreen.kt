package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.coparse.app.data.remote.ClauseItem
import com.coparse.app.ui.components.AppScaffold

@Composable
fun ClauseDetailScreen(
    documentId: String,
    clauseId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    var clause by remember { mutableStateOf<ClauseItem?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documentId, clauseId) {
        try {
            val a = app.repository.getAnalysis(documentId)
            clause = a.clauses.firstOrNull { it.id == clauseId }
        } catch (e: Exception) {
            error = e.message
        }
    }

    AppScaffold(
        title = stringResource(R.string.screen_clause_title),
        onNavigateBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (clause == null && error == null) {
                CircularProgressIndicator()
                return@Column
            }
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                return@Column
            }
            val c = clause ?: return@Column

            Text(
                text = c.theme.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Risk: ${c.riskLevel}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            c.flagReason?.let {
                Text(text = "Why flagged: $it", style = MaterialTheme.typography.bodySmall)
            }
            c.confidenceNote?.let {
                Text(text = "Confidence note: $it", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Original text", style = MaterialTheme.typography.titleSmall)
            Text(text = c.text, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Plain English", style = MaterialTheme.typography.titleSmall)
            Text(text = c.plainEnglish, style = MaterialTheme.typography.bodyMedium)
            c.compareNote?.let {
                Text(text = "Compared to typical", style = MaterialTheme.typography.titleSmall)
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            c.suggestedQuestionNeutral?.let {
                Text(text = "Question to ask", style = MaterialTheme.typography.titleSmall)
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }

            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.action_back))
            }
        }
    }
}
