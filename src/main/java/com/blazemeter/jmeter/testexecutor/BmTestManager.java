package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.testinfo.UserInfo;
import com.blazemeter.jmeter.utils.*;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/28/12
 * Time: 14:26
 */

public class BmTestManager {
    private static BmTestManager instance;
    private static final Object lock = new Object();
    private String propUserKey;
    private long lastUpdateCheck = 0;
    private boolean isLocalRunMode = false;
    private UserInfo userInfo;
    private volatile TestInfo testInfo;
    private BlazemeterApi rpc;
    public static int c = 0;
    private String userKey;
    private boolean isUserKeyValid = false;
    private static boolean isTestRunning = false;


    private static ServerStatus serverStatus = ServerStatus.NOT_AVAILABLE;

    protected enum ServerStatus {AVAILABLE, NOT_AVAILABLE}

    public static BmTestManager getInstance() {
        if (instance == null)
            synchronized (lock) {
                if (instance == null)
                    instance = new BmTestManager();
            }
        return instance;
    }

    public static ServerStatus getServerStatus() {
        return serverStatus;
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

    public void destroy() {
        instance = null;
    }


    private void checkConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverURL = BlazemeterApi.BmUrlManager.getServerUrl();
                ServerStatus latestServerStatus = serverStatus;

                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setInstanceFollowRedirects(false);
                    httpConn.setRequestMethod("HEAD");
                    httpConn.setConnectTimeout(5000);
                    httpConn.connect();
                    BmLog.debug("Connection with " + serverURL + " is OK.");
                    serverStatus = ServerStatus.AVAILABLE;
                    httpConn.disconnect();
                } catch (SocketTimeoutException e) {
                    BmLog.error("Connection with " + serverURL + " was not established, server is unavailable");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (MalformedURLException e) {
                    BmLog.error("SERVER URL is invalid! Check 'blazemeter.url' in jmeter.properties");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (java.net.ConnectException e) {
                    BmLog.error(serverURL + "is down ");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (ProtocolException e) {
                    BmLog.error("HTTP Request method was not set up for checking connection");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (IOException e) {
                    BmLog.error("Connection with" + serverURL + "was not established, server is unavailable");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } finally {
                    if (!latestServerStatus.equals(serverStatus)) {
                        NotifyServerStatusChanged();
                    }
                }
            }
        }).start();
    }

    private Thread serverConnectionChecker;

    public void startCheckingConnection() {
        if (serverConnectionChecker == null) {
            serverConnectionChecker = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;

                        }
                        checkConnection();
                        try {
                            serverConnectionChecker.sleep(15000);
                        } catch (InterruptedException e) {
                            BmLog.debug("Connection checker was interrupted during sleeping");
                            return;
                        } finally {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                        }
                    }
                }
            });
            serverConnectionChecker.start();
        }
    }

    public void stopCheckingConnection() {
        if (serverConnectionChecker != null) {
            if (serverConnectionChecker.isAlive()) {
                serverConnectionChecker.interrupt();
                BmLog.debug("ServerConnectionChecking Thread is interrupted!");
            }
        }
    }

    private BmTestManager() {
        c++;
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
        return this.propUserKey != null && !this.propUserKey.isEmpty();
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

        if (testInfo.status != TestStatus.Running) {
            if (testInfo.id.isEmpty()) {
                String projectName = JMeterPluginUtils.getProjectName();
                if (projectName == null) {
                    BmLog.debug("Running in NON-GUI mode!");
                    projectName = "untitled";
                }

                projectName = projectName + new SimpleDateFormat(" dd/MM/yyyy - HH:mm").format(new Date());
                BmLog.console("Starting local test...");
                testInfo = BlazemeterApi.getInstance().createTest(userKey, projectName);
                if (testInfo == null) {
                    BmLog.error("TestInfo is not set! Enter userkey and select a test!", new NullPointerException());
                }

                if (testInfo.id.isEmpty()) {
                    BmLog.error("Could not get valid id,test will start without blazemeter.");
                }

                setTestInfo(testInfo);
            }

            try {
                startLocalTestResult = rpc.startTestLocal(userKey, testInfo.id);
                if (startLocalTestResult.equals("Test already running, please stop it first")) {
                    return startLocalTestResult;
                }
                testInfo.status = TestStatus.Running;
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
        testInfo.status = TestStatus.NotRunning;
        NotifyTestInfoChanged();
        Uploader.getInstance().Finalize();
    }


    public void setTestInfo(TestInfo testInfo) {
        if (!this.testInfo.equals(testInfo)) {
            synchronized (this.testInfo) {
                this.testInfo = testInfo;
            }
            NotifyTestInfoChanged();
        }
    }

    public void setUserKey(String userKey) {
        if (isUserKeyFromProp())
            return;

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
            url = BlazemeterApi.BmUrlManager.getServerUrl() + "/node/" + testInfo.id;
            if (this.testInfo.status == TestStatus.Running) {
                url += "/gjtl";
            }
        }
        return url;
    }

    public void uploadJmx() {
        new Thread(new jmxUploader()).start();
    }

    public int runInTheCloud() {
        TestInfo ti = this.getTestInfo();
        BmLog.console("Starting test " + ti.id + "-" + ti.name);
        int testId = rpc.runInTheCloud(this.getUserKey(), ti.id);
        this.testInfo.status = (testId != -1 ? TestStatus.Running : TestStatus.NotRunning);
        if (this.testInfo.status == TestStatus.Running) {
            NotifyTestInfoChanged();
        }
        return testId;
    }

    public void stopInTheCloud() {
        TestInfo ti = this.getTestInfo();
        BmLog.console("Finishing test " + ti.id + "-" + ti.name);
        int stopSuccess = rpc.stopInTheCloud(this.getUserKey(), this.getTestInfo().id);
        testInfo = rpc.getTestRunStatus(this.getUserKey(), this.getTestInfo().id, false);
        if (testInfo.status == TestStatus.NotRunning && stopSuccess != -1) {
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
        if (force || userInfo == null || userInfo.getTime() + 3600000 < new Date().getTime()) {
            BmLog.console("Getting user information...");
            userInfo = BlazemeterApi.getInstance().getUserInfo(this.getUserKey());
            NotifyUserInfoChanged(userInfo);
        }
        return userInfo;
    }

    class jmxUploader implements Runnable {

        @Override
        public void run() {
            String projectPath = GuiPackage.getInstance().getTestPlanFile();
            if (projectPath == null || projectPath.isEmpty()) {
                BmLog.debug("Cannot upload JMX,Project path is null or empty");
                return;
            }

            String filename = new File(projectPath).getName();
            String testName = testInfo.name;
            testName = testName.trim().isEmpty() ? filename : testName;

            //Now that  we   have  a  valid ID  check that  if exists
            try {
                if (testInfo.id.isEmpty()) {
                    testInfo = BlazemeterApi.getInstance().createTest(getUserKey(), testName);
                    setTestInfo(testInfo);
                } else {
                    TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(getUserKey(), testInfo.id, true);
                    if (ti.status == TestStatus.NotFound) {
                        testInfo = BlazemeterApi.getInstance().createTest(getUserKey(), testName);
                    }
                    setTestInfo(testInfo);
                }
                BlazemeterApi.getInstance().uploadJmx(getUserKey(), testInfo.id, filename, projectPath);
                NotifyTestInfoChanged();

            } catch (Exception ex) {
                BmLog.console(ex.getMessage());
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

    public interface ServerStatusChangedNotification {
        public void onServerStatusChanged();
    }

    public List<ServerStatusChangedNotification> serverStatusChangedNotificationListeners = new ArrayList<ServerStatusChangedNotification>();

    public void NotifyServerStatusChanged() {
        for (ServerStatusChangedNotification sscn : serverStatusChangedNotificationListeners) {
            sscn.onServerStatusChanged();
        }
    }

    //    private static ActionListener pre_exit_listener;
    private static ActionListener pre_close_listener;
    private static ActionListener post_save_listener;
    private static Class<?> c_close;
    private static Class<?> c_save;
    private static Class<?> c_exit;
    private static Class<?> c_load;
    private static Class<?> c_open;


    private boolean hooksRegistered = false;


    public void hooksRegister() {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.debug("Running in the Cloud will not register hooks!");
            return;
        }

        if (this.hooksRegistered) {
            return;
        }


        BmLog.console("Registering hooks");
        try {
            c_close = Class.forName("org.apache.jmeter.gui.action.Close");
            c_save = Class.forName("org.apache.jmeter.gui.action.Save");
            c_exit = Class.forName("org.apache.jmeter.gui.action.ExitCommand");
            c_load = Class.forName("org.apache.jmeter.gui.action.Load");
            c_open = Class.forName("org.apache.jmeter.gui.action.LoadRecentProject");
        } catch (ClassNotFoundException e) {
            BmLog.error("error while registering hooks", e);
            return;
        }

        post_save_listener = new ActionListener() {


            @Override
            public synchronized void actionPerformed(ActionEvent e) {

                try {
                    doPostSaveActions(e);
                } catch (Throwable ex) {
                    BmLog.error(ex);
                }
            }

            private synchronized void doPostSaveActions(ActionEvent e) throws Throwable {
                BmLog.console("Post save!");

                uploadJmx();
            }
        };


        pre_close_listener = new ActionListener() {


            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                BmTestManager.getInstance().hooksUnregistered();
            }

        };

//        ActionRouter.getInstance().addPreActionListener(c_save, pre_save_listener);
        ActionRouter.getInstance().addPostActionListener(c_save, post_save_listener);
        ActionRouter.getInstance().addPreActionListener(c_close, pre_close_listener);
        ActionRouter.getInstance().addPreActionListener(c_exit, pre_close_listener);
        ActionRouter.getInstance().addPreActionListener(c_open, pre_close_listener);
        ActionRouter.getInstance().addPreActionListener(c_load, pre_close_listener);
        this.hooksRegistered = true;
    }

    public void hooksUnregistered() {

        BmLog.console("De registering our  Hooks");

//       ActionRouter.getInstance().removePreActionListener(c_save, pre_save_listener);
        ActionRouter.getInstance().removePostActionListener(c_save, post_save_listener);
        ActionRouter.getInstance().removePreActionListener(c_close, pre_close_listener);
        ActionRouter.getInstance().removePreActionListener(c_exit, pre_close_listener);
        java.util.List<String> listClasses;
        Map<String, Set<Command>> commands = new HashMap<String, Set<Command>>();
        Command command;
        Iterator<String> iterClasses;
        Class<?> commandClass;
        try {
            listClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[]{Class
                    .forName("org.apache.jmeter.gui.action.Command")});
            commands = new HashMap<String, Set<Command>>(listClasses.size());
            if (listClasses.size() == 0) {
                BmLog.console("!!!!!Uh-oh, didn't find any action handlers!!!!!");
            }
            iterClasses = listClasses.iterator();
            while (iterClasses.hasNext()) {
                String strClassName = iterClasses.next();

                //BmLog.console("classname:: " + strClassName);
                commandClass = Class.forName(strClassName);
                if (!Modifier.isAbstract(commandClass.getModifiers())) {
                    command = (Command) commandClass.newInstance();
                    for (String commandName : command.getActionNames()) {
                        Set<Command> commandObjects = commands.get(commandName);
                        if (commandObjects == null) {
                            commandObjects = new HashSet<Command>();
                            commands.put(commandName, commandObjects);
                        }
                        commandObjects.add(command);
                    }

                }
            }

        } catch (Throwable ex) {
            BmLog.error(ex);
        }
        this.hooksRegistered = false;
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


}
