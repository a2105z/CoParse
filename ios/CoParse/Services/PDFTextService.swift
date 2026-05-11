import Foundation
import PDFKit
import UIKit

enum PDFTextService {
    static func extractText(from url: URL) throws -> String {
        guard let doc = PDFDocument(url: url) else {
            throw NSError(domain: "PDFTextService", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not open PDF."])
        }
        var parts: [String] = []
        for i in 0..<doc.pageCount {
            guard let page = doc.page(at: i) else { continue }
            parts.append(page.string ?? "")
        }
        return parts.joined(separator: "\n\n").trimmingCharacters(in: .whitespacesAndNewlines)
    }

    /// Renders PDF pages to images when text extraction is empty (scanned PDF fallback for Vision OCR).
    static func renderPages(from url: URL, maxPages: Int = 20) -> [UIImage] {
        guard let doc = PDFDocument(url: url) else { return [] }
        var images: [UIImage] = []
        let count = min(doc.pageCount, maxPages)
        for i in 0..<count {
            guard let page = doc.page(at: i) else { continue }
            let bounds = page.bounds(for: .mediaBox)
            let scale: CGFloat = 2.0
            let size = CGSize(width: bounds.width * scale, height: bounds.height * scale)
            let renderer = UIGraphicsImageRenderer(size: size)
            let image = renderer.image { ctx in
                UIColor.white.setFill()
                ctx.fill(CGRect(origin: .zero, size: size))
                ctx.cgContext.translateBy(x: 0, y: size.height)
                ctx.cgContext.scaleBy(x: scale, y: -scale)
                page.draw(with: .mediaBox, to: ctx.cgContext)
            }
            images.append(image)
        }
        return images
    }
}
