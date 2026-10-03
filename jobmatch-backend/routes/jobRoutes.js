const express = require("express");

const router = express.Router();


const {
    createJobController,
    getAllJobsController,
    getJobByIdController,
    getJobsByRecruiterController,
    updateJobController,
    updateJobStatusController,
    deleteJobController,
    searchJobsController
} = require("../controllers/jobController");


// ==========================================
// CREATE JOB
// POST /api/jobs
// ==========================================

router.post(
    "/",
    createJobController
);


// ==========================================
// SEARCH JOBS
// GET /api/jobs/search
// ==========================================

router.get(
    "/search",
    searchJobsController
);


// ==========================================
// GET JOBS BY RECRUITER
// GET /api/jobs/recruiter/:recruiter_id
// ==========================================

router.get(
    "/recruiter/:recruiter_id",
    getJobsByRecruiterController
);


// ==========================================
// GET ALL JOBS
// GET /api/jobs
// ==========================================

router.get(
    "/",
    getAllJobsController
);


// ==========================================
// GET JOB BY ID
// GET /api/jobs/:id
// ==========================================

router.get(
    "/:id",
    getJobByIdController
);


// ==========================================
// UPDATE JOB
// PUT /api/jobs/:id
// ==========================================

router.put(
    "/:id",
    updateJobController
);


// ==========================================
// UPDATE JOB STATUS
// PATCH /api/jobs/:id/status
// ==========================================

router.patch(
    "/:id/status",
    updateJobStatusController
);


// ==========================================
// DELETE JOB
// DELETE /api/jobs/:id
// ==========================================

router.delete(
    "/:id",
    deleteJobController
);


// ==========================================
// EXPORT ROUTER
// ==========================================

module.exports = router;