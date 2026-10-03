const express = require("express");

const router = express.Router();


const {

    applyForJob,

    getMyApplications,

    getMyApplicationDetails,

    getApplicationById,

    getApplicationsForJob,

    updateApplicationStatus,

    withdrawApplication

} = require("../controllers/applicationController");


const {

    verifyToken,

    isJobSeeker

} = require("../middleware/authMiddleware");


// ==========================================
// APPLY FOR A JOB
// POST /api/applications
// ==========================================

router.post(
    "/",
    verifyToken,
    isJobSeeker,
    applyForJob
);


// ==========================================
// GET MY APPLICATIONS
// GET /api/applications/my
// ==========================================
//
// IMPORTANT:
// This must appear BEFORE "/:id"
// otherwise Express may treat "my"
// as an application ID.
//
// ==========================================

router.get(
    "/my",
    verifyToken,
    isJobSeeker,
    getMyApplications
);

router.get(
    "/:id/details",
    verifyToken,
    isJobSeeker,
    getMyApplicationDetails
);

// ==========================================
// GET APPLICATIONS FOR A JOB
// GET /api/applications/job/:job_id
// ==========================================

router.get(
    "/job/:job_id",
    verifyToken,
    getApplicationsForJob
);


// ==========================================
// GET APPLICATION BY ID
// GET /api/applications/:id
// ==========================================

router.get(
    "/:id",
    verifyToken,
    getApplicationById
);


// ==========================================
// UPDATE APPLICATION STATUS
// PATCH /api/applications/:id/status
// ==========================================

router.patch(
    "/:id/status",
    verifyToken,
    updateApplicationStatus
);


// ==========================================
// WITHDRAW APPLICATION
// PATCH /api/applications/:id/withdraw
// ==========================================

router.patch(
    "/:id/withdraw",
    verifyToken,
    isJobSeeker,
    withdrawApplication
);


module.exports = router;