package uk.gov.hmcts.reform.cwrdapi.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder(builderMethodName = "aDeleteUserProfilesRequest")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DeleteUserProfilesRequest {

    private List<String> userIds;

    @JsonCreator
    public DeleteUserProfilesRequest(List<String> userIds) {
        this.userIds = userIds;
    }
}
