const express = require("express");

const router = express.Router();


const {
    createShortlistController,
    getAllShortlistsController,
    getShortlistByIdController,
    getShortlistDetailsController,
    getShortlistsByRecruiterController,
    getShortlistsByJobController,
    updateShortlistStatusController,
    deleteShortlistController
} = require("../controllers/shortlistController");


// ==========================================
// CREATE SHORTLIST
// POST /api/shortlists
// ==========================================

router.post(
    "/",
    createShortlistController
);


// ==========================================
// GET ALL SHORTLISTS
// GET /api/shortlists
// ==========================================

router.get(
    "/",
    getAllShortlistsController
);


// ==========================================
// GET SHORTLISTS BY RECRUITER
// GET /api/shortlists/recruiter/:recruiter_id
// ==========================================

router.get(
    "/recruiter/:recruiter_id",
    getShortlistsByRecruiterController
);


// ==========================================
// GET SHORTLISTS BY JOB
// GET /api/shortlists/job/:job_id
// ==========================================

router.get(
    "/job/:job_id",
    getShortlistsByJobController
);


// ==========================================
// GET SHORTLIST DETAILS
// GET /api/shortlists/:id/details
// ==========================================

router.get(
    "/:id/details",
    getShortlistDetailsController
);


// ==========================================
// GET SHORTLIST BY ID
// GET /api/shortlists/:id
// ==========================================

router.get(
    "/:id",
    getShortlistByIdController
);


// ==========================================
// UPDATE SHORTLIST STATUS
// PATCH /api/shortlists/:id/status
// ==========================================

router.patch(
    "/:id/status",
    updateShortlistStatusController
);


// ==========================================
// REMOVE FROM SHORTLIST
// DELETE /api/shortlists/:id
// ==========================================

router.delete(
    "/:id",
    deleteShortlistController
);


// ==========================================
// EXPORT ROUTER
// ==========================================

module.exports = router;