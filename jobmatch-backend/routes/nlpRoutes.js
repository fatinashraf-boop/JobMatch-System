const express = require("express");
const router = express.Router();

const {
  testMatch
} = require("../controllers/nlpController");


router.post(
  "/test-match",
  testMatch
);


module.exports = router;