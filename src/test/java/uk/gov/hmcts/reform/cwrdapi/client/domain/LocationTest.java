package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationTest {

    @Test
    void testLocation() {
        Location location = new Location();
        location.setLocationName("location name");
        location.setPrimary(true);
        location.setBaseLocationId(1);
        location.setCreatedTime(LocalDateTime.now());
        location.setLastUpdatedTime(LocalDateTime.now());

        assertNotNull(location);
        assertThat(location.getLocationName(), is("location name"));
        assertThat(location.isPrimary(), is(true));
        assertThat(location.getBaseLocationId(), is(1));
        assertNotNull(location.getLastUpdatedTime());
        assertNotNull(location.getCreatedTime());
    }

    @Test
    void testLocationBuilder() {
        Location location = Location.builder()
                .baseLocationId(1)
                .locationName("location name")
                .isPrimary(false)
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();

        assertNotNull(location);
        assertThat(location.getLocationName(), is("location name"));
        assertThat(location.isPrimary(), is(false));
        assertThat(location.getBaseLocationId(), is(1));
        assertNotNull(location.getLastUpdatedTime());
        assertNotNull(location.getCreatedTime());

        String locationString = Location.builder()
                .baseLocationId(1).toString();
        assertTrue(locationString.contains("Location.LocationBuilder(baseLocationId=1"));
    }
}
