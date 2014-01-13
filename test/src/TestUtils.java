package src;

import com.blazemeter.jmeter.entities.UserInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 1/9/14.
 */
public class TestUtils {
    private JMeterContext jmctx;
    private BmTestManager bmTestManager;
    private String LOCATIONS = System.getProperty("user.dir") + "/test/src/resources/locations.txt";

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        bmTestManager = BmTestManager.getInstance();
        String str = Utils.getFileContents(LOCATIONS);
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
        Assert.assertEquals("SANDBOX_FT", Utils.getLocationId("SANDBOX"));
    }

    @Test
    public void getLocationTitle() {
        Assert.assertEquals("SANDBOX", Utils.getLocationTitle("SANDBOX_FT"));
    }

    @Test
    public void getFileContents() {
        String actual_locations_content = "[{\"id\":\"SANDBOX_FT\",\"title\":\"SANDBOX\"},{\"id\":\"eu-west-1\",\"title\":\"EU West (Ireland)\"},{\"id\":\"us-east-1\",\"title\":\"US East (Virginia)\"},{\"id\":\"us-west-1\",\"title\":\"US West (N.California)\"},{\"id\":\"us-west-2\",\"title\":\"US West (Oregon)\"},{\"id\":\"ap-southeast-1\",\"title\":\"Asia Pacific (Singapore)\"},{\"id\":\"ap-southeast-2\",\"title\":\"Australia (Sydney)\"},{\"id\":\"ap-northeast-1\",\"title\":\"Japan (Tokyo)\"},{\"id\":\"sa-east-1\",\"title\":\"South America (San Paulo)\"},{\"id\":\"529c83f6bda5edfe438bdbc8\",\"title\":\"DZMITRY\"}]";
        String expected_locations_content = StringUtils.strip(Utils.getFileContents(LOCATIONS));

        Assert.assertEquals(expected_locations_content, actual_locations_content);
    }

    @Test
    @Ignore
    public void getJSONObject() {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy("HTTPSampler");

        sampler.setProperty(TestElement.GUI_CLASS, "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui");
        sampler.setProperty(TestElement.TEST_CLASS, "org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy");
        sampler.setDomain("google.com");
        sampler.setPath("/");
        sampler.setProtocol("http");
        sampler.setMethod("GET");

        jmctx.setCurrentSampler(sampler);
        SampleResult result = sampler.sample();
        SampleEvent event = new SampleEvent(result, "ThreadGroup");
        JSONObject jo = Utils.getJSONObject(event);
        /* TODO
        1.Start stub web-server
        2.Send request to this server(server takes document from file and sends back)
        3.Compare with JSON which was loaded from file
        */
    }
}