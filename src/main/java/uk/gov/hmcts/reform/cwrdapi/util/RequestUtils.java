package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_COLUMN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;

@Slf4j
@Getter
public class RequestUtils {

    private RequestUtils() {
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

    public static PageRequest validateAndBuildPaginationObject(Integer pageNumber,
                                                               Integer pageSize,
                                                               String sortColumn,
                                                               String sortDirection,
                                                               String loggingComponentName,
                                                               int configPageSize,
                                                               String configSortColumn,
                                                               Class<?> entityClass) {

        if (Objects.nonNull(pageNumber) && pageNumber < 0) {
            log.info("{}:: Invalid Page Number {}", loggingComponentName, pageNumber);
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_NUMBER));
        }
        if (Objects.nonNull(pageSize) && pageSize <= 0) {
            log.info("{}:: Invalid Page Size {}", loggingComponentName, pageSize);
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_SIZE));
        }
        if (!StringUtils.isEmpty(sortDirection)) {
            try {
                Sort.Direction.fromString(sortDirection);
            } catch (IllegalArgumentException illegalArgumentException) {
                log.info("{}:: Invalid Sort Direction {}", loggingComponentName, sortDirection);
                throw new InvalidRequestException(String.format(INVALID_FIELD, SORT_DIRECTION));
            }
        }
        String finalSortColumn = StringUtils.isBlank(sortColumn) ? configSortColumn : sortColumn;
        if (!isValidSortColumn(finalSortColumn, entityClass)) {
            log.info("{}:: Invalid Sort Column {}", loggingComponentName, finalSortColumn);
            throw new InvalidRequestException(String.format(INVALID_FIELD, SORT_COLUMN));
        }
        return PageRequest.of(Objects.isNull(pageNumber) ? 0 : pageNumber,
                Objects.isNull(pageSize) ? configPageSize : pageSize,
                StringUtils.isBlank(sortDirection) ? Sort.Direction.ASC : Sort.Direction.fromString(sortDirection),
                finalSortColumn);
    }

    private static boolean isValidSortColumn(String finalSortColumn,
                                             Class<?> entityClass) {
        Field field = ReflectionUtils.findField(entityClass, finalSortColumn);
        return Objects.nonNull(field);
    }

}
