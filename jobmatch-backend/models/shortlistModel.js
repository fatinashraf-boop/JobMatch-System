const db = require("../config/db");


// ==========================================
// CREATE SHORTLIST
// ==========================================

const createShortlist = async (
    application_id,
    recruiter_id,
    status
) => {

    const sql = `
        INSERT INTO shortlisted_candidates (
            application_id,
            recruiter_id,
            status,
            created_at,
            updated_at
        )
        VALUES (?, ?, ?, NOW(), NOW())
    `;

    const [result] = await db.execute(
        sql,
        [
            application_id,
            recruiter_id,
            status || "shortlisted"
        ]
    );

    return result;
};


// ==========================================
// GET ALL SHORTLISTED CANDIDATES
// ==========================================

const getAllShortlists = async () => {

    const sql = `
        SELECT
            sc.shortlist_id,
            sc.application_id,
            sc.recruiter_id,
            sc.status,
            sc.created_at,
            sc.updated_at

        FROM shortlisted_candidates sc

        ORDER BY sc.created_at DESC
    `;

    const [rows] = await db.execute(sql);

    return rows;
};


// ==========================================
// GET SHORTLIST BY ID
// ==========================================

const getShortlistById = async (
    shortlist_id
) => {

    const sql = `
        SELECT
            sc.shortlist_id,
            sc.application_id,
            sc.recruiter_id,
            sc.status,
            sc.created_at,
            sc.updated_at

        FROM shortlisted_candidates sc

        WHERE sc.shortlist_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [shortlist_id]
    );

    return rows[0];
};


// ==========================================
// GET SHORTLIST WITH APPLICATION AND JOB
// ==========================================

const getShortlistDetails = async (
    shortlist_id
) => {

    const sql = `
        SELECT
            sc.shortlist_id,
            sc.application_id,
            sc.recruiter_id,
            sc.status,
            sc.created_at,
            sc.updated_at,

            a.job_id,
            a.user_id,
            a.application_status,

            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status AS job_status

        FROM shortlisted_candidates sc

        INNER JOIN applications a
            ON sc.application_id = a.application_id

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        WHERE sc.shortlist_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [shortlist_id]
    );

    return rows[0];
};


// ==========================================
// GET SHORTLISTS BY RECRUITER
// ==========================================

const getShortlistsByRecruiter = async (
    recruiter_id
) => {

    const sql = `
        SELECT
            sc.shortlist_id,
            sc.application_id,
            sc.recruiter_id,
            sc.status,
            sc.created_at,
            sc.updated_at,

            a.job_id,
            a.user_id,
            a.application_status,

            j.job_title,
            j.location,
            j.job_type

        FROM shortlisted_candidates sc

        INNER JOIN applications a
            ON sc.application_id = a.application_id

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        WHERE sc.recruiter_id = ?
        AND sc.status <> 'removed'

        ORDER BY sc.created_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [recruiter_id]
    );

    return rows;
};


// ==========================================
// GET SHORTLISTS BY JOB
// ==========================================

const getShortlistsByJob = async (
    job_id
) => {

    const sql = `
        SELECT
            sc.shortlist_id,
            sc.application_id,
            sc.recruiter_id,
            sc.status,
            sc.created_at,
            sc.updated_at,

            a.job_id,
            a.user_id,
            a.application_status,

            j.job_title,
            j.location,
            j.job_type

        FROM shortlisted_candidates sc

        INNER JOIN applications a
            ON sc.application_id = a.application_id

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        WHERE a.job_id = ?
        AND sc.status <> 'removed'

        ORDER BY sc.created_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [job_id]
    );

    return rows;
};


// ==========================================
// CHECK IF APPLICATION IS ALREADY SHORTLISTED
// ==========================================

const checkShortlistExists = async (
    application_id
) => {

    const sql = `
        SELECT
            shortlist_id,
            application_id,
            recruiter_id,
            status,
            created_at,
            updated_at

        FROM shortlisted_candidates

        WHERE application_id = ?

        LIMIT 1
    `;

    const [rows] = await db.execute(
        sql,
        [application_id]
    );

    return rows[0];
};


// ==========================================
// UPDATE SHORTLIST STATUS
// ==========================================

const updateShortlistStatus = async (
    shortlist_id,
    status
) => {

    const sql = `
        UPDATE shortlisted_candidates

        SET
            status = ?,
            updated_at = NOW()

        WHERE shortlist_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            status,
            shortlist_id
        ]
    );

    return result;
};

// ==========================================
// RESTORE REMOVED SHORTLIST
// ==========================================

const restoreShortlist = async (
    shortlist_id,
    application_id
) => {

    const connection =
        await db.getConnection();

    try {

        await connection.beginTransaction();


        // Restore existing shortlist record
        const [shortlistResult] =
            await connection.execute(
                `
                UPDATE shortlisted_candidates

                SET
                    status = 'shortlisted',
                    updated_at = NOW()

                WHERE shortlist_id = ?
                AND application_id = ?
                `,
                [
                    shortlist_id,
                    application_id
                ]
            );


        if (shortlistResult.affectedRows === 0) {

            await connection.rollback();

            return {
                affectedRows: 0
            };
        }


        // Restore application status
        await connection.execute(
            `
            UPDATE applications

            SET
                application_status = 'shortlisted',
                reviewed_at = COALESCE(
                    reviewed_at,
                    NOW()
                )

            WHERE application_id = ?
            `,
            [application_id]
        );


        await connection.commit();


        return {
            affectedRows: 1
        };


    } catch (error) {

        await connection.rollback();

        throw error;

    } finally {

        connection.release();
    }
};

// ==========================================
// REMOVE FROM SHORTLIST
// Keep shortlist history and return
// application to REVIEWED
// ==========================================

const deleteShortlist = async (shortlist_id) => {

    const connection =
        await db.getConnection();

    try {

        await connection.beginTransaction();


        // ------------------------------------------
        // Find the related application
        // ------------------------------------------

        const [rows] =
            await connection.execute(
                `
                SELECT application_id
                FROM shortlisted_candidates
                WHERE shortlist_id = ?
                LIMIT 1
                `,
                [shortlist_id]
            );


        if (rows.length === 0) {

            await connection.rollback();

            return {
                affectedRows: 0
            };
        }


        const application_id =
            rows[0].application_id;


        // ------------------------------------------
        // Mark shortlist as REMOVED
        // Do NOT delete the record
        // ------------------------------------------

        await connection.execute(
            `
            UPDATE shortlisted_candidates

            SET
                status = 'removed',
                updated_at = NOW()

            WHERE shortlist_id = ?
            `,
            [shortlist_id]
        );


        // ------------------------------------------
        // Return application to REVIEWED
        // ------------------------------------------

        await connection.execute(
            `
            UPDATE applications

            SET
                application_status = 'reviewed',
                reviewed_at = COALESCE(reviewed_at, NOW())

            WHERE application_id = ?
            `,
            [application_id]
        );


        await connection.commit();


        return {
            affectedRows: 1,
            application_id: application_id
        };


    } catch (error) {

        await connection.rollback();

        throw error;

    } finally {

        connection.release();
    }
};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    createShortlist,

    getAllShortlists,

    getShortlistById,

    getShortlistDetails,

    getShortlistsByRecruiter,

    getShortlistsByJob,

    checkShortlistExists,

    updateShortlistStatus,

    restoreShortlist,


    deleteShortlist

};