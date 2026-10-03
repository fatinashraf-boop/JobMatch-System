const multer = require('multer');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const directory = path.join(__dirname, '../uploads/company-logos');
fs.mkdirSync(directory, { recursive: true });
const extensions = { 'image/jpeg': '.jpg', 'image/png': '.png', 'image/webp': '.webp' };
module.exports = multer({
  storage: multer.diskStorage({
    destination: (_req, _file, cb) => cb(null, directory),
    filename: (req, file, cb) => cb(null, `company-${req.user.user_id}-${crypto.randomUUID()}${extensions[file.mimetype]}`)
  }),
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => cb(null, Boolean(extensions[file.mimetype]))
});
