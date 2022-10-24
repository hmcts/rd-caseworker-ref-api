package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

@Service
public class FeatureToggleServiceImpl implements FeatureToggleService {

    public static final String CWD_UPLOAD_FILE_FLAG = "cwd-upload-file-flag";
    public static final String CWD_DELETE_BY_ID_OR_EMAILPATTERN_FLAG = "delete-caseworker-by-id-or-emailpattern";
    public static final String CWD_FETCH_STAFF_BY_CCD_SERVICE_NAMES = "fetch-staff-by-ccd-service-names";
    public static final String STAFF_REF_DATA_RD_STAFF_UI = "rd-staff-ui";

    public static final String SRD_RD_STAFF_UI = "rd-staff-ui";


    public static final String RD_STAFF_UI = "rd-staff-ui";

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;

    private Map<String, String> launchDarklyMap;

    @Autowired
    public FeatureToggleServiceImpl(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    @PostConstruct
    public void mapServiceToFlag() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("CaseWorkerRefUsersController.fetchCaseworkersById",
                CWD_UPLOAD_FILE_FLAG);
        launchDarklyMap.put("CaseWorkerRefUsersController.createCaseWorkerProfiles",
                CWD_UPLOAD_FILE_FLAG);
        launchDarklyMap.put("CaseWorkerRefController.buildIdamRoleMappings",
                CWD_UPLOAD_FILE_FLAG);
        launchDarklyMap.put("CaseWorkerRefController.caseWorkerFileUpload",
                CWD_UPLOAD_FILE_FLAG);
        launchDarklyMap.put("CaseWorkerRefUsersController.deleteCaseWorkerProfileByIdOrEmailPattern",
                CWD_DELETE_BY_ID_OR_EMAILPATTERN_FLAG);
        launchDarklyMap.put("StaffReferenceInternalController.fetchStaffByCcdServiceNames",
                CWD_FETCH_STAFF_BY_CCD_SERVICE_NAMES);
        launchDarklyMap.put("StaffRefDataController.searchStaffUserByName",
                SRD_RD_STAFF_UI);
        launchDarklyMap.put("StaffRefDataController.searchStaffProfile",
                SRD_RD_STAFF_UI);
        launchDarklyMap.put("StaffRefDataController.retrieveAllServiceSkills",
                STAFF_REF_DATA_RD_STAFF_UI);


        launchDarklyMap.put("StaffRefDataController.fetchUserTypes",
                RD_STAFF_UI);
        launchDarklyMap.put("StaffRefDataController.createStaffUserProfile",
                RD_STAFF_UI);
        launchDarklyMap.put("StaffRefDataController.retrieveJobTitles",
                RD_STAFF_UI);

    }

    @Override
    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(userName)
                .firstName(userName)
                .custom("servicename", serviceName)
                .custom("environment", environment)
                .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    @Override
    public Map<String, String> getLaunchDarklyMap() {
        return launchDarklyMap;
    }
}




