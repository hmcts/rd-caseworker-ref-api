package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRefDataUserType {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    public StaffRefDataUserType(final UserType obj) {
        this(obj.getUserTypeId(), obj.getDescription());
    }
}
