const express = require("express");
const router = express.Router();

const upload = require("../middleware/uploadMiddleware");

const {
    uploadDocument,
    getDocument,
    getUserDocuments,
    getUserDocumentsByType,
    getMyDocuments,
    editDocument,
    changeUploadStatus,
    removeDocument,
    getDocumentSkillSuggestions,
    reviewDocumentSkill
} = require("../controllers/documentController");

const {
    verifyToken,
    authorizeRoles,
    isJobSeeker
} = require("../middleware/authMiddleware");

router.post(
    "/",
    upload.single("document"),
    uploadDocument
);

router.get(
    "/user/:user_id",
    getUserDocuments
);

router.get(
    "/user/:user_id/type/:document_type",
    getUserDocumentsByType
);

router.get(
        "/me",
        verifyToken,
        isJobSeeker,
        getMyDocuments
);

router.get(
    "/:id",
    getDocument
);

router.put(
    "/:id",
    editDocument
);

router.patch(
    "/:id/status",
    changeUploadStatus
);

router.delete(
    "/:id",
    verifyToken,
    authorizeRoles("jobseeker", "admin"),
    removeDocument
);

router.get(
    "/:id/skills",
    verifyToken,
    getDocumentSkillSuggestions
);

router.patch(
    "/:id/skills/:skillId/review",
    verifyToken,
    reviewDocumentSkill
);


module.exports = router;
