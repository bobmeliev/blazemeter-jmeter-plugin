package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.common.BmLog;
import com.blazemeter.jmeter.common.JMeterPluginUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------
public class RemoteTestRunner extends AbstractListenerElement implements SampleListener, RemoteSampleListener, Remoteable, Serializable, TestListener, ActionListener {

    private static final long serialVersionUID = 1L;
    public static final String LOCAL_TEST_STRING = "_local_";
    static boolean isTestStarted = false;
    private static int instanceCount=0;

    public RemoteTestRunner() {
        this(null);
    }

    public RemoteSampleListener listener;

    public boolean canRemove(){
        BmLog.console("can remove? " + instanceCount);
        instanceCount--;
        if(instanceCount==0){
            BmTestManager.getInstance().hooksUnregistered();
        }
        return super.canRemove();
    }
    public RemoteTestRunner(RemoteSampleListener listener) {
        super();

        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunner(),Running in the cloud!");
            return;
        }
        this.listener = listener;
//        BmLog.console("RemoteTestRunner create");

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
            }
        });
        instanceCount++;
        BmTestManager.getInstance().hooksRegister();
    }

    public TestInfo getTestInfo() {
        return new TestInfo(this.getPropertyAsString("testName", ""), this.getPropertyAsString("testId", ""));
    }

    public void setTestInfo(TestInfo testInfo) {
        if (testInfo == null)
            testInfo = new TestInfo();

        this.setProperty("testName", testInfo.name);
        this.setProperty("testId", testInfo.id);

    }

    public String getUserKey() {
        return this.getPropertyAsString("userKey", "");
    }

    public void setUserKey(String userKey) {
        this.setProperty("userKey", userKey);
    }

    public String getReportName() {
        return this.getPropertyAsString("reportName", "sample.jtl");
    }

    public void setReportName(String reportName) {
        this.setProperty("reportName", reportName);
    }

    @Override
    public void testStarted(String host) {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("testStarted ,Running in the cloud!");
            return;
        }
        BmLog.console("Test started " + host);
        isTestStarted = true;
        if (LOCAL_TEST_STRING.equals(host)) {
            BmTestManager.getInstance().startTest();
            Uploader.getInstance().SamplingStarted(getReportName());
        } else {
            if (listener != null) {
                try {
                    listener.testStarted(host);
                } catch (RemoteException e) {
                    BmLog.error( e);
                }
            }
        }
        LogFilesUploader.getInstance().startListening();
    }


    @Override
    public void testEnded(String host) {
        BmLog.console("Test End Event " + host);
        isTestStarted = false;
        LogFilesUploader.getInstance().stopListening();
        if (LOCAL_TEST_STRING.equals(host))
            BmTestManager.getInstance().stopTest();
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
        Uploader.getInstance().AddSample(getReportName(), b.toString());
    }

    @Override
    public synchronized void sampleOccurred(SampleEvent evt) {
        if (isTestStarted) {
            String templateJTL = GetJtlString(evt);
            Uploader.getInstance().AddSample(getReportName(), templateJTL);
        } else {
            BmLog.console("Got sample. not started !!!!!");
        }
    }

    private String GetJtlString(SampleEvent evt) {
        SampleResult res = evt.getResult();
        String t = Long.toString(res.getTime());
        String lt = Long.toString(res.getLatency());
        String ts = Long.toString(res.getTimeStamp());
        String s = Boolean.toString(res.isSuccessful());
        String lb = res.getSampleLabel();
        String rc = res.getResponseCode();
        String rm = res.getResponseMessage();
        String tn = res.getThreadName();
        String dt = res.getDataType();
        String by = Integer.toString(res.getBytes());
        String sc = Integer.toString(res.getSampleCount());
        String ec = Integer.toString(res.getErrorCount());
        String ng = Integer.toString(res.getGroupThreads());
        String na = Integer.toString(res.getAllThreads());
        String hn = JMeterUtils.getLocalHostFullName();
        String in = Long.toString(res.getIdleTime());
        return String.format("<httpSample t=\"%s\" lt=\"%s\" ts=\"%s\" s=\"%s\"  lb=\"%s\" rc=\"%s\" rm=\"%s\" tn=\"%s\" dt=\"%s\" by=\"%s\" sc=\"%s\" ec=\"%s\" ng=\"%s\" na=\"%s\" hn=\"%s\" in=\"%s\"/>\n", t, lt, ts, s, lb, rc, rm, tn, dt, by, sc, ec, ng, na, hn, in);
    }


    @Override
    public void sampleStarted(SampleEvent se) {
        BmLog.console("SAMPLE started " + se.toString());
    }

    @Override
    public void sampleStopped(SampleEvent se) {
        BmLog.console("SAMPLE stopped " + se.toString());
    }


    @Override
    public void testStarted() {
        testStarted(LOCAL_TEST_STRING);
    }

    @Override
    public void testEnded() {
        testEnded(LOCAL_TEST_STRING);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
