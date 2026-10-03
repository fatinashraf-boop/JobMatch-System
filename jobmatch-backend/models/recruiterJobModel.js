const db = require("../config/db");


// =====================================================
// FIND RECRUITER USING JWT USER ID
// =====================================================

exports.getRecruiterByUserId = async (userId) => {

    const [rows] = await db.query(
        `
        SELECT
            recruiter_id,
            user_id,
            recruiter_status,
            verification_status
        FROM recruiters
        WHERE user_id = ?
        LIMIT 1
        `,
        [userId]
    );

    return rows.length > 0
        ? rows[0]
        : null;
};


// =====================================================
// CREATE JOB
// =====================================================

exports.createJob = async (
    recruiterId,
    jobData
) => {

    const {
        job_title,
        job_description,
        required_skills,
        qualifications,
        location,
        salary_min,
        salary_max,
        job_type,
        status
    } = jobData;


    const [result] = await db.query(
        `
        INSERT INTO jobs
        (
            recruiter_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `,
        [
            recruiterId,
            job_title,
            job_description,
            required_skills,
            qualifications || null,
            location,
            salary_min,
            salary_max,
            job_type,
            status
        ]
    );


    return result.insertId;
};


// =====================================================
// GET CREATED JOB
// =====================================================

exports.getJobById = async (
    jobId,
    recruiterId
) => {

    const [rows] = await db.query(
        `
        SELECT
            job_id,
            recruiter_id,
            job_title,
            status
        FROM jobs
        WHERE job_id = ?
          AND recruiter_id = ?
        LIMIT 1
        `,
        [
            jobId,
            recruiterId
        ]
    );


    return rows.length > 0
        ? rows[0]
        : null;
};

// =====================================================
// GET ALL JOBS OWNED BY RECRUITER
// WITH APPLICANT + SHORTLIST COUNTS
// =====================================================

exports.getJobsByRecruiter = async (recruiterId) => {

    const [rows] = await db.query(
        `
        SELECT
            j.job_id,
            j.recruiter_id,
            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status,
            j.created_at,
            j.updated_at,

            (
                SELECT COUNT(*)
                FROM applications a
                WHERE a.job_id = j.job_id
                  AND a.application_status IN (
                        'pending',
                        'reviewed',
                        'shortlisted'
                    )
            ) AS applicant_count,

            (
                SELECT COUNT(*)
                FROM shortlisted_candidates sc
                INNER JOIN applications a2
                    ON a2.application_id = sc.application_id
                WHERE a2.job_id = j.job_id
                  AND a2.application_status <> 'withdrawn'
                  AND sc.shortlist_status <> 'removed'
            ) AS shortlisted_count

        FROM jobs j

        WHERE j.recruiter_id = ?

        ORDER BY j.created_at DESC
        `,
        [recruiterId]
    );

    return rows;
};


// =====================================================
// GET ONE RECRUITER-OWNED JOB
// =====================================================

exports.getOwnedJobDetail = async (
    recruiterId,
    jobId
) => {

    const [rows] = await db.query(
        `
        SELECT
            j.job_id,
            j.recruiter_id,
            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status,
            j.created_at,
            j.updated_at,

            COUNT(DISTINCT a.application_id)
                AS applicant_count

        FROM jobs j

        LEFT JOIN applications a
            ON a.job_id = j.job_id

        WHERE j.job_id = ?
          AND j.recruiter_id = ?

        GROUP BY
            j.job_id,
            j.recruiter_id,
            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.location,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status,
            j.created_at,
            j.updated_at

        LIMIT 1
        `,
        [
            jobId,
            recruiterId
        ]
    );

    return rows.length > 0
            ? rows[0]
            : null;
};


// =====================================================
// UPDATE JOB
// =====================================================

exports.updateJob = async (
    recruiterId,
    jobId,
    jobData
) => {

    const {
        job_title,
        job_description,
        required_skills,
        qualifications,
        location,
        salary_min,
        salary_max,
        job_type,
        status
    } = jobData;


    const [result] = await db.query(
        `
        UPDATE jobs

        SET
            job_title = ?,
            job_description = ?,
            required_skills = ?,
            qualifications = ?,
            location = ?,
            salary_min = ?,
            salary_max = ?,
            job_type = ?,
            status = ?,
            updated_at = NOW()

        WHERE job_id = ?
          AND recruiter_id = ?
        `,
        [
            job_title,
            job_description,
            required_skills,
            qualifications || null,
            location,
            salary_min,
            salary_max,
            job_type,
            status,
            jobId,
            recruiterId
        ]
    );

    return result;
};


// =====================================================
// UPDATE JOB STATUS
// =====================================================

exports.updateJobStatus = async (
    recruiterId,
    jobId,
    status
) => {

    const [result] = await db.query(
        `
        UPDATE jobs

        SET
            status = ?,
            updated_at = NOW()

        WHERE job_id = ?
          AND recruiter_id = ?
        `,
        [
            status,
            jobId,
            recruiterId
        ]
    );

    return result;
};