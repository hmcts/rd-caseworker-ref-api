package uk.gov.hmcts.reform.cwrdapi.util;

public @interface MappingField {

    String columnName() default "";

    Class clazz() default Object.class;
}
