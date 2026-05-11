import SwiftUI

struct ProcessingView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .stroke(CoParseColors.navy.opacity(0.15), lineWidth: 10)
                    .frame(width: 120, height: 120)
                Circle()
                    .trim(from: 0, to: model.progressValue)
                    .stroke(CoParseColors.navy, style: StrokeStyle(lineWidth: 10, lineCap: .round))
                    .frame(width: 120, height: 120)
                    .rotationEffect(.degrees(-90))
                    .animation(.easeInOut(duration: 0.25), value: model.progressValue)
                Text("\(Int(model.progressValue * 100))%")
                    .font(.title2.weight(.bold))
                    .foregroundStyle(CoParseColors.navy)
            }
            .padding(.top, 40)

            Text(model.progressLabel)
                .font(.headline)
                .multilineTextAlignment(.center)

            if let error = model.errorMessage {
                Text(error)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                Button("Back to scan") {
                    model.path.removeAll { route in
                        if case .processing = route { return true }
                        return false
                    }
                }
                .buttonStyle(.borderedProminent)
                .tint(CoParseColors.navy)
            } else {
                Text("OCR and scoring run on your iPhone. Your contract isn’t uploaded on the free path.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 28)
            }
            Spacer()
        }
        .padding(28)
        .navigationTitle("Analyzing")
        .navigationBarBackButtonHidden(model.errorMessage == nil && model.progressValue < 1)
    }
}
