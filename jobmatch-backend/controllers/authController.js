const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const db = require("../config/db");

const User = require("../models/userModel");
const Profile = require("../models/profileModel");
const Recruiter = require("../models/recruiterModel");

const SALT_ROUNDS = 10;

// =====================================================
// REGISTRATION VALIDATION
// =====================================================

const EMAIL_MAX_LENGTH = 100;
const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_MAX_LENGTH = 72;

const EMAIL_REGEX =
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const PASSWORD_REGEX =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/;

// =====================================================
// GENERATE JWT TOKEN
// =====================================================

const generateToken = (user) => {

    return jwt.sign(
        {
            user_id: user.user_id,
            email: user.email,
            role: user.role
        },
        process.env.JWT_SECRET,
        {
            expiresIn:
                process.env.JWT_EXPIRES_IN || "1d"
        }
    );
};


// =====================================================
// REGISTER USER
// POST /api/auth/register
// =====================================================


exports.register = async (req, res) => {

    let connection = null;
    let transactionStarted = false;

    try {

        // ==========================================
        // 1. NORMALIZE INPUT
        // ==========================================

        let {
            full_name,
            email,
            password,
            role,
            phone_number
        } = req.body || {};

        full_name = typeof full_name === "string"
            ? full_name.trim()
            : "";

        email = typeof email === "string"
            ? email.trim().toLowerCase()
            : "";

        password = typeof password === "string"
            ? password
            : "";

        role = typeof role === "string"
            ? role.trim().toLowerCase()
            : "";

        phone_number = typeof phone_number === "string"
            ? phone_number.trim()
            : "";


        // ==========================================
        // 2. VALIDATE REQUIRED FIELDS
        // ==========================================

        if (
            !full_name ||
            !email ||
            !password ||
            !role ||
            !phone_number
        ) {
            return res.status(400).json({
                success: false,
                message: "Please fill in all required fields."
            });
        }


        // ==========================================
        // 3. VALIDATE EMAIL
        // ==========================================

        if (
            email.length > EMAIL_MAX_LENGTH ||
            !EMAIL_REGEX.test(email)
        ) {
            return res.status(400).json({
                success: false,
                message: "Please enter a valid email address."
            });
        }


        // ==========================================
        // 4. VALIDATE PASSWORD
        // ==========================================

        if (
            password.length < PASSWORD_MIN_LENGTH ||
            password.length > PASSWORD_MAX_LENGTH
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Password must be between 8 and 72 characters."
            });
        }

        if (!PASSWORD_REGEX.test(password)) {
            return res.status(400).json({
                success: false,
                message:
                    "Password must include at least one uppercase letter, one lowercase letter, one number and one special character."
            });
        }


        // ==========================================
        // 5. VALIDATE ROLE
        // ==========================================

        if (!["jobseeker", "recruiter"].includes(role)) {
            return res.status(400).json({
                success: false,
                message: "Invalid user role."
            });
        }


        // ==========================================
        // 6. CHECK DUPLICATE EMAIL
        // ==========================================

        const emailExists = await User.emailExists(email);

        if (emailExists) {
            return res.status(409).json({
                success: false,
                message: "Email already exists."
            });
        }


        // ==========================================
        // 7. HASH PASSWORD
        // ==========================================

        const password_hash = await bcrypt.hash(
            password,
            SALT_ROUNDS
        );


        // ==========================================
        // 8. BEGIN DATABASE TRANSACTION
        // ==========================================

        connection = await db.getConnection();

        await connection.beginTransaction();

        transactionStarted = true;


        // ==========================================
        // 9. CREATE USER
        // ==========================================

        const userId = await User.create(
            {
                full_name,
                email,
                password_hash,
                role,
                phone_number
            },
            connection
        );


        // ==========================================
        // 10. CREATE ROLE-SPECIFIC PROFILE
        // ==========================================

        if (role === "jobseeker") {

            await Profile.create(
                {
                    user_id: userId,
                    headline: null,
                    skills: null,
                    education: null,
                    experience: null,
                    expected_salary: null,
                    location: null,
                    profile_picture: null,
                    resume_summary: null,
                    profile_status: "incomplete"
                },
                connection
            );

            console.log(
                "Jobseeker profile created for user:",
                userId
            );

        } else if (role === "recruiter") {

            await Recruiter.create(
                {
                    user_id: userId,
                    company_name: null,
                    company_email: null,
                    company_description: null,
                    website: null,
                    company_logo: null,
                    address: null,
                    verification_status: "pending",
                    recruiter_status: "incomplete"
                },
                connection
            );

            console.log(
                "Recruiter profile created for user:",
                userId
            );
        }

        // ==========================================
        // 11. PREPARE RESPONSE BEFORE COMMIT
        // ==========================================

        const [createdUsers] = await connection.execute(
            `SELECT user_id, full_name, email, role,
                    phone_number, created_at, updated_at
            FROM users
            WHERE user_id = ?
            LIMIT 1`,
            [userId]
        );

        if (createdUsers.length === 0) {
            throw new Error("Created user could not be retrieved.");
        }

        const newUser = createdUsers[0];

        const token = generateToken({
            user_id: userId,
            email,
            role
        });

        // ==========================================
        // 12. COMMIT TRANSACTION
        // ==========================================

        await connection.commit();

        transactionStarted = false;

        connection.release();
        connection = null;

        // ==========================================
        // 13. RETURN SUCCESS
        // ==========================================

        return res.status(201).json({
            success: true,
            message: "Registration successful.",
            token,
            user: newUser
        });

    } catch (error) {

        if (connection && transactionStarted) {
            try {
                await connection.rollback();
                console.log("Registration transaction rolled back.");
            } catch (rollbackError) {
                console.error("Registration Rollback Error:", rollbackError);
            }
        }

        console.error("Registration Error:", error);

        if (error.code === "ER_DUP_ENTRY") {
            return res.status(409).json({
                success: false,
                message: "Email already exists."
            });
        }

        return res.status(500).json({
            success: false,
            message: "Internal Server Error"
        });

    } finally {

        if (connection) {
            connection.release();
        }
    }
};
// =====================================================
// LOGIN USER
// POST /api/auth/login
// =====================================================

exports.login = async (req, res) => {

    try {

        const {
            email,
            password
        } = req.body;


        // =====================================================
        // VALIDATE
        // =====================================================

        if (!email || !password) {

            return res.status(400).json({

                success: false,

                message:
                    "Email and Password are required."

            });
        }


        // =====================================================
        // FIND USER
        // =====================================================

        const user =
            await User.findByEmail(
                email.trim().toLowerCase()
            );


        if (!user) {

            return res.status(401).json({

                success: false,

                message:
                    "Invalid email or password."

            });
        }


        // =====================================================
        // CHECK PASSWORD
        // =====================================================

        const validPassword =
            await bcrypt.compare(
                password,
                user.password_hash
            );


        if (!validPassword) {

            return res.status(401).json({

                success: false,

                message:
                    "Invalid email or password."

            });
        }


        // =====================================================
        // GET ONBOARDING STATUS
        // =====================================================

        let onboardingStatus =
            "incomplete";


        if (user.role === "jobseeker") {

            const profile =
                await Profile.findByUserId(
                    user.user_id
                );


            if (profile) {

                onboardingStatus =
                    profile.profile_status
                    || "incomplete";
            }

        } else if (user.role === "recruiter") {

            const recruiter =
                await Recruiter.findByUserId(
                    user.user_id
                );


            if (recruiter) {

                onboardingStatus =
                    recruiter.recruiter_status
                    || "incomplete";
            }
        }


        // =====================================================
        // GENERATE TOKEN
        // =====================================================

        const token =
            generateToken(user);


        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return res.status(200).json({

            success: true,

            message:
                "Login successful.",

            token,

            user: {

                user_id:
                    user.user_id,

                full_name:
                    user.full_name,

                email:
                    user.email,

                phone_number:
                    user.phone_number,

                role:
                    user.role,

                onboarding_status:
                    onboardingStatus
            }

        });


    } catch (error) {

        console.error(
            "Login Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Internal Server Error"

        });
    }
};


// =====================================================
// GET LOGGED-IN USER
// GET /api/auth/profile
// =====================================================

exports.getProfile = async (req, res) => {

    try {

        const user =
            await User.findById(
                req.user.user_id
            );


        if (!user) {

            return res.status(404).json({

                success: false,

                message:
                    "User not found."

            });
        }


        return res.status(200).json({

            success: true,

            user

        });


    } catch (error) {

        console.error(
            "Get Profile Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Internal Server Error"

        });
    }
};


// =====================================================
// CHANGE PASSWORD
// PUT /api/auth/change-password
// =====================================================

exports.changePassword = async (req, res) => {

    try {

        const {
            currentPassword,
            newPassword
        } = req.body;


        // =====================================================
        // VALIDATE
        // =====================================================

        if (
            !currentPassword ||
            !newPassword
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Current password and new password are required."

            });
        }


        // =====================================================
        // FIND USER
        // =====================================================

        const user =
            await User.findById(
                req.user.user_id
            );


        if (!user) {

            return res.status(404).json({

                success: false,

                message:
                    "User not found."

            });
        }


        const existingUser =
            await User.findByEmail(
                user.email
            );


        // =====================================================
        // CHECK CURRENT PASSWORD
        // =====================================================

        const validPassword =
            await bcrypt.compare(
                currentPassword,
                existingUser.password_hash
            );


        if (!validPassword) {

            return res.status(400).json({

                success: false,

                message:
                    "Current password is incorrect."

            });
        }


        // =====================================================
        // HASH NEW PASSWORD
        // =====================================================

        const hashedPassword =
            await bcrypt.hash(
                newPassword,
                SALT_ROUNDS
            );


        // =====================================================
        // UPDATE PASSWORD
        // =====================================================

        await User.updatePassword(
            req.user.user_id,
            hashedPassword
        );


        return res.status(200).json({

            success: true,

            message:
                "Password updated successfully."

        });


    } catch (error) {

        console.error(
            "Change Password Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Internal Server Error"

        });
    }
};