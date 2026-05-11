import Foundation

enum ContractType: String, CaseIterable, Identifiable {
    case lease
    case internshipOffer = "internship_offer"
    case freelance
    case unknown

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .lease: return "Lease"
        case .internshipOffer: return "Internship / offer"
        case .freelance: return "Freelance"
        case .unknown: return "Unknown"
        }
    }

    var defaultRole: String {
        switch self {
        case .lease: return "renter"
        case .internshipOffer: return "student_intern"
        case .freelance: return "freelancer"
        case .unknown: return "renter"
        }
    }
}

enum ClauseTheme: String {
    case payment, termination, ip, confidentiality, disputes, renewal
    case deposit, maintenance, subletting, scope, indemnity, general
}

enum AnalysisClassify {
    static let typeKeywords: [ContractType: [String]] = [
        .lease: [
            "landlord", "tenant", "rent", "security deposit", "premises", "lease term",
            "lessee", "lessor", "apartment", "dwelling", "move-in", "occupancy",
        ],
        .internshipOffer: [
            "intern", "internship", "at-will", "employment", "offer letter", "wage", "salary",
            "stipend", "supervisor", "orientation", "benefits", "exempt", "non-exempt",
        ],
        .freelance: [
            "independent contractor", "sow", "statement of work", "milestone", "invoice", "services",
            "deliverable", "client", "contractor", "retainer", "change order", "work for hire",
        ],
        .unknown: [],
    ]

    static let themePatterns: [(ClauseTheme, NSRegularExpression)] = {
        let specs: [(ClauseTheme, String)] = [
            (.payment, #"payment|compensation|invoice|fee|rent|deposit|salary|wage|stipend|late fee|net\s*\d+"#),
            (.termination, #"terminat|cancel|breach|notice period|at-will|severance|resignation"#),
            (.ip, #"intellectual property|invention|work product|assignment|derivative|work for hire|work-for-hire"#),
            (.confidentiality, #"confidential|non-disclosure|proprietary|trade secret|nda\b"#),
            (.disputes, #"arbitrat|mediation|jurisdiction|governing law|class action|venue|attorney.?s fees"#),
            (.renewal, #"renew|automatically renew|month-to-month|extension|evergreen"#),
            (.deposit, #"security deposit|damage deposit|withhold|deposit return"#),
            (.maintenance, #"repair|maintain|habitable|landlord|tenant obligation|utilities"#),
            (.subletting, #"sublet|assign|transfer|guest|roommate"#),
            (.scope, #"scope of services|deliverable|change order|milestone|revision"#),
            (.indemnity, #"indemnif|hold harmless|liability cap|limitation of liability|consequential damages"#),
        ]
        return specs.compactMap { theme, pattern in
            guard let re = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else { return nil }
            return (theme, re)
        }
    }()

    static func detectContractType(text: String, hint: String?) -> ContractType {
        if let hint, let typed = ContractType(rawValue: hint), typed != .unknown {
            return typed
        }
        if hint == nil || hint == "auto" || hint == "unknown" {
            let low = text.lowercased()
            var scores: [ContractType: Int] = [.lease: 0, .internshipOffer: 0, .freelance: 0]
            for (ctype, kws) in typeKeywords where ctype != .unknown {
                scores[ctype] = kws.reduce(0) { $0 + (low.contains($1) ? 1 : 0) }
            }
            let best = scores.max(by: { $0.value < $1.value })
            if let best, best.value >= 2 { return best.key }
            return .unknown
        }
        return .unknown
    }

    static func classifyTheme(_ clauseText: String) -> ClauseTheme {
        let range = NSRange(clauseText.startIndex..<clauseText.endIndex, in: clauseText)
        for (theme, re) in themePatterns {
            if re.firstMatch(in: clauseText, options: [], range: range) != nil {
                return theme
            }
        }
        return .general
    }

    static func riskLevel(theme: ClauseTheme, clauseText: String, contractType: ContractType) -> String {
        let low = clauseText.lowercased()
        let aggressive = [
            "sole discretion", "unlimited", "irrevocable", "perpetual", "automatic renewal",
            "binding arbitration", "exclusive", "assigns all", "waive all", "without notice",
            "liquidated damages", "non-compete", "noncompete", "injunctive relief",
        ].contains { low.contains($0) }
        let leaseRed = contractType == .lease && [
            "no pets", "tenant responsible for all repairs", "as-is", "landlord may enter at any time",
        ].contains { low.contains($0) }
        let vague = (["reasonable", "as determined", "may", "sole"].contains { low.contains($0) }) && clauseText.count > 200

        if aggressive || leaseRed { return "high" }
        if vague || theme == .ip || theme == .indemnity || theme == .disputes { return "medium" }
        if theme == .payment || theme == .termination || theme == .deposit { return "medium" }
        return "low"
    }
}
