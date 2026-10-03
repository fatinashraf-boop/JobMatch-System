const express =
        require("express");

const router =
        express.Router();


const jobseekerController =
        require(
                "../controllers/jobseekerController"
        );


const {
    verifyToken,
    isJobSeeker
} =
        require(
                "../middleware/authMiddleware"
        );


router.get(
        "/dashboard",
        verifyToken,
        isJobSeeker,
        jobseekerController.getDashboard
);


module.exports = router;