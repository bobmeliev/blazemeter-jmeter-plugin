package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BlazemeterApi;
import com.blazemeter.jmeter.utils.BmLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/17/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoChecker extends Thread {
    private String testId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TestInfoChecker() {
        this.testId = null;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    @Override
    public void run() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        try {
            if (isInterrupted()) {
                throw new InterruptedException();
            }

            TestInfo testInfo = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                    this.testId, true);
            bmTestManager.setTestInfo(testInfo);

        } catch (InterruptedException e) {
            BmLog.debug("TestInfoChecker was interrupted during sleeping");
            return;
        } finally {
            if (isInterrupted()) {
                return;
            }
        }
    }
}
