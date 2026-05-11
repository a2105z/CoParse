import UIKit
import CoreImage
import CoreImage.CIFilterBuiltins

enum ImagePreprocessor {
    /// Light normalize for OCR: grayscale + contrast boost.
    static func prepareForOCR(_ image: UIImage) -> UIImage {
        guard let cg = image.cgImage else { return image }
        let ci = CIImage(cgImage: cg)
        let context = CIContext(options: nil)

        let mono = ci
            .applyingFilter("CIPhotoEffectMono")
            .applyingFilter("CIColorControls", parameters: [
                kCIInputContrastKey: 1.25,
                kCIInputBrightnessKey: 0.02,
            ])

        guard let out = context.createCGImage(mono, from: mono.extent) else { return image }
        return UIImage(cgImage: out, scale: image.scale, orientation: image.imageOrientation)
    }

    static func prepareAll(_ images: [UIImage]) -> [UIImage] {
        images.map(prepareForOCR)
    }
}
