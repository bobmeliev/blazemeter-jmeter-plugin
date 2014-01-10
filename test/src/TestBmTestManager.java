package src;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 1/9/14.
 */
public class TestBmTestManager {
    private BmTestManager bmTestManager;
    private StubServer server;

    @Before
    public void setUp() {
        bmTestManager = BmTestManager.getInstance();
        server = new StubServer().run();
        int port = server.getPort();
    }


    @After
    public void tearDown() {
        server.stop();
        bmTestManager = null;
    }

    @Test
    public void createTest() {
        /*
        1.Use internal BlzAPI
        2.Mock BM Server
        *
        * */
    }

}
