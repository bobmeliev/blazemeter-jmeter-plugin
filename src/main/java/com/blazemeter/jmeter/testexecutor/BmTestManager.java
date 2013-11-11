package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.results.SamplesUploader;
import com.blazemeter.jmeter.testinfo.*;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.PluginUpdate;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/28/12
 * Time: 14:26
 */

public class BmTestManager {
    private String propUserKey = "";
    private String userKey = "";

    private long lastUpdateCheck = 0;
    private UserInfo userInfo;
    private volatile TestInfo testInfo;
    private BlazemeterApi rpc;
    private boolean isUserKeyValid = false;
    private boolean isLocalRunMode = false;

    private static boolean isTestRunning = false;
    private static BmTestManager instance;
    private static final Object lock = new Object();


    public static BmTestManager getInstance() {
        if (instance == null)
            synchronized (lock) {
                if (instance == null)
                    instance = new BmTestManager();
            }
        return instance;
    }

    public boolean isUserKeyValid() {
        return isUserKeyValid;
    }

    public void setUserKeyValid(boolean userKeyValid) {
        isUserKeyValid = userKeyValid;
    }

    public static boolean isTestRunning() {
        return isTestRunning;
    }

    public static void setTestRunning(boolean testRunning) {
        isTestRunning = testRunning;
    }

    private BmTestManager() {
        this.testInfo = new TestInfo();
        rpc = BlazemeterApi.getInstance();
        this.propUserKey = JMeterUtils.getPropDefault("blazemeter.user_key", "");
        this.testUserKeyNotificationListeners.add(new TestUserKeyNotification() {
            @Override
            public void onTestUserKeyChanged(String userKey) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getUserInfo(true);
                    }
                }).start();
            }
        });
    }

    public boolean isUserKeyFromProp() {
        return userKey.isEmpty() ? this.propUserKey != null && !this.propUserKey.isEmpty() : false;
    }


    public String startLocalTest() {
        /*
         callBackUrl should contain URL for uploading test results to server.
         Other information(e.g. errors) should be processed via testInfo
          */
        String callBackUrl = null;
        final String error;
        TestInfo testInfo = getTestInfo();
        String userKey = getUserKey();

        if (userKey == null || userKey.trim().isEmpty()) {
            callBackUrl = "";
            error = "Local(Reporting only) test was not started: userKey is empty";
            testInfo.setError(error);
            NotifyTestInfoChanged();
            BmLog.error(error);
            BmLog.console(error);
            return callBackUrl;
        }

        String testId = testInfo.getId();
        if (testId == null || testId.trim().isEmpty()) {
            callBackUrl = "";
            error = "Local(Reporting only) test was not started: testID is empty";
            testInfo.setError(error);
            NotifyTestInfoChanged();
            BmLog.error(callBackUrl);
            BmLog.console(callBackUrl);
            return callBackUrl;
        }

        if (Utils.isTestPlanEmpty()) {
            callBackUrl = "";
            error = "Cannot start test: test-plan is empty";
            testInfo.setError(error);
            NotifyTestInfoChanged();
            BmLog.error(callBackUrl);
            BmLog.console(callBackUrl);
            return callBackUrl;
        }

        BmLog.console("Start test " + testInfo);

        if (testInfo.getStatus() != TestStatus.Running) {
            if (testInfo.getId().isEmpty()) {
                String projectName = Utils.getProjectName();
                if (projectName == null) {
                    BmLog.debug("Test is running in non-gui mode!");
                    projectName = "non-gui-test";
                }
                projectName = projectName + new SimpleDateFormat(" dd/MM/yyyy - HH:mm").format(new Date());
                BmLog.console("Starting local test...");
                testInfo = BlazemeterApi.getInstance().createTest(userKey, projectName);
                if (testInfo == null) {
                    BmLog.error("TestInfo is not set! Enter userkey and select a test!", new NullPointerException());
                }
                if (testInfo.getId().isEmpty()) {
                    BmLog.error("Could not get valid id, test will start without blazemeter.");
                }
                setTestInfo(testInfo);
            }
            try {
                if (!JMeter.isNonGUI()) {
                    Utils.checkChangesInTestPlan();
                }
                uploadJmx();
                HashMap<String, String> res = rpc.startTestLocal(userKey, testInfo.getId());
                if (res.containsKey("error")) {
                    callBackUrl = "";
                    error = res.get("error");
                    testInfo.setError(error);
                    NotifyTestInfoChanged();
                    BmLog.error(error);
                    BmLog.console(error);
                    return callBackUrl;

                } else if (res.containsKey(Constants.CALLBACK_URL)) {
                    callBackUrl = res.get(Constants.CALLBACK_URL);
                }
                testInfo.setStatus(TestStatus.Running);
                NotifyTestInfoChanged();

            } catch (Throwable ex) {
                BmLog.error("Test was not started locally: " + ex.getMessage());
            }
        }
        return callBackUrl;
    }

    public void stopTest() {
        BmLog.console("Finishing test...");
        testInfo.setStatus(TestStatus.NotRunning);
        NotifyTestInfoChanged();
        SamplesUploader.stop();
        rpc.stopTest(userKey, testInfo.getId());
    }


    public void setTestInfo(TestInfo testInfo) {
        if (testInfo == null) {
            return;
        }

        if (this.testInfo != null && !this.testInfo.equals(testInfo)) {
            synchronized (this.testInfo) {
                this.testInfo = testInfo;
            }
        } else if (this.testInfo == null & testInfo != null) {
            synchronized (this.testInfo) {
                this.testInfo = testInfo;
            }
        }
        NotifyTestInfoChanged();

    }

    public void setUserKey(String userKey) {
        if (this.userKey == null || !this.userKey.equals(userKey)) {
            this.userKey = userKey;
            NotifyUserKeyChanged();
        }
    }


    public synchronized TestInfo getTestInfo() {
        return testInfo;
    }

    public String getUserKey() {
        return isUserKeyFromProp() ? propUserKey : userKey;
    }

    public String getTestUrl() {
        String url = null;
        if (testInfo != null && testInfo.isValid()) {
            url = BlazemeterApi.BmUrlManager.getServerUrl() + "/node/" + testInfo.getId();
            if (this.testInfo.getStatus() == TestStatus.Running) {
                url += "/gjtl";
            }
        }
        return url;
    }

    public TestInfo createTest(String userKey, String testName) {
        return
                BlazemeterApi.getInstance().createTest(userKey, testName);
    }

    public TestInfo updateTestSettings(String userKey, TestInfo testInfo) {
        ArrayList<String> enginesParameters = Utils.calculateEnginesForTest(testInfo.getNumberOfUsers());
        Overrides overrides = testInfo.getOverrides();
        TestInfo ti = BlazemeterApi.getInstance().updateTestSettings(userKey,
                testInfo.getId(),
                testInfo.getLocation(),
                Integer.parseInt(enginesParameters.get(0)),
                enginesParameters.get(1),
                Integer.parseInt(enginesParameters.get(2)),
                overrides == null ? 0 : overrides.getIterations(),
                overrides == null ? 0 : overrides.getRampup(),
                overrides == null ? 0 : overrides.getDuration()
        );
        return ti;
    }

    public void logUpload(String testId, String reportName, String buff, String dataType) {
        BlazemeterApi.getInstance().logUpload(getUserKey(), testId, reportName, buff, dataType);
    }

    public void samplesUpload(List<JSONObject> samples, String callBackUrl) {
        BlazemeterApi.getInstance().samplesUpload(samples, callBackUrl);
    }

    public void uploadJmx() {
        Thread jmxUploader = new Thread(new jmxUploader());
        jmxUploader.start();
        try {
            jmxUploader.join();
        } catch (InterruptedException ie) {
            BmLog.debug("JMX Uploader was interrupted");
        }
    }


    public void runInTheCloud() {
        TestInfo testInfo = this.getTestInfo();
        if (testInfo == null) {
            BmLog.error("TestInfo is null, test won't be started");
            return;
        }
        BmLog.console("Starting test " + testInfo.getId() + "-" + testInfo.getName());
        TestInfoController.stop();
        testInfo = rpc.runInTheCloud(this.getUserKey(), testInfo.getId());
        setTestInfo(testInfo);
    }

    public void stopInTheCloud() {
        TestInfo ti = this.getTestInfo();
        BmLog.console("Finishing test " + ti.getId() + "-" + ti.getName());
        int stopSuccess = rpc.stopInTheCloud(this.getUserKey(), this.getTestInfo().getId());
        testInfo = rpc.getTestRunStatus(this.getUserKey(), this.getTestInfo().getId(), false);
        if (testInfo.getStatus() == TestStatus.NotRunning && stopSuccess != -1) {
            NotifyTestInfoChanged();
        }
    }

    public boolean getIsLocalRunMode() {
        return isLocalRunMode;
    }

    public void setIsLocalRunMode(Boolean localRunMode) {
        if (this.isLocalRunMode != localRunMode) {
            this.isLocalRunMode = localRunMode;
            NotifyRunModeChanged(localRunMode);
        }
    }


    public UserInfo getUserInfo() {
        return getUserInfo(false);
    }

    public UserInfo getUserInfo(boolean force) {
        String userKey = this.getUserKey();
        if ((force & !userKey.isEmpty()) || userInfo == null || userInfo.getTime() + 3600000 < new Date().getTime()) {
            BmLog.console("Getting user information...");
            userInfo = BlazemeterApi.getInstance().getUserInfo(this.getUserKey());
            NotifyUserInfoChanged(userInfo);
        }
        return userInfo;
    }

    class jmxUploader implements Runnable {

        @Override
        public void run() {
            FileServer fileServer = FileServer.getFileServer();
            String projectPath = null;
            if (fileServer.getScriptName() != null) {
                projectPath = fileServer.getBaseDir() + "/" + fileServer.getScriptName();
            } else if (!JMeter.isNonGUI()) {
                projectPath = GuiPackage.getInstance().getTestPlanFile();
            }
            try {
                String filename = new File(projectPath).getName();
                BlazemeterApi.getInstance().uploadJmx(getUserKey(), testInfo.getId(), filename, projectPath);
            } catch (NullPointerException npe) {
                BmLog.error("JMX was not uploaded to server: test-plan is needed to be saved first ");
            } catch (Exception ex) {
                BmLog.error(ex);

            }
        }
    }

    public interface TestUserKeyNotification {
        public void onTestUserKeyChanged(String userKey);
    }


    List<TestUserKeyNotification> testUserKeyNotificationListeners = new ArrayList<TestUserKeyNotification>();

    public void NotifyUserKeyChanged() {
        for (TestUserKeyNotification ti : testUserKeyNotificationListeners) {
            ti.onTestUserKeyChanged(userKey);
        }

    }

    public interface TestInfoNotification {
        public void onTestInfoChanged(TestInfo testInfo);
    }


    List<TestInfoNotification> testInfoNotificationListeners = new ArrayList<TestInfoNotification>();

    public void NotifyTestInfoChanged() {
        for (TestInfoNotification ti : testInfoNotificationListeners) {
            ti.onTestInfoChanged(testInfo);
        }
    }


    public interface PluginUpdateReceived {
        public void onPluginUpdateReceived(PluginUpdate update);
    }

    public List<PluginUpdateReceived> pluginUpdateReceivedNotificationListeners = new ArrayList<PluginUpdateReceived>();

    public void NotifyPluginUpdateReceived(PluginUpdate update) {
        for (PluginUpdateReceived ti : pluginUpdateReceivedNotificationListeners) {
            ti.onPluginUpdateReceived(update);
        }
    }


    public interface RunModeChanged {
        public void onRunModeChanged(boolean isLocalRunMode);
    }

    public List<RunModeChanged> runModeChangedNotificationListeners = new ArrayList<RunModeChanged>();

    public void NotifyRunModeChanged(boolean isLocalRunMode) {
        for (RunModeChanged rmc : runModeChangedNotificationListeners) {
            rmc.onRunModeChanged(isLocalRunMode);
        }
    }

    public interface UserInfoChanged {
        public void onUserInfoChanged(UserInfo userInfo);
    }

    public List<UserInfoChanged> userInfoChangedNotificationListeners = new ArrayList<UserInfoChanged>();

    public void NotifyUserInfoChanged(UserInfo userInfo) {
        for (UserInfoChanged uic : userInfoChangedNotificationListeners) {
            uic.onUserInfoChanged(userInfo);
        }
    }

    public void checkForUpdates() {
        long now = new Date().getTime();
        if (lastUpdateCheck + 3600000 > now) {
            return;
        }

        lastUpdateCheck = now;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PluginUpdate update = BlazemeterApi.getInstance().getUpdate(BmTestManager.getInstance().getUserKey());
                if (update != null && update.getVersion().isNewerThan(Utils.getPluginVersion())) {
                    BmLog.console(String.format("Update found from %s to %s", Utils.getPluginVersion().toString(true), update.getVersion().toString(true)));
                    NotifyPluginUpdateReceived(update);
                } else {
                    BmLog.console("No update found");
                }
            }
        }).start();
    }

    /*
    Wrapper-method, which provides server URL.
    Incapsulates BlazemeterAPI from TestPanelGui
       @return String

     */
    public static String getServerUrl() {
        return BlazemeterApi.BmUrlManager.getServerUrl();
    }
}
