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




