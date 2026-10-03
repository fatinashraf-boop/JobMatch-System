const RecruiterMatch =
    require("../models/recruiterMatchModel");


// =====================================================
// GET MY JOBS
// =====================================================

exports.getMyJobs = async (req, res) => {

    try {

        const userId =
            req.user.user_id;


        // -----------------------------------------
        // GET RECRUITER
        // -----------------------------------------

        const recruiter =
            await RecruiterMatch
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
        // GET RECRUITER JOBS
        // -----------------------------------------

        const jobs =
            await RecruiterMatch
                .getRecruiterJobs(
                    recruiter.recruiter_id
                );


        return res.status(200).json({

            success: true,

            count: jobs.length,

            jobs: jobs
        });


    } catch (error) {

        console.error(
            "Get Recruiter Jobs Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Failed to retrieve recruiter jobs."
        });
    }
};


// =====================================================
// GET BEST MATCHES FOR SELECTED JOB
// =====================================================

exports.getJobCandidateMatches =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            const jobId =
                parseInt(
                    req.params.jobId,
                    10
                );


            // -----------------------------------------
            // VALIDATE JOB ID
            // -----------------------------------------

            if (
                Number.isNaN(jobId)
                ||
                jobId <= 0
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Invalid job ID."
                });
            }


            // -----------------------------------------
            // GET RECRUITER
            // -----------------------------------------

            const recruiter =
                await RecruiterMatch
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
            // VERIFY JOB BELONGS TO RECRUITER
            // -----------------------------------------

            const job =
                await RecruiterMatch
                    .getOwnedJob(
                        recruiter.recruiter_id,
                        jobId
                    );


            if (!job) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Job not found or you do not have permission to view it."
                });
            }


            // -----------------------------------------
            // GET MATCHES
            // -----------------------------------------

            const matches =
                await RecruiterMatch
                    .getCandidateMatches(
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
                    matches.length,

                matches:
                    matches
            });


        } catch (error) {

            console.error(
                "Recruiter Candidate Matches Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to retrieve candidate matches."
            });
        }
    };

// =====================================================
// GET CANDIDATE DETAIL
// =====================================================

exports.getCandidateDetail =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            const jobId =
                parseInt(
                    req.params.jobId,
                    10
                );


            const profileId =
                parseInt(
                    req.params.profileId,
                    10
                );


            // -----------------------------------------
            // VALIDATE IDS
            // -----------------------------------------

            if (
                Number.isNaN(jobId)
                ||
                Number.isNaN(profileId)
                ||
                jobId <= 0
                ||
                profileId <= 0
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Invalid job or candidate ID."
                });
            }


            // -----------------------------------------
            // GET LOGGED-IN RECRUITER
            // -----------------------------------------

            const recruiter =
                await RecruiterMatch
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
                await RecruiterMatch
                    .getOwnedJob(
                        recruiter.recruiter_id,
                        jobId
                    );


            if (!job) {

                return res.status(403).json({

                    success: false,

                    message:
                        "You do not have permission to view candidates for this job."
                });
            }


            // -----------------------------------------
            // GET CANDIDATE
            // -----------------------------------------

            const candidate =
                await RecruiterMatch
                    .getCandidateDetail(
                        jobId,
                        profileId
                    );


            if (!candidate) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Candidate application not found."
                });
            }


            // -----------------------------------------
            // DOCUMENTS
            // -----------------------------------------

            const documents =
                await RecruiterMatch
                    .getCandidateDocuments(
                        candidate.user_id
                    );


            // -----------------------------------------
            // RESPONSE
            // -----------------------------------------

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


                candidate: {

                    user_id:
                        candidate.user_id,

                    profile_id:
                        candidate.profile_id,

                    full_name:
                        candidate.full_name,

                    email:
                        candidate.email,

                    phone_number:
                        candidate.phone_number,

                    headline:
                        candidate.headline,

                    skills:
                        candidate.skills,

                    education:
                        candidate.education,

                    experience:
                        candidate.experience,

                    expected_salary:
                        candidate.expected_salary,

                    location:
                        candidate.location,

                    profile_picture:
                        candidate.profile_picture,

                    resume_summary:
                        candidate.resume_summary
                },


                match: candidate.match_id != null
                ? {
                    match_id: candidate.match_id,
                    similarity_score: candidate.similarity_score,
                    ranking_position: candidate.ranking_position,
                    match_reason: candidate.match_reason,
                    matching_algorithm: candidate.matching_algorithm
                }
                : null,


                application: {

                    application_id:
                        candidate.application_id,

                    application_status:
                        candidate.application_status,

                    applied_at:
                        candidate.applied_at,

                    reviewed_at:
                        candidate.reviewed_at
                },


                shortlist:
                    candidate.shortlist_id
                        ? {

                            shortlist_id:
                                candidate.shortlist_id,

                            shortlist_status:
                                candidate.shortlist_status,

                            notes:
                                candidate.notes,

                            shortlisted_at:
                                candidate.shortlisted_at
                        }
                        : null,


                documents:
                    documents.map(
                        document => ({

                            document_id:
                                document.document_id,

                            file_name:
                                document.file_name,

                            file_path:
                                document.file_path,

                            file_type:
                                document.file_type,

                            document_type:
                                document.document_type,

                            upload_status:
                                document.upload_status,

                            ocr:
                                document.ocr_id
                                    ? {

                                        ocr_id:
                                            document.ocr_id,

                                        extracted_text:
                                            document.extracted_text,

                                        extracted_name:
                                            document.extracted_name,

                                        extracted_issuer:
                                            document.extracted_issuer,

                                        extracted_date:
                                            document.extracted_date,

                                        confidence_score:
                                            document.confidence_score,

                                        processed_at:
                                            document.processed_at
                                    }
                                    : null
                        })
                    )
            });


        } catch (error) {

            console.error(
                "Candidate Detail Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to retrieve candidate details."
            });
        }
    };