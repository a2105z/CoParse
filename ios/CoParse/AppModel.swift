import SwiftUI
import UIKit

enum AppRoute: Hashable {
    case home
    case scan(hintType: String?, role: String?)
    case processing
    case confirm
    case dashboard
    case clause(String)
    case questions
    case allClauses
    case saved
    case settings
    case privacy
}

@MainActor
final class AppModel: ObservableObject {
    @Published var path: [AppRoute] = []
    @Published var hintType: String?
    @Published var hintRole: String?
    @Published var pendingImages: [UIImage] = []
    @Published var pendingPDFURL: URL?
    @Published var progressLabel = "Preparing…"
    @Published var progressValue = 0.0
    @Published var currentResult: AnalysisResult?
    @Published var errorMessage: String?
    @Published var lastOCRConfidence: Double?
    @Published var autoSaveEnabled = UserDefaults.standard.object(forKey: "coparse.autoSave") as? Bool ?? true
    @Published var disclaimerAccepted = UserDefaults.standard.bool(forKey: "coparse.disclaimerAccepted")
    @Published var onboardingDone = UserDefaults.standard.bool(forKey: "coparse.onboardingDone")
    /// Set when the current result was just produced by analysis (triggers one-shot auto-save).
    @Published var pendingAutoSave = false

    func acceptDisclaimer() {
        disclaimerAccepted = true
        UserDefaults.standard.set(true, forKey: "coparse.disclaimerAccepted")
    }

    func completeOnboarding() {
        onboardingDone = true
        UserDefaults.standard.set(true, forKey: "coparse.onboardingDone")
    }

    func setAutoSave(_ value: Bool) {
        autoSaveEnabled = value
        UserDefaults.standard.set(value, forKey: "coparse.autoSave")
    }

    func openScan(hintType: String?, role: String?) {
        self.hintType = hintType
        self.hintRole = role
        pendingImages = []
        pendingPDFURL = nil
        errorMessage = nil
        path.append(.scan(hintType: hintType, role: role))
    }

    func startAnalysis() {
        path.append(.processing)
        Task { await runAnalysis() }
    }

    func runAnalysis() async {
        errorMessage = nil
        lastOCRConfidence = nil
        pendingAutoSave = false
        progressValue = 0.05
        let fromScan = pendingPDFURL == nil
        progressLabel = fromScan ? "Enhancing pages & running OCR…" : "Extracting PDF text…"
        do {
            var ocrConfidence: Double?
            let text: String
            let source: AnalysisResult.AnalysisSource
            let title: String
            if let pdf = pendingPDFURL {
                var extracted = try PDFTextService.extractText(from: pdf)
                if extracted.count < 200 {
                    progressLabel = "Image-based PDF — running OCR…"
                    let images = PDFTextService.renderPages(from: pdf)
                    let ocr = try await OCRService.recognizeText(in: images) { [weak self] p in
                        Task { @MainActor in
                            self?.progressValue = 0.1 + p * 0.5
                        }
                    }
                    extracted = ocr.text
                    ocrConfidence = ocr.meanConfidence
                }
                text = extracted
                source = .pdf
                title = pdf.deletingPathExtension().lastPathComponent
            } else {
                let ocr = try await OCRService.recognizeText(in: pendingImages) { [weak self] p in
                    Task { @MainActor in
                        self?.progressValue = 0.1 + p * 0.55
                    }
                }
                text = ocr.text
                ocrConfidence = ocr.meanConfidence
                source = .scan
                title = "Scan \(Date.now.formatted(date: .abbreviated, time: .shortened))"
            }

            lastOCRConfidence = ocrConfidence

            guard text.trimmingCharacters(in: .whitespacesAndNewlines).count > 40 else {
                throw NSError(
                    domain: "CoParse",
                    code: 2,
                    userInfo: [NSLocalizedDescriptionKey: "Not enough readable text. Rescan in brighter light, flatten pages, or import a text PDF."]
                )
            }

            progressLabel = "Segmenting clauses & scoring risk…"
            progressValue = 0.78
            let role = hintRole ?? ContractType(rawValue: hintType ?? "")?.defaultRole ?? "renter"
            let result = AnalysisEngine.analyze(
                text: text,
                hintContractType: hintType,
                contractTypeOverride: hintType,
                role: role,
                source: source,
                title: title,
                ocrConfidence: ocrConfidence
            )
            progressValue = 1
            progressLabel = "Ready"
            currentResult = result
            pendingAutoSave = autoSaveEnabled
            UINotificationFeedbackGenerator().notificationOccurred(.success)

            let lowConfidence = result.analysisConfidence?.level == "low" || (ocrConfidence ?? 1) < 0.45
            let needsConfirm = hintType == nil || lowConfidence
            if needsConfirm {
                path.append(.confirm)
            } else {
                path.append(.dashboard)
            }
        } catch {
            errorMessage = error.localizedDescription
            progressLabel = "Couldn't finish"
            UINotificationFeedbackGenerator().notificationOccurred(.error)
        }
    }

    func applyConfirm(type: String, role: String) {
        guard let result = currentResult else { return }
        let text = result.extractedText.isEmpty
            ? result.clauses.map(\.text).joined(separator: "\n\n")
            : result.extractedText
        let rebuilt = AnalysisEngine.analyze(
            text: text,
            hintContractType: type,
            contractTypeOverride: type,
            role: role,
            source: result.source,
            title: result.title,
            ocrConfidence: result.ocrConfidence
        )
        currentResult = rebuilt
        pendingAutoSave = autoSaveEnabled
        path.append(.dashboard)
    }

    func goHome() {
        path.removeAll()
        pendingImages = []
        pendingPDFURL = nil
        errorMessage = nil
    }
}
