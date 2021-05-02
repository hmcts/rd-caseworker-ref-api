package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaginatedStaffProfile implements Serializable {
    private long totalRecords;
    private long totalPages;
    private List<StaffProfileWithServiceName> staffProfiles;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class StaffProfileWithServiceName implements Serializable {
        private String ccdServiceName;
        private CaseWorkerProfile staffProfile;
    }

}
