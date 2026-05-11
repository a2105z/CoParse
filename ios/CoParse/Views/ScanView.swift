import PhotosUI
import SwiftUI
import UIKit
import UniformTypeIdentifiers

struct ScanView: View {
    @EnvironmentObject private var model: AppModel
    @State private var showDocScanner = false
    @State private var showCamera = false
    @State private var showImporter = false
    @State private var photoItems: [PhotosPickerItem] = []
    @State private var cameraAllowed = true

    var body: some View {
        List {
            Section {
                Text("Use Document Scan for the best paper capture. Flatten pages, fill the frame, and avoid glare. Everything runs on-device.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .listRowBackground(Color.clear)
            }

            Section("Capture") {
                if DocumentScannerAvailability.isSupported {
                    Button {
                        showDocScanner = true
                    } label: {
                        Label("Document Scan (recommended)", systemImage: "doc.viewfinder")
                    }
                }
                Button {
                    Task {
                        cameraAllowed = await CameraPermission.requestAccess()
                        if cameraAllowed { showCamera = true }
                    }
                } label: {
                    Label("Camera photo", systemImage: "camera")
                }
                PhotosPicker(selection: $photoItems, maxSelectionCount: 30, matching: .images) {
                    Label("Add from Photos", systemImage: "photo.on.rectangle")
                }
                Button {
                    showImporter = true
                } label: {
                    Label("Import PDF", systemImage: "doc.badge.plus")
                }
            }

            if !cameraAllowed {
                Section {
                    Text("Camera access is off. Enable it in Settings, or use Photos / PDF / Document Scan.")
                        .foregroundStyle(.red)
                        .font(.footnote)
                }
            }

            if !model.pendingImages.isEmpty {
                Section("Pages (\(model.pendingImages.count)) — drag to reorder") {
                    ForEach(Array(model.pendingImages.enumerated()), id: \.offset) { index, image in
                        HStack(spacing: 12) {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 56, height: 72)
                                .clipped()
                                .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                            Text("Page \(index + 1)")
                            Spacer()
                        }
                    }
                    .onMove { from, to in
                        model.pendingImages.move(fromOffsets: from, toOffset: to)
                    }
                    .onDelete { indexSet in
                        model.pendingImages.remove(atOffsets: indexSet)
                    }
                }
            }

            if let pdf = model.pendingPDFURL {
                Section("PDF") {
                    Label(pdf.lastPathComponent, systemImage: "doc.richtext")
                    Button("Remove PDF", role: .destructive) {
                        model.pendingPDFURL = nil
                    }
                }
            }

            Section {
                Button {
                    model.startAnalysis()
                } label: {
                    Text("Analyze on device")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
                .disabled(!canAnalyze)
                .listRowBackground(canAnalyze ? CoParseColors.navy : Color.gray)
                .foregroundStyle(.white)
            }
        }
        .environment(\.editMode, .constant(.active))
        .navigationTitle("Scan")
        .sheet(isPresented: $showDocScanner) {
            DocumentScannerView(pages: $model.pendingImages)
                .ignoresSafeArea()
                .onDisappear { model.pendingPDFURL = nil }
        }
        .sheet(isPresented: $showCamera) {
            CameraPagePicker(pages: $model.pendingImages)
                .ignoresSafeArea()
                .onDisappear { model.pendingPDFURL = nil }
        }
        .fileImporter(
            isPresented: $showImporter,
            allowedContentTypes: [.pdf],
            allowsMultipleSelection: false
        ) { result in
            if case .success(let urls) = result, let url = urls.first {
                let accessed = url.startAccessingSecurityScopedResource()
                defer { if accessed { url.stopAccessingSecurityScopedResource() } }
                let dest = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString + "-" + url.lastPathComponent)
                try? FileManager.default.removeItem(at: dest)
                try? FileManager.default.copyItem(at: url, to: dest)
                model.pendingPDFURL = dest
                model.pendingImages = []
            }
        }
        .onChange(of: photoItems) { _, items in
            Task {
                for item in items {
                    if let data = try? await item.loadTransferable(type: Data.self),
                       let image = UIImage(data: data) {
                        model.pendingImages.append(image)
                        model.pendingPDFURL = nil
                    }
                }
                photoItems = []
            }
        }
    }

    private var canAnalyze: Bool {
        !model.pendingImages.isEmpty || model.pendingPDFURL != nil
    }
}
