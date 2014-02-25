package com.blazemeter.jmeter.api;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 2/25/14.
 */
public class TestBmUrlManager {
    private static BmUrlManager bmUrlManager;

    @BeforeClass
    public static void setUp() {
        bmUrlManager = BmUrlManager.getBmUrlManager();
    }

    @Test
    public void getServerUrl() {
        Assert.assertEquals("https://a.blazemeter.com", bmUrlManager.getServerUrl());
    }

    @Test
    public void getPluginPage() {
        Assert.assertEquals("http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter",
                bmUrlManager.getPluginPage());
    }

    @Test
    public void testStatus() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testGetStatus/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=459587&detailed=true",
                bmUrlManager.testStatus("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "459587", true));
    }

    @Test
    public void scriptCreation() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testCreate/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_name=load_test",
                bmUrlManager.scriptCreation("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "load_test"));
    }

    @Test
    public void scriptUpload() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testScriptUpload/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345&file_name=blazemeter.jmx",
                bmUrlManager.scriptUpload("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345", "blazemeter.jmx"));
    }


    @Test
    public void scriptDownload() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testScriptDownload/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345",
                bmUrlManager.scriptDownload("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345"));
    }

    @Test
    public void testStartLocal() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testStartExternal/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345",
                bmUrlManager.testStartLocal("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345"));
    }

    @Test
    public void testStop() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testStop/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345",
                bmUrlManager.testStop("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345"));
    }

    @Test
    public void logUpload() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testDataUpload/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345&file_name=jmeter.log&data_type=log",
                bmUrlManager.logUpload("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345", "jmeter.log", "log"));

    }

    @Test
    public void getTests() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/getTests/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&type=all",
                bmUrlManager.getTests("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "all"));

    }


    @Test
    public void testUpdateUrl() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testUpdate/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345",
                bmUrlManager.testUpdateUrl("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345"));

    }

    @Test
    public void testStart() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/testStart/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&test_id=12345",
                bmUrlManager.testStart("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "12345"));

    }

    @Test
    public void getUpdate() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/jmeter_plugin_update/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11&current_version=2.2",
                bmUrlManager.getUpdate("75bad111c06f4e10c001", "881a84b35e97c4342bf11", "2.2"));

    }

    @Test
    public void getUserInfo() {
        Assert.assertEquals("https://a.blazemeter.com/api/rest/blazemeter/getUserInfo/?app_key=75bad111c06f4e10c001&user_key=881a84b35e97c4342bf11",
                bmUrlManager.getUserInfo("75bad111c06f4e10c001", "881a84b35e97c4342bf11"));
    }

    @Test
    public void getUsers() {
        Assert.assertEquals("https://a.blazemeter.com/api/latest/users/?api_key=881a84b35e97c4342bf11",
                bmUrlManager.getUsers("881a84b35e97c4342bf11"));

    }


    @AfterClass
    public static void tearDown() {
        bmUrlManager = null;
    }
}
