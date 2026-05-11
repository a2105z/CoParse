import SwiftUI
import SwiftData

@main
struct CoParseApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(AppModel())
        }
        .modelContainer(for: SavedAnalysis.self)
    }
}
