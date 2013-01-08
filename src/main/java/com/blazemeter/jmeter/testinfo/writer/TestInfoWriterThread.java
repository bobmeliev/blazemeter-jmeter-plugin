package com.blazemeter.jmeter.testinfo.writer;

import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.utils.BmLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 26.11.12
 * Time: 8:12
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoWriterThread implements Runnable {
    private TestInfo testInfo;

    public TestInfoWriterThread(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    public void run() {
        String testInfoFile = TestInfo.getTestInfoFilePath();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder testInfoBuilder = docFactory.newDocumentBuilder();

            // create testInfo(root element)
            Document testInfoDoc = testInfoBuilder.newDocument();
            Element testInfoElement = testInfoDoc.createElement("testInfo");
            testInfoDoc.appendChild(testInfoElement);

            // append id to testInfo and set value from field of testInfo
            Element idElement = testInfoDoc.createElement("id");
            idElement.appendChild(testInfoDoc.createTextNode(testInfo.id != null ? testInfo.id : ""));
            testInfoElement.appendChild(idElement);


            // append name to testInfo and set value from field of testInfo
            Element nameElement = testInfoDoc.createElement("name");
            nameElement.appendChild(testInfoDoc.createTextNode(testInfo.name != null ? testInfo.name : ""));
            testInfoElement.appendChild(nameElement);

            // append status to testInfo and set value from field of testInfo
            Element statusElement = testInfoDoc.createElement("status");
            statusElement.appendChild(testInfoDoc.createTextNode(testInfo.status.toString()));
            testInfoElement.appendChild(statusElement);

            // append error to testInfo and set value from field of testInfo
            Element errorElement = testInfoDoc.createElement("error");
            errorElement.appendChild(testInfoDoc.createTextNode(testInfo.error != null ? testInfo.error : ""));
            testInfoElement.appendChild(errorElement);

            // append numberOfUsers to testInfo and set value from field of testInfo
            Element numberOfUsersElement = testInfoDoc.createElement("numberOfUsers");
            numberOfUsersElement.appendChild(testInfoDoc.createTextNode(String.valueOf(testInfo.numberOfUsers)));
            testInfoElement.appendChild(numberOfUsersElement);


            // append location to testInfo and set value from field of testInfo
            Element locationElement = testInfoDoc.createElement("location");
            locationElement.appendChild(testInfoDoc.createTextNode(testInfo.location));
            testInfoElement.appendChild(locationElement);

            // append type to testInfo and set value from field of testInfo
            Element typeElement = testInfoDoc.createElement("type");
            typeElement.appendChild(testInfoDoc.createTextNode(testInfo.type));
            testInfoElement.appendChild(typeElement);


            // write the content into ../lib/ext/testinfo.xml from output stream
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(testInfoDoc);
            File f_testInfoFile = new File(testInfoFile);
            if (!f_testInfoFile.exists()) {
                f_testInfoFile.createNewFile();
            }
            StreamResult streamResult = new StreamResult(f_testInfoFile);
            transformer.transform(source, streamResult);

            BmLog.debug("TestInfo is saved to " + testInfoFile);

        } catch (ParserConfigurationException pce) {
            BmLog.error("ParserConfiguraionException during saving testInfo", pce);
        } catch (TransformerException tfe) {
            BmLog.error("TransformerException during saving testInfo", tfe);

        } catch (IOException ioe) {
            BmLog.error("IOException during creating " + testInfoFile, ioe);
        }
    }
}

