package uk.gov.hmcts.reform.cwrdapi.client.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttributeResponse {
    private Integer idamStatusCode;
    private String idamMessage;
}
