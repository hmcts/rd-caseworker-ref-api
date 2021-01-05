package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserRequest implements Serializable {
    private List<String> userIds;
}