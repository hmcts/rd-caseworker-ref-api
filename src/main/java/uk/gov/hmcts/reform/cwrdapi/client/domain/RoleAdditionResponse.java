package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleAdditionResponse {

    private String idamStatusCode;
    private String idamMessage;
}
