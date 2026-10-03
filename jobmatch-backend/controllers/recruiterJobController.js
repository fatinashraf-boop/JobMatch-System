const RecruiterJob =
    require("../models/recruiterJobModel");


// =====================================================
// CREATE JOB
// POST /api/recruiter/jobs
// =====================================================

exports.createJob = async (req, res) => {

    try {

        // =============================================
        // JWT USER
        // =============================================

        const userId =
            req.user.user_id || req.user.id;


        if (!userId) {

            return res.status(401).json({
                success: false,
                message: "Invalid authentication information."
            });
        }


        // =============================================
        // FIND RECRUITER
        // =============================================

        const recruiter =
            await RecruiterJob.getRecruiterByUserId(
                userId
            );


        if (!recruiter) {

            return res.status(404).json({
                success: false,
                message: "Recruiter profile not found."
            });
        }


        // =============================================
        // REQUIRE COMPLETED RECRUITER PROFILE
        // =============================================

        if (
            recruiter.recruiter_status !== "complete"
        ) {

            return res.status(403).json({
                success: false,
                message:
                    "Please complete your recruiter profile before posting a job."
            });
        }


        // =============================================
        // BODY
        // =============================================

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


        // =============================================
        // REQUIRED FIELDS
        // =============================================

        if (
            !job_title ||
            !job_description ||
            !required_skills ||
            !location ||
            !job_type
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Job title, description, required skills, location and job type are required."
            });
        }


        // =============================================
        // CLEAN VALUES
        // =============================================

        const cleanTitle =
            job_title.trim();

        const cleanDescription =
            job_description.trim();

        const cleanSkills =
            required_skills.trim();

        const cleanLocation =
            location.trim();

        const cleanQualifications =
            qualifications
                ? qualifications.trim()
                : null;


        if (
            !cleanTitle ||
            !cleanDescription ||
            !cleanSkills ||
            !cleanLocation
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Required job fields cannot be empty."
            });
        }


        // =============================================
        // JOB TYPE
        // =============================================

        const allowedJobTypes = [
            "Full-time",
            "Part-time",
            "Contract",
            "Internship"
        ];


        if (
            !allowedJobTypes.includes(job_type)
        ) {

            return res.status(400).json({
                success: false,
                message: "Invalid job type."
            });
        }


        // =============================================
        // STATUS
        // =============================================

        const cleanStatus =
            status
                ? status.toLowerCase().trim()
                : "open";


        if (
            !["open", "draft"].includes(
                cleanStatus
            )
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Job status must be open or draft."
            });
        }


        // =============================================
        // SALARY
        // =============================================

        const minSalary =
            salary_min === null ||
            salary_min === undefined ||
            salary_min === ""
                ? null
                : Number(salary_min);


        const maxSalary =
            salary_max === null ||
            salary_max === undefined ||
            salary_max === ""
                ? null
                : Number(salary_max);


        if (
            minSalary !== null &&
            (
                Number.isNaN(minSalary) ||
                minSalary < 0
            )
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Minimum salary must be a valid positive number."
            });
        }


        if (
            maxSalary !== null &&
            (
                Number.isNaN(maxSalary) ||
                maxSalary < 0
            )
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Maximum salary must be a valid positive number."
            });
        }


        if (
            minSalary !== null &&
            maxSalary !== null &&
            maxSalary < minSalary
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Maximum salary cannot be lower than minimum salary."
            });
        }


        // =============================================
        // INSERT
        // =============================================

        const jobId =
            await RecruiterJob.createJob(
                recruiter.recruiter_id,
                {
                    job_title: cleanTitle,
                    job_description:
                        cleanDescription,
                    required_skills:
                        cleanSkills,
                    qualifications:
                        cleanQualifications,
                    location:
                        cleanLocation,
                    salary_min:
                        minSalary,
                    salary_max:
                        maxSalary,
                    job_type:
                        job_type,
                    status:
                        cleanStatus
                }
            );


        // =============================================
        // GET CREATED JOB
        // =============================================

        const createdJob =
            await RecruiterJob.getJobById(
                jobId,
                recruiter.recruiter_id
            );


        return res.status(201).json({

            success: true,

            message:
                "Job created successfully.",

            data: createdJob
        });


    } catch (error) {

        console.error(
            "Create recruiter job error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Server error while creating job."
        });
    }
};

    exports.getMyJobs = async (req, res) => {

    try {

        const userId =
            req.user.user_id || req.user.id;


        const recruiter =
            await RecruiterJob.getRecruiterByUserId(
                userId
            );


        if (!recruiter) {

            return res.status(404).json({
                success: false,
                message: "Recruiter profile not found."
            });
        }


        const jobs =
            await RecruiterJob.getJobsByRecruiter(
                recruiter.recruiter_id
            );


        return res.status(200).json({

            success: true,

            count: jobs.length,

            jobs: jobs
        });


    } catch (error) {

        console.error(
            "Get recruiter jobs error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Server error while loading jobs."
        });
        }
    };

    exports.getMyJobById = async (req, res) => {

    try {

        const userId =
            req.user.user_id || req.user.id;

        const jobId =
            Number(req.params.jobId);


        const recruiter =
            await RecruiterJob.getRecruiterByUserId(
                userId
            );


        if (!recruiter) {

            return res.status(404).json({
                success: false,
                message: "Recruiter profile not found."
            });
        }


        const job =
            await RecruiterJob.getOwnedJobDetail(
                recruiter.recruiter_id,
                jobId
            );


        if (!job) {

            return res.status(404).json({
                success: false,
                message:
                    "Job not found or you do not own this job."
            });
        }


        return res.status(200).json({
            success: true,
            data: job
        });


    } catch (error) {

        console.error(
            "Get recruiter job error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Server error while loading job."
        });
    }
};
exports.updateJob = async (req, res) => {

    try {

        const userId =
            req.user.user_id || req.user.id;

        const jobId =
            Number(req.params.jobId);


        const recruiter =
            await RecruiterJob.getRecruiterByUserId(
                userId
            );


        if (!recruiter) {

            return res.status(404).json({
                success: false,
                message:
                    "Recruiter profile not found."
            });
        }


        const existingJob =
            await RecruiterJob.getOwnedJobDetail(
                recruiter.recruiter_id,
                jobId
            );


        if (!existingJob) {

            return res.status(404).json({
                success: false,
                message:
                    "Job not found or you do not own this job."
            });
        }


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


        if (
            !job_title ||
            !job_description ||
            !required_skills ||
            !location ||
            !job_type
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Required job information is missing."
            });
        }


        const allowedJobTypes = [
            "Full-time",
            "Part-time",
            "Contract",
            "Internship"
        ];


        if (
            !allowedJobTypes.includes(job_type)
        ) {

            return res.status(400).json({
                success: false,
                message: "Invalid job type."
            });
        }


        const cleanStatus =
            status
                ? status.toLowerCase().trim()
                : existingJob.status;


        if (
            ![
                "open",
                "draft",
                "closed"
            ].includes(cleanStatus)
        ) {

            return res.status(400).json({
                success: false,
                message: "Invalid job status."
            });
        }


        const minSalary =
            salary_min === null ||
            salary_min === undefined ||
            salary_min === ""
                ? null
                : Number(salary_min);


        const maxSalary =
            salary_max === null ||
            salary_max === undefined ||
            salary_max === ""
                ? null
                : Number(salary_max);


        if (
            minSalary !== null &&
            (
                Number.isNaN(minSalary) ||
                minSalary < 0
            )
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Invalid minimum salary."
            });
        }


        if (
            maxSalary !== null &&
            (
                Number.isNaN(maxSalary) ||
                maxSalary < 0
            )
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Invalid maximum salary."
            });
        }


        if (
            minSalary !== null &&
            maxSalary !== null &&
            maxSalary < minSalary
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Maximum salary cannot be lower than minimum salary."
            });
        }


        await RecruiterJob.updateJob(
            recruiter.recruiter_id,
            jobId,
            {
                job_title:
                    job_title.trim(),

                job_description:
                    job_description.trim(),

                required_skills:
                    required_skills.trim(),

                qualifications:
                    qualifications
                        ? qualifications.trim()
                        : null,

                location:
                    location.trim(),

                salary_min:
                    minSalary,

                salary_max:
                    maxSalary,

                job_type:
                    job_type,

                status:
                    cleanStatus
            }
        );


        const updatedJob =
            await RecruiterJob.getOwnedJobDetail(
                recruiter.recruiter_id,
                jobId
            );


        return res.status(200).json({

            success: true,

            message:
                "Job updated successfully.",

            data:
                updatedJob
        });


    } catch (error) {

        console.error(
            "Update recruiter job error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Server error while updating job."
        });
    }
};

exports.updateJobStatus = async (req, res) => {

    try {

        const userId =
            req.user.user_id || req.user.id;

        const jobId =
            Number(req.params.jobId);

        const {
            status
        } = req.body;


        const recruiter =
            await RecruiterJob.getRecruiterByUserId(
                userId
            );


        if (!recruiter) {

            return res.status(404).json({
                success: false,
                message:
                    "Recruiter profile not found."
            });
        }


        const job =
            await RecruiterJob.getOwnedJobDetail(
                recruiter.recruiter_id,
                jobId
            );


        if (!job) {

            return res.status(404).json({
                success: false,
                message:
                    "Job not found or you do not own this job."
            });
        }


        const cleanStatus =
            status
                ? status.toLowerCase().trim()
                : "";


        if (
            ![
                "open",
                "draft",
                "closed"
            ].includes(cleanStatus)
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Status must be open, draft or closed."
            });
        }


        await RecruiterJob.updateJobStatus(
            recruiter.recruiter_id,
            jobId,
            cleanStatus
        );


        return res.status(200).json({

            success: true,

            message:
                "Job status updated successfully."
        });


    } catch (error) {

        console.error(
            "Update job status error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Server error while updating job status."
        });
    }
};