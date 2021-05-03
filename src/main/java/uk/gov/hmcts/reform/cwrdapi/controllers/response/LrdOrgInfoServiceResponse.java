package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LrdOrgInfoServiceResponse {

    private Long serviceId;

    private String orgUnit;

    private String businessArea;

    private String subBusinessArea;

    private String jurisdiction;

    private String serviceDescription;

    private String serviceCode;

    private String serviceShortDescription;

    private String ccdServiceName;

    private String lastUpdate;

    private List<String> ccdCaseTypes;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LrdOrgInfoServiceResponse)) {
            return false;
        }
        LrdOrgInfoServiceResponse that = (LrdOrgInfoServiceResponse) o;
        return Objects.equals(getServiceCode(), that.getServiceCode())
                && Objects.equals(getCcdServiceName(), that.getCcdServiceName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCcdServiceName(), getServiceCode());
    }

}
