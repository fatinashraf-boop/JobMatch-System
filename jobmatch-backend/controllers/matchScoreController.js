const db = require("../config/db");
const {
    calculateMatch
} = require("../services/nlpService");

const {
    recalculateForJob,
    recalculateForProfile,
    recalculateAll
} = require("../services/automaticMatchingService");

const {
    createMatchScore,
    getMatchScoreById,
    getMatchesByJob,
    getMatchesByProfile,
    getExistingMatch,
    updateMatchScore,
    deleteMatchScore,
    updateRankingsByJob
} = require("../models/matchScoreModel");


// ==========================================
// CREATE MATCH
// ==========================================

const createMatch = async (req, res) => {

    try {

        const {
            job_id,
            profile_id
        } = req.body;


        // ==========================================
        // VALIDATE INPUT
        // ==========================================

        if (!job_id || !profile_id) {

            return res.status(400).json({
                success: false,
                message:
                    "job_id and profile_id are required"
            });
        }


        // ==========================================
        // GET REAL JOB DATA
        // ==========================================

        const [jobRows] = await db.execute(
            `
            SELECT
                job_id,
                job_title,
                job_description,
                required_skills,
                qualifications,
                status

            FROM jobs

            WHERE job_id = ?
            `,
            [job_id]
        );


        if (jobRows.length === 0) {

            return res.status(404).json({
                success: false,
                message: "Job not found"
            });
        }


        // ==========================================
        // GET REAL CANDIDATE PROFILE
        // ==========================================

        const [profileRows] = await db.execute(
            `
            SELECT
                profile_id,
                user_id,
                headline,
                skills,
                education,
                experience,
                resume_summary,
                profile_status

            FROM profiles

            WHERE profile_id = ?
            `,
            [profile_id]
        );


        if (profileRows.length === 0) {

            return res.status(404).json({
                success: false,
                message: "Profile not found"
            });
        }


        const job = jobRows[0];
        const profile = profileRows[0];


        // ==========================================
        // BUILD CANDIDATE SEMANTIC TEXT
        // ==========================================

        const candidateText = [
            profile.headline,
            profile.skills,
            profile.education,
            profile.experience,
            profile.resume_summary
        ]
            .filter(value =>
                value !== null &&
                value !== undefined &&
                String(value).trim() !== ""
            )
            .join(". ");


        // ==========================================
        // BUILD JOB SEMANTIC TEXT
        // ==========================================

        const jobText = [
            job.job_title,
            job.job_description,
            job.required_skills,
            job.qualifications
        ]
            .filter(value =>
                value !== null &&
                value !== undefined &&
                String(value).trim() !== ""
            )
            .join(". ");


        if (!candidateText) {

            return res.status(400).json({
                success: false,
                message:
                    "Candidate profile does not contain enough information for matching."
            });
        }


        if (!jobText) {

            return res.status(400).json({
                success: false,
                message:
                    "Job does not contain enough information for matching."
            });
        }


        // ==========================================
        // PREPARE SKILLS
        // ==========================================

        const candidateSkills =
            profile.skills
                ? profile.skills
                    .split(",")
                    .map(skill => skill.trim())
                    .filter(Boolean)
                : [];


        const requiredSkills =
            job.required_skills
                ? job.required_skills
                    .split(",")
                    .map(skill => skill.trim())
                    .filter(Boolean)
                : [];


        // ==========================================
        // CALL PYTHON AI / NLP SERVICE
        // ==========================================

        const nlpResult =
            await calculateMatch({

                candidateText:
                    candidateText,

                jobText:
                    jobText,

                candidateSkills:
                    candidateSkills,

                requiredSkills:
                    requiredSkills
            });


        // ==========================================
        // GET AI RESULTS
        // ==========================================

        const similarityScore =
            Number(nlpResult.final_score);


        if (!Number.isFinite(similarityScore)) {

            throw new Error(
                "NLP service returned an invalid final score."
            );
        }


        const matchReason =
            nlpResult.match_reason;


        const matchingAlgorithm =
            nlpResult.algorithm;


        // ==========================================
        // CHECK EXISTING MATCH
        // ==========================================

        const existingMatch =
            await getExistingMatch(
                job_id,
                profile_id
            );


        // Ranking is calculated in 11A.12
        const rankingPosition = 0;


        // ==========================================
        // UPDATE EXISTING MATCH
        // ==========================================

        if (existingMatch) {

            await updateMatchScore(
                existingMatch.match_id,
                similarityScore,
                rankingPosition,
                matchReason,
                matchingAlgorithm
            );

            // Recalculate rankings after AI score changes
            await updateRankingsByJob(job_id);

            return res.status(200).json({

                success: true,

                message:
                    "AI match score updated successfully",

                data: {

                    match_id:
                        existingMatch.match_id,

                    job_id:
                        Number(job_id),

                    profile_id:
                        Number(profile_id),

                    semantic_score:
                        nlpResult.semantic_score,

                    skill_score:
                        nlpResult.skill_score,

                    similarity_score:
                        similarityScore,

                    ranking_position:
                        rankedMatch.ranking_position,

                    matched_skills:
                        nlpResult.matched_skills,

                    missing_skills:
                        nlpResult.missing_skills,

                    match_reason:
                        matchReason,

                    matching_algorithm:
                        matchingAlgorithm
                }
            });
        }


        // ==========================================
        // CREATE NEW MATCH
        // ==========================================

        const result =
            await createMatchScore(
                job_id,
                profile_id,
                similarityScore,
                rankingPosition,
                matchReason,
                matchingAlgorithm
            );

            // Recalculate all candidate rankings
            // for this job
            await updateRankingsByJob(job_id);

            const rankedMatch =
                await getMatchScoreById(
                    existingMatch.match_id
                );

        return res.status(201).json({

            success: true,

            message:
                "AI match score created successfully",

            data: {

                match_id:
                    result.insertId,

                job_id:
                    Number(job_id),

                profile_id:
                    Number(profile_id),

                semantic_score:
                    nlpResult.semantic_score,

                skill_score:
                    nlpResult.skill_score,

                similarity_score:
                    similarityScore,

                ranking_position:
                    rankedMatch.ranking_position,

                matched_skills:
                    nlpResult.matched_skills,

                missing_skills:
                    nlpResult.missing_skills,

                match_reason:
                    matchReason,

                matching_algorithm:
                    matchingAlgorithm
            }
        });


    } catch (error) {

        console.error(
            "Create AI Match Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while calculating AI match",

            error:
                error.message
        });
    }
};


// ==========================================
// GET MATCH BY ID
// ==========================================

const getMatchById = async (req, res) => {

    try {

        const {
            id
        } = req.params;


        const match =
            await getMatchScoreById(id);


        if (!match) {

            return res.status(404).json({

                success: false,

                message:
                    "Match not found"

            });

        }


        return res.status(200).json({

            success: true,

            data: match

        });


    } catch (error) {

        console.error(
            "Get Match Error:",
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
// GET MATCHES BY JOB
// ==========================================

const getJobMatches = async (req, res) => {

    try {

        const {
            job_id
        } = req.params;


        const matches =
            await getMatchesByJob(job_id);


        return res.status(200).json({

            success: true,

            count:
                matches.length,

            data:
                matches

        });


    } catch (error) {

        console.error(
            "Get Job Matches Error:",
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
// GET MATCHES BY PROFILE
// ==========================================

const getProfileMatches = async (
    req,
    res
) => {

    try {

        const {
            profile_id
        } = req.params;


        const matches =
            await getMatchesByProfile(
                profile_id
            );


        return res.status(200).json({

            success: true,

            count:
                matches.length,

            data:
                matches

        });


    } catch (error) {

        console.error(
            "Get Profile Matches Error:",
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
// UPDATE MATCH
// ==========================================

const updateMatch = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const {
            similarity_score,
            ranking_position,
            match_reason,
            matching_algorithm
        } = req.body;


        const result =
            await updateMatchScore(
                id,
                similarity_score,
                ranking_position,
                match_reason,
                matching_algorithm
            );


        if (result.affectedRows === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Match not found"

            });

        }


        return res.status(200).json({

            success: true,

            message:
                "Match updated successfully"

        });


    } catch (error) {

        console.error(
            "Update Match Error:",
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
// DELETE MATCH
// ==========================================

const deleteMatch = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const result =
            await deleteMatchScore(id);


        if (result.affectedRows === 0) {

            return res.status(404).json({

                success: false,

                message:
                    "Match not found"

            });

        }


        return res.status(200).json({

            success: true,

            message:
                "Match deleted successfully"

        });


    } catch (error) {

        console.error(
            "Delete Match Error:",
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
// RECALCULATE MATCHES FOR ONE JOB
// ==========================================

const recalculateJobMatches =
    async (req, res) => {

        try {

            const {
                job_id
            } = req.params;


            const result =
                await recalculateForJob(
                    job_id
                );


            return res.status(200).json({

                success: true,

                message:
                    "AI matches recalculated for job successfully.",

                data:
                    result
            });


        } catch (error) {

            console.error(
                "Recalculate Job Matches Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to recalculate job matches.",

                error:
                    error.message
            });
        }
    };


// ==========================================
// RECALCULATE MATCHES FOR ONE PROFILE
// ==========================================

const recalculateProfileMatches =
    async (req, res) => {

        try {

            const {
                profile_id
            } = req.params;


            const result =
                await recalculateForProfile(
                    profile_id
                );


            return res.status(200).json({

                success: true,

                message:
                    "AI matches recalculated for profile successfully.",

                data:
                    result
            });


        } catch (error) {

            console.error(
                "Recalculate Profile Matches Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to recalculate profile matches.",

                error:
                    error.message
            });
        }
    };


// ==========================================
// RECALCULATE ALL AI MATCHES
// ==========================================

const recalculateAllMatches =
    async (req, res) => {

        try {

            const result =
                await recalculateAll();


            return res.status(200).json({

                success: true,

                message:
                    "All AI matches recalculated successfully.",

                data:
                    result
            });


        } catch (error) {

            console.error(
                "Recalculate All Matches Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Failed to recalculate all AI matches.",

                error:
                    error.message
            });
        }
    };

module.exports = {

    createMatch,

    getMatchById,

    getJobMatches,

    getProfileMatches,

    updateMatch,

    deleteMatch,

    recalculateJobMatches,

    recalculateProfileMatches,

    recalculateAllMatches
};