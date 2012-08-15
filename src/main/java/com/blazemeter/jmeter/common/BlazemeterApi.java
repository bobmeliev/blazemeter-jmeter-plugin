package com.blazemeter.jmeter.common;

import com.blazemeter.jmeter.testexecutor.TestInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

    public static final String APP_KEY = "75bad111c06f4e10c514"; //TODO: change to 75bad111c06f4e10c001
    private BmUrlManager urlManager = new BmUrlManager();

    private static BlazemeterApi instance;

    public static BlazemeterApi getInstance() {
        if (instance == null)
            instance = new BlazemeterApi();
        return instance;
    }

    private BlazemeterApi() {
//        BmLog.console("REST INTERFACE INIT");
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
            if (statusCode!=200) {
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

    //    public synchronized void getReports(String userKey, String id) throws IOException, JSONException {
//        String url = this.urlManager.TestReport(APP_KEY, userKey, id);
//
//        JSONObject jo = getJson(url, null);
//        ArrayList<JSONObject> arr = (ArrayList<JSONObject>) jo.get("reports");
//        HashMap<String, String> rpt = new HashMap<String, String>();
//
//        for (JSONObject en : arr) {
//            String zipurl = (String) en.get("zip_url");
//            String date = (String) en.get("date");
//            String test_url = (String) en.get("url");
//            String title = (String) en.get("title");
//
//            if (rpt.containsKey(id)) {
//                rpt.put("title", title);
//                rpt.put("date", date);
//                rpt.put("url", url);
//                rpt.put("zipurl", zipurl);
//            }
//
//            BmLog.console("zip URL " + zipurl);
//            BmLog.console("Date of  Test Run " + date);
//            BmLog.console("URL For  Test" + test_url);
//            BmLog.console("Title" + title);
//        }
//    }

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

    public synchronized ArrayList<TestInfo> getTests(String userKey) {
        if (userKey.trim().isEmpty()) {
            BmLog.console("getTests userKey is empty");
            return null;
        }

        String url = this.urlManager.GetTests(APP_KEY, userKey, "all");

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

        String url = this.urlManager.ScriptCreation(APP_KEY, userKey, testName);
        JSONObject properties = new JSONObject();
        try {
            JSONObject jo = new JSONObject();
            jo.put("JMETER_VERSION",JMeterPluginUtils.getJmeterVersion());
            properties.put("options",jo);
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
     * @return test id
     * @throws java.io.IOException
     * @throws org.json.JSONException
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

        String url = this.urlManager.ScriptUpload(APP_KEY, userKey, testId, fileName);
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

        String url = this.urlManager.TestResultsJTLUpload(APP_KEY, userKey, testId, reportName, dataType);

        JSONObject obj = new JSONObject();
        try {
            obj.put("data", buff);
            JSONObject jo = getJson(url, obj);
            if(jo.has("file_size"))
                fileSize = (Integer) jo.get("file_size");
            else
                BmLog.error("Failed to upload "+reportName);
        } catch (JSONException e) {
            BmLog.error(e);
        }


        return fileSize;
    }

    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

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
            String url = this.urlManager.TestStatus(APP_KEY, userKey, testId);

            JSONObject jo = getJson(url, null);
            if (jo.getInt("response_code")==200){
                ti.id = jo.getString("test_id");
                ti.name = jo.getString("test_name");
                ti.status = jo.getString("status").equalsIgnoreCase("running") ?TestStatus.Running:TestStatus.NotRunning;
            }
            else {
                ti.status = jo.getInt("response_code")==404?TestStatus.NotFound:TestStatus.Error;
                ti.error=jo.getString("error");
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

    public synchronized void startTestLocal(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.console("startTestLocal userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.console("testId is empty");
            return;
        }

        String url = this.urlManager.TestStartLocal(APP_KEY, userKey, testId);
        getJson(url, null);
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     * @throws IOException
     * @throws ClientProtocolException
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

        String url = this.urlManager.TestStop(APP_KEY, userKey, testId);
        getJson(url, null);
    }


    public static class BmUrlManager {
        private static String SERVER_URL = "https://a.blazemeter.com";

        public BmUrlManager() {
            SERVER_URL = JMeterUtils.getPropDefault("blazemeter.url", SERVER_URL);
            BmLog.console("Server url is :" + SERVER_URL);
            BmLog.console("Jmeter version :" +JMeterUtils.getJMeterVersion());
            BmLog.console("Plugin version :" + JMeterPluginUtils.getPluginVersion());
        }

        public static String getServerUrl() {
            return SERVER_URL;
        }

        public String TestStatus(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                BmLog.error(e);
            }
            return String.format("%s/api/rest/blazemeter/testGetStatus/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

        public String ScriptCreation(String appKey, String userKey, String testName) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testName = URLEncoder.encode(testName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                BmLog.error(e);
            }
            return String.format("%s/api/rest/blazemeter/testCreate/?app_key=%s&user_key=%s&test_name=%s", SERVER_URL, appKey, userKey, testName);
        }

        public String ScriptUpload(String appKey, String userKey, String testId, String fileName) {
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

        public String TestStartLocal(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                BmLog.error(e);
            }
            return String.format("%s/api/rest/blazemeter/testStartExternal/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

        public String TestStop(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                BmLog.error(e);
            }
            return String.format("%s/api/rest/blazemeter/testStop/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

        public String TestResultsJTLUpload(String appKey, String userKey, String testId, String fileName, String dataType) {
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

        public String GetTests(String appKey, String userKey, String type) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                BmLog.error(e);
            }

            return String.format("%s/api/rest/blazemeter/getTests/?app_key=%s&user_key=%s&type=%s", SERVER_URL, appKey, userKey, type);
        }
    }
}
