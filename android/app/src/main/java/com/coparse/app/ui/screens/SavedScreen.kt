package com.coparse.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication

@Composable
fun SavedScreen(
    onOpen: (documentId: String) -> Unit,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    val items by app.repository.savedAnalyses.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Saved on this device", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.documentId }) { row ->
                Card(
                    modifier = Modifier.clickable { onOpen(row.documentId) },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text = row.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = row.summaryLine, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Score ${row.score}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}
