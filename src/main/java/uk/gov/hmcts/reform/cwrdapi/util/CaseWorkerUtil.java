package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class CaseWorkerUtil {

    private CaseWorkerUtil() {
    }

    private static String loggingComponentName;

    public static String removeEmptySpaces(String value) {
        String modValue = value;
        if (!StringUtils.isEmpty(modValue)) {
            modValue = value.trim().replaceAll("\\s+", " ");
        }
        return modValue;
    }

    public static String removeAllSpaces(String value) {
        String modValue = value;
        if (!StringUtils.isEmpty(modValue)) {
            modValue = modValue.replaceAll("\\s+", "");
        }
        return modValue;
    }

    @Value("${loggingComponentName}")
    public void setLoggingComponentName(String loggingComponentName) {
        CaseWorkerUtil.loggingComponentName = loggingComponentName;
    }

}
