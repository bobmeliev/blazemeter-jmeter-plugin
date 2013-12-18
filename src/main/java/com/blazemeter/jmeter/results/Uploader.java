package com.blazemeter.jmeter.results;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BmLog;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 8/21/13
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Uploader implements Runnable {
    String logName;
    String data;
    String dataType;

    public Uploader(String logName, String data, String dataType) {
        this.logName = logName;
        this.data = data;
        this.dataType = dataType;
    }

    @Override
    public void run() {
        upload(this.logName, this.data, this.dataType);
    }


    private void upload(String logName, String data, String dataType) {
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        String testId = testInfo.getId();
        BmLog.debug(String.format("Log uploader sending log:%s , %d bytes  ", logName, data.length()));
        BmTestManager.getInstance().logUpload(testId, logName, data, dataType);
    }
}
