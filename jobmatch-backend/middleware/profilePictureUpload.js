const multer = require("multer");
const path = require("path");
const fs = require("fs");


// =====================================================
// PROFILE PICTURE DIRECTORY
// =====================================================

const uploadDirectory =
    path.join(
        __dirname,
        "../uploads/profile-pictures"
    );


if (!fs.existsSync(uploadDirectory)) {
    fs.mkdirSync(
        uploadDirectory,
        { recursive: true }
    );
}


// =====================================================
// STORAGE
// =====================================================

const storage =
    multer.diskStorage({

        destination: (req, file, cb) => {
            cb(null, uploadDirectory);
        },

        filename: (req, file, cb) => {

            const extension =
                path.extname(
                    file.originalname
                ).toLowerCase();

            const fileName =
                "profile-"
                + req.user.user_id
                + "-"
                + Date.now()
                + extension;

            cb(null, fileName);
        }
    });


// =====================================================
// FILE FILTER
// =====================================================

const fileFilter =
    (req, file, cb) => {

        const allowedTypes = [
            "image/jpeg",
            "image/png",
            "image/webp"
        ];

        if (allowedTypes.includes(file.mimetype)) {

            cb(null, true);

        } else {

            cb(
                new Error(
                    "Only JPG, PNG and WEBP images are allowed."
                ),
                false
            );
        }
    };


// =====================================================
// MULTER
// =====================================================

const uploadProfilePicture =
    multer({

        storage,

        limits: {
            fileSize: 5 * 1024 * 1024
        },

        fileFilter
    });


module.exports =
    uploadProfilePicture;