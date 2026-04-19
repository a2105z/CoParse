package com.coparse.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DocumentCreateResponse(
    @SerialName("document_id") val documentId: String,
    @SerialName("job_id") val jobId: String,
)

@Serializable
data class JobStatusResponse(
    val id: String,
    @SerialName("document_id") val documentId: String,
    val status: String,
    @SerialName("progress_steps") val progressSteps: List<JsonObject>? = null,
    @SerialName("error_message") val errorMessage: String? = null,
)

@Serializable
data class ReanalyzeRequest(
    @SerialName("contract_type") val contractType: String,
    val role: String,
)

@Serializable
data class AnalysisResponse(
    @SerialName("document_id") val documentId: String,
    @SerialName("job_id") val jobId: String? = null,
    @SerialName("contract_type") val contractType: String,
    val role: String,
    @SerialName("overall_score") val overallScore: Int,
    @SerialName("signature_readiness") val signatureReadiness: JsonObject,
    @SerialName("analysis_confidence") val analysisConfidence: JsonObject? = null,
    val limitations: List<String> = emptyList(),
    @SerialName("category_scores") val categoryScores: Map<String, Int>,
    @SerialName("top_issues") val topIssues: List<JsonObject>,
    val clauses: List<ClauseItem>,
    @SerialName("missing_protections") val missingProtections: List<MissingProtection>,
    @SerialName("questions_to_ask") val questionsToAsk: List<QuestionItem>,
    val timeline: List<JsonObject>,
    @SerialName("student_journey") val studentJourney: JsonObject? = null,
    @SerialName("next_steps") val nextSteps: JsonObject? = null,
    @SerialName("changes_since_last_run") val changesSinceLastRun: JsonObject? = null,
)

@Serializable
data class ClauseItem(
    val id: String,
    val text: String,
    val theme: String,
    @SerialName("risk_level") val riskLevel: String,
    val tag: String? = null,
    @SerialName("flag_reason") val flagReason: String? = null,
    @SerialName("confidence_note") val confidenceNote: String? = null,
    @SerialName("plain_english") val plainEnglish: String,
    @SerialName("compare_note") val compareNote: String? = null,
    @SerialName("suggested_question_neutral") val suggestedQuestionNeutral: String? = null,
)

@Serializable
data class MissingProtection(
    val id: String,
    val label: String,
    val detail: String,
)

@Serializable
data class QuestionItem(
    @SerialName("clause_id") val clauseId: String? = null,
    val question: String,
    val context: String? = null,
)
