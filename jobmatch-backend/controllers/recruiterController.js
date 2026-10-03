const Recruiter =
    require("../models/recruiterModel");


// ==========================================
// GET MY RECRUITER PROFILE
// JOIN users + recruiters
// ==========================================
exports.getMyProfile = async (req, res) => {

    try {

        const user_id = req.user.user_id;

        const data =
            await Recruiter.findFullProfileByUserId(
                user_id
            );

        if (!data) {
            return res.status(404).json({
                success: false,
                message: "Recruiter profile not found."
            });
        }

        return res.status(200).json({

            success: true,

            user: {
                user_id: data.user_id,
                full_name: data.full_name,
                email: data.email,
                phone_number: data.phone_number,
                role: data.role
            },

            recruiter: {
                recruiter_id: data.recruiter_id,
                user_id: data.user_id,
                company_name: data.company_name,
                company_email: data.company_email,
                company_description:
                    data.company_description,
                website: data.website,
                company_logo: data.company_logo,
                address: data.address,
                verification_status:
                    data.verification_status,
                recruiter_status:
                    data.recruiter_status,
                created_at:
                    data.recruiter_created_at,
                updated_at:
                    data.recruiter_updated_at
            }
        });

    } catch (error) {

        console.error(
            "Get Recruiter Profile Error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve recruiter profile."
        });
    }
};


// ==========================================
// UPDATE MY RECRUITER PROFILE
// ==========================================

exports.updateMyProfile = async (req, res) => {

    try {

        const user_id =
            req.user.user_id;


        const {
            company_name,
            company_email,
            company_description,
            website,
            company_logo,
            address
        } = req.body;


        // ==========================================
        // VALIDATE
        // ==========================================

        if (
            !company_name ||
            !company_email ||
            !company_description ||
            !address
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Please fill in all required company fields."

            });
        }


// Ensure the logged-in recruiter has a profile.
// This also recovers accounts whose initial profile creation failed.

const exists = await Recruiter.exists(user_id);

if (!exists) {

    console.log(
        "Creating missing recruiter profile for user:",
        user_id
    );

    await Recruiter.create({
        user_id: user_id,
        company_name: company_name,
        company_email: company_email,
        company_description: company_description,
        website: website || null,
        company_logo: company_logo || null,
        address: address,
        verification_status: "pending",
        recruiter_status: "incomplete"
    });
}


        // ==========================================
        // UPDATE EXISTING RECRUITER
        // ==========================================

        const affectedRows =
            await Recruiter.update(
                user_id,
                {

                    company_name,

                    company_email,

                    company_description,

                    website:
                        website || null,

                    company_logo:
                        (await Recruiter.findByUserId(user_id))?.company_logo || null,

                    address,

                    recruiter_status:
                        "complete"
                }
            );


        if (affectedRows === 0) {

            return res.status(400).json({

                success: false,

                message:
                    "Recruiter profile was not updated."

            });
        }


        // ==========================================
        // GET UPDATED PROFILE
        // ==========================================

        const updatedRecruiter =
            await Recruiter.findByUserId(
                user_id
            );


        return res.status(200).json({

            success: true,

            message:
                "Company account updated successfully.",

            recruiter:
                updatedRecruiter

        });


    } catch (error) {

        console.error(
            "Update Recruiter Profile Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Internal Server Error"

        });
    }
};
// POST /api/recruiters/me/logo
exports.uploadMyLogo = async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ success: false, message: 'Choose a JPG, PNG or WebP image (maximum 5 MB).' });
        const user_id = req.user.user_id;
        const recruiter = await Recruiter.findByUserId(user_id);
        if (!recruiter) return res.status(404).json({ success: false, message: 'Create the company account first.' });
        const logoPath = '/uploads/company-logos/' + req.file.filename;
        await Recruiter.updateLogo(user_id, logoPath);
        return res.json({ success: true, message: 'Company logo uploaded.', recruiter: await Recruiter.findByUserId(user_id) });
    } catch (error) {
        console.error('Company logo upload error:', error);
        return res.status(500).json({ success: false, message: 'Unable to upload company logo.' });
    }
};
