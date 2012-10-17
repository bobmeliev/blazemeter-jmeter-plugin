package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.common.*;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Modifier;
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
    private boolean isTestStarted = false;
    private String propUserKey;
    private long lastUpdateCheck;
    private boolean isLocalRunMode = false;
    private UserInfo userInfo;
    private volatile TestInfo testInfo;

    public static BmTestManager getInstance() {
        if (instance == null)
            synchronized (lock) {
                if (instance == null)
                    instance = new BmTestManager();
            }
        return instance;
    }

    public void destroy() {
        instance = null;
    }

    private BlazemeterApi rpc;

    public static int c = 0;

    public boolean isTestRunning() {
        return this.isTestStarted;
    }

    private BmTestManager() {
        c++;
        this.testInfo = new TestInfo();
        rpc = BlazemeterApi.getInstance();
        this.propUserKey = JMeterUtils.getPropDefault("blazemeter.user_key", "");
        this.lastUpdateCheck = 1;
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

    public void startTest() {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("Start test will not run, Running in the cloud!");
            return;
        }

        TestInfo testInfo = getTestInfo();
        String userKey = getUserKey();
        BmLog.console("startTest" + testInfo);

        if (!isTestStarted) {
            if (testInfo == null || testInfo.id.isEmpty()) {
                String projectName = JMeterPluginUtils.getProjectName();
                if (projectName == null) {
                    BmLog.console("Running in nonGui mode!");
                    projectName = "untitled";
                }

                projectName = projectName + new SimpleDateFormat(" dd/MM/yyyy - HH:mm").format(new Date());
                testInfo = BlazemeterApi.getInstance().createTest(userKey, projectName);
                if (testInfo.id.isEmpty()) {
                    BmLog.console("Could not get valid id,test will start without blazemeter.");
                    return;
                }

                setTestInfo(testInfo);
            }

            try {

                rpc.startTestLocal(userKey, testInfo.id);
                testInfo.status = TestStatus.Running;
                isTestStarted = true;
                NotifyStatusChanged();
            } catch (Throwable ex) {
                BmLog.error("do Start Test", ex);
            }
        }

    }

    public void stopTest() {
        isTestStarted = false;
        testInfo.status = TestStatus.NotRunning;
        NotifyStatusChanged();
        Uploader.getInstance().Finalize();
    }

    private String userKey;

    public synchronized void  setTestInfo(TestInfo testInfo) {
        if (this.testInfo == null || !this.testInfo.equals(testInfo)) {
            this.testInfo = testInfo;
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



    public synchronized     TestInfo getTestInfo() {
        return testInfo;
    }

    public String getUserKey() {
        return isUserKeyFromProp() ? propUserKey : userKey;
    }

    public String getTestUrl() {
        String url = null;
        if (testInfo != null && testInfo.isValid()) {
            url = BlazemeterApi.BmUrlManager.getServerUrl() + "/node/" + testInfo.id;
            if (isTestStarted) {
                url += "/gjtl";
            }
        }
        return url;
    }

    public void uploadJmx() {
        new Thread(new jmxUploader()).start();
    }

    public int runInTheCloud() {
        int testId = rpc.runInTheCloud(this.getUserKey(), this.getTestInfo().id);
        this.isTestStarted = testId != -1;
        if (this.isTestStarted) {
            this.testInfo.status = TestStatus.Running;
            NotifyTestInfoChanged();
            NotifyStatusChanged();
        }
        return testId;
    }

    public void stopInTheCloud(){
         int stopSuccess=rpc.stopInTheCloud(this.getUserKey(), this.getTestInfo().id);
         testInfo = rpc.getTestRunStatus(this.getUserKey(), this.getTestInfo().id, false);
        if(testInfo.status==TestStatus.NotRunning&&stopSuccess!=-1){
            this.isTestStarted = testInfo.status==TestStatus.Running;
            NotifyTestInfoChanged();
            NotifyStatusChanged();
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
        if (force || userInfo == null || userInfo.time + 3600000 < new Date().getTime()) {
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
                BmLog.console("Cannot upload JMX,Project path is null or empty");
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

    public interface StatusChangedNotification {
        public void onTestStatusChanged();
    }

    public List<StatusChangedNotification> statusChangedNotificationListeners = new ArrayList<StatusChangedNotification>();

    public void NotifyStatusChanged() {
        for (StatusChangedNotification ti : statusChangedNotificationListeners) {
            ti.onTestStatusChanged();
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
            BmLog.console("Running in the Cloud will not register hooks!");
            return;
        }

        if (this.hooksRegistered) {
//            BmLog.console("Registering hooks, NO!");
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
