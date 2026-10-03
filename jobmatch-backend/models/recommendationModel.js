const db = require("../config/db");

class RecommendationModel {

    // Get all recommended jobs for a profile
    static async getRecommendations(profileId) {

        const [rows] = await db.query(
            `
            SELECT
                ms.match_id,
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

                r.company_name,
                r.company_logo

            FROM match_scores ms

            INNER JOIN jobs j
                ON ms.job_id = j.job_id

            INNER JOIN recruiters r
                ON j.recruiter_id = r.recruiter_id

            WHERE ms.profile_id = ?

            ORDER BY
                ms.similarity_score DESC,
                ms.ranking_position ASC
            `,
            [profileId]
        );

        return rows;
    }

    // Top recommendations
    static async getTopRecommendations(profileId, limit = 10) {

        const [rows] = await db.query(
            `
            SELECT
                ms.match_id,
                ms.similarity_score,
                ms.ranking_position,

                j.job_id,
                j.job_title,
                j.location,
                j.salary_min,
                j.salary_max,
                j.job_type,

                r.company_name

            FROM match_scores ms

            INNER JOIN jobs j
                ON ms.job_id = j.job_id

            INNER JOIN recruiters r
                ON j.recruiter_id = r.recruiter_id

            WHERE
                ms.profile_id = ?
                AND j.status='open'

            ORDER BY
                ms.similarity_score DESC

            LIMIT ?
            `,
            [profileId, Number(limit)]
        );

        return rows;
    }

    // Single recommendation
    static async getRecommendation(matchId) {

        const [rows] = await db.query(
            `
            SELECT
                ms.*,
                j.*,
                r.company_name,
                r.company_logo

            FROM match_scores ms

            INNER JOIN jobs j
                ON ms.job_id=j.job_id

            INNER JOIN recruiters r
                ON j.recruiter_id=r.recruiter_id

            WHERE ms.match_id=?
            `,
            [matchId]
        );

        return rows[0];
    }

}

module.exports = RecommendationModel;