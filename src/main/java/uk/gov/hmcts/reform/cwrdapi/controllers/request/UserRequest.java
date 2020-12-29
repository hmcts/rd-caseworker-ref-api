package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
public class UserRequest implements Serializable {
    private final List<String> userIds;
}