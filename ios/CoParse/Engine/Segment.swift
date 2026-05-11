import Foundation

enum AnalysisSegment {
    static func segmentClauses(_ text: String, maxClauses: Int = 80) -> [String] {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return [] }

        let pattern = #"\n(?=\s*(?:Section\s+\d+|[\d]+\.|(?:[A-Z][A-Z\s]{8,}))\s*\n)"#
        var chunks: [String] = []
        if let re = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) {
            let ns = trimmed as NSString
            let matches = re.matches(in: trimmed, options: [], range: NSRange(location: 0, length: ns.length))
            var last = 0
            for m in matches {
                let part = ns.substring(with: NSRange(location: last, length: m.range.location - last))
                    .trimmingCharacters(in: .whitespacesAndNewlines)
                if part.count > 40 { chunks.append(part) }
                last = m.range.location
            }
            let tail = ns.substring(from: last).trimmingCharacters(in: .whitespacesAndNewlines)
            if tail.count > 40 { chunks.append(tail) }
        }

        if chunks.count < 2 {
            chunks = trimmed
                .components(separatedBy: "\n\n")
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                .filter { $0.count > 60 }
        }

        if chunks.count < 2 {
            var parts: [String] = []
            var i = trimmed.startIndex
            while i < trimmed.endIndex {
                let end = trimmed.index(i, offsetBy: 2000, limitedBy: trimmed.endIndex) ?? trimmed.endIndex
                parts.append(String(trimmed[i..<end]))
                if end == trimmed.endIndex { break }
                i = trimmed.index(i, offsetBy: 1800, limitedBy: trimmed.endIndex) ?? trimmed.endIndex
            }
            chunks = parts
        }

        return Array(chunks.prefix(maxClauses))
    }
}
