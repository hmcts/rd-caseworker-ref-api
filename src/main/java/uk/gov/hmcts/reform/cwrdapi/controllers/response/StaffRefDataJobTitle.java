package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRefDataJobTitle {

    @JsonProperty("role_id")
    public Long roleId;

    @JsonProperty("role_description")
    public String roleDescription;

    public StaffRefDataJobTitle(final RoleType obj) {
        this(obj.getRoleId(), obj.getDescription());
    }
}
