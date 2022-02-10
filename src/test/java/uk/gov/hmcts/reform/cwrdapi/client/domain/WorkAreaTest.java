package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkAreaTest {

    @Test
    void testWorkArea() {
        WorkArea workArea = new WorkArea();
        workArea.setServiceCode("service code");
        workArea.setAreaOfWork("area of work");
        workArea.setCreatedTime(LocalDateTime.now());
        workArea.setLastUpdatedTime(LocalDateTime.now());

        assertNotNull(workArea);
        assertThat(workArea.getServiceCode(), is("service code"));
        assertThat(workArea.getAreaOfWork(), is("area of work"));
        assertNotNull(workArea.getLastUpdatedTime());
        assertNotNull(workArea.getCreatedTime());
    }

    @Test
    void testWorkAreaBuilder() {
        WorkArea workArea = WorkArea.builder()
                .serviceCode("service code")
                .areaOfWork("area of work")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();

        assertNotNull(workArea);
        assertThat(workArea.getServiceCode(), is("service code"));
        assertThat(workArea.getAreaOfWork(), is("area of work"));
        assertNotNull(workArea.getLastUpdatedTime());
        assertNotNull(workArea.getCreatedTime());

        String workAreaString = WorkArea.builder().serviceCode("1").toString();
        assertTrue(workAreaString.contains("WorkArea.WorkAreaBuilder(serviceCode=1"));
    }
}
