import SwiftUI
import SwiftData

struct SavedView: View {
    @EnvironmentObject private var model: AppModel
    @Query(sort: \SavedAnalysis.createdAt, order: .reverse) private var saved: [SavedAnalysis]
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        List {
            if saved.isEmpty {
                ContentUnavailableView(
                    "No saved analyses",
                    systemImage: "bookmark",
                    description: Text("Save from the dashboard to reopen offline.")
                )
            } else {
                ForEach(saved) { item in
                    Button {
                        if let decoded = try? item.decode() {
                            model.currentResult = decoded
                            model.pendingAutoSave = false
                            model.path.append(.dashboard)
                        }
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title).font(.headline)
                            Text("\(item.contractType.replacingOccurrences(of: "_", with: " ")) · score \(item.overallScore)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text(item.createdAt.formatted(date: .abbreviated, time: .shortened))
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .onDelete { indexSet in
                    for i in indexSet {
                        modelContext.delete(saved[i])
                    }
                }
            }
        }
        .navigationTitle("Saved")
    }
}
