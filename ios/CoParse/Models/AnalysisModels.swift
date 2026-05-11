import Foundation

struct AnalysisResult: Codable, Identifiable, Hashable {
    var id: UUID
    var title: String
    var createdAt: Date
    var source: AnalysisSource
    var contractType: String
    var role: String
    var overallScore: Int
    var signatureReadiness: SignatureReadiness
    var analysisConfidence: AnalysisConfidence?
    var limitations: [String]
    var categoryScores: [String: Int]
    var topIssues: [TopIssue]
    var clauses: [ClauseCard]
    var missingProtections: [MissingProtection]
    var questionsToAsk: [QuestionItem]
    var timeline: [TimelineItem]
    var studentJourney: StudentJourney?
    var nextSteps: NextSteps?
    /// Full extracted text retained for re-analysis and export fidelity.
    var extractedText: String
    var ocrConfidence: Double?

    enum AnalysisSource: String, Codable {
        case scan
        case pdf
        case text
    }
}

struct SignatureReadiness: Codable, Hashable {
    var score: Int
    var recommendationKey: String
    var recommendationText: String
}

struct AnalysisConfidence: Codable, Hashable {
    var level: String
    var reasons: [String]
    var summary: String
}

struct TopIssue: Codable, Identifiable, Hashable {
    var id: String
    var theme: String
    var riskLevel: String
    var flagReason: String?
    var plainEnglish: String
}

struct ClauseCard: Codable, Identifiable, Hashable {
    var id: String
    var text: String
    var theme: String
    var riskLevel: String
    var tag: String
    var flagReason: String
    var confidenceNote: String
    var plainEnglish: String
    var compareNote: String
    var suggestedQuestionNeutral: String
    var suggestedQuestionPolite: String
    var negotiability: String
}

struct MissingProtection: Codable, Identifiable, Hashable {
    var id: String
    var label: String
    var detail: String
}

struct QuestionItem: Codable, Identifiable, Hashable {
    var id: String { "\(clauseId)-\(question)" }
    var clauseId: String
    var question: String
    var context: String
}

struct TimelineItem: Codable, Identifiable, Hashable {
    var id: String { phase }
    var phase: String
    var title: String
    var when: String
    var watchFor: String
    var clauseIds: [String]
}

struct StudentJourney: Codable, Hashable {
    var title: String
    var role: String
    var checklist: [String]
    var verificationPrompts: [String]
}

struct NextSteps: Codable, Hashable {
    var ifThenNudges: [IfThenNudge]
    var emailTemplates: [EmailTemplate]
    var escalationResources: [EscalationResource]
    var privacyNote: String
}

struct IfThenNudge: Codable, Hashable {
    var ifCondition: String
    var thenAction: String

    enum CodingKeys: String, CodingKey {
        case ifCondition = "if"
        case thenAction = "then"
    }
}

struct EmailTemplate: Codable, Identifiable, Hashable {
    var id: String { title }
    var title: String
    var subject: String
    var body: String
}

struct EscalationResource: Codable, Identifiable, Hashable {
    var id: String { label }
    var label: String
    var why: String
}
