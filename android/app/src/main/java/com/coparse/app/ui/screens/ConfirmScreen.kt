package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.menuAnchor
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.data.remote.AnalysisResponse
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    documentId: String,
    onContinue: () -> Unit,
    onReanalyze: (newJobId: String) -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Confirm context", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "We use contract type and your role to tailor flags and questions.",
            style = MaterialTheme.typography.bodyMedium,
        )

        if (analysis == null && loadError == null) {
            CircularProgressIndicator()
            return@Column
        }
        loadError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            return@Column
        }

        var typeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = contractType,
                onValueChange = {},
                label = { Text("Contract type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false },
            ) {
                CONTRACT_TYPES.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t) },
                        onClick = {
                            contractType = t
                            typeExpanded = false
                        },
                    )
                }
            }
        }

        var roleExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = roleExpanded,
            onExpandedChange = { roleExpanded = !roleExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = role,
                onValueChange = {},
                label = { Text("Your role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
            )
            ExposedDropdownMenu(
                expanded = roleExpanded,
                onDismissRequest = { roleExpanded = false },
            ) {
                ROLES.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r) },
                        onClick = {
                            role = r
                            roleExpanded = false
                        },
                    )
                }
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
