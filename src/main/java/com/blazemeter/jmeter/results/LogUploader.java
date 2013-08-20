package com.blazemeter.jmeter.results;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.utils.BmLog;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 8/14/13
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogUploader implements Runnable {
    private final String logName;
    private final String data;
    private String dataType;


    public LogUploader(String logName, String data, String dataType) {

        this.logName = logName;
        this.data = data;
        this.dataType = dataType;
    }

    @Override
    public void run() {
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        String testId = testInfo.getId();
        BmLog.debug(String.format("Log uploader sending log:%s , %d bytes  ", this.logName, this.data.length()));
        BmTestManager.getInstance().logUpload(testId, this.logName, this.data, this.dataType);
    }
}
