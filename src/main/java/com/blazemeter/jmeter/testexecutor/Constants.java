package com.blazemeter.jmeter.testexecutor;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/30/13
 * Time: 10:01 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Constants {
    // used by userKeyTextField for validation
    public static final String USERKEY_REGEX = "\\w{3,}+";
    //used by userKeyTextField for linking with Document object
    public static final String PARENT = "parent";
    // default value of textidComboBox
    public static final String NEW = "---NEW---";
    public static final String EMPTY = "";
    public static final String HELP_URL = "http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter#user_key";
    public static final String BLAZEMETER_TESTPANELGUI_INITIALIZED = "blazemeter.testpanelgui.initialized";
    public static final String REQUEST_FEATURE_REPORT_BUG_URL = "http://community.blazemeter.com/forums/175399-blazemeter-jmeter-plugin";
    //name of JMeter property for storing current testId, which is used by TestPanelGui
    public static final String CURRENT_TEST = "blazemeter.current_test_id";
    public static final String LOCALHOST = "localhost/127.0.0.1";
    public static final String TEST_URL_WAS_OPENED = "blazemeter.attempts_to_start_test";
    public static final String ATTEMPTS_TO_START_TEST = "blazemeter.attempts_to_start_test";
    public static final String BLAZEMETER_LABEL = "BlazeMeter";
    public static String BLAZEMETER_RUNNERGUI_INITIALIZED = "blazemeter.runnergui.initialized";
    public static final String APP_KEY = "75bad111c06f4e10c001";
    public static final String JMX_FILE_EXTENSTION = ".jmx";

    public static final String TEST_ID = "testId";
    public static final String TEST_NAME = "testName";
    public static final String TEST_NUMBER_OF_USERS = "numberOfUsers";
    public static final String TEST_LOCATION = "location";
    public static final String TEST_DURATION = "duration";
    public static final String TEST_ITERATIONS = "iterations";
    public static final String TEST_RAMP_UP = "rampUp";
    public static final String TEST_THREADS = "threads";
    public static final String JMETER_SERVER_LOG_FILENAME = "jmeter-server.log";
    public static final String DATA = "data";
    public static final String SAMPLES = "samples";
    public static final String CALLBACK_URL = "callbackurl";
    public static final String REMOTE_HOSTS = "remote_hosts";
}
