package com.blazemeter.jmeter.common;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.Overrides;
import com.blazemeter.jmeter.testexecutor.TestInfo;
import com.blazemeter.jmeter.testexecutor.UserInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class BlazemeterApi {

    public static final String APP_KEY = "75bad111c06f4e10c001"; //was:75bad111c06f4e10c514
    private BmUrlManager urlManager = new BmUrlManager();
    private static BlazemeterApi instance;

    public static BlazemeterApi getInstance() {
        if (instance == null)
            instance = new BlazemeterApi();
        return instance;
    }

    private BlazemeterApi() {
    }

    private HttpResponse getResponse(String url, JSONObject data) throws IOException {

        BmLog.console("Requesting : " + url);
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null) {
            postRequest.setEntity(new StringEntity(data.toString()));
        }

        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if (statusCode != 200) {
                BmLog.error(String.format("Wrong response : %d %s", statusCode, error));
            }
        } catch (IOException e) {
            BmLog.error("Wrong response", e);
        }
        return response;
    }


    private JSONObject getJson(String url, JSONObject data) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponse(url, data);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                BmLog.console(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            BmLog.error("error while decoding Json", e);
        } catch (JSONException e) {
            BmLog.error("error while decoding Json", e);
        }
        return jo;
    }

    public UserInfo getUserInfo(String userKey) {
        UserInfo userInfo = null;
        if (userKey == null || userKey.isEmpty())
            return userInfo;

        try {
            String url = this.urlManager.getUserInfo(APP_KEY, userKey);

            JSONObject jo = getJson(url, null);
            if (jo.getInt("response_code") == 200) {
                userInfo = new UserInfo(jo.getString("username"), jo.getString("mail"), jo.getInt("credits"), jo.getInt("max_users_limit"));
            }
        } catch (JSONException e) {
            BmLog.error("status getting status", e);
        } catch (Throwable e) {
            BmLog.error("status getting status", e);
        }
        return userInfo;
    }


    public interface TestContainerNotifier {
        public void testReceived(ArrayList<TestInfo> tests);
    }


    public synchronized void getTestsAsync(String userKey, TestContainerNotifier notifier) {
        class TestsFetcher implements Runnable {
            String userKey;
            TestContainerNotifier notifier;

            TestsFetcher(String userKey, TestContainerNotifier notifier) {
                this.userKey = userKey;
                this.notifier = notifier;
            }

            public void run() {
                ArrayList<TestInfo> tests = getTests(userKey);
                notifier.testReceived(tests);
            }
        }
        new Thread(new TestsFetcher(userKey, notifier)).start();
    }


    public int stopInTheCloud(String userKey, String testId){
        if (userKey == null || userKey.trim().isEmpty()) {
                    BmLog.console("stopTest userKey is empty");
                    return -1;
                }

                if (testId == null || testId.trim().isEmpty()) {
                    BmLog.console("testId is empty");
                    return -1;
                }

                String url = this.urlManager.testStop(APP_KEY, userKey, testId);
                JSONObject jo = getJson(url, null);
                try {
                    if (!jo.get("response_code").toString().equals("200"))
                        return -1;

                    return jo.getInt("response_code");
                } catch (JSONException e) {
                    BmLog.error(e);
                    return -1;
                }
    }

    public int runInTheCloud(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("startTestLocal userKey is empty");
            return -1;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return -2;
        }

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject jo = getJson(url, null);
        try {
            if (!jo.get("response_code").toString().equals("200"))
                return -1;

            return jo.getInt("test_id");
        } catch (JSONException e) {
            BmLog.error(e);
            return -1;
        }
    }

    public synchronized ArrayList<TestInfo> getTests(String userKey) {
        if (userKey.trim().isEmpty()) {
            BmLog.console("getTests userKey is empty");
            return null;
        }

        String url = this.urlManager.getTests(APP_KEY, userKey, "all");

        JSONObject jo = getJson(url, null);
        JSONArray arr;
        try {
            String r = jo.get("response_code").toString();
            if (!r.equals("200"))
                return null;
            arr = (JSONArray) jo.get("tests");
        } catch (JSONException e) {
            return null;
        }


        ArrayList<TestInfo> tests = new ArrayList<TestInfo>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject en;
            try {
                en = arr.getJSONObject(i);
            } catch (JSONException e) {
                BmLog.error(e);
                continue;
            }
            String id = null;
            String name = null;
            try {
                id = en.getString("test_id");
                name = en.getString("test_name");
            } catch (JSONException ignored) {
            }
            TestInfo testInfo = new TestInfo();
            testInfo.name = name;
            testInfo.id = id;
            tests.add(testInfo);
        }
        return tests;
    }

    public synchronized TestInfo createTest(String userKey, String testName) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("createTest userKey is empty");
            return null;
        }

        String url = this.urlManager.scriptCreation(APP_KEY, userKey, testName);
        JSONObject properties = new JSONObject();
        try {
            JSONObject jo = new JSONObject();
            jo.put("JMETER_VERSION", JMeterPluginUtils.getJmeterVersion());
            properties.put("options", jo);
        } catch (JSONException e) {
            BmLog.error(e);
        }
        JSONObject jo = getJson(url, properties);
        TestInfo ti = new TestInfo();
        try {
            if (jo.isNull("error")) {
                ti.id = jo.getString("test_id");
                ti.name = jo.getString("test_name");
            } else {
                ti.error = jo.getString("error");
                ti.status = TestStatus.Error;
            }
        } catch (JSONException e) {
            ti.status = TestStatus.Error;
        }
        return ti;
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param filePath - jmx file path
     */
    public synchronized void uploadJmx(String userKey, String testId, String fileName, String filePath) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("uploadJmx userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return;
        }

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(filePath);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            BmLog.error(e);
        }

        getJson(url, jmxData);
    }

    public synchronized int dataUpload(String userKey, String testId, String reportName, String buff, String dataType) {

        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("dataUpload userKey is empty");
            return -1;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return -1;
        }

        Integer fileSize = -1;


        reportName = reportName.trim().isEmpty() ? "sample" : reportName;
        if (dataType.equals("jtl"))
            reportName = reportName.toLowerCase().endsWith(".jtl") ? reportName : reportName + ".jtl";

        String url = this.urlManager.testResultsJTLUpload(APP_KEY, userKey, testId, reportName, dataType);

        JSONObject obj = new JSONObject();
        try {
            obj.put("data", buff);
            JSONObject jo = getJson(url, obj);
            if (jo.has("file_size"))
                fileSize = (Integer) jo.get("file_size");
            else
                BmLog.error("Failed to upload " + reportName);
        } catch (JSONException e) {
            BmLog.error(e);
        }


        return fileSize;
    }

    public boolean updateTestSettings(String userKey, String testId, String location, //boolean override,
                                      int engines, String engineType, int usersPerEngine,
                                      int iterations, int rumpUp, int duration) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("dataUpload userKey is empty");
            return false;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return false;
        }

        String url = this.urlManager.testUpdateUrl(APP_KEY, userKey, testId);
        JSONObject obj = new JSONObject();
        try {
            JSONObject options = new JSONObject();
            options.put("NUMBER_OF_ENGINES", engines);//engine
            options.put("INSTANCE_TYPE", engineType);//engine
            options.put("OVERRIDE", 1);
            options.put("OVERRIDE_THREADS", usersPerEngine);//threads
            options.put("OVERRIDE_ITERATIONS", iterations);//iter
            options.put("OVERRIDE_RAMP_UP", rumpUp);//ranpup
            options.put("OVERRIDE_DURATION", duration);//duration
            options.put("LOCATION", location);
            obj.put("options", options);
            JSONObject jo = getJson(url, obj);
            if (jo.getInt("response_code") != 200)
                BmLog.error("Failed to update" + testId);
        } catch (JSONException e) {
            BmLog.error(e);
            return false;
        }


        return true;
    }

    public TestInfo getTestRunStatus(String userKey, String testId, boolean detailed) {
        TestInfo ti = BmTestManager.getInstance().getTestInfo();

        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("getTestRunStatus userKey is empty");
            ti.status = TestStatus.NotFound;
            return ti;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            ti.status = TestStatus.NotFound;
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId, detailed);

            JSONObject jo = getJson(url, null);
            if (jo.getInt("response_code") == 200) {
                ti.id = jo.getString("test_id");
                ti.name = jo.getString("test_name");
                ti.status = jo.getString("status").equalsIgnoreCase("running") ? TestStatus.Running : TestStatus.NotRunning;
                JSONObject options = jo.getJSONObject("options");
                if (options != null) {
                    ti.numberOfUsers = options.getInt("USERS");
                    ti.location = options.getString("LOCATION");
                    ti.type = options.getString("TEST_TYPE");
                    if (options.getBoolean("OVERRIDE")) {
                        ti.overrides = new Overrides(
                                options.getInt("OVERRIDE_DURATION")
                                , options.getInt("OVERRIDE_ITERATIONS")
                                , options.getInt("OVERRIDE_RAMP_UP")
                                , options.getInt("OVERRIDE_THREADS")
                        );
                    }
                }
            } else {
                ti.status = jo.getInt("response_code") == 404 ? TestStatus.NotFound : TestStatus.Error;
                ti.error = jo.getString("error");
            }
        } catch (JSONException e) {
            BmLog.error("status getting status", e);
            ti.status = TestStatus.Error;
        } catch (Throwable e) {
            BmLog.error("status getting status", e);
            ti.status = TestStatus.Error;
        }

        return ti;
    }

    public PluginUpdate getUpdate(String userKey) {
        PluginUpdate update = null;
        try {
            userKey = userKey == null ? "" : userKey;
            String url = this.urlManager.getUpdate(APP_KEY, userKey, JMeterPluginUtils.getPluginVersion().toString(true));

            JSONObject jo = getJson(url, null);
            if (jo.getInt("response_code") == 200) {
                update = new PluginUpdate(new PluginVersion(jo.getInt("version_major"),
                                                            jo.getInt("version_minor"),
                                                            jo.getString("version_build")),
                                                            jo.getString("download_url"),
                                                            jo.getString("changes"),
                                                            jo.getString("more_info_url"));
            }
        } catch (JSONException e) {
            BmLog.error("status getting status", e);
        } catch (Throwable e) {
            BmLog.error("status getting status", e);
        }
        return update;
    }

    public synchronized void startTestLocal(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("startTestLocal userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return;
        }

        String url = this.urlManager.testStartLocal(APP_KEY, userKey, testId);
        getJson(url, null);
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     */
    public void stopTest(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("stopTest userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return;
        }

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        getJson(url, null);
    }


    public static class BmUrlManager {
        private static String SERVER_URL = "https://a.blazemeter.com";

        protected BmUrlManager() {
            SERVER_URL = JMeterUtils.getPropDefault("blazemeter.url", SERVER_URL);
            BmLog.console("Server url is :" + SERVER_URL);
            BmLog.console("Jmeter version :" + JMeterUtils.getJMeterVersion());
            BmLog.console("Plugin version :" + JMeterPluginUtils.getPluginVersion().toString(true));
        }

        public static String getServerUrl() {
            return SERVER_URL;
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

        public String testResultsJTLUpload(String appKey, String userKey, String testId, String fileName, String dataType) {
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
            return String.format("%s/api/rest/blazemeter/jmeter_plugin_update/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, version);
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
}
