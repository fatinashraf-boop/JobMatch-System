const db = require("../config/db");

class RecruiterApplicantModel {

    // =====================================================
    // GET RECRUITER FROM LOGGED-IN USER
    // =====================================================

    static async getRecruiterByUserId(userId) {

        const sql = `
            SELECT
                recruiter_id,
                user_id,
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

        return rows.length > 0
            ? rows[0]
            : null;
    }


    // =====================================================
    // VERIFY JOB BELONGS TO RECRUITER
    // =====================================================

    static async getOwnedJob(
        recruiterId,
        jobId
    ) {

        const sql = `
            SELECT
                job_id,
                recruiter_id,
                job_title,
                status
            FROM jobs
            WHERE job_id = ?
            AND recruiter_id = ?
            LIMIT 1
        `;

        const [rows] =
            await db.execute(
                sql,
                [
                    jobId,
                    recruiterId
                ]
            );

        return rows.length > 0
            ? rows[0]
            : null;
    }


    // =====================================================
    // GET APPLICANTS FOR JOB
    // =====================================================

    static async getApplicantsByJob(jobId) {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,

            p.headline,
            p.location,
            p.expected_salary,
            p.profile_picture,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,

            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM applications a

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        INNER JOIN users u
            ON p.user_id = u.user_id

        LEFT JOIN match_scores ms
            ON ms.job_id = a.job_id
            AND ms.profile_id = a.profile_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE a.job_id = ?
        AND a.application_status NOT IN (
            'rejected',
            'withdrawn',
            'hired'
        )
        AND (
            sc.shortlist_status IS NULL
            OR sc.shortlist_status <> 'hired'
        )
        ORDER BY
            CASE
                WHEN ms.similarity_score IS NULL THEN 1
                ELSE 0
            END ASC,

            ms.similarity_score DESC,
            a.applied_at ASC
    `;

    const [rows] =
        await db.execute(
            sql,
            [jobId]
        );

    return rows;
}

// =====================================================
// GET ALL APPLICANTS FOR RECRUITER
// =====================================================

static async getApplicantsByRecruiter(
    recruiterId
) {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,

            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,

            p.headline,
            p.location,
            p.expected_salary,
            p.profile_picture,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,

            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        INNER JOIN users u
            ON p.user_id = u.user_id

        LEFT JOIN match_scores ms
            ON ms.job_id = a.job_id
            AND ms.profile_id = a.profile_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE j.recruiter_id = ?
        AND a.application_status NOT IN (
            'rejected',
            'withdrawn',
            'hired'
        )
        AND (
            sc.shortlist_status IS NULL
            OR sc.shortlist_status <> 'hired'
        )

        ORDER BY
            CASE
                WHEN ms.similarity_score IS NULL THEN 1
                ELSE 0
            END ASC,

            ms.similarity_score DESC,
            a.applied_at DESC
    `;

    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );

    return rows;
}

// =====================================================
// GET APPLICANT HISTORY FOR RECRUITER
// =====================================================

static async getApplicantHistoryByRecruiter(
    recruiterId
) {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,

            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,

            p.headline,
            p.location,
            p.expected_salary,
            p.profile_picture,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,

            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM applications a

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        INNER JOIN users u
            ON p.user_id = u.user_id

        LEFT JOIN match_scores ms
            ON ms.job_id = a.job_id
            AND ms.profile_id = a.profile_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE j.recruiter_id = ?

        AND (
            a.application_status IN (
                'rejected',
                'withdrawn',
                'hired'
            )

            OR sc.shortlist_status = 'hired'
        )

        ORDER BY
            COALESCE(
                a.reviewed_at,
                a.applied_at
            ) DESC
    `;


    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );


    return rows;
}

// =====================================================
// GET SHORTLISTED APPLICANTS FOR RECRUITER
// =====================================================

static async getShortlistedByRecruiter(
    recruiterId
) {

    const sql = `
        SELECT
            a.application_id,
            a.job_id,
            a.profile_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            j.job_title,

            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,

            p.headline,
            p.location,
            p.expected_salary,
            p.profile_picture,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,

            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM shortlisted_candidates sc

        INNER JOIN applications a
            ON sc.application_id = a.application_id

        INNER JOIN jobs j
            ON a.job_id = j.job_id

        INNER JOIN profiles p
            ON a.profile_id = p.profile_id

        INNER JOIN users u
            ON p.user_id = u.user_id

        LEFT JOIN match_scores ms
            ON ms.job_id = a.job_id
            AND ms.profile_id = a.profile_id

        WHERE j.recruiter_id = ?
        
        AND a.application_status NOT IN (
            'rejected',
            'withdrawn'
        )
        AND sc.shortlist_status NOT IN (
            'removed',
            'hired'
        )

        ORDER BY
            CASE
                WHEN ms.similarity_score IS NULL THEN 1
                ELSE 0
            END ASC,

            ms.similarity_score DESC,
            sc.shortlisted_at DESC
    `;

    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );

    return rows;
}

    // =====================================================
    // GET SINGLE APPLICATION
    // =====================================================

    static async getApplication(
        applicationId
    ) {

        const sql = `
            SELECT
                a.application_id,
                a.job_id,
                a.profile_id,
                a.application_status,
                a.applied_at,
                a.reviewed_at,

                j.recruiter_id

            FROM applications a

            INNER JOIN jobs j
                ON a.job_id = j.job_id

            WHERE a.application_id = ?

            LIMIT 1
        `;

        const [rows] =
            await db.execute(
                sql,
                [applicationId]
            );

        return rows.length > 0
            ? rows[0]
            : null;
    }


    // =====================================================
    // UPDATE APPLICATION STATUS
    // =====================================================

    static async updateApplicationStatus(
        applicationId,
        status
    ) {

        const sql = `
            UPDATE applications
            SET
                application_status = ?,
                reviewed_at =
                    CASE
                        WHEN ? IN ('reviewed', 'rejected')
                        THEN CURRENT_TIMESTAMP
                        ELSE reviewed_at
                    END
            WHERE application_id = ?
        `;

        const [result] =
            await db.execute(
                sql,
                [
                    status,
                    status,
                    applicationId
                ]
            );

        return result.affectedRows;
    }


    // =====================================================
    // GET SHORTLIST
    // =====================================================

    static async getShortlistByApplication(
        applicationId
    ) {

        const sql = `
            SELECT
                shortlist_id,
                shortlisted_by,
                application_id,
                shortlist_status,
                notes,
                shortlisted_at
            FROM shortlisted_candidates
            WHERE application_id = ?
            LIMIT 1
        `;

        const [rows] =
            await db.execute(
                sql,
                [applicationId]
            );

        return rows.length > 0
            ? rows[0]
            : null;
    }


    // =====================================================
    // CREATE SHORTLIST
    // =====================================================

    static async createShortlist(
        recruiterId,
        applicationId
    ) {

        const sql = `
            INSERT INTO shortlisted_candidates
            (
                shortlisted_by,
                application_id,
                shortlist_status
            )
            VALUES (?, ?, 'active')
        `;

        const [result] =
            await db.execute(
                sql,
                [
                    recruiterId,
                    applicationId
                ]
            );

        return result.insertId;
    }


    // =====================================================
    // UPDATE RECRUITMENT STATUS
    // =====================================================

    static async updateRecruitmentStatus(
        applicationId,
        status
    ) {

        const sql = `
            UPDATE shortlisted_candidates
            SET shortlist_status = ?
            WHERE application_id = ?
        `;

        const [result] =
            await db.execute(
                sql,
                [
                    status,
                    applicationId
                ]
            );

        return result.affectedRows;
    }
}

module.exports =
    RecruiterApplicantModel;