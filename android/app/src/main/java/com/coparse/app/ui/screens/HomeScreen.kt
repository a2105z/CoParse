package com.coparse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coparse.app.R
import com.coparse.app.ui.components.AppScaffold
import com.coparse.app.ui.components.HomeActionCard

@Composable
fun HomeScreen(
    onOpenIntake: (hintContractType: String?, hintRole: String?) -> Unit,
    onOpenSaved: () -> Unit,
) {
    AppScaffold(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.screen_home_subtitle),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.home_what_reviewing),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        modifier = Modifier.padding(top = 6.dp),
                        text = stringResource(R.string.home_hero_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.92f),
                    )
                }
            }

            HomeActionCard(
                title = stringResource(R.string.home_review_lease),
                icon = Icons.Outlined.Home,
                onClick = { onOpenIntake("lease", "renter") },
            )
            HomeActionCard(
                title = stringResource(R.string.home_review_internship),
                icon = Icons.Outlined.School,
                onClick = { onOpenIntake("internship_offer", "student_intern") },
            )
            HomeActionCard(
                title = stringResource(R.string.home_review_freelance),
                icon = Icons.Outlined.Business,
                onClick = { onOpenIntake("freelance", "freelancer") },
            )
            HomeActionCard(
                title = stringResource(R.string.home_upload_any),
                icon = Icons.Outlined.UploadFile,
                onClick = { onOpenIntake("auto", "general") },
                useTonalStyle = true,
            )
            HomeActionCard(
                title = stringResource(R.string.home_saved_reviews),
                icon = Icons.Outlined.Bookmarks,
                onClick = onOpenSaved,
                useTonalStyle = true,
            )
        }
    }
}
