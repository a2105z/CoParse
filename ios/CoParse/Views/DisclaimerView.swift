import SwiftUI

struct DisclaimerView: View {
    @EnvironmentObject private var model: AppModel
    @State private var showPrivacy = false

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [CoParseColors.navy, CoParseColors.navyMid],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 20) {
                Text("CoParse")
                    .font(CoParseType.titleFont)
                    .foregroundStyle(.white)
                Text("Educational contract review — not legal advice.")
                    .font(.title3.weight(.medium))
                    .foregroundStyle(.white.opacity(0.95))
                Text("CoParse highlights clauses that may deserve attention and suggests questions to ask. It can miss or misread text (especially from photos), and it never tells you to sign or not sign.\n\nVerify important terms against the original document. Seek a lawyer, legal aid, or campus legal clinic when stakes are high.")
                    .foregroundStyle(.white.opacity(0.85))
                    .fixedSize(horizontal: false, vertical: true)
                Spacer()
                Button {
                    model.acceptDisclaimer()
                } label: {
                    Text("I understand — continue")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.white)
                        .foregroundStyle(CoParseColors.navy)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                Button {
                    showPrivacy = true
                } label: {
                    Text("Privacy details")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.white.opacity(0.9))
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(28)
        }
        .sheet(isPresented: $showPrivacy) {
            NavigationStack {
                PrivacyView()
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Close") { showPrivacy = false }
                        }
                    }
            }
        }
    }
}
