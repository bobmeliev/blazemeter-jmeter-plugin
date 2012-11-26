package com.blazemeter.jmeter.testinfo.writer;

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

    public void setTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    public TestInfo getTestInfo() {
        return testInfo;
    }

    public void saveTestInfo(TestInfo testInfo) {
        Runnable testInfoWriterThread = new TestInfoWriterThread(testInfo);
        new Thread(testInfoWriterThread).start();
        this.testInfo = testInfo;
    }
}






