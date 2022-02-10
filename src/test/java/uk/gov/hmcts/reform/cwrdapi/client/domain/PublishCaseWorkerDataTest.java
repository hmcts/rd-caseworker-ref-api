package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PublishCaseWorkerDataTest {

    @Test
    void testPublishCaseWorkerData() {
        List<String> userIds = new ArrayList<>();
        userIds.add("userId1");
        userIds.add("userId2");

        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        publishCaseWorkerData.setUserIds(userIds);

        assertNotNull(publishCaseWorkerData);
        assertFalse(publishCaseWorkerData.getUserIds().isEmpty());
        assertThat(publishCaseWorkerData.getUserIds().size(), is(2));
    }
}
