const express = require("express");

const router = express.Router();

const recruiterController =
    require("../controllers/recruiterController");

const {
    verifyToken,
    isRecruiter
} = require("../middleware/authMiddleware");


// ==========================================
// GET MY RECRUITER PROFILE
// ==========================================

router.get(
    "/me",
    verifyToken,
    isRecruiter,
    recruiterController.getMyProfile
);


// ==========================================
// UPDATE MY RECRUITER PROFILE
// ==========================================

router.put(
    "/me",
    verifyToken,
    isRecruiter,
    recruiterController.updateMyProfile
);


const companyLogoUpload = require('../middleware/companyLogoUpload');
router.post('/me/logo', verifyToken, isRecruiter,
    companyLogoUpload.single('company_logo'), recruiterController.uploadMyLogo);

module.exports = router;