// =====================================================
// AUTOMATIC AI MATCHING SERVICE
// =====================================================

const db = require("../config/db");

const {
    calculateMatch
} = require("./nlpService");

const {
    createMatchScore,
    getExistingMatch,
    updateMatchScore,
    updateRankingsByJob
} = require("../models/matchScoreModel");


// =====================================================
// HELPER: PARSE SKILLS
// =====================================================

const parseSkills = (skills) => {

    if (!skills) {
        return [];
    }

    return String(skills)
        .split(",")
        .map(skill => skill.trim())
        .filter(Boolean);
};


/**
 * Merge manually entered profile skills with
 * confirmed document-derived skills.
 *
 * Removes duplicates without changing the
 * original display names.
 */
const mergeSkills = (
    profileSkills,
    confirmedDocumentSkills
) => {

    const merged = new Map();

    const allSkills = [
        ...parseSkills(profileSkills),
        ...parseSkills(confirmedDocumentSkills)
    ];

    for (const skill of allSkills) {

        const cleanSkill =
            String(skill).trim();

        const normalizedSkill =
            cleanSkill.toLowerCase();

        if (
            normalizedSkill &&
            !merged.has(normalizedSkill)
        ) {
            merged.set(
                normalizedSkill,
                cleanSkill
            );
        }
    }

    return [...merged.values()];
};

// =====================================================
// HELPER: BUILD CANDIDATE TEXT
// =====================================================


const buildCandidateText = (profile) => {

    return [
        profile.headline,
        profile.skills,
        profile.education,
        profile.experience,
        profile.resume_summary,
        profile.resume_ocr_text,

        // NEW: Only confirmed document skills
        profile.confirmed_document_skills

    ]
        .filter(
            value =>
                value !== null &&
                value !== undefined &&
                String(value).trim() !== ""
        )
        .map(
            value =>
                String(value).trim()
        )
        .join(". ");
};


// =====================================================
// HELPER: BUILD JOB TEXT
// =====================================================

const buildJobText = (job) => {

    return [
        job.job_title,
        job.job_description,
        job.required_skills,
        job.qualifications
    ]
        .filter(
            value =>
                value !== null &&
                value !== undefined &&
                String(value).trim() !== ""
        )
        .join(". ");
};


// =====================================================
// CALCULATE + SAVE ONE MATCH
// =====================================================

const calculateAndSaveMatch = async (job, profile) => {

    const candidateText =
        buildCandidateText(profile);

    const jobText =
        buildJobText(job);


    if (!candidateText || !jobText) {

        return {
            success: false,
            skipped: true,
            job_id: job.job_id,
            profile_id: profile.profile_id
        };
    }

    const candidateSkills = mergeSkills(
        profile.skills,
        profile.confirmed_document_skills
    );

    const requiredSkills =
        parseSkills(job.required_skills);


    // Python NLP service
    const nlpResult =
        await calculateMatch({
            candidateText,
            jobText,
            candidateSkills,
            requiredSkills
        });

        console.log("\n========== AI MATCH PAYLOAD ==========");

            console.log(
                "Job ID:",
                job.job_id
            );

            console.log(
                "Profile ID:",
                profile.profile_id
            );

            console.log(
                "candidateText:",
                candidateText,
                "| type:",
                typeof candidateText
            );

            console.log(
                "jobText:",
                jobText,
                "| type:",
                typeof jobText
            );

            console.log(
                "candidateSkills:",
                candidateSkills,
                "| isArray:",
                Array.isArray(candidateSkills)
            );

            console.log(
                "requiredSkills:",
                requiredSkills,
                "| isArray:",
                Array.isArray(requiredSkills)
            );

            console.log("======================================\n");

    const similarityScore =
        Number(nlpResult.final_score);


    if (!Number.isFinite(similarityScore)) {

        throw new Error(
            "NLP service returned an invalid final_score."
        );
    }


    const existingMatch =
        await getExistingMatch(
            job.job_id,
            profile.profile_id
        );


    if (existingMatch) {

        await updateMatchScore(
            existingMatch.match_id,
            similarityScore,
            0,
            nlpResult.match_reason,
            nlpResult.algorithm
        );

    } else {

        await createMatchScore(
            job.job_id,
            profile.profile_id,
            similarityScore,
            0,
            nlpResult.match_reason,
            nlpResult.algorithm
        );
    }


    return {
        success: true,
        skipped: false,

        job_id:
            job.job_id,

        profile_id:
            profile.profile_id,

        semantic_score:
            nlpResult.semantic_score,

        skill_score:
            nlpResult.skill_score,

        similarity_score:
            similarityScore,

        matched_skills:
            nlpResult.matched_skills || [],

        missing_skills:
            nlpResult.missing_skills || [],

        match_reason:
            nlpResult.match_reason,

        matching_algorithm:
            nlpResult.algorithm
    };
};


// =====================================================
// RECALCULATE ONE JOB AGAINST ALL COMPLETE PROFILES
// =====================================================

const recalculateForJob = async (job_id) => {

    const [jobRows] =
        await db.execute(
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

        throw new Error(
            "Job not found."
        );
    }


    const job =
        jobRows[0];


    // IMPORTANT:
    // Your database uses open / closed / draft.
    if (job.status !== "open") {

        return {
            job_id: Number(job_id),
            processed: 0,
            skipped: 0,
            failed: 0,
            message:
                "Job is not open. Matching skipped."
        };
    }


    const [profiles] =
    await db.execute(
        `
        SELECT
            p.profile_id,
            p.user_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,
            p.profile_status,

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
           
            ) AS resume_ocr_text,

            (
                SELECT GROUP_CONCAT(
                    DISTINCT ds.skill_name
                    ORDER BY ds.skill_name
                    SEPARATOR ','
                )

                FROM document_skills ds

                INNER JOIN documents d
                    ON d.document_id = ds.document_id

                WHERE d.user_id = p.user_id
                AND d.upload_status = 'processed'
                AND d.document_type IN ('resume', 'certificate')
                AND ds.review_status = 'confirmed'

            ) AS confirmed_document_skills

            FROM profiles p

        WHERE p.profile_status = 'complete'
        `
    );

    let processed = 0;
    let skipped = 0;
    let failed = 0;


    for (const profile of profiles) {

        try {

            const result =
                await calculateAndSaveMatch(
                    job,
                    profile
                );


            if (result.skipped) {
                skipped++;
            } else {
                processed++;
            }

        } catch (error) {

            failed++;

            console.error(
                `AI matching failed for job ${job.job_id}, profile ${profile.profile_id}:`,
                error.message
            );
        }
    }


    await updateRankingsByJob(
        job_id
    );


    return {
        job_id: Number(job_id),
        profiles_found: profiles.length,
        processed,
        skipped,
        failed
    };
};


// =====================================================
// RECALCULATE ONE PROFILE AGAINST ALL OPEN JOBS
// =====================================================

const recalculateForProfile = async (profile_id) => {

    
    const [profileRows] = await db.execute(
        `
        SELECT
            p.profile_id,
            p.user_id,
            p.headline,
            p.skills,
            p.education,
            p.experience,
            p.resume_summary,
            p.profile_status,

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
                    o.processed_at DESC,
                    d.document_id DESC

                LIMIT 1

            ) AS resume_ocr_text,

            (
                SELECT GROUP_CONCAT(
                    DISTINCT ds.skill_name
                    ORDER BY ds.skill_name
                    SEPARATOR ','
                )

                FROM document_skills ds

                INNER JOIN documents d
                    ON d.document_id = ds.document_id

                WHERE d.user_id = p.user_id
                AND d.upload_status = 'processed'
                AND d.document_type IN ('resume', 'certificate')
                AND ds.review_status = 'confirmed'

            ) AS confirmed_document_skills

        FROM profiles p

        WHERE p.profile_id = ?
        `,
        [profile_id]
    );

    if (profileRows.length === 0) {

        throw new Error(
            "Profile not found."
        );
    }


    const profile =
        profileRows[0];


    if (profile.profile_status !== "complete") {

        return {
            profile_id: Number(profile_id),
            jobs_found: 0,
            processed: 0,
            skipped: 0,
            failed: 0,
            message:
                "Profile is incomplete. Matching skipped."
        };
    }


    // IMPORTANT:
    // Database status is "open", not "active".
    const [jobs] =
        await db.execute(
            `
            SELECT
                job_id,
                job_title,
                job_description,
                required_skills,
                qualifications,
                status
            FROM jobs
            WHERE status = 'open'
            `
        );


    console.log(
        `AI matching profile ${profile_id}: found ${jobs.length} open jobs`
    );


    let processed = 0;
    let skipped = 0;
    let failed = 0;


    for (const job of jobs) {

        try {

            console.log(
                `Matching profile ${profile.profile_id} with job ${job.job_id} (${job.job_title})`
            );


            const result =
                await calculateAndSaveMatch(
                    job,
                    profile
                );


            if (result.skipped) {
                skipped++;
            } else {
                processed++;
            }


            await updateRankingsByJob(
                job.job_id
            );

        } catch (error) {

            failed++;

            console.error(
                `AI matching failed for profile ${profile.profile_id}, job ${job.job_id}:`,
                error.message
            );
        }
    }


    return {
        profile_id: Number(profile_id),
        jobs_found: jobs.length,
        processed,
        skipped,
        failed
    };
};


// =====================================================
// RECALCULATE ALL OPEN JOBS × COMPLETE PROFILES
// =====================================================

const recalculateAll = async () => {

    const [jobs] =
        await db.execute(
            `
            SELECT
                job_id
            FROM jobs
            WHERE status = 'open'
            `
        );


    let jobsProcessed = 0;
    let matchesProcessed = 0;
    let matchesSkipped = 0;
    let matchesFailed = 0;


    for (const job of jobs) {

        try {

            const result =
                await recalculateForJob(
                    job.job_id
                );


            jobsProcessed++;

            matchesProcessed +=
                result.processed || 0;

            matchesSkipped +=
                result.skipped || 0;

            matchesFailed +=
                result.failed || 0;

        } catch (error) {

            console.error(
                `Full recalculation failed for job ${job.job_id}:`,
                error.message
            );
        }
    }


    return {
        jobs_found: jobs.length,
        jobs_processed: jobsProcessed,
        matches_processed: matchesProcessed,
        matches_skipped: matchesSkipped,
        matches_failed: matchesFailed
    };
};


// =====================================================
// EXPORTS
// =====================================================

module.exports = {
    calculateAndSaveMatch,
    recalculateForJob,
    recalculateForProfile,
    recalculateAll
};