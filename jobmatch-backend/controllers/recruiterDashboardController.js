const recruiterDashboardModel =
    require("../models/recruiterDashboardModel");


// ==========================================
// GET RECRUITER DASHBOARD SUMMARY
// ==========================================

const getRecruiterDashboard =
    async (req, res) => {

        try {

            const userId =
                req.user.user_id;


            // ----------------------------------
            // GET RECRUITER
            // ----------------------------------

            const recruiter =
                await recruiterDashboardModel
                    .getRecruiterByUserId(
                        userId
                    );


            if (!recruiter) {

                return res.status(404).json({
                    success: false,
                    message: "Recruiter profile not found"
                });
            }


            const recruiterId =
                recruiter.recruiter_id;


            // ----------------------------------
            // DASHBOARD COUNTS
            // ----------------------------------

            const [
                activeJobs,
                totalApplicants,
                shortlisted
            ] = await Promise.all([

                recruiterDashboardModel
                    .countActiveJobs(
                        recruiterId
                    ),

                recruiterDashboardModel
                    .countApplicants(
                        recruiterId
                    ),

                recruiterDashboardModel
                    .countShortlisted(
                        recruiterId
                    )
            ]);


            // ----------------------------------
            // RESPONSE
            // ----------------------------------

            return res.status(200).json({

                success: true,

                data: {

                    company_name:
                        recruiter.company_name,

                    active_jobs:
                        activeJobs,

                    total_applicants:
                        totalApplicants,

                    shortlisted:
                        shortlisted
                }
            });


        } catch (error) {

            console.error(
                "Recruiter Dashboard Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to load recruiter dashboard"
            });
        }
    };


module.exports = {
    getRecruiterDashboard
};