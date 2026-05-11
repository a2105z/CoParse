import SwiftUI
import UIKit

struct QuestionsView: View {
    @EnvironmentObject private var model: AppModel
    @State private var copied = false

    var body: some View {
        List {
            if let result = model.currentResult {
                Section("Questions to ask") {
                    ForEach(result.questionsToAsk) { q in
                        VStack(alignment: .leading, spacing: 6) {
                            Text(q.context.replacingOccurrences(of: "_", with: " ").capitalized)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text(q.question)
                            Button("Copy") {
                                UIPasteboard.general.string = q.question
                                copied = true
                            }
                            .font(.caption.weight(.semibold))
                        }
                        .padding(.vertical, 4)
                    }
                }

                if let next = result.nextSteps {
                    Section("Email template") {
                        ForEach(next.emailTemplates) { t in
                            VStack(alignment: .leading, spacing: 8) {
                                Text(t.title).font(.headline)
                                Text(t.subject).font(.subheadline)
                                Text(t.body).font(.footnote)
                                Button("Copy email") {
                                    UIPasteboard.general.string = "Subject: \(t.subject)\n\n\(t.body)"
                                    copied = true
                                }
                            }
                        }
                    }
                    Section("If needed, escalate") {
                        ForEach(next.escalationResources) { r in
                            VStack(alignment: .leading) {
                                Text(r.label).font(.subheadline.weight(.semibold))
                                Text(r.why).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Questions")
        .alert("Copied", isPresented: $copied) {
            Button("OK", role: .cancel) {}
        }
    }
}
