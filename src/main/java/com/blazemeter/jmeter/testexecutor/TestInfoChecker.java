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
    private final int testId;
    private final int updateInterval;
    private static TestInfoChecker testInfoChecker = null;

    private TestInfoChecker(int testId, int updateInterval) {
        this.testId = testId;
        this.updateInterval = updateInterval;
    }

    public TestInfoChecker getTestInfoChecker(int testId, int updateInterval) {
        if (testInfoChecker == null) {
            testInfoChecker = new TestInfoChecker(testId, updateInterval);
        }
        return testInfoChecker;
    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        BmTestManager bmTestManager = BmTestManager.getInstance();
        while (!Thread.currentThread().isInterrupted()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            TestInfo testInfo = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                    Integer.toString(this.testId), true);
            bmTestManager.setTestInfo(testInfo);

            //check testInfo
            //   updateCloudPanel(0);
            try {
                Thread.sleep(this.updateInterval);
            } catch (InterruptedException e) {
                BmLog.debug("TestStatusChecker was interrupted during sleeping");
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
                BmLog.debug("TestStatusChecker is interrupted!");
            }
        }
    }
}
