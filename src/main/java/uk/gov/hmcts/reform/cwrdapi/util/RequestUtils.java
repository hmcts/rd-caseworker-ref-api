package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.EmptyRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.REG_EXP_SPCL_CHAR;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.REG_EXP_WHITE_SPACE;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.NUMERIC_REGEX;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.EXCEPTION_MSG_SPCL_CHAR;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.LOCATION_ID_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.REG_EXP_COMMA_DILIMETER;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.SKILL_ID_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.USER_TYPE_ID_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.JOB_TITLE_ID_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.SERVICE_ID_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.ALPHA_NUMERIC_WITH_SPECIAL_CHAR_REGEX;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.CaseWorkerRefConstants.ROLE_START_END_WITH_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.NUMERIC_VALUE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SEARCH_STRING_REGEX_PATTERN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_FIRST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_LAST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_COLUMN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;

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
        if (isEmpty(searchRequest.getJobTitle()) && isEmpty(searchRequest.getLocation())
                && isEmpty(searchRequest.getRole()) && isEmpty(searchRequest.getServiceCode())
                && isEmpty(searchRequest.getSkill())
                && isEmpty(searchRequest.getUserType())) {
            throw new EmptyRequestException("Unexpected character");
        }

        if (searchRequest.getJobTitle() != null) {
            checkSpecialCharacters(searchRequest.getJobTitle());
            checkNumericValues(searchRequest.getJobTitle());
            checkIfStringStartsAndEndsWithComma(searchRequest.getJobTitle(), JOB_TITLE_ID_START_END_WITH_COMMA);
        }

        if (!StringUtils.isEmpty(searchRequest.getUserType())) {
            checkSpecialCharacters(searchRequest.getUserType());
            checkNumericValues(searchRequest.getUserType());
            checkIfStringStartsAndEndsWithComma(searchRequest.getUserType(), USER_TYPE_ID_START_END_WITH_COMMA);

        }

        if (!StringUtils.isEmpty(searchRequest.getSkill())) {
            checkSpecialCharacters(searchRequest.getSkill());
            checkNumericValues(searchRequest.getSkill());
            checkIfStringStartsAndEndsWithComma(searchRequest.getSkill(), SKILL_ID_START_END_WITH_COMMA);

        }

        if (!StringUtils.isEmpty(searchRequest.getLocation())) {
            validateLocationId(searchRequest.getLocation());
        }

        if (!StringUtils.isEmpty(searchRequest.getServiceCode())) {
            validateServiceId(searchRequest.getServiceCode());
        }

        if (!StringUtils.isEmpty(searchRequest.getRole())) {
            validateRole(searchRequest.getRole());
        }

    }



    private static void checkSpecialCharacters(String inputValue) {
        inputValue = StringUtils.trim(inputValue);
        if (Pattern.compile(REG_EXP_WHITE_SPACE).matcher(inputValue).find()
                || !Pattern.compile(REG_EXP_SPCL_CHAR).matcher(inputValue).matches()) {
            throw new InvalidRequestException(EXCEPTION_MSG_SPCL_CHAR);
        }
    }

    private static void checkNumericValues(String inputValue) {
        inputValue = StringUtils.trim(inputValue);
        if (!Pattern.compile(NUMERIC_REGEX).matcher(inputValue).find()) {
            throw new InvalidRequestException(NUMERIC_VALUE_ERROR_MESSAGE);
        }
    }

    public static void validateLocationId(String locationId) {
        checkIfStringStartsAndEndsWithComma(locationId, LOCATION_ID_START_END_WITH_COMMA);
        Arrays.stream(locationId.strip().split(REG_EXP_COMMA_DILIMETER)).forEach(c -> {
            if (!isRegexSatisfied(c.trim(), NUMERIC_REGEX)) {
                throw new InvalidRequestException(String.format(LOCATION_ID_START_END_WITH_COMMA, locationId));
            }
        });
    }

    private static void checkIfStringStartsAndEndsWithComma(String csvIds, String exceptionMessage) {
        if (StringUtils.startsWith(csvIds, COMMA) || StringUtils.endsWith(csvIds, COMMA)) {
            throw new InvalidRequestException(String.format(exceptionMessage, csvIds));
        }
    }

    public static boolean isRegexSatisfied(String stringToEvaluate, String regex) {
        return Pattern.compile(regex).matcher(stringToEvaluate).matches();
    }

    public static void validateServiceId(String serviceId) {
        checkIfStringStartsAndEndsWithComma(serviceId, SERVICE_ID_START_END_WITH_COMMA);
        Arrays.stream(serviceId.strip().split(REG_EXP_COMMA_DILIMETER)).forEach(c -> {
            if (!isRegexSatisfied(c.trim(), ALPHA_NUMERIC_WITH_SPECIAL_CHAR_REGEX)) {
                throw new InvalidRequestException(String.format(SERVICE_ID_START_END_WITH_COMMA, serviceId));
            }
        });
    }

    public static void validateRole(String role) {
        checkIfStringStartsAndEndsWithComma(role, ROLE_START_END_WITH_COMMA);
        Arrays.stream(role.strip().split(REG_EXP_COMMA_DILIMETER)).forEach(c -> {
            if (!isRegexSatisfied(c.trim(), ALPHA_NUMERIC_WITH_SPECIAL_CHAR_REGEX)) {
                throw new InvalidRequestException(String.format(ROLE_START_END_WITH_COMMA, role));
            }
        });
    }



}






