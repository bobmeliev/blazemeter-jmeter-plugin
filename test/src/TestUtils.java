package src;

import com.blazemeter.jmeter.entities.UserInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import junit.framework.Assert;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 1/9/14.
 */
public class TestUtils {
    private JMeterContext jmctx;
    private BmTestManager bmTestManager;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        bmTestManager = BmTestManager.getInstance();
    }


    @After
    public void tearDown() {
        jmctx = null;
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
        String str = Utils.getFileContents(System.getProperty("user.dir") + "/test/src/resources/locations.txt");
        JSONArray locations = null;

        try {
            locations = new JSONArray(str);
        } catch (JSONException e) {
            BmLog.error("Failed to construct LOCATIONS from locations.txt: " + e);
        }

        UserInfo userInfo = new UserInfo("dzmitry",
                1234, "dzmitrykashlach@gmail.com",
                12, 14, 15, 16, "enterprise", locations);
        bmTestManager.setUserInfo(userInfo);
        /*1.Create list of titles;
          2.Create list of id's
          3.
        */
        Assert.assertEquals("SANDBOX_FT", Utils.getLocationId("SANDBOX"));
    }
}
