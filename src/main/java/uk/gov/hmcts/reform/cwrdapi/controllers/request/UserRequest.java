package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class UserRequest implements Serializable {
    private final List<String> userIds;
}