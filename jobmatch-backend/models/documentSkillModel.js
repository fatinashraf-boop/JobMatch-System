
const db = require("../config/db");

/**
 * Save extracted skill suggestions.
 * Suggestions are initially pending.
 */
const saveDocumentSkills = async (
    documentId,
    suggestions
) => {

    let saved = 0;

    for (const skill of suggestions) {

        await db.execute(
            `
            INSERT INTO document_skills (
                document_id,
                skill_name,
                matched_term,
                source,
                review_status
            )
            VALUES (?, ?, ?, ?, 'pending')

            ON DUPLICATE KEY UPDATE
                matched_term = VALUES(matched_term)
            `,
            [
                documentId,
                skill.skill_name,
                skill.matched_term,
                skill.source
            ]
        );

        saved++;
    }

    return saved;
};


/**
 * Retrieve suggestions belonging to a document.
 */
const getDocumentSkills = async (
    documentId
) => {

    const [rows] = await db.execute(
        `
        SELECT
            document_skill_id,
            document_id,
            skill_name,
            matched_term,
            source,
            review_status,
            created_at,
            reviewed_at

        FROM document_skills

        WHERE document_id = ?

        ORDER BY skill_name ASC
        `,
        [documentId]
    );

    return rows;
};


/**
 * Confirm or reject a single suggestion.
 */
const updateSkillReviewStatus = async (
    documentSkillId,
    documentId,
    reviewStatus
) => {

    const [result] = await db.execute(
        `
        UPDATE document_skills

        SET
            review_status = ?,
            reviewed_at = CURRENT_TIMESTAMP

        WHERE document_skill_id = ?
          AND document_id = ?
          AND review_status = 'pending'
        `,
        [
            reviewStatus,
            documentSkillId,
            documentId
        ]
    );

    return result;
};


module.exports = {
    saveDocumentSkills,
    getDocumentSkills,
    updateSkillReviewStatus
};
