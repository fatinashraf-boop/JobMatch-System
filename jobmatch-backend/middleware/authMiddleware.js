const jwt = require("jsonwebtoken");
const User = require("../models/userModel");

/**
 * ==========================================
 * Verify JWT Token
 * ==========================================
 */
exports.verifyToken = async (req, res, next) => {

    try {

        const authHeader = req.headers.authorization;

        if (!authHeader) {

            return res.status(401).json({
                success: false,
                message: "Access denied. No token provided."
            });

        }

        // Authorization: Bearer <token>
        const token = authHeader.split(" ")[1];

        if (!token) {

            return res.status(401).json({
                success: false,
                message: "Invalid authorization format."
            });

        }

        // Verify Token
        const decoded = jwt.verify(
            token,
            process.env.JWT_SECRET
        );

        // Check if user still exists
        const user = await User.findById(decoded.user_id);

        if (!user) {

            return res.status(401).json({
                success: false,
                message: "User no longer exists."
            });

        }

        // Store user info for next middleware/controller
        req.user = {

            user_id: decoded.user_id,
            email: decoded.email,
            role: decoded.role

        };

        next();

    }

    catch (error) {

        if (error.name === "TokenExpiredError") {

            return res.status(401).json({

                success: false,
                message: "Token has expired."

            });

        }

        if (error.name === "JsonWebTokenError") {

            return res.status(401).json({

                success: false,
                message: "Invalid token."

            });

        }

        console.error(error);

        return res.status(500).json({

            success: false,
            message: "Internal Server Error"

        });

    }

};

/**
 * ==========================================
 * Role-Based Authorization
 * Usage:
 * authorizeRoles("admin")
 * authorizeRoles("recruiter")
 * authorizeRoles("jobseeker","recruiter")
 * ==========================================
 */
exports.authorizeRoles = (...roles) => {

    return (req, res, next) => {

        if (!req.user) {

            return res.status(401).json({

                success: false,
                message: "Unauthorized."

            });

        }

        if (!roles.includes(req.user.role)) {

            return res.status(403).json({

                success: false,
                message: "You do not have permission to access this resource."

            });

        }

        next();

    };

};

/**
 * ==========================================
 * Admin Only
 * ==========================================
 */
exports.isAdmin = (req, res, next) => {

    if (req.user.role !== "admin") {

        return res.status(403).json({

            success: false,
            message: "Admin access only."

        });

    }

    next();

};

/**
 * ==========================================
 * Recruiter Only
 * ==========================================
 */
exports.isRecruiter = (req, res, next) => {

    if (req.user.role !== "recruiter") {

        return res.status(403).json({

            success: false,
            message: "Recruiter access only."

        });

    }

    next();

};

/**
 * ==========================================
 * Job Seeker Only
 * ==========================================
 */
exports.isJobSeeker = (req, res, next) => {

    if (req.user.role !== "jobseeker") {

        return res.status(403).json({

            success: false,
            message: "Job Seeker access only."

        });

    }

    next();

};