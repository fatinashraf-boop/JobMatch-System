const {
    createOCRResult,
    getOCRResultById,
    getOCRResultByDocument,
    getOCRResultWithDocument,
    updateOCRResult,
    deleteOCRResult,
    checkOCRResultExists
} = require("../models/ocrModel");

const db = require("../config/db");


// ==========================================
// CREATE OCR RESULT
// ==========================================

const processOCR = async (
    req,
    res
) => {

    try {

        const {
            document_id,
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score
        } = req.body;


        // ==========================================
        // VALIDATE REQUIRED FIELD
        // ==========================================

        if (!document_id) {

            return res.status(400).json({

                success: false,

                message:
                    "document_id is required"

            });

        }


        // ==========================================
        // CHECK DOCUMENT EXISTS
        // ==========================================

        const [documents] =
            await db.execute(
                `
                SELECT
                    document_id

                FROM documents

                WHERE document_id = ?
                `,
                [document_id]
            );


        if (documents.length === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Document not found"

            });

        }


        // ==========================================
        // CHECK EXISTING OCR RESULT
        // ==========================================

        const existingOCR =
            await checkOCRResultExists(
                document_id
            );


        // ==========================================
        // UPDATE EXISTING OCR
        // ==========================================

        if (existingOCR) {

            const result =
                await updateOCRResult(
                    existingOCR.ocr_id,
                    extracted_text || null,
                    extracted_name || null,
                    extracted_issuer || null,
                    extracted_date || null,
                    confidence_score || null
                );


            return res.status(200).json({

                success: true,

                message:
                    "OCR result updated successfully",

                data: {

                    ocr_id:
                        existingOCR.ocr_id,

                    document_id,

                    extracted_text:
                        extracted_text || null,

                    extracted_name:
                        extracted_name || null,

                    extracted_issuer:
                        extracted_issuer || null,

                    extracted_date:
                        extracted_date || null,

                    confidence_score:
                        confidence_score || null

                }

            });

        }


        // ==========================================
        // CREATE NEW OCR RESULT
        // ==========================================

        const result =
            await createOCRResult(
                document_id,
                extracted_text || null,
                extracted_name || null,
                extracted_issuer || null,
                extracted_date || null,
                confidence_score || null
            );


        return res.status(201).json({

            success: true,

            message:
                "OCR result created successfully",

            data: {

                ocr_id:
                    result.insertId,

                document_id,

                extracted_text:
                    extracted_text || null,

                extracted_name:
                    extracted_name || null,

                extracted_issuer:
                    extracted_issuer || null,

                extracted_date:
                    extracted_date || null,

                confidence_score:
                    confidence_score || null

            }

        });


    } catch (error) {

        console.error(
            "OCR Processing Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while processing OCR",

            error:
                error.message

        });

    }

};


// ==========================================
// GET OCR RESULT BY ID
// ==========================================

const getOCR = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const ocr =
            await getOCRResultWithDocument(
                id
            );


        if (!ocr) {

            return res.status(404).json({

                success: false,

                message:
                    "OCR result not found"

            });

        }


        return res.status(200).json({

            success: true,

            data:
                ocr

        });


    } catch (error) {

        console.error(
            "Get OCR Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error",

            error:
                error.message

        });

    }

};


// ==========================================
// GET OCR BY DOCUMENT
// ==========================================

const getDocumentOCR = async (
    req,
    res
) => {

    try {

        const {
            document_id
        } = req.params;


        const ocrResults =
            await getOCRResultByDocument(
                document_id
            );


        return res.status(200).json({

            success: true,

            count:
                ocrResults.length,

            data:
                ocrResults

        });


    } catch (error) {

        console.error(
            "Get Document OCR Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error",

            error:
                error.message

        });

    }

};


// ==========================================
// UPDATE OCR RESULT
// ==========================================

const editOCR = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const {
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score
        } = req.body;


        const result =
            await updateOCRResult(
                id,
                extracted_text,
                extracted_name,
                extracted_issuer,
                extracted_date,
                confidence_score
            );


        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "OCR result not found"

            });

        }


        return res.status(200).json({

            success: true,

            message:
                "OCR result updated successfully"

        });


    } catch (error) {

        console.error(
            "Update OCR Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error",

            error:
                error.message

        });

    }

};


// ==========================================
// DELETE OCR RESULT
// ==========================================

const removeOCR = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const result =
            await deleteOCRResult(id);


        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "OCR result not found"

            });

        }


        return res.status(200).json({

            success: true,

            message:
                "OCR result deleted successfully"

        });

    } catch (error) {

        console.error(
            "Delete OCR Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error",

            error:
                error.message

        });

    }

};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    processOCR,

    getOCR,

    getDocumentOCR,

    editOCR,

    removeOCR

};