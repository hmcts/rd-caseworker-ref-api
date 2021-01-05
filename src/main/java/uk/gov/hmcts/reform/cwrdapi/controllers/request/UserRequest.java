package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Getter
public class UserRequest implements Serializable {
    private List<String> userIds;
}