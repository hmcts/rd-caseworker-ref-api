package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void test_ErrorResponse() {

        int code = 1;
        String status = "status";
        String expectMsg = "msg";
        String expectDesc = "desc";
        String expectTs = "time";

        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorCode(code)
                .status(status)
                .errorDescription("desc")
                .errorMessage(expectMsg)
                .timeStamp("time")
                .build();

        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.getErrorCode()).isEqualTo(code);
        assertThat(errorDetails.getStatus()).isEqualTo(status);
        assertThat(errorDetails.getErrorMessage()).isEqualTo(expectMsg);
        assertThat(errorDetails.getTimeStamp()).isEqualTo(expectTs);
        assertThat(errorDetails.getErrorDescription()).isEqualTo(expectDesc);
    }

    @Test
    void test_NoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertThat(errorResponse).isNotNull();
    }

    @Test
    void test_ErrorResponseWithConstructor() {

        int code = 1;
        String status = "status";
        String expectMsg = "msg";
        String expectDesc = "desc";
        String expectTs = "time";

        ErrorResponse errorResponse = new ErrorResponse(1,"status","msg",
                "desc","time");
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorCode()).isEqualTo(code);
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectMsg);
        assertThat(errorResponse.getTimeStamp()).isEqualTo(expectTs);
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectDesc);
    }

}
