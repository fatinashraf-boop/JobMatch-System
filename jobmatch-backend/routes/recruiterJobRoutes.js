const express =
    require("express");

const router =
    express.Router();


const {
    createJob,
    getMyJobs,
    getMyJobById,
    updateJob,
    updateJobStatus
} = require(
    "../controllers/recruiterJobController"
);


const {
    verifyToken,
    isRecruiter
} = require(
    "../middleware/authMiddleware"
);


// CREATE
router.post(
    "/jobs",
    verifyToken,
    isRecruiter,
    createJob
);


// LIST MY JOBS
router.get(
    "/jobs",
    verifyToken,
    isRecruiter,
    getMyJobs
);


// GET ONE JOB
router.get(
    "/jobs/:jobId",
    verifyToken,
    isRecruiter,
    getMyJobById
);


// EDIT JOB
router.put(
    "/jobs/:jobId",
    verifyToken,
    isRecruiter,
    updateJob
);


// CHANGE STATUS
router.patch(
    "/jobs/:jobId/status",
    verifyToken,
    isRecruiter,
    updateJobStatus
);


module.exports = router;