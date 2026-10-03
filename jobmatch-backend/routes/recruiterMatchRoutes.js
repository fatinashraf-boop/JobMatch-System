const express =
    require("express");

const router =
    express.Router();


const recruiterMatchController =
    require(
        "../controllers/recruiterMatchController"
    );


const {
    verifyToken,
    isRecruiter
} = require(
    "../middleware/authMiddleware"
);


// =====================================================
// GET BEST CANDIDATE MATCHES FOR ONE JOB
// =====================================================

router.get(
    "/jobs/:jobId/matches",
    verifyToken,
    isRecruiter,
    recruiterMatchController
        .getJobCandidateMatches
);

// =====================================================
// GET CANDIDATE DETAIL
// =====================================================

router.get(
    "/jobs/:jobId/candidates/:profileId",
    verifyToken,
    isRecruiter,
    recruiterMatchController.getCandidateDetail
);


module.exports =
    router;