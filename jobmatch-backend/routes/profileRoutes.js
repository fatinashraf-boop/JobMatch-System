const express =
    require("express");

const router =
    express.Router();


const {

    getMyProfile,
    getProfileById,
    updateProfile,
    deleteProfile,
    uploadProfilePicture

} = require(
    "../controllers/profileController"
);


const {

    verifyToken,
    isJobSeeker

} = require(
    "../middleware/authMiddleware"
);


// =====================================================
// GET LOGGED-IN JOB SEEKER PROFILE
// =====================================================

router.get(
    "/me",
    verifyToken,
    isJobSeeker,
    getMyProfile
);


// =====================================================
// UPDATE LOGGED-IN JOB SEEKER PROFILE
// =====================================================

router.put(
    "/me",
    verifyToken,
    isJobSeeker,
    updateProfile
);

// =====================================================
// UPLOAD PROFILE PICTURE
// =====================================================

const uploadProfilePictureMiddleware =
    require(
        "../middleware/profilePictureUpload"
    );


router.post(
    "/me/picture",
    verifyToken,
    isJobSeeker,
    uploadProfilePictureMiddleware.single(
        "profile_picture"
    ),
    uploadProfilePicture
);

// =====================================================
// DELETE LOGGED-IN JOB SEEKER PROFILE
// =====================================================

router.delete(
    "/me",
    verifyToken,
    isJobSeeker,
    deleteProfile
);


// =====================================================
// GET PROFILE BY ID
// =====================================================

router.get(
    "/:id",
    verifyToken,
    getProfileById
);


module.exports =
    router;