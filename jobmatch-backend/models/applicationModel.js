const db = require("../config/db");


// ==========================================
// CREATE APPLICATION
// ==========================================

const createApplication = async (job_id, profile_id) => {

    const sql = `
        INSERT INTO applications
        (
            job_id,
            profile_id,
            application_status,
            applied_at
        )
        VALUES (?, ?, 'pending', NOW())
    `;

    const [result] = await db.execute(
        sql,
        [job_id, profile_id]
    );

    return result;
};

const getApplicationDetailsById = async (
    application_id,
    profile_id
) => {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,

            r.company_name,
            r.company_email,
            r.company_description,
            r.website,

            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN recruiters r
            ON j.recruiter_id = r.recruiter_id

        LEFT JOIN shortlisted_candidates sc
            ON a.application_id = sc.application_id

        WHERE
            a.application_id = ?
            AND a.profile_id = ?

        LIMIT 1
    `;


    const [rows] =
        await db.execute(
            sql,
            [
                application_id,
                profile_id
            ]
        );


    return rows[0];
};


// ==========================================
// GET APPLICATION BY ID
// ==========================================

const getApplicationById = async (application_id) => {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,
            j.recruiter_id,

            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.expected_salary,
            p.location,
            p.profile_picture,
            p.resume_summary

        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        WHERE a.application_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [application_id]
    );

    return rows[0];
};


// ==========================================
// GET APPLICATIONS BY PROFILE
// ==========================================

const getApplicationsByProfile = async (profile_id) => {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status AS job_status,

            r.recruiter_id,
            r.company_name,
            r.company_logo,

            sc.shortlist_status

        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN recruiters r
            ON j.recruiter_id = r.recruiter_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE a.profile_id = ?

        ORDER BY a.applied_at DESC
    `;


    const [rows] =
            await db.execute(
                    sql,
                    [profile_id]
            );


    return rows;
};


// ==========================================
// GET APPLICATIONS BY JOB
// ==========================================

const getApplicationsByJob = async (job_id) => {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            p.user_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.expected_salary,
            p.location,
            p.profile_picture,
            p.resume_summary

        FROM applications a

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        WHERE a.job_id = ?

        ORDER BY a.applied_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [job_id]
    );

    return rows;
};


// ==========================================
// CHECK DUPLICATE APPLICATION
// ==========================================

const checkExistingApplication = async (
    job_id,
    profile_id
) => {

    const sql = `
        SELECT
            application_id,
            application_status

        FROM applications

        WHERE job_id = ?
        AND profile_id = ?
    `;

    const [rows] = await db.execute(
        sql,
        [job_id, profile_id]
    );

    return rows[0];
};


// ==========================================
// UPDATE APPLICATION STATUS
// ==========================================

const updateApplicationStatus = async (
    application_id,
    application_status
) => {

    const sql = `
        UPDATE applications

        SET
            application_status = ?,
            reviewed_at = NOW()

        WHERE application_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            application_status,
            application_id
        ]
    );

    return result;
};


// ==========================================
// WITHDRAW APPLICATION
// ==========================================

const withdrawApplication = async (
    application_id
) => {

    const sql = `
        UPDATE applications

        SET
            application_status = 'withdrawn'

        WHERE application_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [application_id]
    );

    return result;
};

const getProfileIdByUserId = async (user_id) => {

    const sql = `
        SELECT profile_id
        FROM profiles
        WHERE user_id = ?
        LIMIT 1
    `;


    const [rows] =
            await db.execute(
                    sql,
                    [user_id]
            );


    return rows.length > 0
            ? rows[0].profile_id
            : null;
};

// ==========================================
// EXPORT
// ==========================================

module.exports = {

    createApplication,

    getApplicationDetailsById,

    getApplicationById,

    getApplicationsByProfile,

    getApplicationsByJob,

    checkExistingApplication,

    updateApplicationStatus,

    withdrawApplication,

    getProfileIdByUserId

};