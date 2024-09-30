package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.config.TrimStringFields;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateCaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateEmail;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CASEWORKER_ID_MISSING;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_NOT_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_NOT_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NAME_REGEX;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SUSPENDED_FLAG_MANDATORY;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersProfileUpdationRequest")
@ValidateCaseWorkerProfile
public class CaseWorkersProfileUpdationRequest {


    @JsonProperty("case_worker_id")
    @JsonDeserialize(using = TrimStringFields.class)
    @NotEmpty(message = CASEWORKER_ID_MISSING)
    private String userId;

    @JsonProperty("first_name")
    @JsonDeserialize(using = TrimStringFields.class)
    @Pattern(regexp = NAME_REGEX, message = FIRST_NAME_INVALID)
    @NotEmpty(message = FIRST_NAME_NOT_PRESENT)
    private String firstName;

    @JsonProperty("last_name")
    @Pattern(regexp = NAME_REGEX, message = LAST_NAME_INVALID)
    @NotEmpty(message = LAST_NAME_NOT_PRESENT)
    @JsonDeserialize(using = TrimStringFields.class)
    private String lastName;

    @JsonProperty("email_id")
    @JsonDeserialize(using = TrimStringFields.class)
    @ValidateEmail(message = CaseWorkerConstants.INVALID_EMAIL)
    @NotEmpty(message = CaseWorkerConstants.INVALID_EMAIL)
    private String emailId;

    @JsonProperty("suspended")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = SUSPENDED_FLAG_MANDATORY)
    private Boolean suspended;

}
