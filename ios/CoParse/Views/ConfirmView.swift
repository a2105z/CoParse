import SwiftUI

struct ConfirmView: View {
    @EnvironmentObject private var model: AppModel
    @State private var type = "lease"
    @State private var role = "renter"

    var body: some View {
        Form {
            Section {
                Text("Confirm the contract type and your role so scoring and questions match how you’ll use this agreement.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Section("Contract type") {
                Picker("Type", selection: $type) {
                    Text("Lease").tag("lease")
                    Text("Internship / offer").tag("internship_offer")
                    Text("Freelance").tag("freelance")
                }
            }
            Section("Your role") {
                Picker("Role", selection: $role) {
                    Text("Renter").tag("renter")
                    Text("Student intern").tag("student_intern")
                    Text("Freelancer").tag("freelancer")
                }
            }
            if let conf = model.currentResult?.analysisConfidence {
                Section("Confidence") {
                    Text(conf.summary)
                    ForEach(conf.reasons, id: \.self) { r in
                        Text("• \(r)").font(.footnote).foregroundStyle(.secondary)
                    }
                }
            }
            Section {
                Button("Continue to report") {
                    model.applyConfirm(type: type, role: role)
                }
                .font(.headline)
            }
        }
        .navigationTitle("Confirm")
        .navigationBarBackButtonHidden(true)
        .onAppear {
            if let t = model.hintType ?? model.currentResult?.contractType {
                type = t
            }
            if let r = model.hintRole ?? model.currentResult?.role {
                role = r
            } else {
                role = ContractType(rawValue: type)?.defaultRole ?? role
            }
        }
        .onChange(of: type) { _, new in
            role = ContractType(rawValue: new)?.defaultRole ?? role
        }
    }
}
