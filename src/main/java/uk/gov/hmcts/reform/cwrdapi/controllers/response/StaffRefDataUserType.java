package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class StaffRefDataUserType {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("code")
    public String code;

    public StaffRefDataUserType(final UserType obj) {
        this(obj.getUserTypeId(), obj.getDescription());
    }
}
