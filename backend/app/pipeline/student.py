"""Student-focused guidance packs for checklist, actions, and escalation."""

from __future__ import annotations

from typing import Any


def build_student_journey(contract_type: str, role: str) -> dict[str, Any]:
    packs = {
        "lease": {
            "title": "First apartment checklist",
            "checklist": [
                "Confirm exact total move-in cost (rent + deposit + fees) in writing.",
                "Ask how repair requests are submitted and expected response time.",
                "Verify early termination and renewal notice windows.",
                "Save photos + move-in condition report on day one.",
            ],
            "verification_prompts": [
                "Will this lease overlap with semester breaks or relocation plans?",
                "Are utilities, internet, and renter's insurance requirements clear?",
            ],
        },
        "internship_offer": {
            "title": "First internship checklist",
            "checklist": [
                "Confirm pay, schedule, and overtime expectations in writing.",
                "Review IP/confidentiality terms before signing projects.",
                "Check termination and notice language so expectations are clear.",
                "Save the final offer and policy docs in one folder.",
            ],
            "verification_prompts": [
                "Could these terms affect your school schedule, visa, or aid obligations?",
                "Do you need career services or legal clinic review before signing?",
            ],
        },
        "freelance": {
            "title": "First client contract checklist",
            "checklist": [
                "Define deliverables, revisions, and approvals clearly.",
                "Lock payment schedule and late-payment consequences in writing.",
                "Clarify who owns work drafts vs final deliverables.",
                "Confirm how either side can pause or end work.",
            ],
            "verification_prompts": [
                "Will this timeline conflict with exams or class deadlines?",
                "Are taxes, tools, and out-of-pocket expenses accounted for?",
            ],
        },
    }
    selected = packs.get(contract_type, packs["lease"])
    return {
        "title": selected["title"],
        "role": role,
        "checklist": selected["checklist"],
        "verification_prompts": selected["verification_prompts"],
    }


def build_next_steps(contract_type: str, top_issues: list[dict[str, Any]], missing: list[dict[str, Any]]) -> dict[str, Any]:
    nudges = []
    for issue in top_issues[:3]:
        theme = issue.get("theme", "general").replace("_", " ")
        question = issue.get("suggested_question_neutral") or "Could we clarify this section in writing?"
        nudges.append(
            {
                "if": f"If the {theme} clause stays vague after discussion",
                "then": f"Ask: {question}",
            }
        )

    for mp in missing[:2]:
        label = mp.get("label", "this topic")
        nudges.append(
            {
                "if": f"If there is no clear language about {label}",
                "then": f"Request a short written addendum covering {label} before signing.",
            }
        )

    return {
        "if_then_nudges": nudges,
        "email_templates": _email_templates(contract_type),
        "escalation_resources": _escalation_resources(contract_type),
        "privacy_note": "CoParse does not replace legal advice. Keep personal/legal details minimal when sharing excerpts.",
    }


def _email_templates(contract_type: str) -> list[dict[str, str]]:
    subject = {
        "lease": "Questions before signing the lease",
        "internship_offer": "Questions before I accept the offer",
        "freelance": "Quick contract clarifications",
    }.get(contract_type, "Questions before signing")

    body = (
        "Hi,\n\n"
        "Thank you for sharing the agreement. I reviewed it and I am excited to move forward. "
        "Before signing, could we clarify a few points in writing:\n"
        "1) [Insert payment/scope/termination question]\n"
        "2) [Insert timeline or notice question]\n"
        "3) [Insert missing protection question]\n\n"
        "I appreciate your help and can sign once these are confirmed.\n\n"
        "Best,\n[Your Name]"
    )
    return [{"title": "Polite clarification email", "subject": subject, "body": body}]


def _escalation_resources(contract_type: str) -> list[dict[str, str]]:
    base = [
        {
            "label": "Campus legal clinic or student legal services",
            "why": "Best first stop for student-specific contract questions.",
        },
        {
            "label": "Local legal aid organization",
            "why": "Useful when terms are high-stakes or urgent.",
        },
    ]
    if contract_type == "lease":
        base.append({"label": "Local tenant union or housing hotline", "why": "Helps with lease-specific rights and repair disputes."})
    return base
