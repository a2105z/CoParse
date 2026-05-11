import Foundation

enum AnalysisEngine {
    static func analyze(
        text: String,
        hintContractType: String?,
        contractTypeOverride: String?,
        role: String,
        source: AnalysisResult.AnalysisSource,
        title: String,
        ocrConfidence: Double? = nil
    ) -> AnalysisResult {
        var ctype = ContractType(rawValue: contractTypeOverride ?? "")
            ?? AnalysisClassify.detectContractType(text: text, hint: hintContractType)
        if ctype == .unknown { ctype = .lease }

        let roleValue = role.isEmpty ? ctype.defaultRole : role
        let chunks = AnalysisSegment.segmentClauses(text)
        let confidence = analysisConfidence(text: text, clauses: chunks, ocrConfidence: ocrConfidence)

        var clauses: [ClauseCard] = []
        for (i, chunk) in chunks.enumerated() {
            let theme = AnalysisClassify.classifyTheme(chunk)
            let risk = AnalysisClassify.riskLevel(theme: theme, clauseText: chunk, contractType: ctype)
            let card = AnalysisExplain.buildClauseCard(
                clauseText: chunk,
                theme: theme,
                riskLevel: risk,
                contractType: ctype,
                role: roleValue
            )
            clauses.append(
                ClauseCard(
                    id: "c\(i)",
                    text: chunk,
                    theme: theme.rawValue,
                    riskLevel: risk,
                    tag: risk == "high" ? "high_risk" : (risk == "medium" ? "unusual" : "standard"),
                    flagReason: flagReason(theme: theme, riskLevel: risk, contractType: ctype),
                    confidenceNote: clauseConfidenceNote(chunk),
                    plainEnglish: card.plainEnglish,
                    compareNote: card.compareNote,
                    suggestedQuestionNeutral: card.question,
                    suggestedQuestionPolite: card.polite,
                    negotiability: card.negotiability
                )
            )
        }

        let order = ["high": 0, "medium": 1, "low": 2]
        clauses.sort { (order[$0.riskLevel] ?? 3) < (order[$1.riskLevel] ?? 3) }

        let missing = AnalysisMissing.findMissingProtections(contractType: ctype, fullText: text)
        let scored = AnalysisScore.compute(contractType: ctype, clauses: clauses, missing: missing)
        let topIssues = Array(clauses.filter { $0.riskLevel == "high" || $0.riskLevel == "medium" }.prefix(5))
        let questions: [QuestionItem] = topIssues.prefix(6).compactMap { c in
            guard !c.suggestedQuestionNeutral.isEmpty else { return nil }
            return QuestionItem(clauseId: c.id, question: c.suggestedQuestionNeutral, context: c.theme)
        }

        return AnalysisResult(
            id: UUID(),
            title: title,
            createdAt: Date(),
            source: source,
            contractType: ctype.rawValue,
            role: roleValue,
            overallScore: scored.overall,
            signatureReadiness: SignatureReadiness(
                score: scored.overall,
                recommendationKey: scored.recommendation,
                recommendationText: recText(scored.recommendation)
            ),
            analysisConfidence: confidence,
            limitations: [
                "Educational support only — not legal advice.",
                "OCR/text extraction can miss or distort clauses, especially from photos or scanned pages.",
                "Always verify key terms against the original document before signing.",
                "CoParse does not tell you to sign or not sign.",
            ],
            categoryScores: scored.categoryScores,
            topIssues: topIssues.map {
                TopIssue(
                    id: $0.id,
                    theme: $0.theme,
                    riskLevel: $0.riskLevel,
                    flagReason: $0.flagReason,
                    plainEnglish: String($0.plainEnglish.prefix(400))
                )
            },
            clauses: clauses,
            missingProtections: missing,
            questionsToAsk: questions,
            timeline: timeline(contractType: ctype, clauses: clauses),
            studentJourney: AnalysisStudent.buildStudentJourney(contractType: ctype, role: roleValue),
            nextSteps: AnalysisStudent.buildNextSteps(contractType: ctype, topIssues: topIssues, missing: missing),
            extractedText: text,
            ocrConfidence: ocrConfidence
        )
    }

    private static func recText(_ key: String) -> String {
        switch key {
        case "mostly_standard":
            return "Looks mostly standard but still review flagged items before signing."
        case "worth_clarifying":
            return "Worth clarifying several items before signing."
        case "caution":
            return "Several terms may be unusually restrictive or unclear; consider professional review."
        case "strongly_consider_review":
            return "Strongly consider reviewing with legal aid or counsel before signing."
        default:
            return "Review details carefully before signing."
        }
    }

    private static func analysisConfidence(text: String, clauses: [String], ocrConfidence: Double?) -> AnalysisConfidence {
        var reasons: [String] = []
        let low = text.lowercased()
        let alphaRatio = Double(text.filter(\.isLetter).count) / Double(max(1, text.count))
        var score = 0
        if text.count < 1200 {
            score -= 1
            reasons.append("The extracted text is short, so some sections may be missing.")
        }
        if clauses.count < 4 {
            score -= 1
            reasons.append("Few clause segments were detected; section boundaries may be unclear.")
        }
        if alphaRatio < 0.55 {
            score -= 1
            reasons.append("Text quality appears noisy, likely from scan/OCR issues.")
        }
        if let ocr = ocrConfidence {
            if ocr < 0.45 {
                score -= 1
                reasons.append("OCR confidence is low — retake pages in brighter, flatter conditions if possible.")
            } else if ocr >= 0.75 {
                score += 1
            }
        }
        if ["signature", "agreement", "term", "payment", "liability"].contains(where: { low.contains($0) }) {
            score += 1
        }
        let level = score >= 1 ? "high" : (score == 0 ? "medium" : "low")
        if reasons.isEmpty {
            reasons.append("Clause boundaries and text quality look usable for a first-pass review.")
        }
        let summary: String
        switch level {
        case "high": summary = "High confidence in extraction and segmentation quality."
        case "medium": summary = "Moderate confidence; review flagged items with original text side by side."
        default: summary = "Lower confidence due to extraction/segmentation quality. Treat results as directional."
        }
        return AnalysisConfidence(level: level, reasons: reasons, summary: summary)
    }

    private static func flagReason(theme: ClauseTheme, riskLevel: String, contractType: ContractType) -> String {
        let themeReason: [ClauseTheme: String] = [
            .payment: "money timing or obligations may be unclear or one-sided",
            .termination: "exit terms or penalties can materially affect your options",
            .ip: "ownership language may transfer rights beyond what you expect",
            .confidentiality: "scope or duration may be broader than typical",
            .disputes: "dispute process/location can reduce your practical leverage",
            .renewal: "auto-renewal can lock you in if notice is missed",
            .deposit: "deposit return/withholding terms may be ambiguous",
            .maintenance: "repair duties may be shifted in a way that increases your burden",
            .subletting: "restrictions can limit flexibility during the term",
            .scope: "scope ambiguity often leads to unpaid or disputed extra work",
            .indemnity: "liability-shifting can create outsized financial risk",
            .general: "wording appears broad enough to deserve clarification",
        ]
        let severity = riskLevel == "high" ? "Flagged high risk because" : (riskLevel == "medium" ? "Flagged medium risk because" : "Flagged for awareness because")
        let label = contractType.rawValue.replacingOccurrences(of: "_", with: " ")
        return "\(severity) \(themeReason[theme] ?? themeReason[.general]!) for this \(label) context."
    }

    private static func clauseConfidenceNote(_ clauseText: String) -> String {
        if clauseText.count < 80 {
            return "Short section detected; surrounding context may change interpretation."
        }
        if clauseText.range(of: #"[\[\]{}]{2,}|_{3,}|\.{5,}"#, options: .regularExpression) != nil {
            return "Formatting appears noisy in this section, which may reduce analysis accuracy."
        }
        return "Text quality in this section looks adequate for educational review."
    }

    private static func timeline(contractType: ContractType, clauses: [ClauseCard]) -> [TimelineItem] {
        let stageMap: [(String, String, [String])] = [
            ("before_signing", "Before signing", ["payment", "deposit", "scope", "ip"]),
            ("during_term", "During the term", ["confidentiality", "maintenance", "subletting"]),
            ("ending_or_dispute", "If things change or end", ["termination", "disputes", "renewal"]),
        ]
        var items: [TimelineItem] = []
        for (phase, title, keys) in stageMap {
            let selected = Array(clauses.filter { keys.contains($0.theme) }.prefix(3))
            guard !selected.isEmpty else { continue }
            let watch = Array(Set(selected.map { $0.theme.replacingOccurrences(of: "_", with: " ") })).joined(separator: ", ")
            items.append(
                TimelineItem(
                    phase: phase,
                    title: title,
                    when: phaseWhen(phase: phase, contractType: contractType),
                    watchFor: watch,
                    clauseIds: selected.map(\.id)
                )
            )
        }
        return items
    }

    private static func phaseWhen(phase: String, contractType: ContractType) -> String {
        if phase == "before_signing" { return "Verify these terms before committing." }
        if phase == "during_term" { return "Track these terms while the agreement is active." }
        if contractType == .lease { return "Critical around move-out, renewal notices, or disputes." }
        return "Critical when ending, renewing, or handling disputes."
    }
}
