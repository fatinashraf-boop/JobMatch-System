const { createWorker } = require("tesseract.js");
const path = require("path");
const sharp = require("sharp");


// =====================================================
// CLEAN OCR TEXT
// =====================================================

const cleanOCRText = (rawText) => {

    if (!rawText || typeof rawText !== "string") {
        return "";
    }

    return rawText
        .replace(/\r\n/g, "\n")
        .replace(/\r/g, "\n")
        .replace(/\t/g, " ")
        .replace(/[ ]{2,}/g, " ")
        .replace(/\s+([,.;:!?])/g, "$1")
        .split("\n")
        .map(line => line.trim())
        .join("\n")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
};


// =====================================================
// PREPROCESS CAMERA / UPLOADED IMAGE
// =====================================================

const preprocessImage = async (
    imageInput,
    thresholdVersion = false
) => {

    /*
     * sharp().rotate() automatically uses EXIF orientation.
     *
     * The image is resized only when necessary so that
     * small camera images become easier for OCR to read.
     */

    let pipeline = sharp(imageInput)
        .rotate()
        .resize({
            width: 2200,
            withoutEnlargement: false,
            fit: "inside"
        })
        .grayscale()
        .normalize()
        .sharpen();

    /*
     * Create a high-contrast black/white version.
     *
     * This can work better for certificates and
     * photographed printed documents.
     */
    if (thresholdVersion) {

        pipeline = pipeline.threshold(170);
    }

    return await pipeline
        .png()
        .toBuffer();
};


// =====================================================
// RUN TESSERACT ON ONE IMAGE
// =====================================================

const recognizeImage = async (
    worker,
    imageInput
) => {

    const result =
        await worker.recognize(imageInput);

    return {

        text:
            cleanOCRText(
                result.data.text || ""
            ),

        confidence:
            Number(
                result.data.confidence || 0
            )
    };
};


// =====================================================
// OCR IMAGE
// JPG / JPEG / PNG
// =====================================================

const performImageOCR = async (imageInput) => {

    const worker =
        await createWorker("eng");

    try {

        console.log(
            "Preparing image for OCR..."
        );


        // ==========================================
        // VERSION 1
        // ENHANCED GRAYSCALE
        // ==========================================

        const enhancedImage =
            await preprocessImage(
                imageInput,
                false
            );


        console.log(
            "Running OCR on enhanced image..."
        );


        const enhancedResult =
            await recognizeImage(
                worker,
                enhancedImage
            );


        console.log(
            "Enhanced OCR confidence:",
            enhancedResult.confidence
        );


        // ==========================================
        // VERSION 2
        // HIGH-CONTRAST / THRESHOLD
        // ==========================================

        const thresholdImage =
            await preprocessImage(
                imageInput,
                true
            );


        console.log(
            "Running OCR on threshold image..."
        );


        const thresholdResult =
            await recognizeImage(
                worker,
                thresholdImage
            );


        console.log(
            "Threshold OCR confidence:",
            thresholdResult.confidence
        );


        // ==========================================
        // CHOOSE BEST OCR RESULT
        // ==========================================

        let bestResult;

        let selectedVersion;


        if (
            thresholdResult.confidence >
            enhancedResult.confidence
        ) {

            bestResult =
                thresholdResult;

            selectedVersion =
                "threshold";

        } else {

            bestResult =
                enhancedResult;

            selectedVersion =
                "enhanced";
        }


        console.log(
            `OCR selected ${selectedVersion} image; ` +
            `confidence: ${bestResult.confidence.toFixed(2)}%`
        );


        if (bestResult.confidence < 50) {

            console.warn(
                "Low OCR confidence. " +
                "The user should review or retake the photo."
            );
        }


        return bestResult;


    } finally {

        await worker.terminate();
    }
};


// =====================================================
// OCR PDF
// PDF → IMAGE → TESSERACT
// =====================================================

const performPdfOCR = async (pdfPath) => {

    const { pdf } =
        await import("pdf-to-img");


    const document =
        await pdf(
            pdfPath,
            {
                scale: 2.5
            }
        );


    let combinedText = "";

    let totalConfidence = 0;

    let processedPages = 0;


    try {

        for await (
            const pageImage of document
        ) {

            console.log(
                `Processing PDF page ${
                    processedPages + 1
                }...`
            );


            /*
             * PDF pages use the same OCR pipeline.
             *
             * This keeps PDF upload compatible with
             * the existing JobMatch workflow.
             */

            const result =
                await performImageOCR(
                    pageImage
                );


            combinedText +=
                result.text.trim()
                + "\n\n";


            totalConfidence +=
                result.confidence;


            processedPages++;
        }


    } finally {

        if (
            document &&
            typeof document.destroy === "function"
        ) {

            await document.destroy();
        }
    }


    if (processedPages === 0) {

        throw new Error(
            "No pages could be extracted from PDF."
        );
    }


    const averageConfidence =
        totalConfidence /
        processedPages;


    return {

        text:
            cleanOCRText(
                combinedText
            ),

        confidence:
            averageConfidence
    };
};


// =====================================================
// MAIN OCR FUNCTION
// =====================================================

const performOCR = async (filePath) => {

    const extension =
        path.extname(
            filePath
        )
        .toLowerCase();


    // ==============================
    // PDF
    // ==============================

    if (extension === ".pdf") {

        console.log(
            "PDF detected. Converting pages to images..."
        );

        return await performPdfOCR(
            filePath
        );
    }


    // ==============================
    // IMAGE
    // ==============================

    if (
        extension === ".jpg" ||
        extension === ".jpeg" ||
        extension === ".png"
    ) {

        console.log(
            "Image detected. Preprocessing before Tesseract OCR..."
        );

        return await performImageOCR(
            filePath
        );
    }


    throw new Error(
        "Unsupported file type for OCR."
    );
};


// =====================================================
// NORMALIZE DATE FOR DATABASE
// =====================================================

const normalizeDateForDatabase = (dateText) => {

    if (!dateText) {
        return null;
    }


    // DD/MM/YYYY or DD-MM-YYYY

    let match =
        dateText.match(
            /^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/
        );


    if (match) {

        const day =
            match[1].padStart(
                2,
                "0"
            );

        const month =
            match[2].padStart(
                2,
                "0"
            );

        const year =
            match[3];


        return `${year}-${month}-${day}`;
    }


    // Written English dates

    const parsed =
        new Date(dateText);


    if (
        !Number.isNaN(
            parsed.getTime()
        )
    ) {

        const year =
            parsed.getFullYear();

        const month =
            String(
                parsed.getMonth() + 1
            ).padStart(
                2,
                "0"
            );

        const day =
            String(
                parsed.getDate()
            ).padStart(
                2,
                "0"
            );


        return `${year}-${month}-${day}`;
    }


    return null;
};


// =====================================================
// EXTRACT DOCUMENT METADATA FROM OCR TEXT
// =====================================================

const extractDocumentMetadata = (
    text,
    documentType
) => {

    if (
        !text ||
        !text.trim()
    ) {

        return {
            name: null,
            issuer: null,
            date: null
        };
    }


    const cleanedText =
        text
            .replace(/\r/g, "")
            .replace(/[ \t]+/g, " ")
            .trim();


    const lines =
        cleanedText
            .split("\n")
            .map(
                line =>
                    line.trim()
            )
            .filter(Boolean);


    let extractedName = null;

    let extractedIssuer = null;

    let extractedDate = null;


    // =============================================
    // DATE
    // =============================================

    const datePatterns = [

        // 15 August 2026
        /\b(\d{1,2}\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{4})\b/i,

        // August 15, 2026
        /\b((?:January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{1,2},?\s+\d{4})\b/i,

        // 15/08/2026 or 15-08-2026
        /\b(\d{1,2}[\/\-]\d{1,2}[\/\-]\d{4})\b/
    ];


    for (
        const pattern of datePatterns
    ) {

        const match =
            cleanedText.match(
                pattern
            );


        if (match) {

            extractedDate =
                normalizeDateForDatabase(
                    match[1]
                );

            break;
        }
    }


    // =============================================
    // CERTIFICATE METADATA
    // =============================================

    if (
        String(documentType)
            .toLowerCase()
        === "certificate"
    ) {

        for (
            let i = 0;
            i < lines.length;
            i++
        ) {

            const lower =
                lines[i]
                    .toLowerCase();


            // Awarded to / Presented to

            if (
                lower.includes(
                    "awarded to"
                ) ||
                lower.includes(
                    "presented to"
                ) ||
                lower.includes(
                    "certify that"
                )
            ) {

                if (
                    i + 1 <
                    lines.length
                ) {

                    extractedName =
                        lines[i + 1];
                }
            }


            // Issued by

            if (
                lower.startsWith(
                    "issued by"
                ) ||
                lower.startsWith(
                    "issuer"
                )
            ) {

                const sameLine =
                    lines[i]
                        .replace(
                            /^(issued by|issuer)\s*:?\s*/i,
                            ""
                        )
                        .trim();


                if (sameLine) {

                    extractedIssuer =
                        sameLine;

                } else if (
                    i + 1 <
                    lines.length
                ) {

                    extractedIssuer =
                        lines[i + 1];
                }
            }
        }
    }


    // =============================================
    // RESUME NAME
    // =============================================

    if (
        String(documentType)
            .toLowerCase()
        === "resume"
    ) {

        for (
            const line of
            lines.slice(0, 8)
        ) {

            const lower =
                line.toLowerCase();


            const looksLikeHeading =
                lower.includes(
                    "resume"
                ) ||
                lower.includes(
                    "curriculum vitae"
                ) ||
                lower.includes(
                    "profile"
                ) ||
                lower.includes(
                    "summary"
                );


            const looksLikeContact =
                line.includes("@") ||
                /\d{3,}/.test(line);


            if (
                !looksLikeHeading &&
                !looksLikeContact &&
                line.length >= 3 &&
                line.length <= 80
            ) {

                extractedName =
                    line;

                break;
            }
        }
    }


    return {

        name:
            extractedName,

        issuer:
            extractedIssuer,

        date:
            extractedDate
    };
};


// =====================================================
// EXPORT
// =====================================================

module.exports = {

    performOCR,
    extractDocumentMetadata
};