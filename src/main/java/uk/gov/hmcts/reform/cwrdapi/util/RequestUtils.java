package uk.gov.hmcts.reform.cwrdapi.util;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@SuppressWarnings("HideUtilityClassConstructor")
public class RequestUtils {

    private RequestUtils() {
    }

    /**
     * trims idam roles for all requests.
     * @param requests cw requests
     */
    public static void trimIdamRoles(List<CaseWorkersProfileCreationRequest> requests) {
        requests.forEach(request -> {
            Set<String> roles = request.getIdamRoles();
            if (isNotEmpty(roles)) {
                request.setIdamRoles(roles.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .collect(toSet()));
            }
        });
    }
}
