const db = require("../config/db");


// ==========================================
// CREATE OCR RESULT
// ==========================================

const createOCRResult = async (
    document_id,
    extracted_text,
    extracted_name,
    extracted_issuer,
    extracted_date,
    confidence_score
) => {

    const sql = `
        INSERT INTO ocr_results
        (
            document_id,
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score,
            processed_at
        )
        VALUES (?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await db.execute(
        sql,
        [
            document_id,
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score
        ]
    );

    return result;
};


// ==========================================
// GET OCR RESULT BY ID
// ==========================================

const getOCRResultById = async (ocr_id) => {

    const sql = `
        SELECT
            ocr_id,
            document_id,
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score,
            processed_at
        FROM ocr_results
        WHERE ocr_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [ocr_id]
    );

    return rows[0];
};


// ==========================================
// GET OCR RESULTS BY DOCUMENT
// ==========================================

const getOCRResultByDocument = async (document_id) => {

    const sql = `
        SELECT
            ocr_id,
            document_id,
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score,
            processed_at
        FROM ocr_results
        WHERE document_id = ?
        ORDER BY processed_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [document_id]
    );

    return rows;
};


// ==========================================
// GET OCR RESULT WITH DOCUMENT DETAILS
// ==========================================

const getOCRResultWithDocument = async (ocr_id) => {

    const sql = `
        SELECT
            o.ocr_id,
            o.document_id,
            o.extracted_text,
            o.extracted_name,
            o.extracted_issuer,
            o.extracted_date,
            o.confidence_score,
            o.processed_at,

            d.user_id,
            d.file_name,
            d.file_path,
            d.file_type,
            d.document_type,
            d.upload_status

        FROM ocr_results o

        INNER JOIN documents d
            ON o.document_id = d.document_id

        WHERE o.ocr_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [ocr_id]
    );

    return rows[0];
};


// ==========================================
// UPDATE OCR RESULT
// ==========================================

const updateOCRResult = async (
    ocr_id,
    extracted_text,
    extracted_name,
    extracted_issuer,
    extracted_date,
    confidence_score
) => {

    const sql = `
        UPDATE ocr_results

        SET
            extracted_text = ?,
            extracted_name = ?,
            extracted_issuer = ?,
            extracted_date = ?,
            confidence_score = ?,
            processed_at = NOW()

        WHERE ocr_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            extracted_text,
            extracted_name,
            extracted_issuer,
            extracted_date,
            confidence_score,
            ocr_id
        ]
    );

    return result;
};


// ==========================================
// DELETE OCR RESULT
// ==========================================

const deleteOCRResult = async (ocr_id) => {

    const sql = `
        DELETE FROM ocr_results
        WHERE ocr_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [ocr_id]
    );

    return result;
};


// ==========================================
// CHECK OCR RESULT EXISTS
// ==========================================

const checkOCRResultExists = async (document_id) => {

    const sql = `
        SELECT
            ocr_id,
            document_id
        FROM ocr_results
        WHERE document_id = ?
        LIMIT 1
    `;

    const [rows] = await db.execute(
        sql,
        [document_id]
    );

    return rows[0];
};


// ==========================================
// EXPORT MODEL FUNCTIONS
// ==========================================

module.exports = {
    createOCRResult,
    getOCRResultById,
    getOCRResultByDocument,
    getOCRResultWithDocument,
    updateOCRResult,
    deleteOCRResult,
    checkOCRResultExists
};