package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JrdUserRequest {

    @JsonProperty("ccdServiceName")
    private String ccdServiceNames;

    @JsonProperty("object_ids")
    private Set<String> objectIds;

    @JsonProperty("sidam_ids")
    private Set<String> sidamIds;
}
