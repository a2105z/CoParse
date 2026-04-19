package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenIntake: (hintContractType: String?, hintRole: String?) -> Unit,
    onOpenSaved: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "What are you reviewing?", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Contract safety for students, renters, and early-career workers.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = { onOpenIntake("lease", "renter") }) {
            Text("Review a lease")
        }
        Button(onClick = { onOpenIntake("internship_offer", "student_intern") }) {
            Text("Review an internship / offer")
        }
        Button(onClick = { onOpenIntake("freelance", "freelancer") }) {
            Text("Review a freelance / contractor agreement")
        }
        OutlinedButton(onClick = { onOpenIntake("auto", "general") }) {
            Text("Upload any supported PDF")
        }
        OutlinedButton(onClick = onOpenSaved) {
            Text("Saved reviews")
        }
    }
}
