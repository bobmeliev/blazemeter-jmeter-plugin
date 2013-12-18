package com.blazemeter.jmeter.controllers.testinfocontroller;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/17/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoChecker implements Runnable {
    private String testId;

    TestInfoChecker(String testId) {
        this.testId = testId;
    }


    @Override
    public void run() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        TestInfo testInfo = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                this.testId, true);
        bmTestManager.setTestInfo(testInfo);
    }
}
