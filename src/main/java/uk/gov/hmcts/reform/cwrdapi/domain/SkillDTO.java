package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"skillId", "skillCode", "description","userType"})
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
