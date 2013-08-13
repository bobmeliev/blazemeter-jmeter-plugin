package com.blazemeter.jmeter.upload;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 12/10/12
 * Time: 13:16
 * <p/>
 * <assertionResult>
 * <name>net200</name>
 * <failure>true</failure>
 * <error>false</error>
 * <failureMessage>Test failed: code expected not to equal /
 * <p/>
 * ***** received  : 200[[[]]]
 * <p/>
 * ***** comparison: 200[[[]]]
 * <p/>
 * /</failureMessage>
 * </assertionResult>
 */
public class AssertionResult {
    private String name;
    private String failureMessage;
    private Boolean failure;
    private Boolean error;

    public AssertionResult() {

    }

    public static AssertionResult Create(StreamReaderDelegate streamReaderDelegate) throws XMLStreamException {
        AssertionResult assertionResult = new AssertionResult();
        int eventType;
        boolean exit = false;
        String data;
        String name;
        do {
            eventType = streamReaderDelegate.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                name = streamReaderDelegate.getLocalName();
                data = streamReaderDelegate.getElementText();
                switch (name) {
                    case "name":
                        assertionResult.setName(data);
                        break;
                    case "error":
                        assertionResult.setError(data);
                        break;
                    case "failure":
                        assertionResult.setFailure(data);
                        break;
                    case "failureMessage":
                        assertionResult.setFailureMessage(data);
                        break;
                    default:
                        System.out.println(name + ":" + data);
                        break;
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                exit = "assertionResult".equals(streamReaderDelegate.getLocalName());
            }
        } while (!exit);
        return assertionResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Boolean getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = "true".equals(failure);
    }

    public Boolean getError() {
        return error;
    }

    public void setError(String error) {
        this.error = "true".equals(error);
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    @Override
    public String toString() {
        return "AssertionResult{" +
                "name='" + name + '\'' +
                ", failureMessage='" + failureMessage + '\'' +
                ", failure='" + failure + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
