import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var model: AppModel

    private var versionLabel: String {
        let short = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
        return "\(short) (\(build))"
    }

    var body: some View {
        Form {
            Section("Analysis") {
                Toggle("Auto-save reports on this device", isOn: Binding(
                    get: { model.autoSaveEnabled },
                    set: { model.setAutoSave($0) }
                ))
                Text("Keeps full results offline so you can reopen without rescanning.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

            Section("About") {
                LabeledContent("Version", value: versionLabel)
                LabeledContent("Analysis", value: "On-device · free")
                NavigationLink("Privacy") {
                    PrivacyView()
                }
            }

            Section("Legal") {
                Text("CoParse provides educational information only and does not provide legal advice. It does not tell you to sign or not sign.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

            Section {
                Button("Show welcome tips again") {
                    model.onboardingDone = false
                    UserDefaults.standard.set(false, forKey: "coparse.onboardingDone")
                    model.goHome()
                }
            }
        }
        .navigationTitle("Settings")
    }
}
