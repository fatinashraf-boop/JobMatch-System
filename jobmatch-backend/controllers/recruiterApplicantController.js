const RecruiterApplicant =
    require("../models/recruiterApplicantModel");

// =====================================================
// GET ALL APPLICANTS FOR LOGGED-IN RECRUITER
// =====================================================

exports.getAllApplicants =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            // -----------------------------------------
            // FIND RECRUITER
            // -----------------------------------------

            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Recruiter profile not found."
                });
            }


            // -----------------------------------------
            // GET ALL APPLICANTS
            // -----------------------------------------

            const applicants =
                await RecruiterApplicant
                    .getApplicantsByRecruiter(
                        recruiter.recruiter_id
                    );


            return res.status(200).json({

                success: true,

                count:
                    applicants.length,

                applicants:
                    applicants
            });


        } catch (error) {

            console.error(
                "Get All Recruiter Applicants Error:",
                error
            );


            return res.status(500).json({
                success: false,
                message:
                    "Failed to retrieve applicants."
            });
        }
    };



// =====================================================
// GET APPLICANT HISTORY
// =====================================================

exports.getApplicantHistory =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Recruiter profile not found."

                });
            }


            const applicants =
                await RecruiterApplicant
                    .getApplicantHistoryByRecruiter(
                        recruiter.recruiter_id
                    );


            return res.status(200).json({

                success: true,

                count:
                    applicants.length,

                applicants:
                    applicants

            });


        } catch (error) {

            console.error(
                "Get Applicant History Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to retrieve applicant history."

            });
        }
    };
// =====================================================
// GET APPLICANTS FOR RECRUITER JOB
// =====================================================

exports.getJobApplicants =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;

            const jobId =
                parseInt(
                    req.params.jobId,
                    10
                );


            if (
                Number.isNaN(jobId)
                ||
                jobId <= 0
            ) {

                return res.status(400).json({
                    success: false,
                    message: "Invalid job ID."
                });
            }


            // -----------------------------------------
            // FIND RECRUITER
            // -----------------------------------------

            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Recruiter profile not found."
                });
            }


            // -----------------------------------------
            // VERIFY JOB OWNERSHIP
            // -----------------------------------------

            const job =
                await RecruiterApplicant
                    .getOwnedJob(
                        recruiter.recruiter_id,
                        jobId
                    );


            if (!job) {

                return res.status(403).json({
                    success: false,
                    message:
                        "You do not have permission to view applicants for this job."
                });
            }


            // -----------------------------------------
            // GET APPLICANTS
            // -----------------------------------------

            const applicants =
                await RecruiterApplicant
                    .getApplicantsByJob(
                        jobId
                    );


            return res.status(200).json({

                success: true,

                job: {
                    job_id:
                        job.job_id,

                    job_title:
                        job.job_title,

                    status:
                        job.status
                },

                count:
                    applicants.length,

                applicants:
                    applicants
            });


        } catch (error) {

            console.error(
                "Get Job Applicants Error:",
                error
            );


            return res.status(500).json({
                success: false,
                message:
                    "Failed to retrieve applicants."
            });
        }
    };

    // =====================================================
// GET SHORTLISTED APPLICANTS
// =====================================================

exports.getShortlistedApplicants =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Recruiter profile not found."
                });
            }


            const applicants =
                await RecruiterApplicant
                    .getShortlistedByRecruiter(
                        recruiter.recruiter_id
                    );


            return res.status(200).json({

                success: true,

                count:
                    applicants.length,

                applicants:
                    applicants
            });


        } catch (error) {

            console.error(
                "Get Shortlisted Applicants Error:",
                error
            );


            return res.status(500).json({
                success: false,
                message:
                    "Failed to retrieve shortlisted applicants."
            });
        }
    };


// =====================================================
// UPDATE APPLICATION STATUS
// =====================================================


exports.updateApplicationStatus =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;

            const applicationId =
                parseInt(
                    req.params.applicationId,
                    10
                );

            const {
                status
            } = req.body;


            const allowedStatuses = [
                "pending",
                "reviewed",
                "shortlisted",
                "rejected",
                "hired"
            ];


            if (
                Number.isNaN(applicationId)
                ||
                applicationId <= 0
            ) {

                return res.status(400).json({
                    success: false,
                    message:
                        "Invalid application ID."
                });
            }


            if (
                !status
                ||
                !allowedStatuses.includes(status)
            ) {

                return res.status(400).json({
                    success: false,
                    message:
                        "Invalid application status."
                });
            }


            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Recruiter profile not found."
                });
            }


            const application =
                await RecruiterApplicant
                    .getApplication(
                        applicationId
                    );


            if (!application) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Application not found."
                });
            }


            // -----------------------------------------
            // OWNERSHIP CHECK
            // -----------------------------------------

            if (
                Number(application.recruiter_id)
                !==
                Number(recruiter.recruiter_id)
            ) {

                return res.status(403).json({
                    success: false,
                    message:
                        "You cannot update this application."
                });
            }


            // -----------------------------------------
            // WITHDRAWN IS FINAL
            // -----------------------------------------

            if (
                application.application_status
                === "withdrawn"
            ) {

                return res.status(409).json({
                    success: false,
                    message:
                        "A withdrawn application cannot be updated."
                });
            }


            // -----------------------------------------
            // SHORTLIST SPECIAL CASE
            // -----------------------------------------

            if (status === "shortlisted") {

                const existingShortlist =
                    await RecruiterApplicant
                        .getShortlistByApplication(
                            applicationId
                        );


                if (!existingShortlist) {

                    await RecruiterApplicant
                        .createShortlist(
                            recruiter.recruiter_id,
                            applicationId
                        );
                }
            }

            // -----------------------------------------
            // REJECTED CANDIDATE:
            // REMOVE FROM ACTIVE SHORTLIST
            // -----------------------------------------

            if (status === "rejected") {

                const existingShortlist =
                    await RecruiterApplicant
                        .getShortlistByApplication(
                            applicationId
                        );


                if (existingShortlist) {

                    await RecruiterApplicant
                        .updateRecruitmentStatus(
                            applicationId,
                            "removed"
                        );
                }
            }

            await RecruiterApplicant
                .updateApplicationStatus(
                    applicationId,
                    status
                );


            return res.status(200).json({

                success: true,

                message:
                    "Application status updated successfully.",

                application_status:
                    status
            });


        } catch (error) {

            console.error(
                "Update Application Status Error:",
                error
            );


            return res.status(500).json({
                success: false,
                message:
                    "Failed to update application status."
            });
        }
    };


// =====================================================
// UPDATE RECRUITMENT STATUS
// =====================================================

exports.updateRecruitmentStatus =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;

            const applicationId =
                parseInt(
                    req.params.applicationId,
                    10
                );

            const {
                status
            } = req.body;


            // shortlist_status values from your ERD
            const allowedStatuses = [
                "active",
                "interviewed",
                "offer_sent",
                "hired",
                "removed"
            ];


            // -----------------------------------------
            // VALIDATE APPLICATION ID
            // -----------------------------------------

            if (
                Number.isNaN(applicationId)
                ||
                applicationId <= 0
            ) {

                return res.status(400).json({
                    success: false,
                    message:
                        "Invalid application ID."
                });
            }


            // -----------------------------------------
            // VALIDATE STATUS
            // -----------------------------------------

            if (
                !status
                ||
                !allowedStatuses.includes(status)
            ) {

                return res.status(400).json({
                    success: false,
                    message:
                        "Invalid recruitment status."
                });
            }


            // -----------------------------------------
            // FIND LOGGED-IN RECRUITER
            // -----------------------------------------

            const recruiter =
                await RecruiterApplicant
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Recruiter profile not found."
                });
            }


            // -----------------------------------------
            // GET APPLICATION
            // -----------------------------------------

            const application =
                await RecruiterApplicant
                    .getApplication(
                        applicationId
                    );


            if (!application) {

                return res.status(404).json({
                    success: false,
                    message:
                        "Application not found."
                });
            }


            // -----------------------------------------
            // VERIFY RECRUITER OWNS JOB
            // -----------------------------------------

            if (
                Number(application.recruiter_id)
                !==
                Number(recruiter.recruiter_id)
            ) {

                return res.status(403).json({
                    success: false,
                    message:
                        "You cannot update this application's recruitment status."
                });
            }


            // -----------------------------------------
            // WITHDRAWN APPLICATION CANNOT PROGRESS
            // -----------------------------------------

            if (
                application.application_status
                === "withdrawn"
            ) {

                return res.status(409).json({
                    success: false,
                    message:
                        "A withdrawn application cannot progress through recruitment."
                });
            }


            // -----------------------------------------
            // MUST ALREADY BE SHORTLISTED
            // -----------------------------------------

            const shortlist =
                await RecruiterApplicant
                    .getShortlistByApplication(
                        applicationId
                    );


            if (!shortlist) {

                return res.status(409).json({
                    success: false,
                    message:
                        "Candidate must be shortlisted before recruitment status can be updated."
                });
            }


            // -----------------------------------------
            // UPDATE SHORTLIST STATUS
            // -----------------------------------------

            const affectedRows =
                await RecruiterApplicant
                    .updateRecruitmentStatus(
                        applicationId,
                        status
                    );


            if (affectedRows === 0) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Recruitment status was not updated."

                });
            }


            // -----------------------------------------
            // SYNCHRONIZE APPLICATION STATUS
            // -----------------------------------------

            if (status === "hired") {

                await RecruiterApplicant
                    .updateApplicationStatus(
                        applicationId,
                        "hired"
                    );

            } else if (status === "removed") {

                await RecruiterApplicant
                    .updateApplicationStatus(
                        applicationId,
                        "reviewed"
                    );
            }
            
            // -----------------------------------------
            // SYNCHRONIZE FINAL / REMOVED STATES
            // -----------------------------------------

            if (status === "hired") {

                await RecruiterApplicant
                    .updateApplicationStatus(
                        applicationId,
                        "hired"
                    );

            } else if (status === "removed") {

                await RecruiterApplicant
                    .updateApplicationStatus(
                        applicationId,
                        "reviewed"
                    );
            }

            if (affectedRows === 0) {

                return res.status(400).json({
                    success: false,
                    message:
                        "Recruitment status was not updated."
                });
            }


            return res.status(200).json({

                success: true,

                message:
                    "Recruitment status updated successfully.",

                application_id:
                    applicationId,

                recruitment_status:
                    status
            });


        } catch (error) {

            console.error(
                "Update Recruitment Status Error:",
                error
            );


            return res.status(500).json({
                success: false,
                message:
                    "Failed to update recruitment status."
            });
        }
    };

    