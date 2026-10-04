const {
    recalculateForProfile
} = require("../services/automaticMatchingService");

const {
    performOCR,
    extractDocumentMetadata
} = require("../services/ocrService");

const {
    createOCRResult
} = require("../models/ocrModel");

const {
    createDocument,
    getDocumentById,
    getDocumentsByUser,
    getDocumentsWithOCRByUser,
    getDocumentsByType,
    updateDocument,
    updateUploadStatus,
    deleteDocument
} = require("../models/documentModel");


const {
    extractSkills
} = require("../services/skillExtractionService");

const {
    saveDocumentSkills,
    getDocumentSkills,
    updateSkillReviewStatus
} = require("../models/documentSkillModel");

const db = require("../config/db");

// ==========================================
// CLEAN OCR TEXT FOR DISPLAY / PROCESSING
// ==========================================
const cleanOCRText = (rawText) => {

    if (!rawText || typeof rawText !== "string") {
        return "";
    }

    let text = rawText
        .replace(/\r\n/g, "\n")
        .replace(/\r/g, "\n")
        .replace(/\t/g, " ")

        // Remove strange invisible/control characters
        .replace(/[^\x20-\x7E\n\u00A0-\uFFFF]/g, "")

        // Normalize repeated spaces
        .replace(/[ ]{2,}/g, " ")

        // Remove spaces before punctuation
        .replace(/\s+([,.;:!?])/g, "$1")

        // Add missing space after common punctuation
        .replace(/([,;:!?])([A-Za-z])/g, "$1 $2");

    // Clean individual lines
    let lines = text
        .split("\n")
        .map(line => line.trim())
        .filter((line, index, array) => {

            // Keep intentional blank lines,
            // but prevent many blank lines together
            if (line !== "") {
                return true;
            }

            return index > 0 &&
                array[index - 1] !== "";
        });

    text = lines.join("\n");

    // Add visual separation before common resume headings
    const headings = [
        "PROFILE",
        "SUMMARY",
        "OBJECTIVE",
        "EDUCATION",
        "EXPERIENCE",
        "WORK EXPERIENCE",
        "EMPLOYMENT",
        "SKILLS",
        "TECHNICAL SKILLS",
        "SOFT SKILLS",
        "PROJECTS",
        "CERTIFICATIONS",
        "CERTIFICATES",
        "ACHIEVEMENTS",
        "LANGUAGES",
        "REFERENCES",
        "CONTACT"
    ];

    for (const heading of headings) {

        const regex =
            new RegExp(
                `(^|\\n)\\s*${heading}\\s*:?\\s*(?=\\n|$)`,
                "gi"
            );

        text = text.replace(
            regex,
            `\n\n${heading}\n`
        );
    }

    // Remove excessive blank lines again
    text = text
        .replace(/\n{3,}/g, "\n\n")
        .trim();

    return text;
};

// ==========================================
// CREATE / UPLOAD DOCUMENT
// POST /api/documents
// ==========================================

const uploadDocument = async (req, res) => {

    try {

        console.log("DOCUMENT UPLOAD REQUEST");

        // ==========================================
        // CHECK UPLOADED FILE
        // ==========================================

        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "No document file was uploaded."
            });
        }

        if (!req.file.size || req.file.size <= 0) {

            try {

                const fs = require("fs");

                if (
                    req.file.path &&
                    fs.existsSync(req.file.path)
                ) {
                    fs.unlinkSync(req.file.path);
                }

            } catch (cleanupError) {

                console.error(
                    "Unable to remove empty upload:",
                    cleanupError
                );
            }

            return res.status(400).json({
                success: false,
                message: "The uploaded document is empty."
            });
        }


        // ==========================================
        // REQUEST DATA
        // ==========================================

        const {
            user_id,
            document_type
        } = req.body;

        console.log("User ID:", user_id);
        console.log("Document Type:", document_type);
        console.log("File:", req.file);


        // ==========================================
        // VALIDATE REQUIRED FIELDS
        // ==========================================

        if (user_id == null || !document_type) {

            return res.status(400).json({
                success: false,
                message: "user_id and document_type are required"
            });
        }


        // ==========================================
        // FILE INFORMATION
        // ==========================================

        const file_name =
            req.file.originalname;

        const file_path =
            req.file.path;

        const file_type =
            req.file.mimetype;

        const upload_status =
            "processing";


        // ==========================================
        // 1. CREATE DOCUMENT
        // ==========================================

        const documentResult =
            await createDocument(
                user_id,
                file_name,
                file_path,
                file_type,
                document_type,
                upload_status
            );

        const document_id =
            documentResult.insertId;

        console.log(
            "Document created:",
            document_id
        );


        // ==========================================
        // 2. RUN OCR
        // ==========================================

        let ocrResult;

        try {

            console.log(
                "Starting Tesseract OCR..."
            );

            ocrResult =
                await performOCR(file_path);

            // Clean raw Tesseract output
            const cleanedText =
                cleanOCRText(ocrResult.text);

            // Keep the cleaned version in the OCR result
            ocrResult.text =
                cleanedText;

            console.log(
                "Cleaned OCR text:",
                ocrResult.text
            );

            console.log(
                "OCR confidence:",
                ocrResult.confidence
            );

            console.log(
                "Extracted text:",
                ocrResult.text
            );

            console.log(
                "OCR confidence:",
                ocrResult.confidence
            );

        } catch (ocrError) {

            console.error(
                "OCR failed:",
                ocrError
            );

            await updateUploadStatus(
                document_id,
                "failed"
            );

            return res.status(500).json({

                success: false,

                message:
                    "Document uploaded, but OCR processing failed",

                document_id:
                    document_id,

                error:
                    ocrError.message
            });
        }


        // ==========================================
        // 3. SAVE OCR RESULT
        // ==========================================

        const metadata =
            extractDocumentMetadata(
                ocrResult.text,
                document_type
            );

        console.log(
            "Extracted metadata:",
            metadata
        );

        const ocrDatabaseResult =
            await createOCRResult(
                document_id,
                ocrResult.text,
                metadata.name,
                metadata.issuer,
                metadata.date,
                ocrResult.confidence
            );


        // ==========================================
        // 4. EXTRACT AND SAVE SKILL SUGGESTIONS
        // ==========================================

        let skillExtractionResult = {
            success: true,
            total_skills: 0,
            suggested_skills: []
        };

        try {

            const normalizedDocumentType =
                String(document_type)
                    .toLowerCase();

            if (
                normalizedDocumentType === "resume" ||
                normalizedDocumentType === "certificate"
            ) {

                skillExtractionResult =
                    extractSkills(
                        ocrResult.text,
                        normalizedDocumentType
                    );

                await saveDocumentSkills(
                    document_id,
                    skillExtractionResult.suggested_skills
                );

                console.log(
                    "Skill suggestions saved:",
                    skillExtractionResult.total_skills
                );
            }

        } catch (skillError) {

            // IMPORTANT:
            // OCR and document upload have succeeded.
            // A skill extraction failure should NOT
            // make the whole upload fail.

            console.error(
                "Skill extraction failed:",
                skillError.message
            );

            // Keep a safe empty result so that
            // the Android response remains valid.
            skillExtractionResult = {
                success: false,
                total_skills: 0,
                suggested_skills: []
            };
        }


        // ==========================================
        // 5. UPDATE DOCUMENT STATUS
        // ==========================================

        await updateUploadStatus(
            document_id,
            "processed"
        );


        // ==========================================
        // 6. RECALCULATE AI MATCHES
        //    ONLY AFTER RESUME OCR
        // ==========================================

        if (
            String(document_type)
                .toLowerCase()
                === "resume"
        ) {

            try {

                const [profileRows] =
                    await db.execute(
                        `
                        SELECT profile_id
                        FROM profiles
                        WHERE user_id = ?
                        LIMIT 1
                        `,
                        [user_id]
                    );

                if (profileRows.length > 0) {

                    const matchingResult =
                        await recalculateForProfile(
                            profileRows[0].profile_id
                        );

                    console.log(
                        "AI matches recalculated after resume OCR:",
                        matchingResult
                    );
                }

            } catch (matchingError) {

                // Upload + OCR already succeeded.
                // Matching failure must not invalidate
                // the uploaded document.

                console.error(
                    "Automatic AI recalculation after resume OCR failed:",
                    matchingError.message
                );
            }
        }


        // ==========================================
        // 7. RETURN SUCCESS RESPONSE
        // ==========================================

        return res.status(201).json({

            success: true,

            message:
                "Document uploaded and OCR processed successfully",

            data: {

                document_id:
                    document_id,

                file_name:
                    file_name,

                file_path:
                    file_path,

                file_type:
                    file_type,

                document_type:
                    document_type,

                upload_status:
                    "processed",

                ocr_id:
                    ocrDatabaseResult.insertId,

                extracted_text:
                    ocrResult.text,

                confidence_score:
                    ocrResult.confidence,

                skill_suggestions:
                    skillExtractionResult.suggested_skills,

                total_suggested_skills:
                    skillExtractionResult.total_skills
            }
        });


    } catch (error) {

        console.error(
            "Document Upload / OCR Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message:
                "Server error while uploading and processing document",

            error:
                error.message
        });
    }
};


// ==========================================
// GET DOCUMENT BY ID
// GET /api/documents/:id
// ==========================================

const getDocument = async (req, res) => {

    try {

        const {
            id
        } = req.params;

        const document =
            await getDocumentById(id);

        if (!document) {

            return res.status(404).json({

                success: false,

                message:
                    "Document not found"

            });
        }

        return res.status(200).json({

            success: true,

            data: document

        });

    } catch (error) {

        console.error(
            "Get Document Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message: "Server error",

            error:
                error.message

        });
    }
};


// ==========================================
// GET ALL DOCUMENTS BY USER
// GET /api/documents/user/:user_id
// ==========================================

const getUserDocuments = async (req, res) => {

    try {

        const {
            user_id
        } = req.params;

        const documents =
            await getDocumentsByUser(
                user_id
            );

        return res.status(200).json({

            success: true,

            count:
                documents.length,

            data:
                documents

        });

    } catch (error) {

        console.error(
            "Get User Documents Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message: "Server error",

            error:
                error.message

        });
    }
};

// ==========================================
// GET LOGGED-IN USER DOCUMENTS + OCR
// GET /api/documents/me
// ==========================================

const getMyDocuments = async (req, res) => {

    try {

        const userId =
                req.user.user_id;


        const documents =
                await getDocumentsWithOCRByUser(
                        userId
                );


        const data =
                documents.map(document => ({

                    document_id:
                            document.document_id,

                    file_name:
                            document.file_name,

                    file_path:
                            document.file_path,

                    file_type:
                            document.file_type,

                    document_type:
                            document.document_type,

                    upload_status:
                            document.upload_status,

                    created_at:
                            document.created_at,

                    updated_at:
                            document.updated_at,

                    ocr: document.ocr_id
                            ? {
                                ocr_id:
                                        document.ocr_id,

                                extracted_text:
                                        document.extracted_text,

                                extracted_name:
                                        document.extracted_name,

                                extracted_issuer:
                                        document.extracted_issuer,

                                extracted_date:
                                        document.extracted_date,

                                confidence_score:
                                        document.confidence_score !== null
                                                ? Number(document.confidence_score)
                                                : null,

                                processed_at:
                                        document.processed_at
                            }
                            : null
                }));


        return res.status(200).json({

            success: true,

            count: data.length,

            data
        });


    } catch (error) {

        console.error(
                "Get My Documents Error:",
                error
        );


        return res.status(500).json({

            success: false,

            message:
                    "Failed to load documents."
        });
    }
};

// ==========================================
// GET DOCUMENTS BY TYPE
// GET /api/documents/user/:user_id/type/:document_type
// ==========================================

const getUserDocumentsByType = async (
    req,
    res
) => {

    try {

        const {
            user_id,
            document_type
        } = req.params;

        const documents =
            await getDocumentsByType(
                user_id,
                document_type
            );

        return res.status(200).json({

            success: true,

            count:
                documents.length,

            data:
                documents

        });

    } catch (error) {

        console.error(
            "Get Documents By Type Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message: "Server error",

            error:
                error.message

        });
    }
};


// ==========================================
// UPDATE DOCUMENT
// PUT /api/documents/:id
// ==========================================

const editDocument = async (req, res) => {

    try {

        const {
            id
        } = req.params;

        const {
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status
        } = req.body;

        const result =
            await updateDocument(
                id,
                file_name,
                file_path,
                file_type,
                document_type,
                upload_status
            );

        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "Document not found"

            });
        }

        return res.status(200).json({

            success: true,

            message:
                "Document updated successfully"

        });

    } catch (error) {

        console.error(
            "Update Document Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message: "Server error",

            error:
                error.message

        });
    }
};


// ==========================================
// UPDATE UPLOAD STATUS
// PATCH /api/documents/:id/status
// ==========================================

const changeUploadStatus = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;

        const {
            upload_status
        } = req.body;

        if (!upload_status) {

            return res.status(400).json({

                success: false,

                message:
                    "upload_status is required"

            });
        }

        const result =
            await updateUploadStatus(
                id,
                upload_status
            );

        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "Document not found"

            });
        }

        return res.status(200).json({

            success: true,

            message:
                "Upload status updated successfully"

        });

    } catch (error) {

        console.error(
            "Update Upload Status Error:",
            error
        );

        return res.status(500).json({

            success: false,

            message: "Server error",

            error:
                error.message

        });
    }
};


// ==========================================
// DELETE DOCUMENT
// DELETE /api/documents/:id
// ==========================================

const removeDocument = async (req, res) => {
    try {
        const id = Number(req.params.id);
        if (!Number.isSafeInteger(id) || id <= 0) {
            return res.status(400).json({ success: false, message: "Invalid document ID." });
        }

        const document = await getDocumentById(id);
        if (!document) {
            return res.status(404).json({ success: false, message: "Document not found." });
        }
        if (req.user.role !== "admin" && Number(document.user_id) !== Number(req.user.user_id)) {
            return res.status(403).json({ success: false, message: "You can only delete your own documents." });
        }

        // Only delete files located in the application's controlled uploads directory.
        // Never accept a deletion path from the HTTP request.
        const fs = require("fs/promises");
        const path = require("path");
        const uploadRoot = path.resolve(__dirname, "../uploads");
        let diskFile = null;
        if (document.file_path) {
            const candidate = path.resolve(process.cwd(), document.file_path);
            const relative = path.relative(uploadRoot, candidate);
            if (relative === "" || relative === ".." || relative.startsWith(".." + path.sep) || path.isAbsolute(relative)) {
                console.error("Unsafe stored document path; refusing deletion:", document.document_id);
                return res.status(500).json({ success: false, message: "Document storage path requires administrator review." });
            }
            // Resolve symbolic links when the file exists to prevent escaping uploadRoot.
            try {
                const actualRoot = await fs.realpath(uploadRoot);
                const actualFile = await fs.realpath(candidate);
                const actualRelative = path.relative(actualRoot, actualFile);
                if (actualRelative === "" || actualRelative === ".." || actualRelative.startsWith(".." + path.sep) || path.isAbsolute(actualRelative)) {
                    return res.status(500).json({ success: false, message: "Unsafe document storage path." });
                }
                diskFile = candidate;
            } catch (error) {
                if (error.code !== "ENOENT") throw error;
                // A missing file should not prevent removal of its database record.
            }
        }

        // Delete the database record first. The OCR result is removed by its FK cascade.
        // If filesystem cleanup fails, report it for administrator reconciliation.
        const result = await deleteDocument(id);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: "Document not found." });
        }
        if (diskFile) {
            try { await fs.unlink(diskFile); }
            catch (error) {
                if (error.code !== "ENOENT") {
                    console.error("Orphaned uploaded file after document deletion:", diskFile, error);
                    return res.status(200).json({ success: true, cleanup_warning: true, message: "Document record deleted; file cleanup requires administrator review." });
                }
            }
        }
        return res.status(200).json({ success: true, message: "Document deleted successfully." });
    } catch (error) {
        console.error("Delete Document Error:", error);
        return res.status(500).json({ success: false, message: "Unable to delete document." });
    }
};


const getDocumentSkillSuggestions = async (
    req,
    res
) => {

    try {

        const documentId = Number(req.params.id);

        if (
            !Number.isSafeInteger(documentId) ||
            documentId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message: "Invalid document ID."
            });
        }

        const document =
            await getDocumentById(documentId);

        if (!document) {
            return res.status(404).json({
                success: false,
                message: "Document not found."
            });
        }

        // Users can only access their own suggestions.
        if (
            Number(document.user_id) !==
            Number(req.user.user_id)
        ) {
            return res.status(403).json({
                success: false,
                message: "Access denied."
            });
        }

        const suggestions =
            await getDocumentSkills(documentId);

        return res.status(200).json({
            success: true,
            document_id: documentId,
            count: suggestions.length,
            data: suggestions
        });

    } catch (error) {

        console.error(
            "Get Document Skills Error:",
            error.message
        );

        return res.status(500).json({
            success: false,
            message: "Unable to retrieve skill suggestions."
        });
    }
};


const reviewDocumentSkill = async (req, res) => {

    try {

        const documentId =
            Number(req.params.id);

        const skillId =
            Number(req.params.skillId);

        const {
            review_status
        } = req.body;

        if (
            !Number.isSafeInteger(documentId) ||
            documentId <= 0 ||
            !Number.isSafeInteger(skillId) ||
            skillId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message: "Invalid document or skill ID."
            });
        }

        if (
            !["confirmed", "rejected"]
                .includes(review_status)
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "review_status must be confirmed or rejected."
            });
        }

        const document =
            await getDocumentById(documentId);

        if (!document) {
            return res.status(404).json({
                success: false,
                message: "Document not found."
            });
        }

        if (
            Number(document.user_id) !==
            Number(req.user.user_id)
        ) {
            return res.status(403).json({
                success: false,
                message: "Access denied."
            });
        }

        const result =
            await updateSkillReviewStatus(
                skillId,
                documentId,
                review_status
            );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message:
                    "Pending skill suggestion not found."
            });
        }
        
        // ==========================================
        // RECALCULATE MATCHES AFTER SKILL REVIEW
        // ==========================================

        let matchingResult = null;
        let matchingWarning = null;

        try {

            const [profileRows] = await db.execute(
                `
                SELECT profile_id

                FROM profiles

                WHERE user_id = ?

                LIMIT 1
                `,
                [req.user.user_id]
            );

            if (profileRows.length > 0) {

                matchingResult = await recalculateForProfile(
                    profileRows[0].profile_id
                );

                if (matchingResult.failed > 0) {
                    matchingWarning =
                        "Some matching calculations failed.";
                }
            }

        } catch (matchingError) {

            console.error(
                "Recalculation after skill review failed:",
                matchingError.message
            );

            matchingWarning =
                "Skill review was saved, but recommendations could not be fully refreshed.";
        }
            
    return res.status(200).json({
        success: true,

        message:
            `Skill ${review_status} successfully.`,

        document_skill_id: skillId,

        review_status,

        matching_updated:
            matchingResult !== null &&
            matchingResult.failed === 0 &&
            matchingResult.skipped === 0,

        matching_result: matchingResult,

        matching_warning: matchingWarning
    });

        } catch (error) {

        console.error(
            "Review Document Skill Error:",
            error.message
        );

        return res.status(500).json({
            success: false,
            message: "Unable to review skill."
        });
    }
};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    uploadDocument,
    getDocument,
    getUserDocuments,
    getUserDocumentsByType,
    getMyDocuments,
    editDocument,
    changeUploadStatus,
    removeDocument,
    getDocumentSkillSuggestions,
    reviewDocumentSkill
};