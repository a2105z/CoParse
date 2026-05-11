import SwiftUI

struct CategoryBarsView: View {
    let scores: [String: Int]

    private var ordered: [(String, Int)] {
        let labels = ["money", "termination", "ip_privacy", "disputes", "flexibility"]
        return labels.compactMap { key in
            guard let v = scores[key] else { return nil }
            return (key.replacingOccurrences(of: "_", with: " ").capitalized, v)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(ordered, id: \.0) { label, value in
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(label).font(.caption)
                        Spacer()
                        Text("\(value)").font(.caption.weight(.semibold))
                    }
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule().fill(Color(.tertiarySystemFill))
                            Capsule()
                                .fill(CoParseColors.navy.opacity(0.85))
                                .frame(width: geo.size.width * CGFloat(value) / 100.0)
                        }
                    }
                    .frame(height: 8)
                }
            }
        }
    }
}
