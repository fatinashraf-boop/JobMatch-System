const express = require('express');
const multer = require('multer');

const router = express.Router();

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/');
    },

    filename: function (req, file, cb) {
        cb(null, Date.now() + '-' + file.originalname);
    }
});

const upload = multer({ storage: storage });

router.post('/upload', upload.single('resume'), (req, res) => {

    if (!req.file) {
        return res.status(400).json({
            message: 'No resume uploaded'
        });
    }

    res.status(200).json({
        message: 'Resume uploaded successfully',
        filename: req.file.filename,
        path: req.file.path
    });
});

module.exports = router;