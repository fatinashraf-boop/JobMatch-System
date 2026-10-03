const db = require("../config/db");


// =====================================================
// GET RECRUITER BY LOGGED-IN USER
// =====================================================

const getRecruiterByUserId = async (userId) => {

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

    return rows[0] || null;
};


// =====================================================
// GET JOBS BELONGING TO RECRUITER
// =====================================================

const getRecruiterJobs = async (recruiterId) => {

    const sql = `
        SELECT
            job_id,
            job_title,
            location,
            job_type,
            status,
            created_at
        FROM jobs
        WHERE recruiter_id = ?
        ORDER BY created_at DESC
    `;

    const [rows] =
        await db.execute(
            sql,
            [recruiterId]
        );

    return rows;
};


// =====================================================
// CHECK JOB OWNERSHIP
// =====================================================

const getOwnedJob = async (
    recruiterId,
    jobId
) => {

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

    return rows[0] || null;
};


// =====================================================
// GET BEST CANDIDATE MATCHES FOR SELECTED JOB
// =====================================================

const getCandidateMatches = async (jobId) => {

    const sql = `
        SELECT
            ms.match_id,
            ms.job_id,
            ms.profile_id,
            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            u.full_name AS candidate_name,

            p.headline,
            p.location,
            p.expected_salary,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,
            p.profile_picture,

            a.application_id,
            a.application_status,

            sc.shortlist_id,
            sc.shortlist_status

        FROM match_scores ms

        INNER JOIN profiles p
            ON ms.profile_id = p.profile_id

        INNER JOIN users u
            ON p.user_id = u.user_id

        LEFT JOIN applications a
            ON a.job_id = ms.job_id
            AND a.profile_id = ms.profile_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        
        WHERE ms.job_id = ?

        AND p.profile_status = 'complete'

        -- Exclude candidates rejected or withdrawn for THIS job.
        AND NOT EXISTS (
            SELECT 1
            FROM applications a_excluded
            WHERE a_excluded.job_id = ms.job_id
            AND a_excluded.profile_id = ms.profile_id
            AND a_excluded.application_status IN (
                'rejected',
                'withdrawn'
            )
        )

        ORDER BY
            ms.similarity_score DESC,
            ms.ranking_position ASC

    `;

    const [rows] =
        await db.execute(
            sql,
            [jobId]
        );

    return rows;
};

// =====================================================
// GET CANDIDATE DETAIL FOR A SPECIFIC JOB
// =====================================================


const getCandidateDetail = async (jobId, profileId) => {

    const sql = `
        SELECT
            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,

            p.profile_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.expected_salary,
            p.location,
            p.profile_picture,
            p.resume_summary,
            p.profile_status,

            ms.match_id,
            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,

            a.application_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM applications a

        INNER JOIN profiles p
            ON p.profile_id = a.profile_id

        INNER JOIN users u
            ON u.user_id = p.user_id

        LEFT JOIN match_scores ms
            ON ms.job_id = a.job_id
            AND ms.profile_id = a.profile_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE a.job_id = ?
          AND a.profile_id = ?
          AND a.application_status != 'withdrawn'

        LIMIT 1
    `;

    const [rows] = await db.execute(
        sql,
        [jobId, profileId]
    );

    return rows[0] || null;
};


// =====================================================
// GET CANDIDATE DOCUMENTS + OCR
// =====================================================

const getCandidateDocuments = async (
    userId
) => {

    const sql = `
        SELECT
            d.document_id,
            d.file_name,
            d.file_path,
            d.file_type,
            d.document_type,
            d.upload_status,
            d.created_at,

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
            [userId]
        );


    return rows;
};


module.exports = {

    getRecruiterByUserId,

    getRecruiterJobs,

    getOwnedJob,

    getCandidateMatches,

    getCandidateDetail,

    getCandidateDocuments
};