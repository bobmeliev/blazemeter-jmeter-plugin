package com.blazemeter.jmeter.entities;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 2/12/14.
 */
public class TestEnginesParameters {
    private BmTestManager bmTestManager;


    @Before
    public void setUp() {
        bmTestManager = BmTestManager.getInstance();
        bmTestManager = BmTestManager.getInstance();
        UserInfo userInfo = new UserInfo("dzmitrykashlach",
                9999, "dzmitrykashlach@gmail.com",
                36000, 60, 300, 600, "enterprise", null);
        bmTestManager.setUserInfo(userInfo);

        /*

        <result>
<response_code>200</response_code>
<error/>
<username>dzmitrykashlach</username>
<credits>4458</credits>
<mail>dzmitry.kashlach@blazemeter.com</mail>
<max_users_limit>36000</max_users_limit>
<max_engines_limit>60</max_engines_limit>
<max_threads_medium>300</max_threads_medium>
<max_threads_large>600</max_threads_large>
<plan>ENTERPRISE</plan>
<recurring>1</recurring>
<locations is_array="true">...</locations>
</result>

         */


    }


    @Test
    public void countParameters_299_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(299);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 299);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_300_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(300);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 300);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_0_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(0);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 0);
        Assert.assertTrue(enginesParameters.getConsoles() == 0);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_1_user() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(1);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 1);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 0);
    }

    @Test
    public void countParameters_302_user() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(302);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 151);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 1);
    }

    @Test
    public void countParameters_598_users() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(598);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 299);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 1);
    }

    @Test
    public void countParameters_600_users() {

        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(600);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 300);
        Assert.assertTrue(enginesParameters.getConsoles() == 1);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 1);
    }

    @Test
    public void countParameters_18000_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(18000);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 300);
        Assert.assertTrue(enginesParameters.getConsoles() == 4);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.medium"));
        Assert.assertTrue(enginesParameters.getEngines() == 56);
    }

    @Test
    public void countParameters_19200_users() {
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(19200);
        Assert.assertTrue(enginesParameters.getUserPerEngine() == 600);
        Assert.assertTrue(enginesParameters.getConsoles() == 2);
        Assert.assertTrue(enginesParameters.getEngineSize().equals("m1.large"));
        Assert.assertTrue(enginesParameters.getEngines() == 30);
    }


}
