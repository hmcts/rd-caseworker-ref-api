package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

@Component
public class CaseWorkerProfileConverter implements Converter<List<CaseWorkerDomain>,
        List<CaseWorkersProfileCreationRequest>> {
    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@cwrfunctestuser.com";
    public static final String COMMA = ",";

    /**
     * Convert the source object of type {@code CaseWorkerDomain}
     * to target type {@code CaseWorkersProfileCreationRequest}.
     *
     * @param caseWorkerProfiles the source object to convert,
     *                           which must be an instance of {@code CaseWorkerDomain} (never {@code null})
     * @return the converted object, which must be an instance of {@code CaseWorkersProfileCreationRequest}
     *              (potentially {@code null})
     * @throws IllegalArgumentException if the source cannot be converted to the desired target type
     */
    public List<CaseWorkersProfileCreationRequest> convert(List<CaseWorkerDomain> caseWorkerProfiles) {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();
        caseWorkerProfiles
                .stream()
                .map(obj -> (CaseWorkerProfile) obj)
                .forEach(obj -> {
                    CaseWorkersProfileCreationRequest caseWorkersProfileCreationRequest =
                            CaseWorkersProfileCreationRequest
                            .caseWorkersProfileCreationRequest()
                            .firstName(obj.getLastName())
                            .lastName(obj.getLastName())
                            .emailId(obj.getOfficialEmail())
                            .region(obj.getRegionName())
                            .regionId(obj.getRegionId())
                            .suspended(Boolean.parseBoolean(obj.getSuspended()))
                            .userType(obj.getUserType())
                            .idamRoles(null == obj.getIdamRoles() ? null :
                                    generateIdamRoles(obj.getIdamRoles()))
                            .baseLocations(generateCaseWorkerLocations(obj.getLocations()))
                            .roles(generateCaseWorkerRoles(obj.getRoles()))
                            .workerWorkAreaRequests(generateCaseWorkerWorkAreaRequests(obj.getWorkAreas()))
                            .build();
                    caseWorkersProfileCreationRequests.add(caseWorkersProfileCreationRequest);
                });
        return caseWorkersProfileCreationRequests;
    }

    private List<CaseWorkerWorkAreaRequest> generateCaseWorkerWorkAreaRequests(List<WorkArea> workAreas) {
        List<CaseWorkerWorkAreaRequest> caseWorkerWorkAreaRequests = new ArrayList<>();
        workAreas.forEach(w -> {
            CaseWorkerWorkAreaRequest caseWorkerWorkAreaRequest = CaseWorkerWorkAreaRequest
                    .caseWorkerWorkAreaRequest()
                    .areaOfWork(w.getAreaOfWork())
                    .serviceCode(w.getServiceCode())
                    .build();
            caseWorkerWorkAreaRequests.add(caseWorkerWorkAreaRequest);
        });
        return caseWorkerWorkAreaRequests;
    }

    private List<CaseWorkerRoleRequest> generateCaseWorkerRoles(List<Role> roles) {
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = new ArrayList<>();
        roles
            .stream()
            .filter(r -> StringUtils.isNotBlank(r.getRoleName()))
            .forEach((r -> {
                CaseWorkerRoleRequest caseWorkerRoleRequest = CaseWorkerRoleRequest
                        .caseWorkerRoleRequest()
                        .role(r.getRoleName())
                        .isPrimaryFlag(r.isPrimary())
                        .build();
                caseWorkerRoleRequests.add(caseWorkerRoleRequest);
            }));
        return caseWorkerRoleRequests;
    }

    private List<CaseWorkerLocationRequest> generateCaseWorkerLocations(List<Location> locations) {
        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = new ArrayList<>();
        locations.forEach(l -> {
            CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                    .caseWorkersLocationRequest()
                    .location(l.getLocationName())
                    .locationId(l.getBaseLocationId())
                    .isPrimaryFlag(l.isPrimary())
                    .build();
            caseWorkerLocationRequests.add(caseWorkerLocationRequest);
        });
        return caseWorkerLocationRequests;
    }

    private Set<String> generateIdamRoles(String idamRoles) {
        return idamRoles.contains(COMMA)
                ? Stream.of(idamRoles.split(COMMA))
                        .collect(Collectors.toUnmodifiableSet())
                : Set.of(idamRoles);
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }
}
