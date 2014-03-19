package com.blazemeter.jmeter.api;

import com.blazemeter.jmeter.api.checkers.TestsChecker;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.constants.Methods;
import com.blazemeter.jmeter.entities.*;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.services.FileServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class BlazemeterApi {

    private BmUrlManager urlManager = BmUrlManager.getBmUrlManager();
    private static BlazemeterApi instance;

    public static BlazemeterApi getInstance() {
        if (instance == null)
            instance = new BlazemeterApi();
        return instance;
    }

    private BlazemeterApi() {
    }

    private HttpResponse getJMX(String url) {
        BmLog.debug("Requesting : " + url);
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Connection", "keep-alive");
        getRequest.setHeader("Host", BmUrlManager.SERVER_URL.substring(8, com.blazemeter.jmeter.api.BmUrlManager.SERVER_URL.length()));
        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if (statusCode != 200) {
                BmLog.error(String.format("Wrong response : %d %s", statusCode, error));
            }

        } catch (IOException ioe) {
            BmLog.error("Wrong response", ioe);
        }
        return response;
    }

    private HttpResponse doHTTPRequest(String method, String url, JSONObject data) throws IOException {

        BmLog.debug("Requesting : " + url);
        HttpRequestBase request = method.equals(Methods.POST) ? new HttpPost(url) : new HttpGet(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null & request instanceof HttpPost) {
            StringEntity stringEntity = null;

            if (data.has(Constants.SAMPLES)) {
                try {
                    stringEntity = new StringEntity(data.getString(Constants.SAMPLES));

                } catch (JSONException je) {
                    BmLog.error("Failed to prepare samples for sending: " + je.getMessage());
                }
            } else {
                stringEntity = new StringEntity(data.toString());
            }
            ((HttpPost) request).setEntity(stringEntity);

        }

        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(request);

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


    private JSONObject getJson(String method, String url, JSONObject data) {
        JSONObject jo = null;
        HttpResponse response = null;
        try {

            response = doHTTPRequest(method, url, data);
            if (data != null) {
                BmLog.debug("HTTP Request body=" + data.toString());
            }

            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                BmLog.debug(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            BmLog.error("Error while decoding Json: " + e.getMessage());
            BmLog.debug("Error while decoding Json: " + e.getMessage());
        } catch (JSONException e) {
            BmLog.error("Error while decoding Json: " + e.getMessage());
            BmLog.debug("Error while decoding Json: " + e.getMessage());
        } finally {
            return jo;
        }
    }

    private List<String> getJMXasList(String url) {
        List<String> jmx = new ArrayList<String>(2);
        String jmxScript = null;
        String jmxScriptName = null;
        try {
            HttpResponse response = getJMX(url);
            if (response != null) {
                HeaderIterator headerIterator = response.headerIterator();
                while (headerIterator.hasNext()) {
                    Header header = headerIterator.nextHeader();
                    if (header.getName().equals("Content-Disposition")) {
                        String headerValue = header.getValue();
                        // added filename of *.jmx
                        jmxScriptName = headerValue.substring(10, headerValue.length() - 1);
                        jmx.add(jmxScriptName);
                        BmLog.debug("JMX is downloaded from server");
                    }
                }
                jmxScript = EntityUtils.toString(response.getEntity());
                jmx.add(jmxScript);

            }
        } catch (IOException ioe) {
            BmLog.error("error while decoding response from server", ioe);
        } finally {
            return jmx;
        }
    }


    private synchronized JSONArray getLocations(String userKey) {
        JSONArray locations = null;
        if (userKey == null || userKey.isEmpty())
            return locations;

        try {
            String url = this.urlManager.getUserInfo(Constants.APP_KEY, userKey);

            JSONObject jo = getJson(Methods.POST, url, null);
            if (jo.getInt("response_code") == 200) {
                locations = jo.getJSONArray("locations");
            }
        } catch (JSONException e) {
            BmLog.error("Error while getting locations: ", e);
        } catch (Throwable e) {
            BmLog.error("Error while getting locations: ", e);
        }
        return locations;
    }


    public synchronized Users getUsers(String userKey) {
        Users users = null;
        if (userKey == null || userKey.isEmpty())
            return users;

        try {
            String url = this.urlManager.getUsers(Constants.APP_KEY, userKey);

            JSONObject jo = getJson(Methods.GET, url, null);
            if (!jo.has("error")) {

                users = new Users(jo.getString("id"),
                        jo.getString("name"),
                        jo.getString("mail"),
                        jo.getString("access"),
                        jo.getString("login"),
                        jo.getString("created"),
                        jo.getBoolean("enabled"),
                        null, null
                );
                JSONObject plan_JO = jo.getJSONObject("plan");
                Plan plan = new Plan(plan_JO.getString("id"),
                        plan_JO.getInt("concurrency"),
                        plan_JO.getInt("engines"),
                        plan_JO.getBoolean("isMetered"),
                        plan_JO.getInt("threadsPerEngine"),
                        plan_JO.getInt("threadsPerMediumEngine"));
                users.setPlan(plan);
                JSONArray locations = this.getLocations(userKey);
                users.setLocations(locations);
            }
        } catch (JSONException e) {
            BmLog.error("Error getting users: " + e);
        } catch (Throwable e) {
            BmLog.error("Error getting users: " + e);
        }
        return users;
    }


    public synchronized void getTests(String userKey, ITestListReceivedNotification notifier) {
        TestsChecker testsChecker = new TestsChecker(userKey, notifier);
        new Thread(testsChecker).start();
    }


    public TestInfo stopInTheCloud(String userKey, TestInfo testInfo) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("Test cannot be stopped in the cloud, userKey is empty");
            return testInfo;
        }
        String url = this.urlManager.testStop(Constants.APP_KEY, userKey, testInfo.getId());
        JSONObject jo = getJson(Methods.POST, url, null);
        try {
            testInfo.setStatus(jo.getInt("response_code") == 200 ? TestStatus.NotRunning : TestStatus.Running);
        } catch (JSONException je) {
            BmLog.debug("Failed to set test status: " + je.getMessage());
        }
        return testInfo;
    }

    public TestInfo runInTheCloud(String userKey, String testId) {
        TestInfo testInfo = null;
        String error = null;
        if (userKey == null || userKey.trim().isEmpty()) {
            error = "Test cannot be started in the cloud, userKey is empty";
            BmLog.debug(error);
            testInfo = new TestInfo();
            testInfo.setError(error);
            return testInfo;
        }

        if (testId == null || testId.trim().isEmpty()) {
            error = "Test cannot be started in the cloud, testId is empty";
            BmLog.debug(error);
            testInfo = new TestInfo();
            testInfo.setError(error);
            return testInfo;
        }

        String url = this.urlManager.testStart(Constants.APP_KEY, userKey, testId);
        JSONObject jo = getJson(Methods.POST, url, null);
        testInfo = Utils.parseTestInfo(jo);
        try {
            testInfo.setStatus(jo.getInt("response_code") == 200 ? TestStatus.Running : TestStatus.NotRunning);
        } catch (JSONException je) {
            BmLog.debug("Failed to set test status: " + je.getMessage());
        }
        return testInfo;
    }

    public synchronized ArrayList<TestInfo> getTests(String userKey) {
        if (userKey.trim().isEmpty()) {
            BmLog.debug("List of tests cannot be received, userKey is empty");
            return null;
        }

        String url = this.urlManager.getTests(Constants.APP_KEY, userKey, "all");

        JSONObject jo = getJson(Methods.POST, url, null);
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
            testInfo.setName(name);
            testInfo.setId(id);
            tests.add(testInfo);
        }
        return tests;
    }

    public synchronized TestInfo createTest(String userKey, String testName) {
        TestInfo testInfo = null;
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("Test cannot be created, userKey is empty");
            return null;
        }

        String url = this.urlManager.scriptCreation(Constants.APP_KEY, userKey, testName);
        JSONObject properties = new JSONObject();
        try {
            JSONObject jo = new JSONObject();
            jo.put("JMETER_VERSION", Utils.getJmeterVersion());
            properties.put("options", jo);
        } catch (JSONException e) {
            BmLog.error(e);
        }
        JSONObject jo = getJson(Methods.POST, url, properties);
        testInfo = Utils.parseTestInfo(jo);

        return testInfo;
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param filePath - jmx file path
     */
    public synchronized void uploadJmx(String userKey, String testId, String fileName, String filePath) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("JMX cannot be uploaded, userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("JMX cannot be uploaded, testId is empty");
            return;
        }

        String url = this.urlManager.scriptUpload(Constants.APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(filePath);

        try {
            jmxData.put(Constants.DATA, fileCon);
        } catch (JSONException e) {
            BmLog.error(e);
        }

        getJson(Methods.POST, url, jmxData);
    }

    /*
       This method is used for downloading *.jmx from server to
       local machine for editing in Jmeter.
    */
    public synchronized File downloadJmx(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("JMX cannot be downloaded, userKey is empty");
            return null;
        }
        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("JMX cannot be downloaded, testId is empty");
            return null;
        }
        String url = this.urlManager.scriptDownload(Constants.APP_KEY, userKey, testId);
        BmLog.debug("Downloading JMX from server...");
        List<String> jmx = getJMXasList(url);
        String jmxName = jmx.get(0);
        FileOutputStream fileOutputStream = null;
        File jmxFile = null;
        try {
            BmLog.debug("JMX name=" + jmxName);
            jmxFile = new File(FileServer.getDefaultBase() + File.separator + jmxName);
            BmLog.debug("Setting JMX file=" + jmxFile.getCanonicalPath());
            // if file doesnt exists, then create it
            if (!jmxFile.exists()) {
                try {
                    BmLog.debug("Creating file " + jmxFile.getCanonicalPath());
                    jmxFile.createNewFile();

                } catch (IOException io) {
                    BmLog.debug("Failed to create file for saving JMX: " + io);

                }
            }


            BmLog.debug("Saving JMX to " + jmxFile.getCanonicalPath());
            fileOutputStream = new FileOutputStream(jmxFile);
            // get the content in bytes
            byte[] jmxInBytes = jmx.get(1).getBytes();
            fileOutputStream.write(jmxInBytes);
            fileOutputStream.flush();
            fileOutputStream.close();
            BmLog.debug("JMX script was saved to " + jmxFile.getCanonicalPath());
        } catch (IOException ioe) {
            BmLog.debug("Failed to download&save JMX: " + ioe);
        } catch (IndexOutOfBoundsException ioube) {
            BmLog.debug("Verify bug https://blazemeter.atlassian.net/browse/BPC-146");
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException fioe) {
                BmLog.debug("Failed to close fileinputstream: " + fioe);
            }
        }
        return jmxFile;
    }


    public synchronized int logUpload(String userKey, String testId, String logName, String buff, String dataType) {

        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("Log cannot be uploaded, userKey is empty");
            return -1;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("Log cannot be uploaded, testId is empty");
            return -1;
        }

        Integer fileSize = -1;
        logName = logName.trim().isEmpty() ? "log" : logName;

        String url = this.urlManager.logUpload(Constants.APP_KEY, userKey, testId, logName, dataType);

        JSONObject obj = new JSONObject();
        try {
            obj.put(Constants.DATA, buff);
            JSONObject jo = getJson(Methods.POST, url, obj);
            if (jo.has("file_size"))
                fileSize = (Integer) jo.get("file_size");
            else
                BmLog.error("Failed to upload " + logName);
        } catch (JSONException e) {
            BmLog.error(e);
        }
        return fileSize;
    }

    public synchronized void samplesUpload(List<JSONObject> samples, String callBackUrl) {
        try {
            JSONObject data = new JSONObject();
            data.put(Constants.SAMPLES, new JSONArray(samples));
            getJson(Methods.POST, callBackUrl, data);
        } catch (JSONException e) {
            BmLog.error("Failed to upload samples: " + e.getMessage());
        }
    }

    public TestInfo updateTestSettings(String userKey, String testId, String location,
                                       int engines, String engineType, int usersPerEngine,
                                       int iterations, int rumpUp, int duration, Properties jmeterProperties) {
        TestInfo testInfo = new TestInfo();
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("Test settings cannot be updated, userKey is empty");
            return testInfo;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("Test settings cannot be updated, testId is empty");
            return testInfo;
        }

        String url = this.urlManager.testUpdateUrl(Constants.APP_KEY, userKey, testId);
        JSONObject obj = new JSONObject();
        try {
            JSONObject options = new JSONObject();
            options.put("NUMBER_OF_ENGINES", engines);//engine
            options.put("JMETER_VERSION", Utils.getJmeterVersion());//engine
            options.put("INSTANCE_TYPE", engineType);//engine
            options.put("OVERRIDE", 1);
            options.put("OVERRIDE_THREADS", usersPerEngine);//threads
            options.put("OVERRIDE_ITERATIONS", iterations);//iter
            options.put("OVERRIDE_RAMP_UP", rumpUp);//ranpup
            options.put("OVERRIDE_DURATION", duration);//duration
            options.put("LOCATION", location);
            // pass Properties jmeterProperties to method;
            JSONObject jmeter_params = Utils.convertToJSON(jmeterProperties);
            options.put("JMETER_PARAMS", jmeter_params);

            obj.put("options", options);
            JSONObject jo = getJson(Methods.POST, url, obj);
            if (jo == null || jo.getInt("response_code") != 200) {
                BmLog.error("Failed to update: " + testId);
                BmLog.error("JSON options were sent with JSON object: " + options.toString());
                BmLog.error("UserKey = " + userKey);
                BmLog.error("Test id = " + userKey);
                BmLog.error("LOCATION = " + location);

            } else if (jo.getInt("response_code") == 200) {
                testInfo = Utils.parseTestInfo(jo);
            }
        } catch (JSONException e) {
            BmLog.error(e);
        } catch (NullPointerException npe) {
            BmLog.error("Invalid answer from server! Turn to customer support to resolve issue. See log for details: " + npe);
        } finally {
            return testInfo;
        }
    }

    public TestInfo getTestRunStatus(String userKey, String testId, boolean detailed) {
        TestInfo ti = BmTestManager.getInstance().getTestInfo();

        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("TestRunStatus cannot be received, userKey is empty");
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("TestRunStatus cannot be received, testId is empty");
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(Constants.APP_KEY, userKey, testId, detailed);

            JSONObject jo = getJson(Methods.POST, url, null);
            if (jo.getInt("response_code") == 200) {
                ti = Utils.parseTestInfo(jo);
            } else {
                ti.setStatus(jo.getInt("response_code") == 404 ? TestStatus.NotFound : TestStatus.Error);
                ti.setError(jo.getString("error"));
            }
        } catch (JSONException e) {
            BmLog.error("Error getting status:", e);
            ti.setStatus(TestStatus.Error);
        } catch (Throwable e) {
            BmLog.error("Error getting status:", e);
            ti.setStatus(TestStatus.Error);
        }

        return ti;
    }

    public PluginUpdate getUpdate(String userKey) {
        PluginUpdate update = null;
        try {
            userKey = userKey == null ? "" : userKey;
            String url = this.urlManager.getUpdate(Constants.APP_KEY, userKey, Utils.getPluginVersion().toString(true));

            JSONObject jo = getJson(Methods.POST, url, null);
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

    public synchronized HashMap<String, String> startTestLocal(String userKey, String testId) {
        HashMap<String, String> res = new HashMap<String, String>();

        String url = this.urlManager.testStartLocal(Constants.APP_KEY, userKey, testId);
        String responseCode = null;
        String errorMessage = null;
        String callBackUrl = null;

        try {
            JSONObject jsonObject = getJson(Methods.POST, url, null);
            responseCode = jsonObject.get("response_code").toString();
            errorMessage = jsonObject.get("error").toString();
            callBackUrl = jsonObject.get("submit").toString();

        } catch (JSONException je) {
            BmLog.error("Error during processing JSON request: ", je);
        }
        if (!responseCode.equals("200")) {
            res.put("error", errorMessage);
        } else {
            res.put(Constants.CALLBACK_URL, callBackUrl);
        }
        return res;
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     */
    public void stopTest(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            BmLog.debug("Test cannot be stopped, userKey is empty");
            return;
        }

        if (testId == null || testId.trim().isEmpty()) {
            BmLog.debug("Test cannot be stopped, testId is empty");
            return;
        }

        String url = this.urlManager.testStop(Constants.APP_KEY, userKey, testId);
        getJson(Methods.POST, url, null);
    }
}
