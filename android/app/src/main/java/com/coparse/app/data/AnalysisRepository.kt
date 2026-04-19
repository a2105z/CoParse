package com.coparse.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.coparse.app.data.local.AppDatabase
import com.coparse.app.data.local.SavedAnalysisEntity
import com.coparse.app.data.remote.AnalysisResponse
import com.coparse.app.data.remote.ApiClient
import com.coparse.app.data.remote.ReanalyzeRequest
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AnalysisRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val api = ApiClient.api
    private val db = AppDatabase.build(appContext)
    private val dao = db.savedAnalysisDao()
    private val json = Json { prettyPrint = false }

    val savedAnalyses = dao.observeAll()

    suspend fun uploadAndAnalyze(
        uri: Uri,
        hintContractType: String?,
        hintRole: String?,
    ): Pair<String, String> {
        val cr = appContext.contentResolver
        val mime = cr.getType(uri) ?: "application/octet-stream"
        val name = queryDisplayName(cr, uri) ?: "document.pdf"
        val input = cr.openInputStream(uri) ?: error("Cannot open file")
        val bytes = input.use { it.readBytes() }
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", name, body)
        val hintType = (hintContractType ?: "auto").toRequestBody(null)
        val hintR = (hintRole ?: "general").toRequestBody(null)
        val res = api.uploadDocument(part, hintType, hintR)
        return res.documentId to res.jobId
    }

    suspend fun waitForJob(jobId: String, maxAttempts: Int = 120): String {
        repeat(maxAttempts) {
            val job = api.getJob(jobId)
            when (job.status) {
                "completed" -> return jobId
                "failed" -> error(job.errorMessage ?: "Analysis failed")
                else -> delay(500L)
            }
        }
        error("Timed out waiting for analysis")
    }

    suspend fun getAnalysis(documentId: String): AnalysisResponse =
        api.getAnalysis(documentId)

    suspend fun downloadDocumentToCache(documentId: String): File {
        val res = api.downloadDocument(documentId)
        if (!res.isSuccessful) error("Failed to download document (${res.code()})")
        val body = res.body() ?: error("Empty document response")

        val contentType = res.headers()["Content-Type"].orEmpty().lowercase()
        val ext = when {
            contentType.contains("pdf") -> "pdf"
            contentType.contains("text") -> "txt"
            else -> "bin"
        }
        val out = File(appContext.cacheDir, "coparse-$documentId.$ext")
        body.byteStream().use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
        return out
    }

    suspend fun reanalyze(documentId: String, contractType: String, role: String): String {
        val res = api.reanalyze(documentId, ReanalyzeRequest(contractType, role))
        return res.jobId
    }

    suspend fun saveToLocal(analysis: AnalysisResponse) {
        val entity = SavedAnalysisEntity(
            documentId = analysis.documentId,
            title = analysis.contractType.replace('_', ' ').replaceFirstChar { it.titlecase() },
            summaryLine = "Readiness ${analysis.overallScore}/100 · ${analysis.role}",
            score = analysis.overallScore,
            payloadJson = json.encodeToString(analysis),
        )
        dao.upsert(entity)
    }

    private fun queryDisplayName(cr: android.content.ContentResolver, uri: Uri): String? {
        cr.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) return c.getString(idx)
        }
        return null
    }
}
