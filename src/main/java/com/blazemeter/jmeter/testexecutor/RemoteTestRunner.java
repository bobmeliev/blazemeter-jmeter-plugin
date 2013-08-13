package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.testinfo.Overrides;
import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.testinfo.TestInfoController;
import com.blazemeter.jmeter.upload.JMeterLogFilesUploader;
import com.blazemeter.jmeter.upload.Uploader;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.JMeterPluginUtils;
import com.blazemeter.jmeter.utils.TestStatus;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.ShutdownClient;
import org.json.XML;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.List;


//~--- JDK imports ------------------------------------------------------------
public class RemoteTestRunner extends ResultCollector implements SampleListener, RemoteSampleListener, Remoteable, Serializable, TestListener, ActionListener {


    public RemoteTestRunner() {
        super();
        if (Float.parseFloat(JMeterPluginUtils.getJmeterVersion()) < 2.5) {
            BmLog.error("Blazemeter Listener won't work with this version of JMeter. Please, update Jmeter to 2.5 or later.");

        }
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.debug("RemoteTestRunner is running in the cloud!");
            return;
        }

        ServerStatusController serverStatusController = ServerStatusController.getServerStatusController();
        serverStatusController.start();

        BmTestManager.getInstance().testUserKeyNotificationListeners.add(new BmTestManager.TestUserKeyNotification() {
            @Override
            public void onTestUserKeyChanged(String userKey) {
                setUserKey(userKey);
            }
        });

        BmTestManager.getInstance().testInfoNotificationListeners.add(new BmTestManager.TestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                setTestInfo(testInfo);
                if ((testInfo.getStatus() == TestStatus.NotRunning) & JMeter.isNonGUI() & BmTestManager.isTestRunning()) {
                    if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {
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

        BmTestManager.getInstance().runModeChangedNotificationListeners.add(new BmTestManager.RunModeChanged() {
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

        this.setProperty(Constants.TEST_NAME, testInfo.getName());
        this.setProperty(Constants.TEST_ID, testInfo.getId());
        this.setProperty(Constants.TEST_NUMBER_OF_USERS, testInfo.getNumberOfUsers());

        Overrides overrides = testInfo.getOverrides();
        if (overrides != null) {
            overrides.setThreads(testInfo.getNumberOfUsers());
            this.setProperty(Constants.TEST_DURATION, overrides.getDuration());
            this.setProperty(Constants.TEST_ITERATIONS, overrides.getIterations());
            this.setProperty(Constants.TEST_RAMP_UP, overrides.getRampup());
            this.setProperty(Constants.TEST_THREADS, overrides.getThreads());
        }
    }

    public TestInfo getTestInfo() {
        TestInfo testInfo = new TestInfo();
        testInfo.setId(this.getPropertyAsString(Constants.TEST_ID, ""));
        testInfo.setName(this.getPropertyAsString(Constants.TEST_NAME, ""));
        testInfo.setNumberOfUsers(this.getPropertyAsInt(Constants.TEST_NUMBER_OF_USERS, 0));
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

    public String getReportName() {
        return this.getPropertyAsString("reportName", "test_results.jtl");
    }

    public void setReportName(String reportName) {
        this.setProperty("reportName", reportName);
    }

    @Override
    public void testStarted(String host) {
        BmTestManager bmTestManager = BmTestManager.getInstance();

        if (JMeter.isNonGUI()) {
            bmTestManager.setIsLocalRunMode(true);
            TestInfo testInfo = this.getTestInfo();
            bmTestManager.setTestInfo(testInfo);
            bmTestManager.setUserKey(this.getUserKey());
            TestInfoController.start(testInfo.getId());
        }
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.debug("Test is started, running in the cloud!");
            return;
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
            JMeterUtils.setProperty(Constants.START_LOCAL_TEST_RESULT, bmTestManager.startLocalTest());
            String startLocalTestResult = JMeterUtils.getProperty(Constants.START_LOCAL_TEST_RESULT);
            if (!startLocalTestResult.isEmpty()) {
                if (!JMeter.isNonGUI()) {
                    JMeterUtils.reportErrorToUser("Results can not be uploaded to server due to the following reason: "
                            + startLocalTestResult.toLowerCase(), "Unable to start uploading results");


                    JMeterUtils.setProperty(Constants.ATTEMPTS_TO_START_TEST, "1");
                    return;
                }
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
        } else {
            BmLog.debug("Test is started without uploading report to server");
            return;
        }
        Uploader.getInstance().samplingStarted(getReportName());
        JMeterLogFilesUploader.getInstance().startListening();
    }


    @Override
    public void testEnded(String host) {
        JMeterUtils.setProperty(Constants.TEST_URL_WAS_OPENED, "false");
        BmTestManager bmTestManager = BmTestManager.getInstance();
        BmTestManager.setTestRunning(false);
        BmLog.console("Test is ended at " + host);
        StandardJMeterEngine.stopEngine();
        if (JMeter.isNonGUI()) {
            System.exit(0);
        }
        bmTestManager.stopTest();
    }

    @Override
    public void testIterationStart(LoopIterationEvent loopIterationEvent) {
    }

    @Override
    public void processBatch(List<SampleEvent> sampleEvents) throws RemoteException {
        StringBuilder b = new StringBuilder();
        for (SampleEvent se : sampleEvents) {
            b.append(GetJtlString(se));
            b.append("\n");
        }
        Uploader.getInstance().addSample(getReportName(), b.toString());
    }

    @Override
    public synchronized void sampleOccurred(SampleEvent evt) {
        if (BmTestManager.isTestRunning()) {
            String templateJTL = GetJtlString(evt);
            Uploader.getInstance().addSample(getReportName(), templateJTL);
        } else {
            BmLog.debug("Sample will not be uploaded: test was not started on server or test is running in the cloud.");
        }
    }

    private String escape(String str) {
        int len = str.length();
        StringWriter writer = new StringWriter((int) (len * 0.1));
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    writer.write("&quot;");
                    break;
                case '&':
                    writer.write("&amp;");
                    break;
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                case '\'':
                    writer.write("&apos;");
                    break;
                default:
                    if (c > 0x7F) {
                        writer.write("&#");
                        writer.write(Integer.toString(c, 10));
                        writer.write(';');
                    } else {
                        writer.write(c);
                    }
            }
        }
        return writer.toString();
    }

    private String GetJtlString(SampleEvent evt) {

        SampleResult res = evt.getResult();
        String t = Long.toString(res.getTime());
        String lt = Long.toString(res.getLatency());
        String ts = Long.toString(res.getTimeStamp());
        String s = Boolean.toString(res.isSuccessful());
        String lb = escape(res.getSampleLabel());
        String rc = escape(res.getResponseCode());
        String rm = escape(res.getResponseMessage());
        String tn = escape(res.getThreadName());
        String dt = escape(res.getDataType());
        String by = Integer.toString(res.getBytes());
        String sc = Integer.toString(res.getSampleCount());
        String ec = Integer.toString(res.getErrorCount());
        String ng = Integer.toString(res.getGroupThreads());
        String na = Integer.toString(res.getAllThreads());
        String hn = XML.escape(JMeterUtils.getLocalHostFullName());
        String in = Long.toString(res.getIdleTime());

        return String.format("<httpSample t=\"%s\" lt=\"%s\" ts=\"%s\" s=\"%s\" lb=\"%s\" rc=\"%s\" rm=\"%s\" tn=\"%s\" dt=\"%s\" by=\"%s\" sc=\"%s\" ec=\"%s\" ng=\"%s\" na=\"%s\" hn=\"%s\" in=\"%s\"/>\n", t, lt, ts, s, lb, rc, rm, tn, dt, by, sc, ec, ng, na, hn, in);
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
