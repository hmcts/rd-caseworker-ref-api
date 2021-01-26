package uk.gov.hmcts.reform.cwrdapi;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

@TestConfiguration
public class CaseWorkerProviderTestConfiguration {

    @Bean
    @Primary
    public CaseWorkerServiceImpl getCaseWorkerServiceImpl() {
        return new CaseWorkerServiceImpl();
    }

    @MockBean
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @MockBean
    private RoleTypeRepository roleTypeRepository;

    @MockBean
    private UserTypeRepository userTypeRepository;

    @MockBean
    private CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @MockBean
    private IdamRoleMappingService idamRoleMappingService;

    @MockBean
    private UserProfileFeignClient userProfileFeignClient;

    @MockBean
    private TopicPublisher topicPublisher;

    @MockBean
    private CaseWorkerLocationRepository caseWorkerLocationRepository;

    @MockBean
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @MockBean
    private CaseWorkerRoleRepository caseWorkerRoleRepository;

    @TestConfiguration
    static class Configuration {
        Connection connection;

        @Bean
        public EmbeddedPostgres embeddedPostgres() throws IOException {
            return EmbeddedPostgres
                    .builder()
                    .setPort(0)
                    .start();
        }

        @Bean
        public DataSource dataSource(@Autowired EmbeddedPostgres pg) throws Exception {

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);
            DataSource datasource = new SingleConnectionDataSource(connection, true);
            return datasource;
        }


        @PreDestroy
        public void contextDestroyed() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
