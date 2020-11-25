package uk.gov.hmcts.reform.cwrdapi.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserProfileResponse {

    @JsonProperty ("userIdentifier")
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private String idamStatus;
    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;
    private AttributeResponse attributeResponse;
    private List<String> roles;
}

