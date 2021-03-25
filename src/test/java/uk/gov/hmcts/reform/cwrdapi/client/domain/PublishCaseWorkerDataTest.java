package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class PublishCaseWorkerDataTest {

    @Test
    public void testPublishCaseWorkerData() {
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
