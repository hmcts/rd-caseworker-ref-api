package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class JsrFileErrors {

    @JsonProperty("row_id")
    public String rowId;

    @JsonProperty("field_in_error")
    public String filedInError;

    @JsonProperty("error_description")
    public String errorDescription;
}
