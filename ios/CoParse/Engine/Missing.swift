import Foundation

enum AnalysisMissing {
    private static let leaseChecks: [(String, [String])] = [
        ("subletting", ["sublet", "assignment", "transfer of lease"]),
        ("early termination", ["early termination", "break lease", "terminate prior"]),
        ("repairs", ["repair", "maintenance", "habitable"]),
        ("security deposit", ["security deposit", "deposit"]),
        ("renewal", ["renew", "automatic renewal"]),
    ]

    private static let internshipChecks: [(String, [String])] = [
        ("compensation", ["compensation", "wage", "salary", "stipend", "pay"]),
        ("ip assignment", ["intellectual property", "invention", "work product"]),
        ("confidentiality", ["confidential", "non-disclosure"]),
        ("dispute resolution", ["arbitration", "mediation", "jurisdiction"]),
        ("duration", ["start date", "end date", "term"]),
    ]

    private static let freelanceChecks: [(String, [String])] = [
        ("payment timing", ["payment", "invoice", "net ", "due"]),
        ("scope / changes", ["scope", "change order", "additional work"]),
        ("ownership", ["work product", "deliverable", "intellectual property"]),
        ("termination", ["terminat", "cancel"]),
        ("late payment", ["late fee", "interest", "past due"]),
    ]

    static func findMissingProtections(contractType: ContractType, fullText: String) -> [MissingProtection] {
        let low = fullText.lowercased()
        let checks: [(String, [String])]
        switch contractType {
        case .lease: checks = leaseChecks
        case .internshipOffer: checks = internshipChecks
        case .freelance: checks = freelanceChecks
        case .unknown: checks = Array(leaseChecks.prefix(3))
        }

        var missing: [MissingProtection] = []
        for (label, keywords) in checks {
            if !keywords.contains(where: { low.contains($0) }) {
                missing.append(
                    MissingProtection(
                        id: label.replacingOccurrences(of: " ", with: "_"),
                        label: label,
                        detail: "No clear language about \(label) was detected; worth asking for written clarity."
                    )
                )
            }
        }
        return Array(missing.prefix(8))
    }
}
