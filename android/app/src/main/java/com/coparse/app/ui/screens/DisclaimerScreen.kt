package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import kotlinx.coroutines.launch

@Composable
fun DisclaimerScreen(
    onAccepted: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CoParse",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "Educational information only",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "CoParse highlights common contract topics and questions to ask. " +
                "It does not provide legal advice and does not tell you whether to sign a document. " +
                "When in doubt, consult legal aid or a qualified attorney.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = {
                scope.launch {
                    app.disclaimerStore.setDisclaimerAccepted(true)
                    onAccepted()
                }
            },
        ) {
            Text("I understand")
        }
    }
}
