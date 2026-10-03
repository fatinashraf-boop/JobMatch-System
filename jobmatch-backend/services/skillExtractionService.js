
const SKILL_DICTIONARY =
  require("../config/skillDictionary");

/**
 * Normalize OCR text before searching.
 */
const normalizeText = (text) => {
  return String(text || "")
    .normalize("NFKC")
    .replace(/\s+/g, " ")
    .toLowerCase()
    .trim();
};

/**
 * Escape special regular expression characters.
 */
const escapeRegex = (value) => {
  return value.replace(
    /[.*+?^${}()|[\]\\]/g,
    "\\$&"
  );
};

/**
 * Check whether a skill alias exists in the text.
 *
 * The boundaries prevent:
 * - Java from matching JavaScript
 * - SQL from matching MySQL
 * - Git from matching GitHub
 */
const containsSkill = (text, alias) => {
  const normalizedAlias = normalizeText(alias);

  if (!normalizedAlias) {
    return false;
  }

  const escapedAlias =
    escapeRegex(normalizedAlias)
      .replace(/\s+/g, "\\s+");

  const regex = new RegExp(
    `(^|[^a-z0-9])${escapedAlias}(?=$|[^a-z0-9])`,
    "i"
  );

  return regex.test(text);
};

/**
 * Extract possible skills from OCR text.
 *
 * Returns:
 * {
 *   success: true,
 *   total_skills: 2,
 *   suggested_skills: [
 *     {
 *       skill_name: "Python",
 *       matched_term: "python programming",
 *       source: "resume",
 *       review_status: "pending"
 *     }
 *   ]
 * }
 */
const extractSkills = (
  ocrText,
  source = "resume"
) => {

  const normalizedText =
    normalizeText(ocrText);

  const suggestedSkills = [];

  if (!normalizedText) {
    return {
      success: true,
      total_skills: 0,
      suggested_skills: []
    };
  }

  for (const [
    canonicalSkill,
    aliases
  ] of Object.entries(SKILL_DICTIONARY)) {

    for (const alias of aliases) {

      if (containsSkill(normalizedText, alias)) {

        suggestedSkills.push({
          skill_name: canonicalSkill,
          matched_term: alias,
          source: source,
          review_status: "pending"
        });

        // Prevent duplicate suggestions
        // for the same canonical skill.
        break;
      }
    }
  }

  return {
    success: true,
    total_skills: suggestedSkills.length,
    suggested_skills: suggestedSkills
  };
};

module.exports = {
  extractSkills,
  normalizeText,
  containsSkill
};
