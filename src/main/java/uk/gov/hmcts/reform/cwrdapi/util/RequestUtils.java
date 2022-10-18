package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.EmptyRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_FIRST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_LAST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SEARCH_STRING_REGEX_PATTERN;
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
                                                               int configPageSize,
                                                               String configSortColumn,
                                                               Class<?> entityClass) {

        if (Objects.nonNull(pageNumber) && pageNumber < 0) {
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_NUMBER));
        }
        if (Objects.nonNull(pageSize) && pageSize <= 0) {
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_SIZE));
        }
        if (!StringUtils.isEmpty(sortDirection)) {
            try {
                Sort.Direction.fromString(sortDirection);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new InvalidRequestException(String.format(INVALID_FIELD, SORT_DIRECTION));
            }
        }
        String finalSortColumn = StringUtils.isBlank(sortColumn) ? configSortColumn : sortColumn;
        if (!isValidSortColumn(finalSortColumn, entityClass)) {
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

    public static String removeEmptySpaces(String value) {
        if (isNotBlank(value)) {
            return value.trim().replaceAll("\\s+", " ");
        }
        return value;
    }

    public static void validateSearchString(String searchString) {
        if (isNotEmpty(searchString)) {
            if (searchString.length() < 3) {
                throw new InvalidRequestException("The search string should contain at least 3 characters.");
            }
            if (!searchString.matches(SEARCH_STRING_REGEX_PATTERN)) {
                throw new InvalidRequestException("Invalid search string. Please input a valid string.");
            }
        } else {
            throw new InvalidRequestException("Empty search string. Please enter a valid search string.");
        }
    }

    public static PageRequest validateAndBuildPagination(Integer pageSize, Integer pageNumber,
                                                         int configPageSize, int configPageNumber) {
        if (Objects.nonNull(pageNumber) && pageNumber < 1) {
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_NUMBER));
        }
        if (Objects.nonNull(pageSize) && pageSize <= 0) {
            throw new InvalidRequestException(String.format(INVALID_FIELD, PAGE_SIZE));
        }

        return PageRequest.of((Objects.isNull(pageNumber) ? configPageNumber : pageNumber) - 1,
            Objects.isNull(pageSize) ? configPageSize : pageSize,
            Sort.by(Sort.DEFAULT_DIRECTION, CW_LAST_NAME, CW_FIRST_NAME));
    }

    public static void validateSearchRequest(SearchRequest searchRequest) {
        if (isEmpty(searchRequest.getJobTitle()) && isEmpty(searchRequest.getLocation()) &&
                isEmpty(searchRequest.getRole()) && isEmpty(searchRequest.getServiceCode()) &&
                isEmpty(searchRequest.getSkill()) &&
                isEmpty(searchRequest.getUserType()))
            throw new EmptyRequestException("Unexpected character");
        }
    }



