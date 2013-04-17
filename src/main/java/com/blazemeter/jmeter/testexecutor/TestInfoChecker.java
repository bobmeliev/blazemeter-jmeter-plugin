package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.utils.BmLog;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/17/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoChecker implements Runnable {
    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
        while (!Thread.currentThread().isInterrupted()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            //check testInfo
            //   updateCloudPanel(0);
            try {
                Thread.sleep(30000);
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
}
