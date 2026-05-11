import SwiftUI

struct OnboardingView: View {
    @EnvironmentObject private var model: AppModel
    @State private var page = 0

    private let pages: [(String, String, String)] = [
        ("doc.text.viewfinder", "Scan on the spot", "Use Document Scan for paper leases, offers, or freelance contracts — page by page, with reorder before you analyze."),
        ("lock.shield", "Private by design", "OCR and risk analysis run on your iPhone. The free path never uploads your contract."),
        ("chart.bar.doc.horizontal", "Clear reports", "Get a score, category breakdown, timeline, and flagged clauses you can open in detail."),
        ("questionmark.bubble", "Ask better questions", "Copy-ready questions and an email draft help you clarify terms before you sign."),
    ]

    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $page) {
                ForEach(Array(pages.enumerated()), id: \.offset) { index, item in
                    VStack(spacing: 20) {
                        Spacer()
                        Image(systemName: item.0)
                            .font(.system(size: 56, weight: .light))
                            .foregroundStyle(CoParseColors.navy)
                        Text(item.1)
                            .font(CoParseType.sectionFont)
                            .multilineTextAlignment(.center)
                        Text(item.2)
                            .font(.body)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 28)
                        Spacer()
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))

            Button {
                if page < pages.count - 1 {
                    withAnimation { page += 1 }
                } else {
                    model.completeOnboarding()
                }
            } label: {
                Text(page < pages.count - 1 ? "Next" : "Get started")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(CoParseColors.navy)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
            .padding(24)
        }
        .background(Color(.systemBackground))
    }
}
