import SwiftUI

struct ClauseDetailView: View {
    @EnvironmentObject private var model: AppModel
    let clauseId: String

    var body: some View {
        Group {
            if let clause = model.currentResult?.clauses.first(where: { $0.id == clauseId }) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            Text(clause.theme.replacingOccurrences(of: "_", with: " ").capitalized)
                                .font(.title2.weight(.semibold))
                            Spacer()
                            Text(clause.riskLevel.uppercased())
                                .font(.caption.weight(.bold))
                                .foregroundStyle(riskColor(clause.riskLevel))
                        }
                        Text(clause.flagReason).font(.subheadline).foregroundStyle(.secondary)
                        Text("Plain English").font(.headline)
                        Text(clause.plainEnglish)
                        Text("Compare").font(.headline)
                        Text(clause.compareNote)
                        Text("Ask").font(.headline)
                        Text(clause.suggestedQuestionNeutral)
                        Text(clause.confidenceNote).font(.caption).foregroundStyle(.secondary)
                        Divider()
                        Text("Original excerpt").font(.headline)
                        Text(clause.text).font(.footnote).foregroundStyle(.secondary)
                    }
                    .padding(20)
                }
            } else {
                ContentUnavailableView("Clause not found", systemImage: "doc")
            }
        }
        .navigationTitle("Clause")
    }
}
