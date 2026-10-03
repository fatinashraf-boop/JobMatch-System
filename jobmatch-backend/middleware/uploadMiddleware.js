const multer = require("multer");
const path = require("path");
const fs = require("fs");


// ==========================================
// UPLOAD DIRECTORY
// ==========================================

const uploadDirectory = path.join(
    __dirname,
    "..",
    "uploads"
);


// Create uploads folder if it doesn't exist
if (!fs.existsSync(uploadDirectory)) {

    fs.mkdirSync(uploadDirectory, {
        recursive: true
    });
}


// ==========================================
// STORAGE
// ==========================================

const storage = multer.diskStorage({

    destination: (req, file, cb) => {

        cb(
            null,
            uploadDirectory
        );
    },


    filename: (req, file, cb) => {

        const extension =
            path.extname(
                file.originalname
            ).toLowerCase();


        const filename =
            `document_${Date.now()}${extension}`;


        cb(
            null,
            filename
        );
    }
});


// ==========================================
// FILE FILTER
// JPG / JPEG / PNG / PDF
// ==========================================

const fileFilter = (req, file, cb) => {

    const allowedMimeTypes = [
        "image/jpeg",
        "image/png",
        "application/pdf"
    ];


    const allowedExtensions = [
        ".jpg",
        ".jpeg",
        ".png",
        ".pdf"
    ];


    const extension =
        path.extname(
            file.originalname
        ).toLowerCase();


    const validMimeType =
        allowedMimeTypes.includes(
            file.mimetype
        );


    const validExtension =
        allowedExtensions.includes(
            extension
        );


    if (
        validMimeType
        &&
        validExtension
    ) {

        cb(
            null,
            true
        );

    } else {

        cb(
            new Error(
                "Only JPG, JPEG, PNG and PDF files are allowed"
            ),
            false
        );
    }
};


// ==========================================
// MULTER CONFIGURATION
// ==========================================

const upload = multer({

    storage: storage,

    fileFilter: fileFilter,

    limits: {

        // Maximum file size: 10 MB
        fileSize:
            10 * 1024 * 1024
    }
});


module.exports = upload;