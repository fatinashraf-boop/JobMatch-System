const db = require("../config/db");


// ==========================================
// CREATE JOB
// ==========================================

const createJob = async (
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
) => {

    const sql = `
        INSERT INTO jobs (
            recruiter_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status,
            created_at,
            updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    `;

    const [result] = await db.execute(
        sql,
        [
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
        ]
    );

    return result;
};


// ==========================================
// GET ALL JOBS
// ==========================================

const getAllJobs = async () => {

    const sql = `
        SELECT
            job_id,
            recruiter_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status,
            created_at,
            updated_at
        FROM jobs
        ORDER BY created_at DESC
    `;

    const [rows] = await db.execute(sql);

    return rows;
};

// ==========================================
// GET JOB BY ID WITH RECRUITER DETAILS
// ==========================================

const getJobById = async (job_id) => {

    const sql = `
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

            r.company_name,
            r.company_email,
            r.company_description,
            r.website,
            r.company_logo,
            r.address,
            r.verification_status

        FROM jobs j

        INNER JOIN recruiters r
            ON j.recruiter_id = r.recruiter_id

        WHERE j.job_id = ?
    `;

    const [rows] =
        await db.execute(
            sql,
            [job_id]
        );

    return rows[0];
};


// ==========================================
// GET JOBS BY RECRUITER
// ==========================================


const getJobsByRecruiter = async (recruiter_id) => {

    const sql = `
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
                  AND NOT EXISTS (
                      SELECT 1
                      FROM shortlisted_candidates sc_done
                      WHERE sc_done.application_id = a.application_id
                        AND sc_done.shortlist_status = 'hired'
                  )
            ) AS applicant_count,

            (
                SELECT COUNT(*)
                FROM shortlisted_candidates sc
                INNER JOIN applications a
                    ON a.application_id = sc.application_id
                WHERE a.job_id = j.job_id
                  AND a.application_status IN (
                      'pending',
                      'reviewed',
                      'shortlisted'
                  )
                  AND sc.shortlist_status IN (
                      'active',
                      'interviewed',
                      'offer_sent'
                  )
            ) AS shortlisted_count

        FROM jobs j
        WHERE j.recruiter_id = ?
        ORDER BY j.created_at DESC
    `;

    const [rows] = await db.execute(
        sql,
        [recruiter_id]
    );

    return rows;
};



// ==========================================
// UPDATE JOB
// ==========================================

const updateJob = async (
    job_id,
    job_title,
    job_description,
    required_skills,
    qualifications,
    location,
    salary_min,
    salary_max,
    job_type,
    status
) => {

    const sql = `
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
    `;

    const [result] = await db.execute(
        sql,
        [
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status,
            job_id
        ]
    );

    return result;
};


// ==========================================
// UPDATE JOB STATUS ONLY
// ==========================================

const updateJobStatus = async (
    job_id,
    status
) => {

    const sql = `
        UPDATE jobs
        SET
            status = ?,
            updated_at = NOW()
        WHERE job_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [
            status,
            job_id
        ]
    );

    return result;
};


// ==========================================
// DELETE JOB
// ==========================================

const deleteJob = async (job_id) => {

    const sql = `
        DELETE FROM jobs
        WHERE job_id = ?
    `;

    const [result] = await db.execute(
        sql,
        [job_id]
    );

    return result;
};


// ==========================================
// SEARCH JOBS
// ==========================================

const searchJobs = async (
    keyword,
    location,
    job_type
) => {

    let sql = `
        SELECT
            job_id,
            recruiter_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status,
            created_at,
            updated_at
        FROM jobs
        WHERE status = 'open'
    `;

    const values = [];


    // Search keyword
    if (keyword) {

        sql += `
            AND (
                job_title LIKE ?
                OR job_description LIKE ?
                OR required_skills LIKE ?
                OR qualifications LIKE ?
            )
        `;

        const searchKeyword =
            `%${keyword}%`;

        values.push(
            searchKeyword,
            searchKeyword,
            searchKeyword,
            searchKeyword
        );
    }


    // Search location
    if (location) {

        sql += `
            AND location LIKE ?
        `;

        values.push(
            `%${location}%`
        );
    }


    // Search job type
    if (job_type) {

        sql += `
            AND job_type = ?
        `;

        values.push(
            job_type
        );
    }


    sql += `
        ORDER BY created_at DESC
    `;


    const [rows] =
        await db.execute(
            sql,
            values
        );

    return rows;
};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    createJob,

    getAllJobs,

    getJobById,

    getJobsByRecruiter,

    updateJob,

    updateJobStatus,

    deleteJob,

    searchJobs

};