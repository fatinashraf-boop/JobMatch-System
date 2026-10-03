const db = require("../config/db");

class Profile {

    // Create a new profile
    static async create(profileData, connection = db) {

        const {
            user_id,
            headline,
            skills,
            education,
            experience,
            expected_salary,
            location,
            profile_picture,
            resume_summary,
            profile_status
        } = profileData;

        const sql = `
            INSERT INTO profiles (
                user_id,
                headline,
                skills,
                education,
                experience,
                expected_salary,
                location,
                profile_picture,
                resume_summary,
                profile_status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;

        const [result] = await connection.execute(sql, [
            user_id,
            headline || null,
            skills || null,
            education || null,
            experience || null,
            expected_salary || null,
            location || null,
            profile_picture || null,
            resume_summary || null,
            profile_status || "incomplete"
        ]);

        return result.insertId;
    }


    // Get profile by user ID
    static async findByUserId(user_id) {

        const sql = `
            SELECT
                profile_id,
                user_id,
                headline,
                skills,
                education,
                experience,
                expected_salary,
                location,
                profile_picture,
                resume_summary,
                profile_status,
                created_at,
                updated_at
            FROM profiles
            WHERE user_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [user_id]);

        return rows.length > 0 ? rows[0] : null;
    }


    // Get profile by profile ID
    static async findById(profile_id) {

        const sql = `
            SELECT
                profile_id,
                user_id,
                headline,
                skills,
                education,
                experience,
                expected_salary,
                location,
                profile_picture,
                resume_summary,
                profile_status,
                created_at,
                updated_at
            FROM profiles
            WHERE profile_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [profile_id]);

        return rows.length > 0 ? rows[0] : null;
    }


    // Update profile
    static async update(user_id, profileData) {

        const {
            headline,
            skills,
            education,
            experience,
            expected_salary,
            location,
            profile_picture,
            resume_summary,
            profile_status
        } = profileData;

        const sql = `
            UPDATE profiles
            SET
                headline = ?,
                skills = ?,
                education = ?,
                experience = ?,
                expected_salary = ?,
                location = ?,
                profile_picture = ?,
                resume_summary = ?,
                profile_status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [
            headline || null,
            skills || null,
            education || null,
            experience || null,
            expected_salary || null,
            location || null,
            profile_picture || null,
            resume_summary || null,
            profile_status || "incomplete",
            user_id
        ]);

        return result.affectedRows;
    }


    // Delete profile
    static async delete(user_id) {

        const sql = `
            DELETE FROM profiles
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [user_id]);

        return result.affectedRows;
    }


    // Check whether user already has a profile
    static async exists(user_id) {

        const sql = `
            SELECT profile_id
            FROM profiles
            WHERE user_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [user_id]);

        return rows.length > 0;
    }

// =====================================================
// GET FULL JOBSEEKER PROFILE
// JOIN users + profiles
// =====================================================

static async findFullProfileByUserId(user_id) {

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
            p.created_at,
            p.updated_at

        FROM users u

        INNER JOIN profiles p
            ON u.user_id = p.user_id

        WHERE u.user_id = ?

        LIMIT 1
    `;

    const [rows] =
            await db.execute(
                    sql,
                    [user_id]
            );

    if (rows.length === 0) {
        return null;
    }

    const row = rows[0];

    return {
        user: {
            user_id: row.user_id,
            full_name: row.full_name,
            email: row.email,
            phone_number: row.phone_number
        },

        profile: {
            profile_id: row.profile_id,
            headline: row.headline,
            skills: row.skills,
            education: row.education,
            experience: row.experience,
            expected_salary: row.expected_salary,
            location: row.location,
            profile_picture: row.profile_picture,
            resume_summary: row.resume_summary,
            profile_status: row.profile_status,
            created_at: row.created_at,
            updated_at: row.updated_at
        }
    };
}



}

module.exports = Profile;