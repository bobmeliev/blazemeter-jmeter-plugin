package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.TestStatus;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 21.11.12
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoReader extends DefaultHandler {


    private boolean isTestInfoSet = false;
    private static TestInfoReader instance;
    private TestInfo testInfo;
    private String currentElement;
    private static String testInfoFile = System.getProperty("user.home") + "\\testinfo.xml";

    private TestInfoReader() {
        this.testInfo = new TestInfo();
        this.currentElement = "";
    }

    public boolean isTestInfoSet() {
        return isTestInfoSet;
    }

    public void setTestInfoSet(boolean testInfoSet) {
        isTestInfoSet = testInfoSet;
    }

    public static TestInfoReader getInstance() {
        if (instance == null) {
            instance = new TestInfoReader();
        }
        return instance;
    }

    private TestInfo getTestInfo() {
        return testInfo;
    }

    @Override
    public void startDocument() throws SAXException {
        BmLog.console("Reading testInfo from file " + testInfoFile + " is started");
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        currentElement = qName;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement.equals("id")) {
            this.testInfo.id = new String(ch, start, length);
        }
        if (currentElement.equals("name")) {
            this.testInfo.name = new String(ch, start, length);
        }
        if (currentElement.equals("status")) {
            String testInfo_status = new String(ch, start, length);
            if (testInfo_status.equals("Running")) {
                this.testInfo.status = TestStatus.Running;
            }
            if (testInfo_status.equals("NotRunning")) {
                this.testInfo.status = TestStatus.NotRunning;
            }
        }
        if (currentElement.equals("error")) {
            this.testInfo.error = new String(ch, start, length);
        }
        if (currentElement.equals("numberOfUsers")) {
            this.testInfo.numberOfUsers = new Integer(new String(ch, start, length));
        }
        if (currentElement.equals("location")) {
            this.testInfo.location = new String(ch, start, length);
        }
        if (currentElement.equals("type")) {
            this.testInfo.type = new String(ch, start, length);
        }


    }

    @Override
    public void endDocument() {
        BmLog.console("Reading testInfo from file " + testInfoFile + " is finished");
    }

    public TestInfo loadTestInfo() {
        TestInfoReader testInfoReader = TestInfoReader.getInstance();
        if (!isTestInfoSet) {

            SAXParserFactory factory = SAXParserFactory.newInstance();

            try {
                SAXParser saxParser = factory.newSAXParser();
                File f_testInfoFile = new File(testInfoFile);
                saxParser.parse(f_testInfoFile, testInfoReader);

            } catch (ParserConfigurationException e) {
                BmLog.error("ParseConfiguration exception is got!");
                BmLog.console("ParseConfiguration exception is got!");
            } catch (SAXException e) {
                BmLog.error("SAXException is got!");
                BmLog.console("SAXException is got!");
            } catch (IOException e) {
                BmLog.error("IOException is got!");
                BmLog.console("IOException is got!");
            }

        }
        return testInfoReader.getTestInfo();
    }
}



