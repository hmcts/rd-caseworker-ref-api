package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"skillId", "skillCode", "description", "userType"})
@SuppressWarnings("AbbreviationAsWordInName")
public class SkillDTO {
    @JsonIgnore
    private String serviceId;
    @JsonProperty("id")
    private Long skillId;

    @JsonProperty("code")
    private String skillCode;

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("description")
    private String description;
}
