import Foundation
import SwiftData

@Model
final class SavedAnalysis {
    @Attribute(.unique) var id: UUID
    var title: String
    var createdAt: Date
    var contractType: String
    var role: String
    var overallScore: Int
    var payloadJSON: Data

    init(from result: AnalysisResult) throws {
        self.id = result.id
        self.title = result.title
        self.createdAt = result.createdAt
        self.contractType = result.contractType
        self.role = result.role
        self.overallScore = result.overallScore
        self.payloadJSON = try JSONEncoder().encode(result)
    }

    func decode() throws -> AnalysisResult {
        try JSONDecoder().decode(AnalysisResult.self, from: payloadJSON)
    }
}
