package uk.gov.hmcts.reform.cwrdapi.util;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class CaseworkerRefApiPostgresqlContainer extends PostgreSQLContainer<CaseworkerRefApiPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";

    private CaseworkerRefApiPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Container
    private static final CaseworkerRefApiPostgresqlContainer container = new CaseworkerRefApiPostgresqlContainer();

}