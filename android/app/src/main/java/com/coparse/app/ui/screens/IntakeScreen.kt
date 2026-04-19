package com.coparse.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.R
import com.coparse.app.ui.components.AppScaffold
import kotlinx.coroutines.launch

@Composable
fun IntakeScreen(
    hintContractType: String?,
    hintRole: String?,
    onUploaded: (documentId: String, jobId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        error = null
        scope.launch {
            try {
                val (docId, jobId) = app.repository.uploadAndAnalyze(uri, hintContractType, hintRole)
                onUploaded(docId, jobId)
            } catch (e: Exception) {
                error = e.message ?: "Upload failed"
            } finally {
                busy = false
            }
        }
    }

    AppScaffold(
        title = stringResource(R.string.screen_intake_title),
        onNavigateBack = onNavigateBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Choose a PDF. Analysis runs on the CoParse server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (busy) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { launcher.launch("application/pdf") }) {
                    Text("Pick PDF")
                }
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
