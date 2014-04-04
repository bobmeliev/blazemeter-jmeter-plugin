package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.api.BmUrlManager;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.*;
import com.blazemeter.jmeter.results.SamplesUploader;
import com.blazemeter.jmeter.testexecutor.notifications.*;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import com.blazemeter.jmeter.utils.background.runnables.JMXUploader;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.util.JMeterUtils;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/28/12
 * Time: 14:26
 */

public class BmTestManager {
    private static final Object lock = new Object();
    private static boolean isTestRunning = false;
    private static BmTestManager instance;
    private List<IUserKeyNotification> userKeyNotificationListeners = new ArrayList<IUserKeyNotification>();
    private List<IRunModeChangedNotification> runModeChangedNotificationListeners = new ArrayList<IRunModeChangedNotification>();
    private List<IUsersChangedNotification> usersChangedNotificationListeners = new ArrayList<IUsersChangedNotification>();
    private List<IPluginUpdateNotification> pluginUpdateNotificationListeners = new ArrayList<IPluginUpdateNotification>();
    private List<ITestInfoNotification> testInfoNotificationListeners = new ArrayList<ITestInfoNotification>();
    private String propUserKey = "";
    private String userKey = "";
    private Users users;
    private volatile TestInfo testInfo;
    private BlazemeterApi rpc;
    private boolean isUserKeyValid = true;
    private boolean isLocalRunMode = true;


    private BmTestManager() {
        this.testInfo = new TestInfo();
        rpc = BlazemeterApi.getInstance();
        this.propUserKey = JMeterUtils.getPropDefault("blazemeter.user_key", "");
    }

    public static BmTestManager getInstance() {
        if (instance == null)
            synchronized (lock) {
                if (instance == null)
                    instance = new BmTestManager();
            }
        return instance;
    }

    public static boolean isTestRunning() {
        return isTestRunning;
    }

    public static void setTestRunning(boolean testRunning) {
        isTestRunning = testRunning;
    }

    public static String getServerUrl() {
        return BmUrlManager.getServerUrl();
    }

    public boolean isUserKeyValid() {
        return isUserKeyValid;
    }

    public void setUserKeyValid(boolean userKeyValid) {
        isUserKeyValid = userKeyValid;
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
            BmLog.info(error);
            return callBackUrl;
        }

        String testId = testInfo.getId();
        if (testId == null || testId.trim().isEmpty()) {
            callBackUrl = "";
            error = "Local(Reporting only) test was not started: testID is empty";
            testInfo.setError(error);
            NotifyTestInfoChanged();
            BmLog.error(callBackUrl);
            BmLog.info(callBackUrl);
            return callBackUrl;
        }

        if (Utils.isTestPlanEmpty()) {
            callBackUrl = "";
            error = "Cannot start test: test-plan is empty";
            testInfo.setError(error);
            NotifyTestInfoChanged();
            BmLog.error(callBackUrl);
            BmLog.info(callBackUrl);
            return callBackUrl;
        }

        BmLog.info("Start test " + testInfo);

        if (testInfo.getStatus() != TestStatus.Running) {
            if (testInfo.getId().isEmpty()) {
                String projectName = Utils.getProjectName();
                if (projectName == null) {
                    BmLog.debug("Test is running in non-gui mode!");
                    projectName = "non-gui-test";
                }
                projectName = projectName + new SimpleDateFormat(" dd/MM/yyyy - HH:mm").format(new Date());
                BmLog.info("Starting local test...");
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
                Properties properties = this.getTestInfo().getJmeterProperties();
                if (properties != null) {
                    for (Map.Entry<Object, Object> p : properties.entrySet()) {
                        JMeterUtils.setProperty((String) p.getKey(), (String) p.getValue());
                    }
                }
                JMXUploader.uploadJMX();
                HashMap<String, String> res = rpc.startTestLocal(userKey, testInfo.getId());
                if (res.containsKey("error")) {
                    callBackUrl = "";
                    error = res.get("error");
                    testInfo.setError(error);
                    NotifyTestInfoChanged();
                    BmLog.error(error);
                    BmLog.info(error);
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
        BmLog.info("Finishing test...");
        testInfo.setStatus(TestStatus.NotRunning);
        NotifyTestInfoChanged();
        SamplesUploader.stop(false);
        rpc.stopTest(userKey, testInfo.getId());
    }

    public synchronized TestInfo getTestInfo() {
        return testInfo;
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

    public String getUserKey() {
        return isUserKeyFromProp() ? propUserKey : userKey;
    }

    public void setUserKey(String userKey) {
        if (this.userKey == null | !this.userKey.equals(userKey)) {
            this.userKey = userKey;
            NotifyUserKeyChanged();
        }
    }

    public String getTestUrl() {
        String url = null;
        if (testInfo != null && testInfo.isValid()) {
            url = BmUrlManager.getServerUrl() + "/node/" + testInfo.getId();
            if (this.testInfo.getStatus() == TestStatus.Running) {
                url += "/gjtl";
            }
        }
        return url;
    }

    public TestInfo createTest(String userKey, String testName) {
        return rpc.createTest(userKey, testName);
    }

    public TestInfo updateTestSettings(String userKey, TestInfo testInfo) {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(this.users, testInfo.getNumberOfUsers());
        Overrides overrides = testInfo.getOverrides();
//        int engines = enginesParameters.getConsoles() + enginesParameters.getEngines();
        int engines = enginesParameters.getEngines();
        TestInfo ti = rpc.updateTestSettings(userKey,
                testInfo.getId(),
                testInfo.getLocation(),
                engines,
                enginesParameters.getEngineSize(),
                enginesParameters.getUserPerEngine(),
                overrides == null ? 0 : overrides.getIterations(),
                overrides == null ? 0 : overrides.getRampup(),
                overrides == null ? 0 : overrides.getDuration(),
                testInfo.getJmeterProperties()
        );
        return ti;
    }

    public void logUpload(String testId, String reportName, String buff, String dataType) {
        rpc.logUpload(getUserKey(), testId, reportName, buff, dataType);
    }

    public void samplesUpload(List<JSONObject> samples, String callBackUrl) {
        rpc.samplesUpload(samples, callBackUrl);
    }

    public void runInTheCloud() {
        TestInfo testInfo = this.getTestInfo();
        if (testInfo == null) {
            BmLog.error("TestInfo is null, test won't be started");
            return;
        }
        BmLog.info("Starting test " + testInfo.getId() + "-" + testInfo.getName());
        TestInfoController.stop();
        testInfo = rpc.runInTheCloud(this.getUserKey(), testInfo.getId());
        setTestInfo(testInfo);
    }

    public void stopInTheCloud() {
        TestInfo ti = this.getTestInfo();
        BmLog.info("Finishing test " + ti.getId() + "-" + ti.getName());
        ti = rpc.stopInTheCloud(this.getUserKey(), this.getTestInfo());
        setTestInfo(ti);
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

    public Users getUsers(boolean force) {
        String userKey = this.getUserKey();
        if (force & !userKey.isEmpty()) {
            BmLog.info("Getting users information...");
            users = rpc.getUsers(this.userKey);
            NotifyUsersChanged(users);
        }

        if (users == null) {
            this.isUserKeyValid = false;
        } else {
            this.isUserKeyValid = true;
        }
        return users;
    }

    public Users getUsers() {
        return getUsers(false);
    }

    public void setUsers(Users users) {
        this.users = users;
    }


    public List<ITestInfoNotification> getTestInfoNotificationListeners() {
        return testInfoNotificationListeners;
    }

    public List<IUserKeyNotification> getUserKeyNotificationListeners() {
        return userKeyNotificationListeners;
    }

    public List<IRunModeChangedNotification> getRunModeChangedNotificationListeners() {
        return runModeChangedNotificationListeners;
    }

    public List<IUsersChangedNotification> getUsersChangedNotificationListeners() {
        return usersChangedNotificationListeners;
    }

    public List<IPluginUpdateNotification> getPluginUpdateNotificationListeners() {
        return pluginUpdateNotificationListeners;
    }

    public void NotifyUserKeyChanged() {
        for (IUserKeyNotification ti : userKeyNotificationListeners) {
            ti.onTestUserKeyChanged(userKey);
        }

    }

    public void NotifyTestInfoChanged() {
        for (ITestInfoNotification ti : testInfoNotificationListeners) {
            ti.onTestInfoChanged(testInfo);
        }
    }

    public void NotifyPluginUpdateReceived(PluginUpdate update) {
        for (IPluginUpdateNotification ti : pluginUpdateNotificationListeners) {
            ti.onPluginUpdate(update);
        }
    }

    public void NotifyRunModeChanged(boolean isLocalRunMode) {
        for (IRunModeChangedNotification rmc : runModeChangedNotificationListeners) {
            rmc.onRunModeChanged(isLocalRunMode);
        }
    }

    public void NotifyUsersChanged(Users users) {
        for (IUsersChangedNotification uc : usersChangedNotificationListeners) {
            uc.onUsersChanged(users);
        }
    }

    public void getTestsAsync(String userKey, ITestListReceivedNotification notification) {
        rpc.getTests(userKey, notification);
    }
}
