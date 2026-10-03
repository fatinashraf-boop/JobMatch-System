const express = require("express");

const router = express.Router();

const authController = require("../controllers/authController");

const {
    verifyToken,
    isAdmin,
    isRecruiter,
    isJobSeeker,
    authorizeRoles
} = require("../middleware/authMiddleware");

/*
|--------------------------------------------------------------------------
| Public Routes
|--------------------------------------------------------------------------
*/

// Register User
// POST /api/auth/register
router.post(
    "/register",
    authController.register
);

// Login User
// POST /api/auth/login
router.post(
    "/login",
    authController.login
);

/*
|--------------------------------------------------------------------------
| Protected Routes
|--------------------------------------------------------------------------
*/

// Logged-in User Profile
// GET /api/auth/profile
router.get(
    "/profile",
    verifyToken,
    authController.getProfile
);

// Change Password
// PUT /api/auth/change-password
router.put(
    "/change-password",
    verifyToken,
    authController.changePassword
);

/*
|--------------------------------------------------------------------------
| Role-Based Route Examples
| (Useful for future modules)
|--------------------------------------------------------------------------
*/

// Admin Only
router.get(
    "/admin",
    verifyToken,
    isAdmin,
    (req, res) => {

        res.status(200).json({

            success: true,
            message: "Welcome Admin",

            user: req.user

        });

    }
);

// Recruiter Only
router.get(
    "/recruiter",
    verifyToken,
    isRecruiter,
    (req, res) => {

        res.status(200).json({

            success: true,
            message: "Welcome Recruiter",

            user: req.user

        });

    }
);

// Job Seeker Only
router.get(
    "/jobseeker",
    verifyToken,
    isJobSeeker,
    (req, res) => {

        res.status(200).json({

            success: true,
            message: "Welcome Job Seeker",

            user: req.user

        });

    }
);

// Multiple Roles Example
router.get(
    "/dashboard",
    verifyToken,
    authorizeRoles("admin", "recruiter"),
    (req, res) => {

        res.status(200).json({

            success: true,
            message: "Recruiter/Admin Dashboard",

            user: req.user

        });

    }
);

module.exports = router;