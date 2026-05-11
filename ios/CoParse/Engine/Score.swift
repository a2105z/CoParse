import Foundation

enum AnalysisScore {
    static func compute(
        contractType: ContractType,
        clauses: [ClauseCard],
        missing: [MissingProtection]
    ) -> (overall: Int, categoryScores: [String: Int], recommendation: String) {
        _ = contractType
        var base = 85
        var cat = ["money": 80, "termination": 80, "ip_privacy": 80, "disputes": 80, "flexibility": 80]

        for c in clauses {
            let penalty = c.riskLevel == "high" ? 6 : (c.riskLevel == "medium" ? 3 : 0)
            base -= penalty
            switch c.theme {
            case "payment", "deposit":
                cat["money", default: 80] -= penalty
            case "termination", "renewal":
                cat["termination", default: 80] -= penalty
                cat["flexibility", default: 80] -= penalty / 2
            case "ip", "confidentiality":
                cat["ip_privacy", default: 80] -= penalty
            case "disputes":
                cat["disputes", default: 80] -= penalty
            default:
                break
            }
        }

        for _ in missing {
            base -= 4
            cat["flexibility", default: 80] -= 3
        }

        for key in cat.keys {
            cat[key] = max(35, min(100, cat[key] ?? 80))
        }

        let overall = max(25, min(100, base))
        let rec: String
        if overall >= 75 { rec = "mostly_standard" }
        else if overall >= 55 { rec = "worth_clarifying" }
        else if overall >= 40 { rec = "caution" }
        else { rec = "strongly_consider_review" }

        return (overall, cat, rec)
    }
}
