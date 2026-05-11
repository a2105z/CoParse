import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("CoParse")
                        .font(CoParseType.titleFont)
                    Text("Contract safety, on the spot")
                        .font(.title3.weight(.semibold))
                    Text("Scan a paper agreement or import a PDF. Analysis runs privately on your iPhone — free, no account.")
                        .font(.subheadline)
                        .foregroundStyle(.white.opacity(0.9))
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    LinearGradient(
                        colors: [CoParseColors.navy, CoParseColors.navyMid],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .foregroundStyle(.white)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))

                Text("Start with a vertical")
                    .font(.headline)

                actionCard(
                    title: "Residential lease",
                    subtitle: "Renter lens · scan or import",
                    icon: "house.fill",
                    type: "lease",
                    role: "renter"
                )
                actionCard(
                    title: "Internship / offer",
                    subtitle: "Student intern lens",
                    icon: "graduationcap.fill",
                    type: "internship_offer",
                    role: "student_intern"
                )
                actionCard(
                    title: "Freelance agreement",
                    subtitle: "Freelancer lens",
                    icon: "briefcase.fill",
                    type: "freelance",
                    role: "freelancer"
                )

                Button {
                    model.openScan(hintType: nil, role: nil)
                } label: {
                    Label("Scan any contract", systemImage: "doc.viewfinder")
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)

                Button {
                    model.path.append(.saved)
                } label: {
                    Label("Saved reports", systemImage: "bookmark.fill")
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)

                Text("Educational tool — not legal advice. Always verify against the original document.")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .padding(.top, 4)
            }
            .padding(20)
        }
        .navigationTitle("CoParse")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    model.path.append(.settings)
                } label: {
                    Image(systemName: "gearshape")
                }
                .accessibilityLabel("Settings")
            }
        }
    }

    private func actionCard(title: String, subtitle: String, icon: String, type: String, role: String) -> some View {
        Button {
            model.openScan(hintType: type, role: role)
        } label: {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundStyle(CoParseColors.navy)
                    .frame(width: 36)
                VStack(alignment: .leading, spacing: 4) {
                    Text(title).font(.headline).foregroundStyle(.primary)
                    Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(.tertiary)
            }
            .padding()
            .background(CoParseColors.navy.opacity(0.07))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(CoParseColors.navy.opacity(0.18), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
