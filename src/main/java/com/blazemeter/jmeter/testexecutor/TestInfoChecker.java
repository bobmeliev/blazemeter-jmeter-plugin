package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.utils.BlazemeterApi;
import com.blazemeter.jmeter.utils.BmLog;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/17/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoChecker extends Thread {
    private String testId;
    // Time interval for checking testInfo on server
    private final int updateInterval = 30000;
    private static TestInfoChecker testInfoChecker = null;


    private TestInfoChecker() {
        this.testId = null;
    }

    public static TestInfoChecker getTestInfoChecker() {
        if (testInfoChecker == null) {
            testInfoChecker = new TestInfoChecker();
        }
        return testInfoChecker;
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
        while (!Thread.currentThread().isInterrupted()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            TestInfo testInfo = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                    this.testId, true);
            bmTestManager.setTestInfo(testInfo);

            try {
                Thread.sleep(this.updateInterval);
            } catch (InterruptedException e) {
                BmLog.debug("TestInfoChecker was interrupted during sleeping");
                return;
            } finally {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
    }

    public void stopChecker() {
        if (testInfoChecker != null) {
            if (testInfoChecker.isAlive()) {
                testInfoChecker.interrupt();
                testInfoChecker = null;
                System.gc();
                BmLog.debug("TestInfoChecker is interrupted!");
            }
        }
    }
}
