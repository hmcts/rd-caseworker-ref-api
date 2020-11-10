package uk.gov.hmcts.reform.cwrdapi;

import uk.gov.hmcts.befta.BeftaMain;

public class CaseWorkerBeftaMain {

    private CaseWorkerBeftaMain() {
    }

    public static void main(String[] args) {
        BeftaMain.main(args, new CaseWorkerTestAutomationAdapter());
    }
}
