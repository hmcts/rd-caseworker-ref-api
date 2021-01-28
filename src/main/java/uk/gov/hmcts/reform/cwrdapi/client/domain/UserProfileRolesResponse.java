package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileRolesResponse {
    private RoleAdditionResponse roleAdditionResponse;
    @JsonProperty("statusUpdateResponse")
    private AttributeResponse attributeResponse;
}