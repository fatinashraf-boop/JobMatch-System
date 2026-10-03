const Profile =
    require("../models/profileModel");

const User =
    require("../models/userModel");

const {
    recalculateForProfile
} = require("../services/automaticMatchingService");

// =====================================================
// GET MY FULL JOBSEEKER PROFILE
// GET /api/profiles/me
// JOIN users + profiles
// =====================================================

exports.getMyProfile = async (req, res) => {

    try {

        const user_id = req.user.user_id;

        // =====================================================
        // GET JOINED USER + PROFILE
        // =====================================================

        const fullProfile =
            await Profile.findFullProfileByUserId(user_id);

        if (!fullProfile) {

            return res.status(404).json({
                success: false,
                message: "Jobseeker profile not found."
            });
        }

        // =====================================================
        // RETURN COMBINED PROFILE
        // =====================================================
        // profileModel returns:
        // {
        //     user: {...},
        //     profile: {...}
        // }

        return res.status(200).json({

            success: true,

            user: {
                user_id: fullProfile.user.user_id,
                full_name: fullProfile.user.full_name,
                email: fullProfile.user.email,
                phone_number: fullProfile.user.phone_number
            },

            profile: {
                profile_id: fullProfile.profile.profile_id,
                user_id: fullProfile.user.user_id,

                headline: fullProfile.profile.headline,
                skills: fullProfile.profile.skills,
                education: fullProfile.profile.education,
                experience: fullProfile.profile.experience,
                expected_salary: fullProfile.profile.expected_salary,
                location: fullProfile.profile.location,
                profile_picture: fullProfile.profile.profile_picture,
                resume_summary: fullProfile.profile.resume_summary,
                profile_status: fullProfile.profile.profile_status
            }
        });

    } catch (error) {

        console.error(
            "Get Full Jobseeker Profile Error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve jobseeker profile."
        });
    }
};


// =====================================================
// UPDATE MY PROFILE
// PUT /api/profiles/me
// =====================================================

exports.updateProfile = async (req, res) => {

    try {

        const user_id =
            req.user.user_id;


        // ==============================
        // GET REQUEST DATA
        // ==============================

        const {

            full_name,
            phone_number,

            headline,
            skills,
            education,
            experience,
            expected_salary,

            location,
            profile_picture,
            resume_summary

        } = req.body;


        // ==============================
        // VALIDATE CURRENT FORM
        // ==============================

        if (
            !full_name ||
            !phone_number ||
            !location ||
            !resume_summary
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Please fill in all required profile fields."

            });
        }


        // ==============================
        // CHECK PROFILE EXISTS
        // ==============================

        const existingProfile =
            await Profile.exists(
                user_id
            );


        if (!existingProfile) {

            return res.status(404).json({

                success: false,

                message:
                    "Profile not found."

            });
        }


        // ==============================
        // UPDATE USERS TABLE
        // ==============================

        await User.update(

            user_id,

            {

                full_name:
                    full_name.trim(),

                phone_number:
                    phone_number.trim()
            }
        );


        // ==============================
        // UPDATE PROFILES TABLE
        // ==============================

        await Profile.update(

            user_id,

            {

                headline:
                    headline || null,

                skills:
                    skills || null,

                education:
                    education || null,

                experience:
                    experience || null,

                expected_salary:
                    expected_salary || null,

                location:
                    location.trim(),

                profile_picture:
                    existingProfile.profile_picture,

                resume_summary:
                    resume_summary.trim(),

                profile_status:
                    "complete"
            }
        );


        // ==============================
        // GET UPDATED DATA
        // ==============================

        const updatedUser =
            await User.findById(
                user_id
            );


        const updatedProfile =
            await Profile.findByUserId(
                user_id
            );
        
        // =====================================================
        // AUTOMATIC AI MATCH RECALCULATION
        // =====================================================

        try {

            if (
                updatedProfile &&
                updatedProfile.profile_id &&
                updatedProfile.profile_status === "complete"
            ) {

                await recalculateForProfile(
                    updatedProfile.profile_id
                );

                console.log(
                    `AI matches recalculated for profile ${updatedProfile.profile_id}`
                );
            }

        } catch (matchingError) {

            // Do not fail profile update if NLP service
            // is temporarily unavailable.
            console.error(
                "Automatic profile matching failed:",
                matchingError.message
            );
        }

        // ==============================
        // RESPONSE
        // ==============================

        return res.status(200).json({

            success: true,

            message:
                "Profile updated successfully.",

            user: {

                user_id:
                    updatedUser.user_id,

                full_name:
                    updatedUser.full_name,

                email:
                    updatedUser.email,

                phone_number:
                    updatedUser.phone_number,

                role:
                    updatedUser.role
            },

            profile:
                updatedProfile

        });


    } catch (error) {

        console.error(
            "Update Profile Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to update profile."

        });
    }
};


// =====================================================
// GET PROFILE BY PROFILE ID
// =====================================================

exports.getProfileById = async (req, res) => {

    try {

        const profile_id =
            req.params.id;


        const profile =
            await Profile.findById(
                profile_id
            );


        if (!profile) {

            return res.status(404).json({

                success: false,

                message:
                    "Profile not found."

            });
        }


        return res.status(200).json({

            success: true,

            profile:
                profile

        });


    } catch (error) {

        console.error(
            "Get Profile By ID Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to retrieve profile."

        });
    }
};


// =====================================================
// DELETE PROFILE
// =====================================================

exports.deleteProfile = async (req, res) => {

    try {

        const user_id =
            req.user.user_id;


        const affectedRows =
            await Profile.delete(
                user_id
            );


        if (affectedRows === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Profile not found."

            });
        }


        return res.status(200).json({

            success: true,

            message:
                "Profile deleted successfully."

        });


    } catch (error) {

        console.error(
            "Delete Profile Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to delete profile."

        });
    }};

    // =====================================================
    // UPLOAD PROFILE PICTURE
    // POST /api/profiles/me/picture
    // =====================================================

    exports.uploadProfilePicture =
        async (req, res) => {

            try {

                const user_id =
                    req.user.user_id;


                // ==============================
                // CHECK FILE
                // ==============================

                if (!req.file) {

                    return res.status(400).json({

                        success: false,

                        message:
                            "Please select a profile picture."
                    });
                }


                // ==============================
                // CHECK PROFILE
                // ==============================

                const existingProfile =
                    await Profile.findByUserId(
                        user_id
                    );


                if (!existingProfile) {

                    return res.status(404).json({

                        success: false,

                        message:
                            "Profile not found."
                    });
                }


                // ==============================
                // CREATE SAVED PATH
                // ==============================

                const profilePicturePath =
                    "/uploads/profile-pictures/"
                    + req.file.filename;


                // ==============================
                // UPDATE PROFILE
                // ==============================

                await Profile.update(

                    user_id,

                    {
                        headline:
                            existingProfile.headline,

                        skills:
                            existingProfile.skills,

                        education:
                            existingProfile.education,

                        experience:
                            existingProfile.experience,

                        expected_salary:
                            existingProfile.expected_salary,

                        location:
                            existingProfile.location,

                        profile_picture:
                            profilePicturePath,

                        resume_summary:
                            existingProfile.resume_summary,

                        profile_status:
                            existingProfile.profile_status
                    }
                );


                // ==============================
                // GET UPDATED PROFILE
                // ==============================

                const updatedProfile =
                    await Profile.findByUserId(
                        user_id
                    );


                return res.status(200).json({

                    success: true,

                    message:
                        "Profile picture uploaded successfully.",

                    profile:
                        updatedProfile
                });


            } catch (error) {

                console.error(
                    "Upload Profile Picture Error:",
                    error
                );


                return res.status(500).json({

                    success: false,

                    message:
                        "Failed to upload profile picture."
                });
            }
    };
