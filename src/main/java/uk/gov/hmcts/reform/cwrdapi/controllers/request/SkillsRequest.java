package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(builderMethodName = "skillsRequest")
public class SkillsRequest {

    private int skillId;
    private String description;

    @JsonCreator
    public SkillsRequest(@JsonProperty("skill_id") int skillId,
                                     @JsonProperty("description") String description) {
        this.skillId = skillId;
        this.description = description;
    }
}
