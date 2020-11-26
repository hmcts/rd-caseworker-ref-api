package uk.gov.hmcts.reform.cwrdapi.util;

public final class CaseWorkerConstants {

    private CaseWorkerConstants() {
        super();
    }

    public static final String PARTIAL_SUCCESS = "PARTIAL SUCCESS";

    public static final String SUCCESS = "SUCCESS";

    public static final String FAILURE = "FAILURE";

    public static final String IDAM_ROLE_MAPPINGS_SUCCESS =
            "Successfully built the idam role mappings for case worker roles for the service id provided";
    public static final String DELETE_RECORD_FOR_SERVICE_ID = "deleted all the records for the service id provided";
    public static final String IDAM_ROLE_MAPPINGS_FAILURE =
            "failed to build the idam role mappings for case worker roles for the service id provided";
}
