const NLP_SERVICE_URL =
  process.env.NLP_SERVICE_URL || "http://127.0.0.1:8000";

/**
 * Send candidate/job data to the Python NLP service.
 */
async function calculateMatch({
  candidateText,
  jobText,
  candidateSkills = [],
  requiredSkills = []
}) {
  try {
    const response = await fetch(
      `${NLP_SERVICE_URL}/match`,
      {
        method: "POST",

        headers: {
          "Content-Type": "application/json"
        },

        body: JSON.stringify({
          candidate_text: candidateText,
          job_text: jobText,
          candidate_skills: candidateSkills,
          required_skills: requiredSkills
        })
      }
    );

    if (!response.ok) {

        const errorText = await response.text();

        console.error(
            "NLP REQUEST FAILED:",
            response.status,
            errorText
        );

        console.error(
            "SENT TO PYTHON:",
            JSON.stringify({
                candidate_text: candidateText,
                job_text: jobText,
                candidate_skills: candidateSkills,
                required_skills: requiredSkills
            }, null, 2)
        );

        throw new Error(
            `NLP service returned ${response.status}: ${errorText}`
        );
    }

    const data = await response.json();

    if (!data.success) {
      throw new Error(
        data.message || "NLP matching failed."
      );
    }

    return data;

  } catch (error) {
    console.error(
      "NLP Service Error:",
      error.message
    );

    throw error;
  }
}

module.exports = {
  calculateMatch
};