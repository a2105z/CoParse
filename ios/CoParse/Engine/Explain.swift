import Foundation

enum AnalysisExplain {
    static func buildClauseCard(
        clauseText: String,
        theme: ClauseTheme,
        riskLevel: String,
        contractType: ContractType,
        role: String
    ) -> (plainEnglish: String, compareNote: String, question: String, polite: String, negotiability: String) {
        _ = role
        let meaning = templateMeaning(theme: theme, clauseText: clauseText)
        let compare = templateCompare(riskLevel: riskLevel, contractType: contractType)
        let question = templateQuestion(theme: theme)
        return (
            meaning,
            compare,
            question,
            "Could you help me understand this section: \(question)",
            riskLevel == "high" ? "often_worth_asking" : "sometimes_negotiable"
        )
    }

    private static func templateMeaning(theme: ClauseTheme, clauseText: String) -> String {
        let snippet = clauseText.count > 320 ? String(clauseText.prefix(320)) + "…" : clauseText
        let guides: [ClauseTheme: String] = [
            .payment: "This language describes money flow—who pays whom, when, and what triggers payment.",
            .termination: "This describes how the agreement can end and what happens afterward.",
            .ip: "This may affect who owns work product or inventions related to the relationship.",
            .confidentiality: "This limits what you can share about the other party's information.",
            .disputes: "This describes how disagreements are resolved (courts vs arbitration, location, etc.).",
            .deposit: "This concerns money held up front and when it may be returned or withheld.",
            .maintenance: "This allocates repair and upkeep responsibilities.",
            .subletting: "This limits whether you can sublet, assign, or have guests long-term.",
            .scope: "This defines what work is included and how changes are handled.",
            .indemnity: "This can shift legal/financial responsibility between parties.",
            .renewal: "This covers how and when the agreement may renew or extend.",
            .general: "This section sets expectations or obligations between the parties.",
        ]
        return "\(guides[theme] ?? guides[.general]!)\n\nExcerpt: \(snippet)"
    }

    private static func templateCompare(riskLevel: String, contractType: ContractType) -> String {
        let label = contractType.rawValue.replacingOccurrences(of: "_", with: " ")
        if riskLevel == "high" {
            return "Compared to many \(label) templates, this language looks more restrictive or one-sided—worth clarifying."
        }
        if riskLevel == "medium" {
            return "This is not uncommon, but the wording may leave room for disagreement—confirm details that matter to you."
        }
        return "This looks broadly in line with typical agreements, but read the specifics against your situation."
    }

    private static func templateQuestion(theme: ClauseTheme) -> String {
        let q: [ClauseTheme: String] = [
            .payment: "What is the exact payment schedule, and what happens if payment is late?",
            .termination: "How much notice is required to end this agreement, and are there penalties?",
            .ip: "Does this apply to projects I create on my own time without company resources?",
            .confidentiality: "How long do confidentiality obligations last after the relationship ends?",
            .disputes: "Can we use mediation before binding arbitration, and where would disputes be heard?",
            .deposit: "Under what conditions can the deposit be withheld, and when is it returned?",
            .maintenance: "Who is responsible for specific repairs, and what is the timeline for fixing issues?",
            .subletting: "What is the process and any fees for subletting or assigning the agreement?",
            .scope: "How are scope changes approved and billed?",
            .indemnity: "Can we cap liability or narrow indemnity to direct damages?",
            .renewal: "What notice is required to stop automatic renewal?",
            .general: "Can you walk me through how this section applies day-to-day?",
        ]
        return q[theme] ?? q[.general]!
    }
}
