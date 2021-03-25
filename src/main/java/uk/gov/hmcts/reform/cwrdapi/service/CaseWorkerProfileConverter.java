package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CaseWorkerProfileConverter implements Converter<List<CaseWorkerDomain>,
    List<CaseWorkersProfileCreationRequest>> {

    List<Long> suspendedRowIds;

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
        suspendedRowIds = new ArrayList<>();
        caseWorkerProfiles
            .stream()
            .map(CaseWorkerProfile.class::cast)
            .forEach(obj -> {
                CaseWorkersProfileCreationRequest caseWorkersProfileCreationRequest =
                    CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest()
                        .firstName(obj.getFirstName())
                        .lastName(obj.getLastName())
                        .emailId(obj.getOfficialEmail())
                        .region(obj.getRegionName())
                        .regionId(obj.getRegionId())
                        .suspended(isSuspended(obj))
                        .userType(obj.getUserType())
                        .idamRoles(Objects.isNull(obj.getIdamRoles()) ? null :
                            generateIdamRoles(obj.getIdamRoles()))
                        .baseLocations(generateCaseWorkerLocations(obj.getLocations()))
                        .roles(generateCaseWorkerRoles(obj.getRoles()))
                        .workerWorkAreaRequests(generateCaseWorkerWorkAreaRequests(obj.getWorkAreas()))
                        .rowId(obj.getRowId())
                        .build();
                caseWorkersProfileCreationRequests.add(caseWorkersProfileCreationRequest);

                if (isSuspended(obj)) {
                    suspendedRowIds.add(obj.getRowId());
                }
            });
        return caseWorkersProfileCreationRequests;
    }

    private boolean isSuspended(CaseWorkerProfile obj) {
        return "Y".equals(obj.getSuspended());
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
        return idamRoles.contains(CaseWorkerConstants.DELIMITER_COMMA)
            ? Stream.of(idamRoles.split(CaseWorkerConstants.DELIMITER_COMMA))
            .collect(Collectors.toUnmodifiableSet())
            : Set.of(idamRoles);
    }

    public List<Long> getSuspendedRowIds() {
        return suspendedRowIds;
    }
}
