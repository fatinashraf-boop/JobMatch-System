const db = require("../config/db");

// ==========================================
// CREATE MATCH SCORE
// ==========================================

const createMatchScore = async (
    job_id,
    profile_id,
    similarity_score,
    ranking_position,
    match_reason,
    matching_algorithm
) => {

    const sql = `
        INSERT INTO match_scores
        (
            job_id,
            profile_id,
            similarity_score,
            ranking_position,
            match_reason,
            matching_algorithm,
            calculated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await db.execute(sql, [
        job_id,
        profile_id,
        similarity_score,
        ranking_position,
        match_reason,
        matching_algorithm
    ]);

    return result;
};


// ==========================================
// GET MATCH BY ID
// ==========================================

const getMatchScoreById = async (match_id) => {

    const sql = `
        SELECT
            ms.match_id,
            ms.job_id,
            ms.profile_id,
            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,
            ms.calculated_at,

            j.job_title,

            p.user_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.expected_salary,
            p.location,
            p.profile_picture,
            p.resume_summary

        FROM match_scores ms

        INNER JOIN jobs j
            ON ms.job_id = j.job_id

        INNER JOIN profiles p
            ON ms.profile_id = p.profile_id

        WHERE ms.match_id = ?
    `;

    const [rows] = await db.execute(sql, [match_id]);

    return rows[0];
};


// ==========================================
// GET AI MATCHES BY JOB
// RECRUITER: ONLY CANDIDATES WHO APPLIED
// ==========================================

const getMatchesByJob = async (job_id) => {

    const sql = `
        SELECT
            ms.match_id,
            ms.job_id,
            ms.profile_id,
            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,
            ms.calculated_at,

            a.application_id,
            a.application_status,
            a.applied_at,
            a.reviewed_at,

            p.user_id,

            u.full_name,
            u.email,
            u.phone_number,

            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.expected_salary,
            p.location,
            p.profile_picture,
            p.resume_summary,

            sc.shortlist_id,
            sc.shortlist_status,
            sc.notes,
            sc.shortlisted_at

        FROM match_scores ms

        INNER JOIN applications a
            ON a.job_id = ms.job_id
            AND a.profile_id = ms.profile_id

        INNER JOIN profiles p
            ON p.profile_id = ms.profile_id

        INNER JOIN users u
            ON u.user_id = p.user_id

        LEFT JOIN shortlisted_candidates sc
            ON sc.application_id = a.application_id

        WHERE ms.job_id = ?

        AND a.application_status NOT IN ('rejected', 'withdrawn')

        ORDER BY
            ms.similarity_score DESC,
            ms.ranking_position ASC,
            a.applied_at ASC
    `;


    const [rows] =
        await db.execute(
            sql,
            [job_id]
        );


    return rows;
};


// ==========================================
// GET MATCHES BY PROFILE
// ==========================================

const getMatchesByProfile = async (profile_id) => {

    const sql = `
        SELECT
            ms.match_id,
            ms.job_id,
            ms.profile_id,
            ms.similarity_score,
            ms.ranking_position,
            ms.match_reason,
            ms.matching_algorithm,
            ms.calculated_at,

            j.job_title,
            j.job_description,
            j.required_skills,
            j.qualifications,
            j.salary_min,
            j.salary_max,
            j.job_type,
            j.status

        FROM match_scores ms

        INNER JOIN jobs j
            ON ms.job_id = j.job_id

        WHERE ms.profile_id = ?

        ORDER BY
            ms.similarity_score DESC,
            ms.ranking_position ASC
    `;

    const [rows] = await db.execute(sql, [profile_id]);

    return rows;
};


// ==========================================
// CHECK EXISTING MATCH
// ==========================================

const getExistingMatch = async (
    job_id,
    profile_id
) => {

    const sql = `
        SELECT
            match_id,
            job_id,
            profile_id,
            similarity_score,
            ranking_position,
            match_reason,
            matching_algorithm,
            calculated_at

        FROM match_scores

        WHERE job_id = ?
        AND profile_id = ?
    `;

    const [rows] = await db.execute(sql, [
        job_id,
        profile_id
    ]);

    return rows[0];
};


// ==========================================
// UPDATE MATCH
// ==========================================

const updateMatchScore = async (
    match_id,
    similarity_score,
    ranking_position,
    match_reason,
    matching_algorithm
) => {

    const sql = `
        UPDATE match_scores

        SET
            similarity_score = ?,
            ranking_position = ?,
            match_reason = ?,
            matching_algorithm = ?,
            calculated_at = NOW()

        WHERE match_id = ?
    `;

    const [result] = await db.execute(sql, [
        similarity_score,
        ranking_position,
        match_reason,
        matching_algorithm,
        match_id
    ]);

    return result;
};


// ==========================================
// DELETE MATCH
// ==========================================

const deleteMatchScore = async (match_id) => {

    const sql = `
        DELETE FROM match_scores
        WHERE match_id = ?
    `;

    const [result] = await db.execute(sql, [match_id]);

    return result;
};

const updateRankingsByJob = async (job_id) => {

    const [matches] = await db.execute(
        `
        SELECT
            match_id,
            similarity_score
        FROM match_scores
        WHERE job_id = ?
        ORDER BY
            similarity_score DESC,
            match_id ASC
        `,
        [job_id]
    );

    for (let i = 0; i < matches.length; i++) {

        const rankingPosition = i + 1;

        await db.execute(
            `
            UPDATE match_scores
            SET ranking_position = ?
            WHERE match_id = ?
            `,
            [
                rankingPosition,
                matches[i].match_id
            ]
        );
    }

    return matches.length;
};

module.exports = {
    createMatchScore,
    getMatchScoreById,
    getMatchesByJob,
    getMatchesByProfile,
    getExistingMatch,
    updateMatchScore,
    deleteMatchScore,
    updateRankingsByJob
};