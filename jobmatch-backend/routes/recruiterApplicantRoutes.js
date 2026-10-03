const express =
    require("express");

const router =
    express.Router();

const recruiterApplicantController =
    require(
        "../controllers/recruiterApplicantController"
    );

const {
    verifyToken,
    isRecruiter
} = require(
    "../middleware/authMiddleware"
);

// =====================================================
// GET ALL APPLICANTS FOR LOGGED-IN RECRUITER
// =====================================================

router.get(
    "/applicants",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .getAllApplicants
);

// =====================================================
// GET APPLICANT HISTORY
// =====================================================

router.get(
    "/applicants/history",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .getApplicantHistory
);

// =====================================================
// GET JOB APPLICANTS
// =====================================================

router.get(
    "/jobs/:jobId/applicants",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .getJobApplicants
);

// =====================================================
// GET SHORTLISTED APPLICANTS
// =====================================================

router.get(
    "/shortlisted",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .getShortlistedApplicants
);

// =====================================================
// UPDATE APPLICATION STATUS
// =====================================================

router.patch(
    "/applications/:applicationId/status",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .updateApplicationStatus
);


// =====================================================
// UPDATE RECRUITMENT STATUS
// =====================================================

router.patch(
    "/applications/:applicationId/recruitment-status",
    verifyToken,
    isRecruiter,
    recruiterApplicantController
        .updateRecruitmentStatus
);


module.exports = router;