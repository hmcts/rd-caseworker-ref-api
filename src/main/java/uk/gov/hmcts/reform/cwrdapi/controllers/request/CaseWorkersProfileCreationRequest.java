package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersProfileCreationRequest")
public class CaseWorkersProfileCreationRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String regionId;
    private String userType;
    private String region;
    private boolean deleteFlag;
    Set<String> idamRoles = new HashSet<>();
    List<CaseWorkerLocationRequest> baseLocations = new ArrayList<>();

    List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests = new ArrayList<>();

    public CaseWorkersProfileCreationRequest() {

    }


}
