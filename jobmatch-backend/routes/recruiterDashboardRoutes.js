const express =
    require("express");

const router =
    express.Router();


const {
    getRecruiterDashboard
} = require(
    "../controllers/recruiterDashboardController"
);


const {
    verifyToken,
    isRecruiter
} = require(
    "../middleware/authMiddleware"
);


// ==========================================
// GET RECRUITER DASHBOARD
// ==========================================

router.get(
    "/dashboard",
    verifyToken,
    isRecruiter,
    getRecruiterDashboard
);


module.exports =
    router;