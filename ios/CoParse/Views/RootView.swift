import SwiftUI

struct RootView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        Group {
            if !model.disclaimerAccepted {
                DisclaimerView()
            } else if !model.onboardingDone {
                OnboardingView()
            } else {
                NavigationStack(path: $model.path) {
                    HomeView()
                        .navigationDestination(for: AppRoute.self) { route in
                            switch route {
                            case .home:
                                HomeView()
                            case .scan:
                                ScanView()
                            case .processing:
                                ProcessingView()
                            case .confirm:
                                ConfirmView()
                            case .dashboard:
                                DashboardView()
                            case .clause(let id):
                                ClauseDetailView(clauseId: id)
                            case .questions:
                                QuestionsView()
                            case .allClauses:
                                AllClausesView()
                            case .saved:
                                SavedView()
                            case .settings:
                                SettingsView()
                            case .privacy:
                                PrivacyView()
                            }
                        }
                }
            }
        }
        .tint(CoParseColors.navy)
    }
}
