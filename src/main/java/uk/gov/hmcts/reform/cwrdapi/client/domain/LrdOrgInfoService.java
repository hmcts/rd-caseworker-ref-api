//package uk.gov.hmcts.reform.cwrdapi.client.domain;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import com.fasterxml.jackson.databind.annotation.JsonNaming;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.minidev.json.annotate.JsonIgnore;
//
//import java.util.List;
//import java.util.Objects;
//
//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
//@Slf4j
//public class LrdOrgInfoService {
//
//    @JsonIgnore
//    private static final ObjectMapper objectMapper = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//
//    Long serviceId;
//
//    String orgUnit;
//
//    String businessArea;
//
//    String subBusinessArea;
//
//    String jurisdiction;
//
//    String serviceDescription;
//
//    String serviceCode;
//
//    String serviceShortDescription;
//
//    String ccdServiceName;
//
//    String lastUpdate;
//
//    List<String> ccdCaseTypes;
//
//    public static LrdOrgInfoService castToClazz(Object obj) {
//        return objectMapper.convertValue(obj, LrdOrgInfoService.class);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof LrdOrgInfoService)) return false;
//        LrdOrgInfoService that = (LrdOrgInfoService) o;
//        return Objects.equals(getServiceCode(), that.getServiceCode()) && Objects.equals(getCcdServiceName(), that.getCcdServiceName());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getCcdServiceName(), getServiceCode());
//    }
//}