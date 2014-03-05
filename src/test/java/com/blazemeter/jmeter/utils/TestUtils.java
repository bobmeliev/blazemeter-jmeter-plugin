package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.constants.TestConstants;
import com.blazemeter.jmeter.entities.*;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;


/**
 * Created by dzmitrykashlach on 1/9/14.
 */
public class TestUtils {
    private static String LOCATIONS = TestConstants.RESOURCES + "/locations.txt";
    private static String TEST_INFO = TestConstants.RESOURCES + "/test-info.txt";
    private static BmTestManager bmTestManager;
    private static Users users;
    private static Plan plan;


    @BeforeClass
    public static void setUp() {
        bmTestManager = BmTestManager.getInstance();
        plan = new Plan("HV40KOD", 40000, 40, true, 1000, 0);
        users = new Users("1689", "dzmitrykashlach", "dzmitry.kashlach@blazemeter.com",
                "1394008114", "1392730300", "1324748306", true, plan, null);
        bmTestManager.setUsers(users);

        String str = Utils.getFileContents(LOCATIONS);
        JSONArray locations = null;

        try {
            locations = new JSONArray(str);
        } catch (JSONException e) {
            BmLog.error("Failed to construct LOCATIONS from locations.txt: " + e);
        }
        users.setLocations(locations);


    }

    @AfterClass
    public static void tearDown() {
        bmTestManager = null;
    }

    @Test
    public void isInteger() {
        Assert.assertTrue(Utils.isInteger("12345"));
        Assert.assertFalse(Utils.isInteger("asdfg"));
        Assert.assertFalse(Utils.isInteger("(*&)(&)(*"));
    }

    @Test
    public void getLocationId() {
        Assert.assertEquals("SANDBOX_FT", Utils.getLocationId(users, "SANDBOX"));
    }

    @Test
    public void getLocationTitle() {
        Assert.assertEquals("SANDBOX", Utils.getLocationTitle(users, "SANDBOX_FT"));
    }

    @Test
    public void getFileContents() {
        String actual_locations_content = "[{\"id\":\"SANDBOX_FT\",\"title\":\"SANDBOX\"},{\"id\":\"eu-west-1\",\"title\":\"EU West (Ireland)\"},{\"id\":\"us-east-1\",\"title\":\"US East (Virginia)\"},{\"id\":\"us-west-1\",\"title\":\"US West (N.California)\"},{\"id\":\"us-west-2\",\"title\":\"US West (Oregon)\"},{\"id\":\"ap-southeast-1\",\"title\":\"Asia Pacific (Singapore)\"},{\"id\":\"ap-southeast-2\",\"title\":\"Australia (Sydney)\"},{\"id\":\"ap-northeast-1\",\"title\":\"Japan (Tokyo)\"},{\"id\":\"sa-east-1\",\"title\":\"South America (San Paulo)\"},{\"id\":\"529c83f6bda5edfe438bdbc8\",\"title\":\"DZMITRY\"}]";
        String expected_locations_content = StringUtils.strip(Utils.getFileContents(LOCATIONS));
        Assert.assertEquals(expected_locations_content, actual_locations_content);
    }

    @Test
    public void parseTestInfo() {
        String str = Utils.getFileContents(TEST_INFO);
        JSONObject testInfoJO = null;
        TestInfo ti_actual = new TestInfo();
        TestInfo ti_expected = new TestInfo();
        ti_expected.setId("411711");
        ti_expected.setName("load_test");
        ti_expected.setStatus(TestStatus.NotRunning);
        ti_expected.setNumberOfUsers(690);
        ti_expected.setLocation("us-west-2");
        Overrides overrides = new Overrides(0, -1, 0, 230);
        ti_expected.setOverrides(overrides);
        ti_expected.setType("jmeter");
        try {
            testInfoJO = new JSONObject(str);
            ti_actual = Utils.parseTestInfo(testInfoJO);
        } catch (JSONException e) {
            BmLog.error("Failed to construct TestInfoJO from locations.txt: " + e);
        }
        Assert.assertEquals(ti_expected.getId(), ti_actual.getId());
        Assert.assertEquals(ti_expected.getName(), ti_actual.getName());
        Assert.assertEquals(ti_expected.getLocation(), ti_actual.getLocation());
        Assert.assertEquals(ti_expected.getNumberOfUsers(), ti_actual.getNumberOfUsers());
        Assert.assertEquals(ti_expected.getStatus(), ti_actual.getStatus());
        Assert.assertEquals(ti_expected.getType(), ti_actual.getType());
    }


    @Test
    public void convertToJSON() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(TestConstants.RESOURCES + "/jmeter.properties"));
            JSONObject expected = Utils.convertToJSON(props);
            String s = Utils.getFileContents(TestConstants.RESOURCES + "/jmeter.properties.json");
            JSONObject actual = new JSONObject(s);

            Iterator<String> i = actual.keys();
            StringBuilder k = new StringBuilder();
            while (i.hasNext()) {
                k.append(i.next());
                Assert.assertTrue(actual.get(k.toString()).equals(expected.get(k.toString())));
                k.setLength(0);
            }
        } catch (JSONException je) {
            System.out.println("Failed to convert properties to JSON..." + je);
            Assert.fail();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Failed to read jmeter.properties..." + fnfe);
            Assert.fail();
        } catch (IOException ioe) {
            System.out.println("Failed to read jmeter.properties..." + ioe);
            Assert.fail();
        }
    }

    @Test
    public void getCurrentTestId() {
        String expected_curTestId = "1111";
        String currentTest = expected_curTestId + ";aaaaaaaaaaaaa";
        JMeterUtils.loadJMeterProperties(TestConstants.RESOURCES + "/jmeter.properties");
        JMeterUtils.setProperty(Constants.CURRENT_TEST, currentTest);
        String actual_currentTestId = Utils.getCurrentTestId();
        Assert.assertEquals(expected_curTestId, actual_currentTestId);
    }
}