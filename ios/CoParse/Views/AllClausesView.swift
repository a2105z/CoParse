import SwiftUI

struct AllClausesView: View {
    @EnvironmentObject private var model: AppModel
    @State private var filter = "all"

    private var clauses: [ClauseCard] {
        let all = model.currentResult?.clauses ?? []
        switch filter {
        case "high": return all.filter { $0.riskLevel == "high" }
        case "medium": return all.filter { $0.riskLevel == "medium" }
        default: return all
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("Filter", selection: $filter) {
                Text("All").tag("all")
                Text("High").tag("high")
                Text("Medium").tag("medium")
            }
            .pickerStyle(.segmented)
            .padding()

            List(clauses) { clause in
                Button {
                    model.path.append(.clause(clause.id))
                } label: {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text(clause.theme.replacingOccurrences(of: "_", with: " ").capitalized)
                                .font(.headline)
                            Spacer()
                            Text(clause.riskLevel.uppercased())
                                .font(.caption2.weight(.bold))
                                .foregroundStyle(riskColor(clause.riskLevel))
                        }
                        Text(clause.plainEnglish)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .lineLimit(3)
                    }
                    .padding(.vertical, 4)
                }
                .buttonStyle(.plain)
            }
        }
        .navigationTitle("All clauses")
    }
}
