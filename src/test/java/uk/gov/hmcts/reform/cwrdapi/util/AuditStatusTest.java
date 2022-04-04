package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditStatusTest {

    @Test
    void test_AuditStatus() {
        AuditStatus auditStatus = AuditStatus.IN_PROGRESS;
        assertThat(auditStatus.getStatus()).isEqualTo("InProgress");
    }
}
