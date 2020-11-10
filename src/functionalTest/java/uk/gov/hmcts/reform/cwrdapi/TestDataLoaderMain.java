package uk.gov.hmcts.reform.cwrdapi;

public class TestDataLoaderMain {

    private TestDataLoaderMain() {
    }

    public static void main(String[] args) {
        new CaseWorkerTestAutomationAdapter().doLoadTestData();
    }

}
