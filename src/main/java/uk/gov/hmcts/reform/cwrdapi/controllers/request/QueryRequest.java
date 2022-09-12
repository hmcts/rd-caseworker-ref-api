package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class QueryRequest implements Serializable {

    List<String> actorId;
    List<String> roleName;

    @JsonCreator
    public QueryRequest(final List<String> actorId,
                        final List<String> roleName) {
        this.actorId = actorId;
        this.roleName = roleName;
    }
}
