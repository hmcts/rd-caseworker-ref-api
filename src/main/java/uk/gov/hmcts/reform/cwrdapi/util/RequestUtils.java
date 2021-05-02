package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;

//@SuppressWarnings("HideUtilityClassConstructor")
@Service
@Slf4j
@Getter
public class RequestUtils {

    @Value("${loggingComponentName}")
    static String loggingComponentName;

    @Value("${refresh.pageSize}")
    static int configPageSize;


    @Value("${refresh.sortColumn}")
    static String configSortColumn;

    public RequestUtils(@Value("${loggingComponentName}") String loggingComponentName,
                        @Value("${refresh.pageSize}")
                                int configPageSize,
                        @Value("${refresh.sortColumn}")
                                String configSortColumn) {
        RequestUtils.loggingComponentName = loggingComponentName;
        RequestUtils.configPageSize = configPageSize;
        RequestUtils.configSortColumn = configSortColumn;

    }

    /**
     * trims idam roles for all requests.
     *
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

    public static PageRequest validateAndBuildPaginationObject(Integer pageNumber, Integer pageSize, String sortColumn,
                                                               String sortDirection) {

        if (pageNumber != null && pageNumber < 0) {
            log.info("{}:: Invalid Page Number", loggingComponentName);
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_NUMBER));
        }
        if (pageSize != null && pageSize <= 0) {
            log.info("{}:: Invalid Page Size", loggingComponentName);
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_SIZE));
        }
        if (!StringUtils.isEmpty(sortDirection)) {
            try {
                Sort.Direction.fromString(sortDirection);
            } catch (IllegalArgumentException illegalArgumentException) {
                log.info("{}:: Invalid Sort Direction", loggingComponentName);
                throw new InvalidRequestException(String.format(INVALID_FIELD, SORT_DIRECTION));
            }
        }
        return PageRequest.of(pageNumber == null ? 0 : pageNumber,
                pageSize == null ? configPageSize : pageSize,
                sortDirection == null ? Sort.Direction.ASC : Sort.Direction.fromString(sortDirection),
                StringUtils.isEmpty(sortColumn) ? configSortColumn : sortColumn);
    }

}
