const db = require("../config/db");

// ==========================================
// CREATE DOCUMENT
// ==========================================

const createDocument = async (
    user_id,
    file_name,
    file_path,
    file_type,
    document_type,
    upload_status = "uploaded"
) => {

    const sql = `
        INSERT INTO documents
        (
            user_id,
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status
        )
        VALUES (?, ?, ?, ?, ?, ?)
    `;

    const [result] = await db.execute(
        sql,
        [
            user_id,
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status
        ]
    );

    return result;
};


// ==========================================
// GET DOCUMENT BY ID
// ==========================================

const getDocumentById = async (
    document_id
) => {

    const sql = `
        SELECT
            document_id,
            user_id,
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status,
            created_at,
            updated_at

        FROM documents

        WHERE document_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [document_id]
    );

    return rows[0];
};

// ==========================================
// GET MY DOCUMENTS WITH LATEST OCR
// ==========================================

const getDocumentsWithOCRByUser =
    async (user_id) => {

        const sql = `
            SELECT
                d.document_id,
                d.user_id,
                d.file_name,
                d.file_path,
                d.file_type,
                d.document_type,
                d.upload_status,
                d.created_at,
                d.updated_at,

                o.ocr_id,
                o.extracted_text,
                o.extracted_name,
                o.extracted_issuer,
                o.extracted_date,
                o.confidence_score,
                o.processed_at

            FROM documents d

            LEFT JOIN ocr_results o
                ON o.ocr_id = (
                    SELECT o2.ocr_id
                    FROM ocr_results o2
                    WHERE o2.document_id = d.document_id
                    ORDER BY o2.processed_at DESC,
                             o2.ocr_id DESC
                    LIMIT 1
                )

            WHERE d.user_id = ?

            ORDER BY d.created_at DESC
        `;


        const [rows] =
                await db.execute(
                        sql,
                        [user_id]
                );


        return rows;
};

// ==========================================
// GET DOCUMENTS BY USER
// ==========================================

const getDocumentsByUser = async (
    user_id
) => {

    const sql = `
        SELECT
            document_id,
            user_id,
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status,
            created_at,
            updated_at

        FROM documents

        WHERE user_id = ?

        ORDER BY created_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [user_id]
    );

    return rows;
};

// ==========================================
// GET DOCUMENTS BY TYPE
// ==========================================

const getDocumentsByType = async (
    user_id,
    document_type
) => {

    const sql = `
        SELECT
            document_id,
            user_id,
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status,
            created_at,
            updated_at

        FROM documents

        WHERE user_id = ?
        AND document_type = ?

        ORDER BY created_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [
            user_id,
            document_type
        ]
    );

    return rows;
};

// ==========================================
// UPDATE DOCUMENT
// ==========================================

const updateDocument = async (
    document_id,
    file_name,
    file_path,
    file_type,
    document_type,
    upload_status
) => {

    const sql = `
        UPDATE documents

        SET
            file_name = ?,
            file_path = ?,
            file_type = ?,
            document_type = ?,
            upload_status = ?

        WHERE document_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            file_name,
            file_path,
            file_type,
            document_type,
            upload_status,
            document_id
        ]
    );

    return result;
};

// ==========================================
// UPDATE UPLOAD STATUS
// ==========================================

const updateUploadStatus = async (
    document_id,
    upload_status
) => {

    const sql = `
        UPDATE documents

        SET
            upload_status = ?

        WHERE document_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            upload_status,
            document_id
        ]
    );

    return result;
};

// ==========================================
// DELETE DOCUMENT
// ==========================================

const deleteDocument = async (
    document_id
) => {

    const sql = `
        DELETE FROM documents

        WHERE document_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [document_id]
    );

    return result;
};

// ==========================================
// CHECK DOCUMENT OWNERSHIP
// ==========================================

const checkDocumentOwnership = async (
    document_id,
    user_id
) => {

    const sql = `
        SELECT
            document_id,
            user_id

        FROM documents

        WHERE document_id = ?
        AND user_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [
            document_id,
            user_id
        ]
    );

    return rows[0];
};

// ==========================================
// EXPORT
// ==========================================

module.exports = {
    createDocument,
    getDocumentById,
    getDocumentsWithOCRByUser,
    getDocumentsByUser,
    getDocumentsByType,
    updateDocument,
    updateUploadStatus,
    deleteDocument,
    checkDocumentOwnership
};