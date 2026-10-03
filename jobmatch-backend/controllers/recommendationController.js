const RecommendationModel = require("../models/recommendationModel");

exports.getRecommendations = async (req, res) => {

    try {

        const profileId = req.params.profileId;

        const data =
            await RecommendationModel.getRecommendations(profileId);

        res.json(data);

    } catch (err) {

        console.error(err);

        res.status(500).json({
            message: "Failed to retrieve recommendations."
        });

    }

};

exports.getTopRecommendations = async (req, res) => {

    try {

        const profileId = req.params.profileId;

        const limit = req.query.limit || 10;

        const data =
            await RecommendationModel.getTopRecommendations(profileId, limit);

        res.json(data);

    } catch (err) {

        console.error(err);

        res.status(500).json({
            message: "Failed to retrieve top recommendations."
        });

    }

};

exports.getRecommendation = async (req, res) => {

    try {

        const matchId = req.params.id;

        const recommendation =
            await RecommendationModel.getRecommendation(matchId);

        if (!recommendation) {

            return res.status(404).json({
                message: "Recommendation not found."
            });

        }

        res.json(recommendation);

    } catch (err) {

        console.error(err);

        res.status(500).json({
            message: "Failed to retrieve recommendation."
        });

    }

};