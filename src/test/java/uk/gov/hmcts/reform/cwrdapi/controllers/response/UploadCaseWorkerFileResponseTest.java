package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadCaseWorkerFileResponseTest {
    @Test
    public void test_no_args_constructor() {
        UploadCaseWorkerFileResponse uploadCaseWorkerFileResponse = new UploadCaseWorkerFileResponse();
        uploadCaseWorkerFileResponse.setMessage("testMessage");
        uploadCaseWorkerFileResponse.setMessageDetails("testMessageDetails");

        assertThat(uploadCaseWorkerFileResponse.getMessage()).isEqualTo("testMessage");
        assertThat(uploadCaseWorkerFileResponse.getMessageDetails()).isEqualTo("testMessageDetails");

    }


    @Test
    public void test_all_args_constructor() {
        UploadCaseWorkerFileResponse uploadCaseWorkerFileResponse =
                new UploadCaseWorkerFileResponse("testMessage", "testMessageDetails");

        assertThat(uploadCaseWorkerFileResponse.getMessage()).isEqualTo("testMessage");
        assertThat(uploadCaseWorkerFileResponse.getMessageDetails()).isEqualTo("testMessageDetails");

    }
}