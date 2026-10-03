const Jobseeker =
        require("../models/jobseekerModel");


exports.getDashboard = async (req, res) => {

    try {

        const userId =
                req.user.user_id;


        // ==========================================
        // DASHBOARD SUMMARY
        // ==========================================

        const dashboard =
                await Jobseeker
                        .getDashboardSummary(
                                userId
                        );


        if (!dashboard) {

            return res.status(404).json({
                success: false,
                message: "Jobseeker profile not found."
            });
        }


        // ==========================================
        // BEST MATCHES
        // ==========================================

        const matches =
                await Jobseeker
                        .getBestMatches(
                                userId,
                                3
                        );


        // ==========================================
        // FORMAT MATCHES
        // ==========================================

        const bestMatches =
                matches.map(match => {

                    let score =
                            Number(
                                    match.similarity_score
                            );


                    /*
                     * Supports either:
                     *
                     * 0.82  → 82%
                     * 82    → 82%
                     */

                    if (score <= 1) {

                        score =
                                score * 100;
                    }


                    return {

                        match_id:
                                match.match_id,

                        job_id:
                                match.job_id,

                        recruiter_id:
                                match.recruiter_id,

                        job_title:
                                match.job_title,

                        company_name:
                                match.company_name,

                        company_logo:
                                match.company_logo,

                        location:
                                match.location,

                        salary_min:
                                match.salary_min !== null
                                        ? Number(match.salary_min)
                                        : null,

                        salary_max:
                                match.salary_max !== null
                                        ? Number(match.salary_max)
                                        : null,

                        job_type:
                                match.job_type,

                        similarity_score:
                                Math.round(
                                        score * 100
                                ) / 100,

                        ranking_position:
                                match.ranking_position,

                        match_reason:
                                match.match_reason
                    };
                });


        // ==========================================
        // RESPONSE
        // ==========================================

        return res.status(200).json({

            success: true,

            dashboard: {

                candidate_name:
                        dashboard.full_name,

                jobs_applied:
                        Number(
                                dashboard.jobs_applied
                        ),

                interviews:
                        Number(
                                dashboard.interviews
                        ),

                best_matches:
                        bestMatches
            }
        });


    } catch (error) {

        console.error(
                "Jobseeker Dashboard Error:",
                error
        );


        return res.status(500).json({

            success: false,

            message:
                    "Failed to load dashboard."

        });
    }
};