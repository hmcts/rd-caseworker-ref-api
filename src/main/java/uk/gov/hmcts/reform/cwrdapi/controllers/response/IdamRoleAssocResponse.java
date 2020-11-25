package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdamRoleAssocResponse {

    private int statusCode;
    private String message;
}
