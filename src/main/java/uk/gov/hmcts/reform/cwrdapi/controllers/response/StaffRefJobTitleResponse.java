package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRefJobTitleResponse {

    @JsonProperty("job_title")
    private List<StaffRefDataJobTitle> jobTitles;
}
