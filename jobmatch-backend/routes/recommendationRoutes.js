const express = require("express");
const router = express.Router();

const recommendationController =
require("../controllers/recommendationController");

// All recommendations
router.get(
    "/profile/:profileId",
    recommendationController.getRecommendations
);

// Top recommendations
router.get(
    "/profile/:profileId/top",
    recommendationController.getTopRecommendations
);

// Single recommendation
router.get(
    "/:id",
    recommendationController.getRecommendation
);

module.exports = router;