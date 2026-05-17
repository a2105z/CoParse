# CoParse iOS

Native SwiftUI app — primary product surface for CoParse.

Full product and technical documentation:

- [`../README.md`](../README.md)
- [`../TECHNICAL.md`](../TECHNICAL.md)

## Requirements

- Xcode 15+
- iOS 17+
- Camera / Photos permissions (prompts on first use)
- Physical device recommended for Document Scan (VisionKit)

## Open

```bash
open ios/CoParse.xcodeproj
```

Select a simulator or device → set your Development Team → Run (`⌘R`).

## Features

- VisionKit **Document Scan**, camera, Photos, PDF import
- Page reorder / delete before analyze
- Image preprocess + Vision OCR; PDFKit with OCR fallback
- On-device heuristic analysis (no network required)
- Confirm type/role when confidence is low
- Report: score, category bars, timeline, if/then, questions, email draft
- SwiftData save / auto-save; share text report
- Onboarding, Settings, Privacy, App Icon, privacy manifest

## Bundle ID

`com.coparse.app` — change signing team in Xcode for your Apple Developer account.

## Shipping notes

- Marketing version `1.0` / build `1` (see project settings)
- Privacy nutrition labels: no tracking; on-device analysis; UserDefaults for preferences
- App Store copy should keep the educational / not-legal-advice disclaimer prominent
