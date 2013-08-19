package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.results.SamplesUploader;
import com.blazemeter.jmeter.testinfo.Overrides;
import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.testinfo.UserInfo;
import com.blazemeter.jmeter.utils.*;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.Save;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        String startLocalTestResult = "";
        if (JMeterPluginUtils.inCloudConfig()) {
            startLocalTestResult = "Test will not be started, start test in the cloud";
            BmLog.debug(startLocalTestResult);
            BmLog.error(startLocalTestResult);
            return startLocalTestResult;
        }
        TestInfo testInfo = getTestInfo();
        String userKey = getUserKey();
        BmLog.console("startTest" + testInfo);

        if (testInfo.getStatus() != TestStatus.Running) {
            if (testInfo.getId().isEmpty()) {
                String projectName = JMeterPluginUtils.getProjectName();
                if (projectName == null) {
                    BmLog.debug("Running in NON-GUI mode!");
                    projectName = "non-gui-test";
                }

                projectName = projectName + new SimpleDateFormat(" dd/MM/yyyy - HH:mm").format(new Date());
                BmLog.console("Starting local test...");
                testInfo = BlazemeterApi.getInstance().createTest(userKey, projectName);
                if (testInfo == null) {
                    BmLog.error("TestInfo is not set! Enter userkey and select a test!", new NullPointerException());
                }

                if (testInfo.getId().isEmpty()) {
                    BmLog.error("Could not get valid id,test will start without blazemeter.");
                }

                setTestInfo(testInfo);
            }

            try {
                if (Utils.isTestPlanEmpty()) {
                    startLocalTestResult = "Cannot start test: test-plan is empty";
                    JMeterUtils.reportErrorToUser(startLocalTestResult);
                    BmLog.debug(startLocalTestResult);
                    return startLocalTestResult;
                }
                if (!JMeter.isNonGUI()) {
                    checkChangesInTestPlan();
                }
                uploadJmx();
                startLocalTestResult = rpc.startTestLocal(userKey, testInfo.getId());
                if (startLocalTestResult.equals("Test already running, please stop it first")) {
                    return startLocalTestResult;
                }
                testInfo.setStatus(TestStatus.Running);
                NotifyTestInfoChanged();
                startLocalTestResult = "";

            } catch (Throwable ex) {
                BmLog.error("Test was not started locally", ex);
            }
        }
        return startLocalTestResult;
    }

    public void stopTest() {
        BmLog.console("Finishing test...");
        testInfo.setStatus(TestStatus.NotRunning);
        NotifyTestInfoChanged();
        SamplesUploader.stop();
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

    public void dataUpload(String testId, String reportName, String buff, String dataType) {
        BlazemeterApi.getInstance().dataUpload(getUserKey(), testId, reportName, buff, dataType);
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

    private void checkChangesInTestPlan() {
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    "Do you want to save changes in current test-plan?",
                    JMeterUtils.getResString("save?"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.YES_OPTION) {
                Save save = new Save();
                try {
                    save.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "save"));
                    GuiPackage.showInfoMessage("All changes are saved to " + guiPackage.getTestPlanFile(), "File is saved");
                } catch (IllegalUserActionException iuae) {
                    BmLog.error("Can not save file," + iuae);
                }
            }
        }
    }

    public void runInTheCloud() {
        TestInfo testInfo = this.getTestInfo();
        if (testInfo == null) {
            BmLog.error("TestInfo is null, test won't be started");
            return;
        }
        BmLog.console("Starting test " + testInfo.getId() + "-" + testInfo.getName());
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
                if (update != null && update.getVersion().isNewerThan(JMeterPluginUtils.getPluginVersion())) {
                    BmLog.console(String.format("Update found from %s to %s", JMeterPluginUtils.getPluginVersion().toString(true), update.getVersion().toString(true)));
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
