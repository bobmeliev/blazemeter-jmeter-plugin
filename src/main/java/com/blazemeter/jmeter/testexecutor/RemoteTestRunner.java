package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.controllers.testinfocontroller.TestInfoController;
import com.blazemeter.jmeter.entities.Overrides;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.results.LogUploader;
import com.blazemeter.jmeter.results.SamplesUploader;
import com.blazemeter.jmeter.testexecutor.notifications.IRunModeChangedNotification;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.notifications.ITestUserKeyNotification;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Constants;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.ShutdownClient;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;


//~--- JDK imports ------------------------------------------------------------
public class RemoteTestRunner extends ResultCollector implements SampleListener, RemoteSampleListener, Remoteable, Serializable, TestListener, ActionListener {


    public RemoteTestRunner() {
        super();
        Utils.checkJMeterVersion();

        ServerStatusController serverStatusController = ServerStatusController.getServerStatusController();
        serverStatusController.start();

        BmTestManager.getInstance().testUserKeyNotificationListeners.add(new ITestUserKeyNotification() {
            @Override
            public void onTestUserKeyChanged(String userKey) {
                setUserKey(userKey);
            }
        });

        BmTestManager.getInstance().testInfoNotificationListeners.add(new ITestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                setTestInfo(testInfo);
                if ((testInfo.getStatus() == TestStatus.NotRunning) & JMeter.isNonGUI() & BmTestManager.isTestRunning()) {
                    if (Utils.isJMeterServer()) {
                        try {
                            ShutdownClient.main(new String[]{"StopTestNow"});

                        } catch (IOException ioe) {
                            BmLog.error("Distributed remote test was not stopped: " + ioe);
                        }
                    } else {
                        ServerStatusController.getServerStatusController().stop();
                        TestInfoController.stop();
                        testEnded();
                    }
                }
            }
        });

        BmTestManager.getInstance().runModeChangedNotificationListeners.add(new IRunModeChangedNotification() {
            @Override
            public void onRunModeChanged(boolean isLocalRunMode) {
                setIsLocalRunMode(isLocalRunMode);
            }
        });
        JMeterUtils.setProperty(Constants.ATTEMPTS_TO_START_TEST, "0");
    }

    public void setTestInfo(TestInfo testInfo) {
        if (testInfo == null)
            testInfo = new TestInfo();

        BmLog.debug("Setting testInfo " + testInfo.toString() + " to RemoteTestRunner");
        try {
            this.setProperty(Constants.TEST_NAME, testInfo.getName());
            this.setProperty(Constants.TEST_ID, testInfo.getId());
            this.setProperty(Constants.TEST_NUMBER_OF_USERS, testInfo.getNumberOfUsers());
            if (!BmTestManager.getInstance().getIsLocalRunMode()) {
                String location = testInfo.getLocation();
                if (location != null) {
                    this.setProperty(Constants.TEST_LOCATION, testInfo.getLocation());
                }

                Overrides overrides = testInfo.getOverrides();
                if (overrides != null) {
                    overrides.setThreads(testInfo.getNumberOfUsers());
                    this.setProperty(Constants.TEST_DURATION, overrides.getDuration());
                    this.setProperty(Constants.TEST_ITERATIONS, overrides.getIterations());
                    this.setProperty(Constants.TEST_RAMP_UP, overrides.getRampup());
                    this.setProperty(Constants.TEST_THREADS, overrides.getThreads());
                }
            }
        } catch (NullPointerException npe) {
            BmLog.error("Failed to set " + testInfo.toString() + " to RemoteTestRunner");
        }

    }

    public TestInfo getTestInfo() {
        TestInfo testInfo = new TestInfo();
        testInfo.setId(this.getPropertyAsString(Constants.TEST_ID, ""));
        testInfo.setName(this.getPropertyAsString(Constants.TEST_NAME, ""));
        testInfo.setNumberOfUsers(this.getPropertyAsInt(Constants.TEST_NUMBER_OF_USERS, 0));
        testInfo.setLocation(this.getPropertyAsString(Constants.TEST_LOCATION, "EU West (Ireland)"));
        return testInfo;
    }

    public String getUserKey() {
        return this.getPropertyAsString("userKey", "");
    }

    public void setUserKey(String userKey) {
        this.setProperty("userKey", userKey);
    }

    public void setIsLocalRunMode(boolean isLocalRun) {
        this.setProperty("isLocalRun", isLocalRun);
    }

    public boolean getIsLocalRunMode() {
        return this.getPropertyAsBoolean("isLocalRun", false);
    }


    public void setReportName(String reportName) {
        this.setProperty("reportName", reportName);
    }

    @Override
    public void testStarted(String host) {
        final String callBackUrl;
        BmTestManager bmTestManager = BmTestManager.getInstance();

        if (JMeter.isNonGUI()) {
            bmTestManager.setIsLocalRunMode(true);
            TestInfo testInfo = this.getTestInfo();
            bmTestManager.setTestInfo(testInfo);
            bmTestManager.setUserKey(this.getUserKey());
            TestInfoController.start(testInfo.getId());
        }
        String userKey = bmTestManager.getUserKey();
        if (userKey == null || userKey.isEmpty()) {
            BmLog.error("UserKey is not found, test results won't be uploaded to server");
            return;
        }
        bmTestManager.setUserKeyValid(bmTestManager.getUserInfo() != null);
        if (!bmTestManager.isUserKeyValid()) {
            BmLog.error("UserKey is invalid, test will be started without uploading results");
            return;
        }
        if ((bmTestManager.getIsLocalRunMode() & (JMeterUtils.getProperty(Constants.ATTEMPTS_TO_START_TEST).equals("0")))) {

            callBackUrl = bmTestManager.startLocalTest();

            if (callBackUrl != null && callBackUrl.isEmpty()) {
                JMeterUtils.setProperty(Constants.ATTEMPTS_TO_START_TEST, "1");
                return;
            }

            BmLog.console("Test is started at " + host);


            BmTestManager.setTestRunning(true);
            JMeterUtils.setProperty(Constants.ATTEMPTS_TO_START_TEST, "0");

            if (!JMeterUtils.getPropDefault(Constants.TEST_URL_WAS_OPENED, false)) {
                String url = bmTestManager.getTestUrl();
                Utils.Navigate(url);
                BmLog.debug("Opening test URL: " + url);
                JMeterUtils.setProperty(Constants.TEST_URL_WAS_OPENED, "true");
            }
            LogUploader.getInstance().startListening();
            SamplesUploader.startUploading(callBackUrl);
        }
    }


    @Override
    public void testEnded(String host) {
        if (BmTestManager.isTestRunning()) {
            JMeterUtils.setProperty(Constants.TEST_URL_WAS_OPENED, "false");
            BmTestManager bmTestManager = BmTestManager.getInstance();
            BmTestManager.setTestRunning(false);
            BmLog.console("Test is ended at " + host);
            StandardJMeterEngine.stopEngine();
            if (JMeter.isNonGUI()) {
                System.exit(0);
            } else {
                SamplesUploader.stop();
            }
            LogUploader.getInstance().stopListening();
            bmTestManager.stopTest();
        }
    }

    @Override
    public void testIterationStart(LoopIterationEvent loopIterationEvent) {
    }

    @Override
    public void processBatch(List<SampleEvent> sampleEvents) throws RemoteException {
        StringBuilder b = new StringBuilder();
        for (SampleEvent se : sampleEvents) {
            JSONObject sample = Utils.getJSONObject(se);
            SamplesUploader.addSample(sample);
        }
    }

    @Override
    public synchronized void sampleOccurred(SampleEvent sampleEvent) {
        if (BmTestManager.isTestRunning()) {
            JSONObject sample = Utils.getJSONObject(sampleEvent);
            SamplesUploader.addSample(sample);
        } else {
            BmLog.debug("Sample will not be uploaded: test was not started on server or test is running in the cloud.");
        }
    }


    @Override
    public void sampleStarted(SampleEvent se) {
        BmLog.debug("SAMPLE started " + se.toString());
    }

    @Override
    public void sampleStopped(SampleEvent se) {
        BmLog.debug("SAMPLE stopped " + se.toString());
    }


    @Override
    public void testStarted() {
        testStarted(Constants.LOCALHOST);
    }

    @Override
    public void testEnded() {
        testEnded(Constants.LOCALHOST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
