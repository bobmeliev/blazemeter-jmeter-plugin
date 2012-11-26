package com.blazemeter.jmeter.testinfo.writer;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testinfo.TestInfo;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 22.11.12
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoWriter {
    private static TestInfoWriter instance;

    private TestInfo testInfo;

    private TestInfoWriter() {
    }


    public static TestInfoWriter getInstance() {
        if (instance == null) {
            instance = new TestInfoWriter();
        }
        return instance;
    }

    public void saveTestInfo() {
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        //if testInfo in TestInfoWriter is null(first time call)
        if (this.testInfo == null & testInfo != null) {
            this.testInfo = testInfo;
            return;
        }
        //if testInfo in TestInfoWriter is the same as testInfo in BmTestManager or NULL - do nothing;
        if (testInfo.id == null || this.testInfo.id.equals(testInfo.id)) {
            return;
            //if testInfo in TestInfoWriter is not the same as testInfo in BmTestManager - save testInfo to file;
        }
        if (testInfo.id != null & !this.testInfo.id.equals(testInfo.id) & (this.testInfo.status != testInfo.status)) {
            Runnable testInfoWriterThread = new TestInfoWriterThread(this.testInfo);
            new Thread(testInfoWriterThread).start();
            this.testInfo = testInfo;
        }
    }
}





