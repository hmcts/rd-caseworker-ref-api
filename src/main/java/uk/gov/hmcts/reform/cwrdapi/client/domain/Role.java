package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Role implements Serializable {

    private static final long serialVersionUID = 2020L;

    private String roleId;

    @MappingField(columnName = "Primary Role,Secondary Role", isPrimary = "Primary Role")
    @JsonProperty("role")
    private String roleName;

    @JsonProperty("is_primary")
    private boolean isPrimary;

    private LocalDateTime createdTime;

    private LocalDateTime lastUpdatedTime;
}
