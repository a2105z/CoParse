import SwiftUI

struct PrivacyView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Privacy")
                    .font(CoParseType.sectionFont)
                Text("CoParse is built so the default product path stays free and private.")
                    .foregroundStyle(.secondary)

                group("What stays on your device") {
                    bullet("Camera photos and PDF imports used for scanning")
                    bullet("Vision OCR text extraction")
                    bullet("Risk scoring and explanations (heuristic engine)")
                    bullet("Saved reports in on-device storage (SwiftData)")
                }

                group("What we don’t do on the free path") {
                    bullet("No account or sign-in required")
                    bullet("No upload of contract images/text to CoParse servers for analysis")
                    bullet("No paid cloud OCR or LLM calls")
                    bullet("No ads in this release")
                }

                group("Optional backend") {
                    Text("An optional FastAPI backend exists in the open-source repo for demos. The shipping free scan flow does not require it.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }

                group("Your controls") {
                    bullet("Delete saved reports anytime in Saved (swipe to delete)")
                    bullet("Revoke Camera / Photos access in iOS Settings")
                }
            }
            .padding(20)
        }
        .navigationTitle("Privacy")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func group(_ title: String, @ViewBuilder content: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.headline)
            content()
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private func bullet(_ text: String) -> some View {
        Text("• \(text)").font(.subheadline)
    }
}
