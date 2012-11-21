package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.TestStatus;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 21.11.12
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoReader extends DefaultHandler {
    TestInfo testInfo = new TestInfo();
    String thisElement = "";

    @Override
    public void startDocument() throws SAXException {
        BmLog.console("Reading testInfo from file is started");
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        thisElement = qName;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        thisElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (thisElement.equals("id")) {
            testInfo.id = new String(ch, start, length);
        }
        if (thisElement.equals("name")) {
            testInfo.name = new String(ch, start, length);
        }
        if (thisElement.equals("status")) {
            String testInfo_status = new String(ch, start, length);
            if (testInfo_status.equals("Running")) {
                testInfo.status = TestStatus.Running;
            }
            if (testInfo_status.equals("Not_Running")) {
                testInfo.status = TestStatus.NotRunning;
            }
        }
        if (thisElement.equals("error")) {
            testInfo.error = new String(ch, start, length);
        }
        if (thisElement.equals("numberOfUsers")) {
            testInfo.numberOfUsers = new Integer(new String(ch, start, length));
        }
        if (thisElement.equals("location")) {
            testInfo.location = new String(ch, start, length);
        }
        if (thisElement.equals("type")) {
            testInfo.type = new String(ch, start, length);
        }


    }

    @Override
    public void endDocument() {
        BmLog.console("Reading testInfo from file is finished");
    }
}



