package com.coparse.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import kotlinx.coroutines.launch

@Composable
fun IntakeScreen(
    hintContractType: String?,
    hintRole: String?,
    onUploaded: (documentId: String, jobId: String) -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Upload contract", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Choose a PDF or text file. Analysis runs on the CoParse server.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (busy) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { launcher.launch("application/pdf") }) {
                Text("Pick PDF")
            }
        }
        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
    }
}
