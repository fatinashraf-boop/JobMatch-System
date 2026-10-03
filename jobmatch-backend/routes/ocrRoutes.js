const express = require("express");

const router = express.Router();


const {
    processOCR,
    getOCR,
    getDocumentOCR,
    editOCR,
    removeOCR
} = require("../controllers/ocrController");


// ==========================================
// CREATE / PROCESS OCR
// ==========================================
// POST /api/ocr
// ==========================================

router.post(
    "/",
    processOCR
);


// ==========================================
// GET OCR RESULT BY DOCUMENT
// ==========================================
// GET /api/ocr/document/:document_id
// ==========================================

router.get(
    "/document/:document_id",
    getDocumentOCR
);


// ==========================================
// GET OCR RESULT BY ID
// ==========================================
// GET /api/ocr/:id
// ==========================================

router.get(
    "/:id",
    getOCR
);


// ==========================================
// UPDATE OCR RESULT
// ==========================================
// PUT /api/ocr/:id
// ==========================================

router.put(
    "/:id",
    editOCR
);


// ==========================================
// DELETE OCR RESULT
// ==========================================
// DELETE /api/ocr/:id
// ==========================================

router.delete(
    "/:id",
    removeOCR
);


// ==========================================
// EXPORT ROUTER
// ==========================================

module.exports = router;