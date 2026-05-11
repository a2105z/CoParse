import SwiftUI

enum CoParseColors {
    static let navy = Color(red: 0.08, green: 0.16, blue: 0.32)
    static let navyMid = Color(red: 0.14, green: 0.28, blue: 0.48)
    static let accent = Color(red: 0.20, green: 0.45, blue: 0.62)
    static let riskHigh = Color(red: 0.75, green: 0.22, blue: 0.22)
    static let riskMedium = Color(red: 0.85, green: 0.55, blue: 0.15)
    static let riskLow = Color(red: 0.25, green: 0.55, blue: 0.35)

    static func risk(_ level: String) -> Color {
        switch level {
        case "high": return riskHigh
        case "medium": return riskMedium
        default: return riskLow
        }
    }
}

func riskColor(_ level: String) -> Color {
    CoParseColors.risk(level)
}
