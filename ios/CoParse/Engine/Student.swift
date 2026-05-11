import Foundation

enum AnalysisStudent {
    static func buildStudentJourney(contractType: ContractType, role: String) -> StudentJourney {
        let packs: [ContractType: (String, [String], [String])] = [
            .lease: (
                "First apartment checklist",
                [
                    "Confirm exact total move-in cost (rent + deposit + fees) in writing.",
                    "Ask how repair requests are submitted and expected response time.",
                    "Verify early termination and renewal notice windows.",
                    "Save photos + move-in condition report on day one.",
                ],
                [
                    "Will this lease overlap with semester breaks or relocation plans?",
                    "Are utilities, internet, and renter's insurance requirements clear?",
                ]
            ),
            .internshipOffer: (
                "First internship checklist",
                [
                    "Confirm pay, schedule, and overtime expectations in writing.",
                    "Review IP/confidentiality terms before signing projects.",
                    "Check termination and notice language so expectations are clear.",
                    "Save the final offer and policy docs in one folder.",
                ],
                [
                    "Could these terms affect your school schedule, visa, or aid obligations?",
                    "Do you need career services or legal clinic review before signing?",
                ]
            ),
            .freelance: (
                "First client contract checklist",
                [
                    "Define deliverables, revisions, and approvals clearly.",
                    "Lock payment schedule and late-payment consequences in writing.",
                    "Clarify who owns work drafts vs final deliverables.",
                    "Confirm how either side can pause or end work.",
                ],
                [
                    "Will this timeline conflict with exams or class deadlines?",
                    "Are taxes, tools, and out-of-pocket expenses accounted for?",
                ]
            ),
        ]
        let selected = packs[contractType] ?? packs[.lease]!
        return StudentJourney(
            title: selected.0,
            role: role,
            checklist: selected.1,
            verificationPrompts: selected.2
        )
    }

    static func buildNextSteps(
        contractType: ContractType,
        topIssues: [ClauseCard],
        missing: [MissingProtection]
    ) -> NextSteps {
        var nudges: [IfThenNudge] = []
        for issue in topIssues.prefix(3) {
            let theme = issue.theme.replacingOccurrences(of: "_", with: " ")
            let question = issue.suggestedQuestionNeutral.isEmpty
                ? "Could we clarify this section in writing?"
                : issue.suggestedQuestionNeutral
            nudges.append(
                IfThenNudge(
                    ifCondition: "If the \(theme) clause stays vague after discussion",
                    thenAction: "Ask: \(question)"
                )
            )
        }
        for mp in missing.prefix(2) {
            nudges.append(
                IfThenNudge(
                    ifCondition: "If there is no clear language about \(mp.label)",
                    thenAction: "Request a short written addendum covering \(mp.label) before signing."
                )
            )
        }

        return NextSteps(
            ifThenNudges: nudges,
            emailTemplates: emailTemplates(contractType),
            escalationResources: escalationResources(contractType),
            privacyNote: "CoParse does not replace legal advice. Keep personal/legal details minimal when sharing excerpts."
        )
    }

    private static func emailTemplates(_ contractType: ContractType) -> [EmailTemplate] {
        let subject: String
        switch contractType {
        case .lease: subject = "Questions before signing the lease"
        case .internshipOffer: subject = "Questions before I accept the offer"
        case .freelance: subject = "Quick contract clarifications"
        case .unknown: subject = "Questions before signing"
        }
        let body = """
        Hi,

        Thank you for sharing the agreement. I reviewed it and I am excited to move forward. Before signing, could we clarify a few points in writing:
        1) [Insert payment/scope/termination question]
        2) [Insert timeline or notice question]
        3) [Insert missing protection question]

        I appreciate your help and can sign once these are confirmed.

        Best,
        [Your Name]
        """
        return [EmailTemplate(title: "Polite clarification email", subject: subject, body: body)]
    }

    private static func escalationResources(_ contractType: ContractType) -> [EscalationResource] {
        var base = [
            EscalationResource(label: "Campus legal clinic or student legal services", why: "Best first stop for student-specific contract questions."),
            EscalationResource(label: "Local legal aid organization", why: "Useful when terms are high-stakes or urgent."),
        ]
        if contractType == .lease {
            base.append(EscalationResource(label: "Local tenant union or housing hotline", why: "Helps with lease-specific rights and repair disputes."))
        }
        return base
    }
}
