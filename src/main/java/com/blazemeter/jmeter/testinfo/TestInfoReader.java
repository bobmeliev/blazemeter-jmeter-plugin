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
    private static TestInfoReader instance;
    private TestInfo testInfo;
    private String currentElement;

    private TestInfoReader() {
        this.testInfo = new TestInfo();
        this.currentElement = "";
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
        BmLog.console("Reading testInfo from file is started");
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
            testInfo.id = new String(ch, start, length);
        }
        if (currentElement.equals("name")) {
            testInfo.name = new String(ch, start, length);
        }
        if (currentElement.equals("status")) {
            String testInfo_status = new String(ch, start, length);
            if (testInfo_status.equals("Running")) {
                testInfo.status = TestStatus.Running;
            }
            if (testInfo_status.equals("Not_Running")) {
                testInfo.status = TestStatus.NotRunning;
            }
        }
        if (currentElement.equals("error")) {
            testInfo.error = new String(ch, start, length);
        }
        if (currentElement.equals("numberOfUsers")) {
            testInfo.numberOfUsers = new Integer(new String(ch, start, length));
        }
        if (currentElement.equals("location")) {
            testInfo.location = new String(ch, start, length);
        }
        if (currentElement.equals("type")) {
            testInfo.type = new String(ch, start, length);
        }


    }

    @Override
    public void endDocument() {
        BmLog.console("Reading testInfo from file is finished");
    }

    public TestInfo loadTestInfo() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        TestInfoReader testInfoReader = TestInfoReader.getInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            File testInfoFile = new File("../lib/ext/testinfo.xml");
            saxParser.parse(testInfoFile, testInfoReader);

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

        return testInfoReader.getTestInfo();
    }


}



