const {
  calculateMatch
} = require("../services/nlpService");


exports.testMatch = async (req, res) => {
  try {

    const {
      candidate_text,
      job_text,
      candidate_skills,
      required_skills
    } = req.body;

    if (!candidate_text || !job_text) {
      return res.status(400).json({
        success: false,
        message:
          "candidate_text and job_text are required."
      });
    }

    const result = await calculateMatch({
      candidateText: candidate_text,
      jobText: job_text,
      candidateSkills:
        Array.isArray(candidate_skills)
          ? candidate_skills
          : [],
      requiredSkills:
        Array.isArray(required_skills)
          ? required_skills
          : []
    });

    return res.status(200).json({
      success: true,
      message:
        "Node.js successfully connected to NLP service.",
      match: result
    });

  } catch (error) {

    console.error(
      "NLP Controller Error:",
      error
    );

    return res.status(500).json({
      success: false,
      message:
        "Unable to communicate with NLP service.",
      error: error.message
    });
  }
};