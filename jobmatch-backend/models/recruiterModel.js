const db = require("../config/db");

class Recruiter {

    // ==========================================
    // CREATE RECRUITER PROFILE
    // ==========================================
    static async create(recruiterData, connection = db) {

        const {
            user_id,
            company_name,
            company_email,
            company_description,
            website,
            company_logo,
            address,
            verification_status,
            recruiter_status
        } = recruiterData;

        const sql = `
            INSERT INTO recruiters (
                user_id,
                company_name,
                company_email,
                company_description,
                website,
                company_logo,
                address,
                verification_status,
                recruiter_status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;

        const [result] = await connection.execute(sql, [

            user_id,

            company_name,

            company_email || null,

            company_description || null,

            website || null,

            company_logo || null,

            address || null,

            verification_status || "pending",

            recruiter_status || "incomplete"

        ]);

        return result.insertId;
    }


    // ==========================================
    // FIND RECRUITER BY USER ID
    // ==========================================
    // Used to connect:
    //
    // users.user_id
    //       ↓
    // recruiters.user_id
    //
    // This is useful when the logged-in user
    // wants to access their recruiter profile.
    // ==========================================
    static async findByUserId(user_id) {

        const sql = `
            SELECT
                recruiter_id,
                user_id,
                company_name,
                company_email,
                company_description,
                website,
                company_logo,
                address,
                verification_status,
                recruiter_status,
                created_at,
                updated_at
            FROM recruiters
            WHERE user_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [
            user_id
        ]);

        return rows.length > 0
            ? rows[0]
            : null;
    }


    // ==========================================
    // FIND RECRUITER BY RECRUITER ID
    // ==========================================
    static async findById(recruiter_id) {

        const sql = `
            SELECT
                recruiter_id,
                user_id,
                company_name,
                company_email,
                company_description,
                website,
                company_logo,
                address,
                verification_status,
                recruiter_status,
                created_at,
                updated_at
            FROM recruiters
            WHERE recruiter_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [
            recruiter_id
        ]);

        return rows.length > 0
            ? rows[0]
            : null;
    }

// ==========================================
// FIND FULL RECRUITER PROFILE
// JOIN users + recruiters
// ==========================================
static async findFullProfileByUserId(user_id) {

    const sql = `
        SELECT
            u.user_id,
            u.full_name,
            u.email,
            u.phone_number,
            u.role,

            r.recruiter_id,
            r.company_name,
            r.company_email,
            r.company_description,
            r.website,
            r.company_logo,
            r.address,
            r.verification_status,
            r.recruiter_status,
            r.created_at AS recruiter_created_at,
            r.updated_at AS recruiter_updated_at

        FROM users u

        INNER JOIN recruiters r
            ON u.user_id = r.user_id

        WHERE u.user_id = ?

        LIMIT 1
    `;

    const [rows] =
        await db.execute(sql, [user_id]);

    return rows.length > 0
        ? rows[0]
        : null;
}


    // ==========================================
    // UPDATE RECRUITER PROFILE
    // ==========================================
    // The verification_status is intentionally
    // NOT updated here.
    //
    // Recruiters should not be able to change
    // their own verification status.
    // ==========================================
    static async update(
        user_id,
        recruiterData
    ) {

        const {
            company_name,
            company_email,
            company_description,
            website,
            company_logo,
            address,
            recruiter_status
        } = recruiterData;

        const sql = `
            UPDATE recruiters
            SET
                company_name = ?,
                company_email = ?,
                company_description = ?,
                website = ?,
                company_logo = ?,
                address = ?,
                recruiter_status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [

            company_name,

            company_email || null,

            company_description || null,

            website || null,

            company_logo || null,

            address || null,

            recruiter_status || "incomplete",

            user_id

        ]);

        return result.affectedRows;
    }


    // ==========================================
    // DELETE RECRUITER PROFILE
    // ==========================================
    static async delete(user_id) {

        const sql = `
            DELETE FROM recruiters
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [
            user_id
        ]);

        return result.affectedRows;
    }


    // ==========================================
    // CHECK IF RECRUITER PROFILE EXISTS
    // ==========================================
    static async exists(user_id) {

        const sql = `
            SELECT recruiter_id
            FROM recruiters
            WHERE user_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [
            user_id
        ]);

        return rows.length > 0;
    }


    // ==========================================
    // UPDATE VERIFICATION STATUS
    // ==========================================
    // This function should only be called
    // by an Admin controller.
    //
    // Valid values:
    // pending
    // trusted
    // untrusted
    // ==========================================
    static async updateVerificationStatus(
        recruiter_id,
        verification_status
    ) {

        const sql = `
            UPDATE recruiters
            SET
                verification_status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE recruiter_id = ?
        `;

        const [result] = await db.execute(sql, [

            verification_status,

            recruiter_id

        ]);

        return result.affectedRows;
    }

    // Update only the logo; never reset company information.
    static async updateLogo(user_id, logoPath) {
        const [result] = await db.execute(
            'UPDATE recruiters SET company_logo = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?',
            [logoPath, user_id]
        );
        return result.affectedRows;
    }

}


module.exports = Recruiter;