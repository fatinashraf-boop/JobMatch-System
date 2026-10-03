const express = require("express");

const router = express.Router();


const {
    createMatch,
    getMatchById,
    getJobMatches,
    getProfileMatches,
    updateMatch,
    deleteMatch,
    recalculateJobMatches,
    recalculateProfileMatches,
    recalculateAllMatches
} = require("../controllers/matchScoreController");


// ==========================================
// CREATE MATCH
// POST /api/match-scores
// ==========================================

router.post(
    "/",
    createMatch
);


// ==========================================
// GET MATCH BY ID
// GET /api/match-scores/:id
// ==========================================

router.get(
    "/:id",
    getMatchById
);


// ==========================================
// GET MATCHES FOR A JOB
// GET /api/match-scores/job/:job_id
// ==========================================

router.get(
    "/job/:job_id",
    getJobMatches
);


// ==========================================
// GET MATCHES FOR A PROFILE
// GET /api/match-scores/profile/:profile_id
// ==========================================

router.get(
    "/profile/:profile_id",
    getProfileMatches
);


// ==========================================
// UPDATE MATCH
// PATCH /api/match-scores/:id
// ==========================================

router.patch(
    "/:id",
    updateMatch
);


// ==========================================
// DELETE MATCH
// DELETE /api/match-scores/:id
// ==========================================

router.delete(
    "/:id",
    deleteMatch
);

router.post(
    "/recalculate/job/:job_id",
    recalculateJobMatches
);

router.post(
    "/recalculate/profile/:profile_id",
    recalculateProfileMatches
);

router.post(
    "/recalculate/all",
    recalculateAllMatches
);

module.exports = router;