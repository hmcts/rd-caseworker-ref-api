package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Data
@Builder
public class ServiceSkill {
    private String id;
    private List<Skill> skills;
}
