import SwiftUI
import SwiftData

struct DashboardView: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.modelContext) private var modelContext
    @State private var savedToast = false
    @State private var showShare = false

    var body: some View {
        Group {
            if let result = model.currentResult {
                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        scoreHeader(result)

                        if let conf = result.analysisConfidence {
                            labeledCard(title: "Confidence · \(conf.level.capitalized)") {
                                Text(conf.summary).font(.subheadline)
                                if let ocr = result.ocrConfidence {
                                    Text("OCR quality ≈ \(Int(ocr * 100))%")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .padding(.top, 4)
                                }
                            }
                        }

                        labeledCard(title: "Category scores") {
                            CategoryBarsView(scores: result.categoryScores)
                        }

                        labeledCard(title: "Top issues") {
                            if result.topIssues.isEmpty {
                                Text("No high/medium issues flagged. Still read the full agreement.")
                                    .font(.footnote)
                                    .foregroundStyle(.secondary)
                            } else {
                                ForEach(result.topIssues) { issue in
                                    Button {
                                        model.path.append(.clause(issue.id))
                                    } label: {
                                        VStack(alignment: .leading, spacing: 4) {
                                            HStack {
                                                Text(issue.theme.replacingOccurrences(of: "_", with: " ").capitalized)
                                                    .font(.headline)
                                                Spacer()
                                                Text(issue.riskLevel.uppercased())
                                                    .font(.caption.weight(.bold))
                                                    .foregroundStyle(riskColor(issue.riskLevel))
                                            }
                                            Text(issue.plainEnglish)
                                                .font(.footnote)
                                                .foregroundStyle(.secondary)
                                                .lineLimit(3)
                                        }
                                        .padding(.vertical, 4)
                                    }
                                    .buttonStyle(.plain)
                                    Divider()
                                }
                            }
                        }

                        if !result.missingProtections.isEmpty {
                            labeledCard(title: "Missing protections") {
                                ForEach(result.missingProtections) { m in
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(m.label.capitalized).font(.subheadline.weight(.semibold))
                                        Text(m.detail).font(.footnote).foregroundStyle(.secondary)
                                    }
                                    .padding(.vertical, 2)
                                }
                            }
                        }

                        if !result.timeline.isEmpty {
                            labeledCard(title: "When to watch") {
                                ForEach(result.timeline) { item in
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(item.title).font(.subheadline.weight(.semibold))
                                        Text(item.when).font(.caption).foregroundStyle(.secondary)
                                        Text("Watch: \(item.watchFor)").font(.caption2).foregroundStyle(.secondary)
                                    }
                                    .padding(.vertical, 2)
                                }
                            }
                        }

                        if let journey = result.studentJourney {
                            labeledCard(title: journey.title) {
                                ForEach(journey.checklist, id: \.self) { item in
                                    Text("• \(item)").font(.footnote)
                                }
                            }
                        }

                        if let next = result.nextSteps, !next.ifThenNudges.isEmpty {
                            labeledCard(title: "If / then") {
                                ForEach(Array(next.ifThenNudges.enumerated()), id: \.offset) { _, nudge in
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("If \(nudge.ifCondition)")
                                            .font(.subheadline.weight(.semibold))
                                        Text("Then \(nudge.thenAction)")
                                            .font(.footnote)
                                            .foregroundStyle(.secondary)
                                    }
                                    .padding(.vertical, 2)
                                }
                            }
                        }

                        VStack(spacing: 10) {
                            Button {
                                model.path.append(.questions)
                            } label: {
                                Label("Questions & email draft", systemImage: "questionmark.bubble")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(CoParseColors.navy)

                            Button {
                                model.path.append(.allClauses)
                            } label: {
                                Label("Browse all clauses", systemImage: "list.bullet.rectangle")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)

                            HStack {
                                Button {
                                    save(result)
                                } label: {
                                    Label("Save", systemImage: "bookmark")
                                        .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.bordered)

                                Button {
                                    showShare = true
                                } label: {
                                    Label("Share report", systemImage: "square.and.arrow.up")
                                        .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.bordered)
                            }
                        }

                        Text("Educational information only — not legal advice. Verify against the original document.")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                            .padding(.top, 4)
                    }
                    .padding(20)
                }
                .onAppear {
                    if model.pendingAutoSave {
                        save(result, silent: true)
                        model.pendingAutoSave = false
                    }
                }
            } else {
                ContentUnavailableView("No analysis", systemImage: "doc.text.magnifyingglass")
            }
        }
        .navigationTitle("Report")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Done") { model.goHome() }
            }
        }
        .alert("Saved on this device", isPresented: $savedToast) {
            Button("OK", role: .cancel) {}
        }
        .sheet(isPresented: $showShare) {
            if let result = model.currentResult {
                ShareSheet(items: ReportExporter.shareItems(from: result))
            }
        }
    }

    private func scoreHeader(_ result: AnalysisResult) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(result.title).font(.title2.weight(.semibold))
            Text("\(result.contractType.replacingOccurrences(of: "_", with: " ")) · \(result.role.replacingOccurrences(of: "_", with: " "))")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text("\(result.overallScore)")
                    .font(.system(size: 48, weight: .bold, design: .rounded))
                    .foregroundStyle(CoParseColors.navy)
                VStack(alignment: .leading, spacing: 2) {
                    Text("/ 100").font(.caption).foregroundStyle(.secondary)
                    Text(result.signatureReadiness.recommendationText)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            ForEach(result.limitations.prefix(3), id: \.self) { lim in
                Text("• \(lim)").font(.caption2).foregroundStyle(.secondary)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CoParseColors.navy.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private func labeledCard<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(.headline)
            content()
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private func save(_ result: AnalysisResult, silent: Bool = false) {
        do {
            let entity = try SavedAnalysis(from: result)
            modelContext.insert(entity)
            try modelContext.save()
            if !silent { savedToast = true }
        } catch {
            model.errorMessage = error.localizedDescription
        }
    }
}
