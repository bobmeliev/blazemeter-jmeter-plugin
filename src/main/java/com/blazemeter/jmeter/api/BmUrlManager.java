package com.blazemeter.jmeter.api;

import com.blazemeter.jmeter.utils.BmLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 2/25/14.
 */
public class BmUrlManager {
    protected static String SERVER_URL = "https://a.blazemeter.com";
    private static BmUrlManager bmUrlManager = null;
    private static String PLUGIN_PAGE_URI = "http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter";

    protected static BmUrlManager getBmUrlManager() {
        if (bmUrlManager == null)
            bmUrlManager = new BmUrlManager();
        return bmUrlManager;

    }

    public static String getServerUrl() {
        return SERVER_URL;
    }

    public static String getPluginPage() {
        return PLUGIN_PAGE_URI;
    }

    public String testStatus(String appKey, String userKey, String testId, boolean detailed) {

        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testGetStatus/?app_key=%s&user_key=%s&test_id=%s&detailed=%s", SERVER_URL, appKey, userKey, testId, detailed);
    }

    public String scriptCreation(String appKey, String userKey, String testName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testName = URLEncoder.encode(testName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testCreate/?app_key=%s&user_key=%s&test_name=%s", SERVER_URL, appKey, userKey, testName);
    }

    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testScriptUpload/?app_key=%s&user_key=%s&test_id=%s&file_name=%s", SERVER_URL, appKey, userKey, testId, fileName);
    }

    public String scriptDownload(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testScriptDownload/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
    }

    public String testStartLocal(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testStartExternal/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
    }

    public String testStop(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testStop/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
    }

    public String logUpload(String appKey, String userKey, String testId, String fileName, String dataType) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testDataUpload/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&data_type=%s", SERVER_URL, appKey, userKey, testId, fileName, dataType);
    }

    public String getTests(String appKey, String userKey, String type) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }

        return String.format("%s/api/rest/blazemeter/getTests/?app_key=%s&user_key=%s&type=%s", SERVER_URL, appKey, userKey, type);
    }

    public String testUpdateUrl(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testUpdate/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
    }

    public String testStart(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/testStart/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
    }

    public String getUpdate(String appKey, String userKey, String version) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            version = URLEncoder.encode(version, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/jmeter_plugin_update/?app_key=%s&user_key=%s&current_version=%s", SERVER_URL, appKey, userKey, version);
    }

    public String getUserInfo(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            BmLog.error(e);
        }
        return String.format("%s/api/rest/blazemeter/getUserInfo/?app_key=%s&user_key=%s", SERVER_URL, appKey, userKey);
    }
}
