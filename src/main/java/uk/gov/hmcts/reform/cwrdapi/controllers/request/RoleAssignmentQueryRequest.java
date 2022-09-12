package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Builder(builderMethodName = "roleAssignmentQueryRequest")
public class RoleAssignmentQueryRequest implements Serializable {
    List<String> actorId;
    List<String> roleName;
}
