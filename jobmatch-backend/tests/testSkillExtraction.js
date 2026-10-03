
const assert = require("node:assert/strict");

const {
  extractSkills
} = require("../services/skillExtractionService");

// ---------------------------------------
// TEST 1: Example resume
// ---------------------------------------

const sampleResume = `
NUR IZZATI

Bachelor of Information Technology

Technical Skills:
- Python programming
- MySQL
- SQL queries
- Database normalization
- Entity Relationship Diagram
- Microsoft Excel

Project Experience:
Developed a database application using
MySQL and Python.
`;

const result = extractSkills(
  sampleResume,
  "resume"
);

console.log("\n========== TEST 1 ==========");
console.log(JSON.stringify(result, null, 2));

const names = result.suggested_skills.map(
  item => item.skill_name
);

for (const expected of [
  "Python",
  "MySQL",
  "SQL",
  "Database Normalization",
  "ERD",
  "Microsoft Excel"
]) {
  assert.ok(
    names.includes(expected),
    `Missing expected skill: ${expected}`
  );
}

assert.equal(
  names.length,
  new Set(names).size,
  "Duplicate skills detected"
);

assert.ok(
  result.suggested_skills.every(
    item => item.review_status === "pending"
  )
);

// ---------------------------------------
// TEST 2: Prevent partial word matches
// ---------------------------------------

const javascriptResult = extractSkills(
  "Experienced in JavaScript and GitHub."
);

const javascriptSkills =
  javascriptResult.suggested_skills.map(
    item => item.skill_name
  );

assert.ok(
  javascriptSkills.includes("JavaScript")
);

assert.ok(
  javascriptSkills.includes("GitHub")
);

assert.ok(
  !javascriptSkills.includes("Java"),
  "Java incorrectly matched JavaScript"
);

assert.ok(
  !javascriptSkills.includes("Git"),
  "Git incorrectly matched GitHub"
);

// ---------------------------------------
// TEST 3: Empty OCR text
// ---------------------------------------

const emptyResult = extractSkills("");

assert.equal(emptyResult.total_skills, 0);

// ---------------------------------------
// TEST 4: Duplicate mentions
// ---------------------------------------

const duplicateResult = extractSkills(
  "Python, Python programming, python."
);

assert.equal(
  duplicateResult.suggested_skills.filter(
    item => item.skill_name === "Python"
  ).length,
  1
);

console.log("\nAll skill extraction tests passed!");
