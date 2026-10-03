
const {
  performOCR
} = require("../services/ocrService");

const {
  extractSkills
} = require("../services/skillExtractionService");

const path = require("path");

async function runTest() {
  try {

    // Change this filename to an existing
    // fictional resume in your test folder.
    const resumePath = path.join(
      __dirname,
      "sample_resume.pdf"
    );

    console.log("Starting OCR...");

    const ocrResult =
      await performOCR(resumePath);

    console.log("\nOCR Confidence:");
    console.log(ocrResult.confidence);

    console.log("\nExtracted Text:");
    console.log(ocrResult.text);

    const skillResult =
      extractSkills(
        ocrResult.text,
        "resume"
      );

    console.log("\nSuggested Skills:");
    console.log(
      JSON.stringify(skillResult, null, 2)
    );

  } catch (error) {
    console.error(
      "Test failed:",
      error.message
    );

    process.exitCode = 1;
  }
}

runTest();
