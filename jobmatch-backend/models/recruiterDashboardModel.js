const db = require("../config/db");


// ==========================================
// GET RECRUITER ID BY USER ID
// ==========================================

const getRecruiterByUserId = async (userId) => {

    const sql = `
        SELECT
            recruiter_id,
            company_name
        FROM recruiters
        WHERE user_id = ?
        LIMIT 1
    `;

    const [rows] =
        await db.execute(
            sql,
            [userId]
        );

    return rows[0] || null;
};


// ==========================================
// COUNT ACTIVE JOBS
// ==========================================

const countActiveJobs = async (recruiterId) => {

    const sql = `
        SELECT COUNT(*) AS total
        FROM jobs
        WHERE recruiter_id = ?
        AND status = 'open'
    `;

    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );

    return rows[0].total;
};


// ==========================================
// COUNT TOTAL APPLICANTS
// ==========================================

const countApplicants = async (recruiterId) => {

    const sql = `
        SELECT COUNT(*) AS total
        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        WHERE j.recruiter_id = ?

        AND a.application_status IN (
            'pending',
            'reviewed',
            'shortlisted'
        )

        AND NOT EXISTS (
            SELECT 1
            FROM shortlisted_candidates sc
            WHERE sc.application_id = a.application_id
            AND sc.shortlist_status = 'hired'
        )
    `;

    const [rows] = await db.execute(
        sql,
        [recruiterId]
    );

    return Number(rows[0].total);
};


// ==========================================
// COUNT SHORTLISTED CANDIDATES
// ==========================================

const countShortlisted = async (recruiterId) => {

    const sql = `
        SELECT COUNT(*) AS total
        FROM shortlisted_candidates sc

        INNER JOIN applications a
            ON sc.application_id = a.application_id

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        WHERE j.recruiter_id = ?
        AND sc.shortlist_status IN (
            'active',
            'interviewed',
            'offer_sent'
        )
    `;

    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );

    return rows[0].total;
};


module.exports = {

    getRecruiterByUserId,
    countActiveJobs,
    countApplicants,
    countShortlisted
};