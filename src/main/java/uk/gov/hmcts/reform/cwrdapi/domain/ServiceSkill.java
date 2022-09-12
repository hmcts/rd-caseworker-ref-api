package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceSkill {
    private String id;
    @JsonProperty("skills")
    private List<SkillDTO> skills;
}
