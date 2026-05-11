import Foundation
import UIKit
import Vision

enum OCRService {
    static func recognizeText(
        in images: [UIImage],
        preprocess: Bool = true,
        progress: ((Double) -> Void)? = nil
    ) async throws -> (text: String, meanConfidence: Double) {
        guard !images.isEmpty else {
            return ("", 0)
        }
        let prepared = preprocess ? ImagePreprocessor.prepareAll(images) : images
        var pages: [String] = []
        var confidences: [Double] = []
        for (index, image) in prepared.enumerated() {
            let page = try await recognizePage(image)
            if !page.text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                pages.append("--- Page \(index + 1) ---\n\(page.text)")
            }
            confidences.append(page.confidence)
            progress?(Double(index + 1) / Double(prepared.count))
        }
        let mean = confidences.isEmpty ? 0 : confidences.reduce(0, +) / Double(confidences.count)
        return (pages.joined(separator: "\n\n"), mean)
    }

    private static func recognizePage(_ image: UIImage) async throws -> (text: String, confidence: Double) {
        guard let cgImage = image.cgImage else {
            throw NSError(domain: "OCRService", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not read image."])
        }
        return try await withCheckedThrowingContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                let observations = (request.results as? [VNRecognizedTextObservation]) ?? []
                let sorted = observations.sorted {
                    let a = $0.boundingBox.origin.y
                    let b = $1.boundingBox.origin.y
                    if abs(a - b) > 0.02 { return a > b }
                    return $0.boundingBox.origin.x < $1.boundingBox.origin.x
                }
                var lines: [String] = []
                var confSum = 0.0
                for obs in sorted {
                    guard let top = obs.topCandidates(1).first else { continue }
                    lines.append(top.string)
                    confSum += Double(top.confidence)
                }
                let mean = sorted.isEmpty ? 0 : confSum / Double(sorted.count)
                continuation.resume(returning: (lines.joined(separator: "\n"), mean))
            }
            request.recognitionLevel = .accurate
            request.usesLanguageCorrection = true
            if #available(iOS 16.0, *) {
                request.automaticallyDetectsLanguage = true
            }
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }
}
