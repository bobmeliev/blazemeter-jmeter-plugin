package com.blazemeter.jmeter.entities;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 2/12/14.
 */
public class TestEnginesParameters {
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

    }

    @AfterClass
    public static void tearDown() {
        bmTestManager = null;
    }


    @Test
    public void countParameters_0_users() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 0);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 0);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_1_user() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 1);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_999_users() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 999);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 999);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_1000_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 1000);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1000);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_1002_user() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 1002);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 501);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 1);
    }


    @Test
    public void countParameters_2000_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 2000);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1000);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue("Expected enginesParameters.getEngineSize()=" + Constants.LARGE_ENGINE +
                " actual enginesParameters.getEngineSize()=" + enginesParameters.getEngineSize()
                , enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 1);
    }


    @Test
    public void countParameters_30000_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 30000);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1000);
        Assert.assertTrue(enginesParameters.getConsoles() == 2);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 28);
    }

    @Test
    public void countParameters_40000_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(users, 40000);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1000);
        Assert.assertTrue(enginesParameters.getConsoles() == 3);
        Assert.assertTrue(enginesParameters.getEngineSize().equals(Constants.LARGE_ENGINE));
        Assert.assertTrue(enginesParameters.getEngines() == 37);
    }
}
