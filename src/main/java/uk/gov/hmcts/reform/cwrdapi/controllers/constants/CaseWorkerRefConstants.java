package uk.gov.hmcts.reform.cwrdapi.controllers.constants;

public class CaseWorkerRefConstants {

    public static final String REG_EXP_SPCL_CHAR = "^[^<>{}\"/|;:.~!?@#$%^=&*\\]\\\\()\\[¿§«»ω⊙¤°℃℉€¥£¢¡®©09+]*$";

    public static final String REG_EXP_WHITE_SPACE = "\\s";

    public static final String NUMERIC_REGEX = "[0-9]+";

    public static final String COMMA = ",";

    public static final String LOCATION_ID_START_END_WITH_COMMA =
            "Invalid location type ids: %s";

    public static final String JOB_TITLE_ID_START_END_WITH_COMMA =
            "Invalid job title ids: %s";

    public static final String USER_TYPE_ID_START_END_WITH_COMMA =
            "Invalid user type ids: %s";

    public static final String SKILL_ID_START_END_WITH_COMMA =
            "Invalid skill ids: %s";

    public static final String REG_EXP_COMMA_DILIMETER = ",(?!\\\\s)";

    public static final String SERVICE_ID_START_END_WITH_COMMA =
            "Invalid Service ids: %s";

    public static final String ALPHA_NUMERIC_WITH_SPECIAL_CHAR_REGEX = "^(?![-_.@,'&()])(?!.*[-_.@,'&()]{2})"
            + "[A-Za-z0-9_@.,'&() -]{3,}$";

    public static final String ROLE_START_END_WITH_COMMA =
            "Invalid Service ids: %s";

}
