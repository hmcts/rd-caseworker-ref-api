package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSkill {
    private String id;
    @JsonProperty("skills")
    private List<SkillDTO> skills;
}
