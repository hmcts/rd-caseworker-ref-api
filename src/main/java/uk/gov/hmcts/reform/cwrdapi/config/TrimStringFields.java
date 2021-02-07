package uk.gov.hmcts.reform.cwrdapi.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class TrimStringFields extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext dc) {
        JsonNode node;
        try {
            node = jp.readValueAsTree();
            if (nonNull(node) && node.isTextual() && isNotEmpty(node.asText())) {
                return node.asText().trim();
            }
        } catch (IOException e) {
            log.error("Exception while trimming request fields");
        }
        return null;
    }
}
