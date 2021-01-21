package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.cwrdapi.CaseWorkerRefApiApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CaseWorkerRefApiApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class SpringBootIntegrationTest {

    @LocalServerPort
    protected int port;

}
