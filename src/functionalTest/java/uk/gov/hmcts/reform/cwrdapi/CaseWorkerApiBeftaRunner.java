package uk.gov.hmcts.reform.cwrdapi;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.gov.hmcts.befta.BeftaMain;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:build/cucumber.json",
        glue = "uk.gov.hmcts.befta.player",
        features = {"classpath:features"})
public class CaseWorkerApiBeftaRunner {

    private CaseWorkerApiBeftaRunner() {
    }

    @BeforeClass
    public static void setUp() {
        BeftaMain.setUp(new CaseWorkerTestAutomationAdapter());
    }

    @AfterClass
    public static void tearDown() {
        BeftaMain.tearDown();
    }

}
