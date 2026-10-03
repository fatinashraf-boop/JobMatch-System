const applicationModel =
    require("../models/applicationModel");

const db = require("../config/db");

const {
    getExistingMatch,
    updateRankingsByJob
} = require("../models/matchScoreModel");

const {
    calculateAndSaveMatch
} = require("../services/automaticMatchingService");


const ensureApplicationMatch = async (jobId, profileId) => {

    // Do not recalculate a match that already exists.
    const existingMatch = await getExistingMatch(
        jobId,
        profileId
    );

    if (existingMatch) {
        return {
            status: "existing",
            score: existingMatch.similarity_score
        };
    }

    // Retrieve the specific job.
    const [jobs] = await db.execute(
        `SELECT
            job_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            status
         FROM jobs
         WHERE job_id = ?
         LIMIT 1`,
        [jobId]
    );

    if (jobs.length === 0) {
        throw new Error("Job not found during matching.");
    }

    // Retrieve the applicant's profile and latest processed resume OCR.
    const [profiles] = await db.execute(
        `SELECT
            p.profile_id,
            p.user_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,
            (
                SELECT o.extracted_text
                FROM documents d
                INNER JOIN ocr_results o
                    ON o.document_id = d.document_id
                WHERE d.user_id = p.user_id
                  AND d.document_type = 'resume'
                  AND d.upload_status = 'processed'
                ORDER BY
                    d.created_at DESC,
                    o.processed_at DESC
                LIMIT 1
            ) AS resume_ocr_text
         FROM profiles p
         WHERE p.profile_id = ?
         LIMIT 1`,
        [profileId]
    );

    if (profiles.length === 0) {
        throw new Error("Applicant profile not found during matching.");
    }

    const result = await calculateAndSaveMatch(
        jobs[0],
        profiles[0]
    );

    if (result.skipped) {
        return { status: "unavailable", score: null };
    }

    await updateRankingsByJob(jobId);

    return {
        status: "calculated",
        score: result.similarity_score
    };
};



// ==========================================
// APPLY FOR JOB
// ==========================================

const applyForJob = async (req, res) => {

    try {

        // ======================================
        // LOGGED-IN USER FROM JWT
        // ======================================

        const userId =
            req.user.user_id;


        // ======================================
        // ONLY JOB ID COMES FROM ANDROID
        // ======================================

        const {
            job_id
        } = req.body;


        const jobId =
            parseInt(
                job_id,
                10
            );


        if (
            Number.isNaN(jobId)
            ||
            jobId <= 0
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Valid job_id is required"
            });
        }


        // ======================================
        // CHECK JOB
        // ======================================

        const [jobs] =
            await db.execute(
                `
                SELECT
                    job_id,
                    status
                FROM jobs
                WHERE job_id = ?
                LIMIT 1
                `,
                [jobId]
            );


        if (jobs.length === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Job not found"
            });
        }


        // ======================================
        // JOB MUST BE OPEN
        // ======================================

        if (
            jobs[0].status !== "open"
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "This job is not currently open for applications"
            });
        }


        // ======================================
        // RESOLVE JWT USER -> PROFILE
        // ======================================

        const [profiles] =
            await db.execute(
                `
                SELECT
                    profile_id,
                    user_id
                FROM profiles
                WHERE user_id = ?
                LIMIT 1
                `,
                [userId]
            );


        if (profiles.length === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Jobseeker profile not found"
            });
        }


        const profileId =
            profiles[0].profile_id;


        // ======================================
        // CHECK DUPLICATE APPLICATION
        // ======================================

        const existingApplication =
            await applicationModel
                .checkExistingApplication(
                    jobId,
                    profileId
                );


        if (existingApplication) {

            return res.status(409).json({

                success: false,

                message:
                    "You have already applied for this job",

                data: {

                    application_id:
                        existingApplication.application_id,

                    application_status:
                        existingApplication.application_status
                }
            });
        }


        // ======================================
        // CREATE APPLICATION
        // ======================================

        const result =
            await applicationModel
                .createApplication(
                    jobId,
                    profileId
                );
        
        // ======================================
        // ENSURE AI MATCH AFTER APPLICATION
        // ======================================

        let matching = {
            status: "unavailable",
            score: null
        };

        try {
            matching = await ensureApplicationMatch(
                jobId,
                profileId
            );

        } catch (matchingError) {

            // Application was already saved.
            // AI failure must not fail the application.
            console.error(
                `Application ${result.insertId} matching failed:`,
                matchingError
            );
        }



        // ======================================
        // SUCCESS
        // ======================================

        return res.status(201).json({

            success: true,

            message:
                "Job application submitted successfully",

            data: {

                application_id:
                    result.insertId,

                job_id:
                    jobId,

                profile_id:
                    profileId,

                application_status:
                    "pending",

                matching: matching

            }
        });


    } catch (error) {

        console.error(
            "Apply Job Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to submit application",

            error:
                error.message
        });
    }
};


// ==========================================
// GET MY APPLICATIONS
// GET /api/applications/my
// ==========================================

const getMyApplications = async (
    req,
    res
) => {

    try {

        // ======================================
        // AUTHENTICATED USER
        // ======================================

        const userId =
            req.user.user_id;


        // ======================================
        // RESOLVE PROFILE
        // ======================================

        const [profiles] =
            await db.execute(
                `
                SELECT
                    profile_id
                FROM profiles
                WHERE user_id = ?
                LIMIT 1
                `,
                [userId]
            );


        if (profiles.length === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Jobseeker profile not found"
            });
        }


        const profileId =
            profiles[0].profile_id;


        // ======================================
        // GET APPLICATIONS
        // ======================================

        const applications =
            await applicationModel
                .getApplicationsByProfile(
                    profileId
                );


        // ======================================
        // SUCCESS
        // ======================================

        return res.status(200).json({

            success: true,

            count:
                applications.length,

            applications
        });


    } catch (error) {

        console.error(
            "Get My Applications Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to retrieve applications",

            error:
                error.message
        });
    }
};

const getMyApplicationDetails = async (req, res) => {

    try {

        const userId = req.user.user_id;

        const applicationId =
            parseInt(req.params.id, 10);


        if (
            Number.isNaN(applicationId)
            ||
            applicationId <= 0
        ) {

            return res.status(400).json({
                success: false,
                message: "Invalid application ID"
            });
        }


        const profileId =
            await applicationModel
                .getProfileIdByUserId(userId);


        if (!profileId) {

            return res.status(404).json({
                success: false,
                message: "Jobseeker profile not found"
            });
        }


        const application =
            await applicationModel
                .getApplicationDetailsById(
                    applicationId,
                    profileId
                );


        if (!application) {

            return res.status(404).json({
                success: false,
                message: "Application not found"
            });
        }


        return res.status(200).json({
            success: true,
            data: application
        });


    } catch (error) {

        console.error(
            "Get Application Details Error:",
            error
        );


        return res.status(500).json({
            success: false,
            message: "Failed to load application details"
        });
    }
};


// ==========================================
// GET APPLICATION BY ID
// ==========================================
// GET /api/applications/:id
//
// ==========================================

const getApplicationById = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const application =
            await applicationModel
                .getApplicationById(id);


        if (!application) {

            return res.status(404).json({

                success: false,

                message:
                    "Application not found"

            });

        }


        res.status(200).json({

            success: true,

            application

        });

    } catch (error) {

        console.error(
            "Get Application Error:",
            error
        );

        res.status(500).json({

            success: false,

            message:
                "Failed to retrieve application",

            error:
                error.message

        });

    }

};


// ==========================================
// GET APPLICATIONS FOR JOB
// ==========================================
// GET /api/applications/job/:job_id
//
// Recruiter can view applicants
// for a specific job.
//
// ==========================================

const getApplicationsForJob = async (
    req,
    res
) => {

    try {

        const {
            job_id
        } = req.params;


        const applications =
            await applicationModel
                .getApplicationsByJob(
                    job_id
                );


        res.status(200).json({

            success: true,

            count:
                applications.length,

            applications

        });

    } catch (error) {

        console.error(
            "Get Job Applications Error:",
            error
        );

        res.status(500).json({

            success: false,

            message:
                "Failed to retrieve job applications",

            error:
                error.message

        });

    }

};


// ==========================================
// UPDATE APPLICATION STATUS
// ==========================================
// PATCH /api/applications/:id/status
//
// Body:
//
// {
//     "application_status": "shortlisted"
// }
//
// ==========================================

const updateApplicationStatus = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;

        const {
            application_status
        } = req.body;


        // ERD status values

        const validStatuses = [

            "pending",

            "reviewed",

            "shortlisted",

            "rejected",

            "withdrawn"

        ];


        if (
            !application_status ||
            !validStatuses.includes(
                application_status
            )
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid application status",

                valid_statuses:
                    validStatuses

            });

        }


        // Check application

        const application =
            await applicationModel
                .getApplicationById(id);


        if (!application) {

            return res.status(404).json({

                success: false,

                message:
                    "Application not found"

            });

        }


        // Update status

        const result =
            await applicationModel
                .updateApplicationStatus(
                    id,
                    application_status
                );


        if (result.affectedRows === 0) {

            return res.status(400).json({

                success: false,

                message:
                    "Application status was not updated"

            });

        }


        res.status(200).json({

            success: true,

            message:
                "Application status updated successfully",

            application_id:
                Number(id),

            application_status

        });

    } catch (error) {

        console.error(
            "Update Application Status Error:",
            error
        );

        res.status(500).json({

            success: false,

            message:
                "Failed to update application status",

            error:
                error.message

        });

    }

};


// ==========================================
// WITHDRAW APPLICATION
// PATCH /api/applications/:id/withdraw
// ==========================================

const withdrawApplication = async (
    req,
    res
) => {

    try {

        const userId =
            req.user.user_id;


        const applicationId =
            parseInt(
                req.params.id,
                10
            );


        if (
            Number.isNaN(applicationId)
            ||
            applicationId <= 0
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid application ID"
            });
        }


        // ======================================
        // RESOLVE LOGGED-IN USER'S PROFILE
        // ======================================

        const profileId =
            await applicationModel
                .getProfileIdByUserId(
                    userId
                );


        if (!profileId) {

            return res.status(404).json({

                success: false,

                message:
                    "Jobseeker profile not found"
            });
        }


        // ======================================
        // GET APPLICATION
        // ======================================

        const application =
            await applicationModel
                .getApplicationById(
                    applicationId
                );


        if (!application) {

            return res.status(404).json({

                success: false,

                message:
                    "Application not found"
            });
        }


        // ======================================
        // OWNERSHIP CHECK
        // ======================================

        if (
            Number(application.profile_id)
            !==
            Number(profileId)
        ) {

            return res.status(403).json({

                success: false,

                message:
                    "You are not allowed to withdraw this application"
            });
        }


        // ======================================
        // ALREADY WITHDRAWN
        // ======================================

        if (
            application.application_status
            === "withdrawn"
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Application has already been withdrawn"
            });
        }


        // ======================================
        // DON'T WITHDRAW FINAL RECRUITMENT STATES
        // ======================================

        if (
            application.application_status
            === "rejected"
            ||
            application.application_status
            === "shortlisted"
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "This application can no longer be withdrawn"
            });
        }


        // ======================================
        // WITHDRAW
        // ======================================

        const result =
            await applicationModel
                .withdrawApplication(
                    applicationId
                );


        if (result.affectedRows === 0) {

            return res.status(400).json({

                success: false,

                message:
                    "Application was not withdrawn"
            });
        }


        return res.status(200).json({

            success: true,

            message:
                "Application withdrawn successfully",

            data: {

                application_id:
                    applicationId,

                application_status:
                    "withdrawn"
            }
        });


    } catch (error) {

        console.error(
            "Withdraw Application Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to withdraw application",

            error:
                error.message
        });
    }
};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    applyForJob,

    getMyApplications,

    getMyApplicationDetails,

    getApplicationById,

    getApplicationsForJob,

    updateApplicationStatus,

    withdrawApplication

};