package uk.gov.hmcts.reform.cwrdapi.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MappingField {

    String columnName() default "";

    Class clazz() default Object.class;
}
