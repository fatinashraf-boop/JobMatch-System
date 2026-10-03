const db = require("../config/db");

/**
 * User Model
 * Handles all database operations related to the users table.
 */
class User {

    /**
     * Create a new user
     * @param {Object} userData
     * @returns {Number} insertId
     */
    static async create(userData, connection = db) {

        const {
            full_name,
            email,
            password_hash,
            role,
            phone_number
        } = userData;

        const sql = `
            INSERT INTO users
            (
                full_name,
                email,
                password_hash,
                role,
                phone_number
            )
            VALUES (?, ?, ?, ?, ?)
        `;

        const [result] = await connection.execute(sql, [
            full_name,
            email,
            password_hash,
            role,
            phone_number
        ]);

        return result.insertId;
    }

    /**
     * Find user by Email
     * @param {String} email
     * @returns {Object|null}
     */
    static async findByEmail(email) {

        const sql = `
            SELECT *
            FROM users
            WHERE email = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [email]);

        if (rows.length === 0) {
            return null;
        }

        return rows[0];
    }

    /**
     * Find user by ID
     * @param {Number} userId
     * @returns {Object|null}
     */
    static async findById(userId) {

        const sql = `
            SELECT
                user_id,
                full_name,
                email,
                role,
                phone_number,
                created_at,
                updated_at
            FROM users
            WHERE user_id = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [userId]);

        if (rows.length === 0) {
            return null;
        }

        return rows[0];
    }

    /**
     * Get all users
     * (Admin use)
     */
    static async getAll() {

        const sql = `
            SELECT
                user_id,
                full_name,
                email,
                role,
                phone_number,
                created_at
            FROM users
            ORDER BY created_at DESC
        `;

        const [rows] = await db.execute(sql);

        return rows;
    }

    /**
     * Update basic profile
     */
    static async update(userId, userData) {

        const {
            full_name,
            phone_number
        } = userData;

        const sql = `
            UPDATE users
            SET
                full_name = ?,
                phone_number = ?,
                updated_at = NOW()
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [
            full_name,
            phone_number,
            userId
        ]);

        return result.affectedRows;
    }

    /**
     * Update Password
     */
    static async updatePassword(userId, password_hash) {

        const sql = `
            UPDATE users
            SET
                password_hash = ?,
                updated_at = NOW()
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [
            password_hash,
            userId
        ]);

        return result.affectedRows;
    }

    /**
     * Delete User
     */
    static async delete(userId) {

        const sql = `
            DELETE FROM users
            WHERE user_id = ?
        `;

        const [result] = await db.execute(sql, [userId]);

        return result.affectedRows;
    }

    /**
     * Check if email already exists
     */
    static async emailExists(email) {

        const sql = `
            SELECT user_id
            FROM users
            WHERE email = ?
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [email]);

        return rows.length > 0;
    }

}

module.exports = User;