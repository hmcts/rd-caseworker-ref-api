package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    private static final long serialVersionUID = 2020L;

    @JsonProperty("role_id")
    private String roleId;

    @MappingField(columnName = "Primary Role,Secondary Role", isPrimary = "Primary Role")
    @JsonProperty("role")
    private String roleName;

    @JsonProperty("is_primary")
    private boolean isPrimary;

    @JsonProperty("created_time")
    private LocalDateTime createdTime;

    @JsonProperty("last_updated_time")
    private LocalDateTime lastUpdateTime;
}
