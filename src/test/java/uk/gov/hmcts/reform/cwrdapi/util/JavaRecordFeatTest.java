package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaRecordFeatTest {

    @Test
    void testGettersAndSetters() {
        var javaRecordFeat = new JavaRecordFeat("testId");
        assertEquals("testId", javaRecordFeat.id());
    }

    @Test
    void testTextBlock() {
        String textBlock = """
                First Text
                Second Text
                Third Text
                """;
        var javaRecordFeat = new JavaRecordFeat(textBlock);
        assertEquals(textBlock, javaRecordFeat.id());
    }
}
