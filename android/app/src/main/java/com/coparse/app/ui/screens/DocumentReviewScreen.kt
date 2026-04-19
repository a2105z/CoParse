package com.coparse.app.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coparse.app.CoParseApplication
import com.coparse.app.data.remote.ClauseItem
import com.coparse.app.ui.components.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DocumentReviewScreen(
    documentId: String,
    focusClauseId: String?,
    onBack: () -> Unit,
    onClauseDetail: (clauseId: String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as CoParseApplication
    var clauses by remember { mutableStateOf<List<ClauseItem>>(emptyList()) }
    var pages by remember { mutableStateOf<List<RenderedPage>>(emptyList()) }
    var pageAnchors by remember { mutableStateOf<Map<Int, List<ClauseItem>>>(emptyMap()) }
    var documentType by remember { mutableStateOf("unknown") }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(documentId) {
        try {
            val analysis = app.repository.getAnalysis(documentId)
            clauses = analysis.clauses.sortedBy { clauseIndex(it.id) }
            val file = app.repository.downloadDocumentToCache(documentId)
            if (file.extension.lowercase() == "pdf") {
                documentType = "pdf"
                pages = renderPdfPages(file)
                pageAnchors = buildPageAnchors(clauses, pages.size)
            } else {
                documentType = "text"
            }
        } catch (e: Exception) {
            error = e.message
        }
    }

    LaunchedEffect(focusClauseId, pages.size, clauses.size) {
        val focus = focusClauseId?.ifBlank { null } ?: return@LaunchedEffect
        if (documentType != "pdf" || pages.isEmpty()) return@LaunchedEffect
        val clause = clauses.firstOrNull { it.id == focus } ?: return@LaunchedEffect
        val pageIndex = estimatePageForClause(clause.id, pages.size, clauses.size)
        val row = pageIndex + 2 // intro + anchor cards
        listState.animateScrollToItem(row.coerceAtLeast(0))
    }

    AppScaffold(
        title = "Document review",
        subtitle = if (documentType == "pdf") {
            "Rendered PDF with anchored clause jumps"
        } else {
            "Extracted text with legal term highlights"
        },
        onNavigateBack = onBack,
    ) {
        if (clauses.isEmpty() && error == null) {
            CircularProgressIndicator()
            return@AppScaffold
        }
        if (error != null) {
            Text(error ?: "Unable to load document", color = MaterialTheme.colorScheme.error)
            return@AppScaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = if (documentType == "pdf") {
                        "Tap any highlighted card to jump to clause detail. Page anchors are approximate."
                    } else {
                        "Highlighted terms are likely legal jargon; tap a clause card for full analysis."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
            if (documentType == "pdf" && pages.isNotEmpty()) {
                item {
                    Text("Jump to flagged items", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        clauses.filter { it.riskLevel in setOf("high", "medium") }.take(8).forEach { clause ->
                            Card(
                                onClick = { onClauseDetail(clause.id) },
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = clause.theme.replaceFirstChar { it.titlecase() },
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        text = "Likely page ${estimatePageForClause(clause.id, pages.size, clauses.size) + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                items(pages, key = { it.index }) { page ->
                    val anchors = pageAnchors[page.index].orEmpty()
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Page ${page.index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (anchors.isNotEmpty()) {
                                Text(
                                    text = "Anchors: ${anchors.joinToString { it.theme }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Image(
                                bitmap = page.bitmap.asImageBitmap(),
                                contentDescription = "Document page ${page.index + 1}",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                items(clauses, key = { it.id }) { clause ->
                    val focused = focusClauseId != null && clause.id == focusClauseId
                    Card(
                        onClick = { onClauseDetail(clause.id) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (focused) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (focused) 2.dp else 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "${clause.theme.replaceFirstChar { it.titlecase() }} • ${clause.riskLevel.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (focused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            clause.flagReason?.let {
                                Text(
                                    text = "Flagged because: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (focused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = highlightJargon(clause.text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (focused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

private data class RenderedPage(
    val index: Int,
    val bitmap: Bitmap,
)

private fun clauseIndex(id: String): Int {
    if (!id.startsWith("c")) return Int.MAX_VALUE
    return id.drop(1).toIntOrNull() ?: Int.MAX_VALUE
}

private fun estimatePageForClause(clauseId: String, pageCount: Int, clauseCount: Int): Int {
    if (pageCount <= 1 || clauseCount <= 1) return 0
    val idx = clauseIndex(clauseId).coerceAtLeast(0)
    val ratio = idx.toFloat() / (clauseCount - 1).coerceAtLeast(1)
    return (ratio * (pageCount - 1)).toInt().coerceIn(0, pageCount - 1)
}

private fun buildPageAnchors(
    clauses: List<ClauseItem>,
    pageCount: Int,
): Map<Int, List<ClauseItem>> {
    if (pageCount <= 0 || clauses.isEmpty()) return emptyMap()
    val grouped = mutableMapOf<Int, MutableList<ClauseItem>>()
    clauses.forEach { clause ->
        val page = estimatePageForClause(clause.id, pageCount, clauses.size)
        val list = grouped.getOrPut(page) { mutableListOf() }
        if (list.none { it.theme == clause.theme }) {
            list += clause
        }
    }
    return grouped.mapValues { (_, list) -> list.take(3) }
}

private suspend fun renderPdfPages(file: File): List<RenderedPage> = withContext(Dispatchers.IO) {
    val rendered = mutableListOf<RenderedPage>()
    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    PdfRenderer(pfd).use { renderer ->
        for (i in 0 until renderer.pageCount) {
            renderer.openPage(i).use { page ->
                val width = 1000
                val scale = width.toFloat() / page.width.toFloat().coerceAtLeast(1f)
                val height = (page.height * scale).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                rendered += RenderedPage(index = i, bitmap = bitmap)
            }
        }
    }
    pfd.close()
    rendered
}

private val jargonTerms = listOf(
    "indemnity",
    "indemnify",
    "arbitration",
    "governing law",
    "jurisdiction",
    "liability",
    "liability cap",
    "work product",
    "intellectual property",
    "confidentiality",
    "non-disclosure",
    "automatic renewal",
    "termination",
    "notice period",
    "assignment",
    "sublet",
    "security deposit",
    "late fee",
    "exclusive",
    "sole discretion",
)

private fun highlightJargon(text: String) = buildAnnotatedString {
    append(text)
    val low = text.lowercase()
    for (term in jargonTerms) {
        var start = low.indexOf(term)
        while (start >= 0) {
            val end = start + term.length
            addStyle(
                SpanStyle(
                    background = androidx.compose.ui.graphics.Color(0x66FDE68A),
                    fontWeight = FontWeight.SemiBold,
                ),
                start = start,
                end = end,
            )
            start = low.indexOf(term, end)
        }
    }
}
