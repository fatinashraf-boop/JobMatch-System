const {
    createShortlist,
    getAllShortlists,
    getShortlistById,
    getShortlistDetails,
    getShortlistsByRecruiter,
    getShortlistsByJob,
    checkShortlistExists,
    updateShortlistStatus,
    deleteShortlist
} = require("../models/shortlistModel");


// ==========================================
// CREATE / RESTORE SHORTLIST
// ==========================================

const createShortlistController = async (
    req,
    res
) => {

    try {

        const {
            application_id,
            recruiter_id,
            status
        } = req.body;


        if (
            !application_id ||
            !recruiter_id
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "application_id and recruiter_id are required"

            });
        }


        // ------------------------------------------
        // Check existing shortlist
        // ------------------------------------------

        const existingShortlist =
            await checkShortlistExists(
                application_id
            );


        // ------------------------------------------
        // Existing REMOVED shortlist:
        // restore instead of creating duplicate
        // ------------------------------------------

        if (
            existingShortlist &&
            existingShortlist.status === "removed"
        ) {

            const result =
                await restoreShortlist(
                    existingShortlist.shortlist_id,
                    application_id
                );


            if (result.affectedRows === 0) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Unable to restore shortlist"

                });
            }


            return res.status(200).json({

                success: true,

                message:
                    "Candidate shortlisted again successfully",

                data: {

                    shortlist_id:
                        existingShortlist.shortlist_id,

                    application_id:
                        application_id,

                    recruiter_id:
                        recruiter_id,

                    status:
                        "shortlisted"

                }

            });
        }


        // ------------------------------------------
        // Already actively shortlisted
        // ------------------------------------------

        if (existingShortlist) {

            return res.status(409).json({

                success: false,

                message:
                    "This application is already shortlisted",

                data:
                    existingShortlist

            });
        }


        // ------------------------------------------
        // First time shortlist
        // ------------------------------------------

        const result =
            await createShortlist(
                application_id,
                recruiter_id,
                status || "shortlisted"
            );


        return res.status(201).json({

            success: true,

            message:
                "Candidate shortlisted successfully",

            data: {

                shortlist_id:
                    result.insertId,

                application_id:
                    application_id,

                recruiter_id:
                    recruiter_id,

                status:
                    status || "shortlisted"

            }

        });


    } catch (error) {

        console.error(
            "Create Shortlist Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while shortlisting candidate",

            error:
                error.message

        });
    }
};


// ==========================================
// GET ALL SHORTLISTS
// ==========================================

const getAllShortlistsController = async (
    req,
    res
) => {

    try {

        const shortlists =
            await getAllShortlists();


        return res.status(200).json({

            success: true,

            count:
                shortlists.length,

            data:
                shortlists

        });


    } catch (error) {

        console.error(
            "Get All Shortlists Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while retrieving shortlisted candidates",

            error:
                error.message

        });

    }

};


// ==========================================
// GET SHORTLIST BY ID
// ==========================================

const getShortlistByIdController = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const shortlist =
            await getShortlistById(id);


        if (!shortlist) {

            return res.status(404).json({

                success: false,

                message:
                    "Shortlisted candidate not found"

            });

        }


        return res.status(200).json({

            success: true,

            data:
                shortlist

        });


    } catch (error) {

        console.error(
            "Get Shortlist Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while retrieving shortlist",

            error:
                error.message

        });

    }

};


// ==========================================
// GET SHORTLIST DETAILS
// ==========================================

const getShortlistDetailsController =
    async (
        req,
        res
    ) => {

        try {

            const {
                id
            } = req.params;


            const shortlist =
                await getShortlistDetails(id);


            if (!shortlist) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Shortlisted candidate not found"

                });

            }


            return res.status(200).json({

                success: true,

                data:
                    shortlist

            });


        } catch (error) {

            console.error(
                "Get Shortlist Details Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while retrieving shortlist details",

                error:
                    error.message

            });

        }

    };


// ==========================================
// GET SHORTLISTS BY RECRUITER
// ==========================================

const getShortlistsByRecruiterController =
    async (
        req,
        res
    ) => {

        try {

            const {
                recruiter_id
            } = req.params;


            const shortlists =
                await getShortlistsByRecruiter(
                    recruiter_id
                );


            return res.status(200).json({

                success: true,

                count:
                    shortlists.length,

                data:
                    shortlists

            });


        } catch (error) {

            console.error(
                "Get Recruiter Shortlists Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while retrieving recruiter shortlists",

                error:
                    error.message

            });

        }

    };


// ==========================================
// GET SHORTLISTS BY JOB
// ==========================================

const getShortlistsByJobController =
    async (
        req,
        res
    ) => {

        try {

            const {
                job_id
            } = req.params;


            const shortlists =
                await getShortlistsByJob(
                    job_id
                );


            return res.status(200).json({

                success: true,

                count:
                    shortlists.length,

                data:
                    shortlists

            });


        } catch (error) {

            console.error(
                "Get Job Shortlists Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while retrieving job shortlists",

                error:
                    error.message

            });

        }

    };


// ==========================================
// UPDATE SHORTLIST STATUS
// ==========================================

const updateShortlistStatusController =
    async (
        req,
        res
    ) => {

        try {

            const {
                id
            } = req.params;


            const {
                status
            } = req.body;


            if (!status) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Status is required"

                });

            }


            const result =
                await updateShortlistStatus(
                    id,
                    status
                );


            if (
                result.affectedRows === 0
            ) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Shortlisted candidate not found"

                });

            }


            return res.status(200).json({

                success: true,

                message:
                    "Shortlist status updated successfully"

            });


        } catch (error) {

            console.error(
                "Update Shortlist Status Error:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while updating shortlist status",

                error:
                    error.message

            });

        }

    };

// ==========================================
// REMOVE FROM SHORTLIST
// Candidate returns to REVIEWED
// ==========================================

const deleteShortlistController = async (
    req,
    res
) => {

    try {

        const {
            id
        } = req.params;


        const result =
            await deleteShortlist(id);


        if (
            result.affectedRows === 0
        ) {

            return res.status(404).json({

                success: false,

                message:
                    "Shortlisted candidate not found"

            });
        }


        return res.status(200).json({

            success: true,

            message:
                "Candidate removed from shortlist and returned to reviewed applicants.",

            application_status:
                "reviewed",

            shortlist_status:
                "removed"

        });


    } catch (error) {

        console.error(
            "Remove Shortlist Error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error while removing candidate from shortlist",

            error:
                error.message

        });
    }
};

// ==========================================
// EXPORT
// ==========================================

module.exports = {

    createShortlistController,

    getAllShortlistsController,

    getShortlistByIdController,

    getShortlistDetailsController,

    getShortlistsByRecruiterController,

    getShortlistsByJobController,

    updateShortlistStatusController,

    deleteShortlistController

};