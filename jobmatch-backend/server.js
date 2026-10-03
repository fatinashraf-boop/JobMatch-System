const express = require("express");

const cors = require("cors");

require("dotenv").config();

const app = express();

app.use(cors());

app.use(express.json());

app.use(express.urlencoded({
    extended: true
}));

const authRoutes =  require("./routes/authRoutes");

const profileRoutes =  require("./routes/profileRoutes");

const jobRoutes = require("./routes/jobRoutes");

const recruiterRoutes = require("./routes/recruiterRoutes");

const documentRoutes = require("./routes/documentRoutes");

const ocrRoutes = require("./routes/ocrRoutes");

const applicationRoutes = require("./routes/applicationRoutes");

const matchScoreRoutes = require("./routes/matchScoreRoutes");

const shortlistRoutes = require("./routes/shortlistRoutes");

const recommendationRoutes = require("./routes/recommendationRoutes");

const jobseekerRoutes = require("./routes/jobseekerRoutes");

const recruiterDashboardRoutes = require("./routes/recruiterDashboardRoutes");

const recruiterMatchRoutes = require("./routes/recruiterMatchRoutes");

const recruiterApplicantRoutes = require("./routes/recruiterApplicantRoutes");

const recruiterJobRoutes = require("./routes/recruiterJobRoutes");

const nlpRoutes = require("./routes/nlpRoutes");

const path = require("path");

app.use(
    "/uploads",
    express.static(
        path.join(
            __dirname,
            "uploads"
        )
    )
);

app.use("/api/recommendations", recommendationRoutes);

app.use("/api/auth", authRoutes);

app.use("/api/profiles", profileRoutes);

app.use("/api/jobs", jobRoutes);

app.use("/api/recruiters", recruiterRoutes);

app.use("/api/documents", documentRoutes);

app.use("/api/ocr", ocrRoutes);

app.use("/api/applications", applicationRoutes);

app.use("/api/match-scores", matchScoreRoutes);

app.use("/api/shortlists", shortlistRoutes);

app.use( "/api/jobseeker", jobseekerRoutes);

app.use("/api/recruiter", recruiterDashboardRoutes);

app.use("/api/recruiter", recruiterMatchRoutes);

app.use("/api/recruiter", recruiterApplicantRoutes);

app.use("/api/recruiter", recruiterJobRoutes);

app.use("/api/nlp", nlpRoutes);


const PORT =
    process.env.PORT || 5000;


app.listen(PORT, () => {

    console.log(
        `JobMatch Backend running on port ${PORT}`
    );

});