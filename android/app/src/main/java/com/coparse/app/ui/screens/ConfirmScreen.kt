package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.R
import com.coparse.app.data.remote.AnalysisResponse
import com.coparse.app.ui.components.AppScaffold
import kotlinx.coroutines.launch

private val CONTRACT_TYPES = listOf("lease", "internship_offer", "freelance")
private val ROLES = listOf(
    "renter",
    "student_intern",
    "freelancer",
    "employee",
    "contractor",
    "roommate",
    "parent_guardian",
    "general",
)

@Composable
fun ConfirmScreen(
    documentId: String,
    onContinue: () -> Unit,
    onReanalyze: (newJobId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val scope = rememberCoroutineScope()
    var analysis by remember { mutableStateOf<AnalysisResponse?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var contractType by remember { mutableStateOf("lease") }
    var role by remember { mutableStateOf("general") }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        try {
            val a = app.repository.getAnalysis(documentId)
            analysis = a
            contractType = a.contractType
            role = a.role
        } catch (e: Exception) {
            loadError = e.message
        }
    }

    AppScaffold(
        title = stringResource(R.string.screen_confirm_title),
        onNavigateBack = onNavigateBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "We use contract type and your role to tailor flags and questions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (analysis == null && loadError == null) {
                CircularProgressIndicator()
                return@Column
            }
            loadError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                return@Column
            }

            Text(text = "Contract type", style = MaterialTheme.typography.titleSmall)
            CONTRACT_TYPES.forEach { t ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = contractType == t,
                            onClick = { contractType = t },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = contractType == t,
                        onClick = { contractType = t },
                    )
                    Text(
                        text = t,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Text(
                text = "Your role",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            ROLES.forEach { r ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = role == r,
                            onClick = { role = r },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = role == r,
                        onClick = { role = r },
                    )
                    Text(
                        text = r,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Button(
                enabled = !busy,
                onClick = {
                    val a = analysis ?: return@Button
                    if (a.contractType == contractType && a.role == role) {
                        onContinue()
                        return@Button
                    }
                    busy = true
                    scope.launch {
                        try {
                            val jid = app.repository.reanalyze(documentId, contractType, role)
                            onReanalyze(jid)
                        } finally {
                            busy = false
                        }
                    }
                },
            ) {
                Text(if (busy) "Working…" else "Apply and refresh analysis")
            }

            OutlinedButton(onClick = onContinue, enabled = !busy) {
                Text("Continue with current results")
            }
        }
    }
}
