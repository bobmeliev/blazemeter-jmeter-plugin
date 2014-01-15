package src.com.tests;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.utils.Utils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import src.com.Constants;

/**
 * Created by dzmitrykashlach on 1/14/14.
 */
public class TestTestInfo {
    public TestInfo ti1;
    public TestInfo ti2;
    public TestInfo ti3;


    @Before
    public void setUp() {
        String name = "load_test";
        String id = "1111";
        TestStatus status = TestStatus.NotRunning;

        ti1 = new TestInfo();
        ti2 = new TestInfo();
        ti3 = new TestInfo();

        ti1.setName(name);
        ti2.setName(name);
        ti3.setName(name);

        ti1.setId(id);
        ti2.setId(id);
        ti3.setId(id);

        ti1.setStatus(status);
        ti2.setStatus(status);
        ti3.setStatus(status);
    }

    @Test
    public void ti1_equals_ti1() {
        Assert.assertTrue(ti2.equals(ti1));
    }

    @Test
    public void ti1_equals_ti2() {
        Assert.assertTrue(ti1.equals(ti2));
    }

    @Test
    public void ti2_equals_ti1() {
        Assert.assertTrue(ti2.equals(ti1));
    }

    @Test
    public void ti1_ti2_ti3_equals() {
        Assert.assertTrue(ti1.equals(ti2));
        Assert.assertTrue(ti2.equals(ti3));
        Assert.assertTrue(ti1.equals(ti3));
    }

    @Test
    public void hasCode_equals() {
        Assert.assertTrue(ti1.hashCode() == ti2.hashCode());
    }

    @Test
    public void test_toString() {
        String actual = ti1.toString();
        String expected = Utils.getFileContents(Constants.RESOURCES + "/test-info-to-string.txt");
        Assert.assertEquals(expected, actual);
    }
}
