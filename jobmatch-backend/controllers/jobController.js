const {
    createJob,
    getAllJobs,
    getJobById,
    getJobsByRecruiter,
    updateJob,
    updateJobStatus,
    deleteJob,
    searchJobs
} = require("../models/jobModel");

const {
    recalculateForJob
} = require("../services/automaticMatchingService");


// ==========================================
// CREATE JOB
// ==========================================

const createJobController = async (
    req,
    res
) => {

    try {

        const {
            recruiter_id,
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status
        } = req.body;


        // Validate required fields

        if (
            !recruiter_id ||
            !job_title ||
            !job_description
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "recruiter_id, job_title and job_description are required"

            });

        }


        const result =
            await createJob(
                recruiter_id,
                job_title,
                job_description,
                required_skills || null,
                qualifications || null,
                location || null,
                salary_min || null,
                salary_max || null,
                job_type || null,
                status || "open"
            );

        // ==========================================
        // AUTOMATIC AI MATCHING FOR NEW JOB
        // ==========================================

        try {

            const finalStatus =
                status || "open";

            if (
                result.insertId &&
                finalStatus === "open"
            ) {

                await recalculateForJob(
                    result.insertId
                );

                console.log(
                    `AI matches generated for new job ${result.insertId}`
                );
            }

        } catch (matchingError) {

            // Job creation must remain successful
            // even if NLP is temporarily unavailable.
            console.error(
                "Automatic job matching failed:",
                matchingError.message
            );
        }

        return res.status(201).json({

            success: true,

            message:
                "Job created successfully",

            data: {

                job_id:
                    result.insertId,

                recruiter_id,

                job_title,

                job_description,

                required_skills:
                    required_skills || null,

                qualifications:
                    qualifications || null,

                location:
                    location || null,

                salary_min:
                    salary_min || null,

                salary_max:
                    salary_max || null,

                job_type:
                    job_type || null,

                status:
                    status || "open"

            }

        });


    } catch (error) {

        console.error(
            "Create Job Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while creating job",

            error:
                error.message

        });

    }

};


// ==========================================
// GET ALL JOBS
// ==========================================

const getAllJobsController = async (
    req,
    res
) => {

    try {

        const jobs =
            await getAllJobs();


        return res.status(200).json({

            success: true,

            count:
                jobs.length,

            data:
                jobs

        });


    } catch (error) {

        console.error(
            "Get All Jobs Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while retrieving jobs",

            error:
                error.message

        });

    }

};

// ==========================================
// GET JOB BY ID
// GET /api/jobs/:id
// ==========================================

const getJobByIdController = async (
    req,
    res
) => {

    try {

        // ======================================
        // GET JOB ID
        // ======================================

        const {
            id
        } = req.params;


        const jobId =
            parseInt(
                id,
                10
            );


        // ======================================
        // VALIDATE JOB ID
        // ======================================

        if (
            Number.isNaN(jobId)
            ||
            jobId <= 0
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid job ID"
            });
        }


        // ======================================
        // GET JOB DETAILS
        // ======================================

        const job =
            await getJobById(
                jobId
            );


        // ======================================
        // JOB NOT FOUND
        // ======================================

        if (!job) {

            return res.status(404).json({

                success: false,

                message:
                    "Job not found"
            });
        }


        // ======================================
        // SUCCESS
        // ======================================

        return res.status(200).json({

            success: true,

            data:
                job
        });


    } catch (error) {

        console.error(
            "Get Job Detail Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while retrieving job details",

            error:
                error.message
        });
    }
};

// ==========================================
// GET JOBS BY RECRUITER
// ==========================================

const getJobsByRecruiterController =
    async (
        req,
        res
    ) => {

        try {

            const {
                recruiter_id
            } = req.params;


            const jobs =
                await getJobsByRecruiter(
                    recruiter_id
                );


            return res.status(200).json({

                success: true,

                count:
                    jobs.length,

                data:
                    jobs

            });


        } catch (error) {

            console.error(
                "Get Recruiter Jobs Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while retrieving recruiter jobs",

                error:
                    error.message

            });

        }

    };


// ==========================================
// UPDATE JOB
// ==========================================

const updateJobController = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const {
            job_title,
            job_description,
            required_skills,
            qualifications,
            location,
            salary_min,
            salary_max,
            job_type,
            status
        } = req.body;


        const finalStatus =
            status || "open";


        const result =
            await updateJob(
                id,
                job_title,
                job_description,
                required_skills || null,
                qualifications || null,
                location || null,
                salary_min || null,
                salary_max || null,
                job_type || null,
                finalStatus
            );


        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "Job not found"
            });
        }


        // ==========================================
        // AUTOMATIC AI RECALCULATION
        // ==========================================

        try {

            if (finalStatus === "open") {

                await recalculateForJob(
                    id
                );

                console.log(
                    `AI matches recalculated for updated job ${id}`
                );
            }

        } catch (matchingError) {

            console.error(
                "Automatic job recalculation failed:",
                matchingError.message
            );
        }


        return res.status(200).json({

            success: true,

            message:
                "Job updated successfully"
        });


    } catch (error) {

        console.error(
            "Update Job Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while updating job",

            error:
                error.message
        });
    }
};

// ==========================================
// UPDATE JOB STATUS
// ==========================================

const updateJobStatusController =
    async (
        req,
        res
    ) => {

        try {

            const {
                id
            } = req.params;


            const {
                status
            } = req.body;


            if (!status) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Status is required"
                });
            }


            const result =
                await updateJobStatus(
                    id,
                    status
                );


            if (
                result.affectedRows === 0
            ) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Job not found"
                });
            }


            // ==========================================
            // AI MATCHING WHEN JOB BECOMES ACTIVE
            // ==========================================

            try {

                if (status === "open") {

                    await recalculateForJob(
                        id
                    );

                    console.log(
                        `AI matches recalculated after activating job ${id}`
                    );
                }

            } catch (matchingError) {

                console.error(
                    "Automatic job status matching failed:",
                    matchingError.message
                );
            }


            return res.status(200).json({

                success: true,

                message:
                    "Job status updated successfully"
            });


        } catch (error) {

            console.error(
                "Update Job Status Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error",

                error:
                    error.message
            });
        }
    };


// ==========================================
// DELETE JOB
// ==========================================

const deleteJobController = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const result =
            await deleteJob(id);


        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "Job not found"

            });

        }


        return res.status(200).json({

            success: true,

            message:
                "Job deleted successfully"

        });


    } catch (error) {

        console.error(
            "Delete Job Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while deleting job",

            error:
                error.message

        });

    }

};


// ==========================================
// SEARCH JOBS
// ==========================================

const searchJobsController = async (
    req,
    res
) => {

    try {

        const {
            keyword,
            location,
            job_type
        } = req.query;


        const jobs =
            await searchJobs(
                keyword,
                location,
                job_type
            );


        return res.status(200).json({

            success: true,

            count:
                jobs.length,

            data:
                jobs

        });


    } catch (error) {

        console.error(
            "Search Jobs Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while searching jobs",

            error:
                error.message

        });

    }

};


// ==========================================
// EXPORT
// ==========================================

module.exports = {

    createJobController,

    getAllJobsController,

    getJobByIdController,

    getJobsByRecruiterController,

    updateJobController,

    updateJobStatusController,

    deleteJobController,

    searchJobsController

};