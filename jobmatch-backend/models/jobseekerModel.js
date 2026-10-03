const db = require("../config/db");

class Jobseeker {

    static async getDashboardSummary(userId) {

        const sql = `
            SELECT
                u.user_id,
                u.full_name,

                p.profile_id,

                (
                    SELECT COUNT(*)
                    FROM applications a
                    WHERE a.profile_id = p.profile_id
                ) AS jobs_applied,

                (
                    SELECT COUNT(*)
                    FROM shortlisted_candidates sc
                    INNER JOIN applications a2
                        ON sc.application_id = a2.application_id
                    WHERE a2.profile_id = p.profile_id
                      AND sc.shortlist_status = 'interviewed'
                ) AS interviews

            FROM users u

            INNER JOIN profiles p
                ON u.user_id = p.user_id

            WHERE u.user_id = ?

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

        static async getBestMatches(userId, limit = 3) {

        const sql = `
            SELECT
                ms.match_id,
                ms.profile_id,
                ms.similarity_score,
                ms.ranking_position,
                ms.match_reason,
                ms.matching_algorithm,

                j.job_id,
                j.job_title,
                j.job_description,
                j.required_skills,
                j.qualifications,
                j.location,
                j.salary_min,
                j.salary_max,
                j.job_type,
                j.status,

                r.recruiter_id,
                r.company_name,
                r.company_logo

            FROM users u

            INNER JOIN profiles p
                ON p.user_id = u.user_id

            INNER JOIN match_scores ms
                ON ms.profile_id = p.profile_id

            INNER JOIN jobs j
                ON j.job_id = ms.job_id

            INNER JOIN recruiters r
                ON r.recruiter_id = j.recruiter_id

            WHERE u.user_id = ?
            AND j.status = 'open'

            ORDER BY
                CASE
                    WHEN ms.ranking_position IS NULL THEN 1
                    ELSE 0
                END,
                ms.ranking_position ASC,
                ms.similarity_score DESC

            LIMIT ?
        `;

        const [rows] =
                await db.execute(
                        sql,
                        [
                            userId,
                            Number(limit)
                        ]
                );

        return rows;
    }
}

module.exports = Jobseeker;