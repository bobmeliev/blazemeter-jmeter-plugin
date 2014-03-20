package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.constants.JsonFields;
import com.blazemeter.jmeter.entities.Overrides;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dzmitrykashlach on 3/20/14.
 */
public class TestInfoProcessor {
    public static TestInfo parseTestInfo(JSONObject jsonObject) {
        TestInfo testInfo = new TestInfo();
        try {
            testInfo.setId(jsonObject.getString("test_id"));
            testInfo.setName(jsonObject.getString("test_name"));
            TestStatus status = null;
            if (jsonObject.has("status")) {
                status = jsonObject.getString("status").equals("Running") ? TestStatus.Running : TestStatus.NotRunning;
                testInfo.setStatus(status);
            }
            testInfo.setError(jsonObject.getString("error").equals("null") ? null : jsonObject.getString("error"));

            if (jsonObject.has("options")) {
                JSONObject responseOptions = jsonObject.getJSONObject("options");
                if (responseOptions != null) {
//                    int numberOfEngines=Integer.parseInt((String)responseOptions.get("NUMBER_OF_ENGINES"));
                    int numberOfEngines = getEngines(responseOptions);
                    int numberOfUsers = (Integer) responseOptions.get(JsonFields.USERS) * (numberOfEngines + 1);
                    testInfo.setNumberOfUsers(numberOfUsers);
                    testInfo.setType(responseOptions.getString(JsonFields.TEST_TYPE));
                    testInfo.setLocation(responseOptions.getString(JsonFields.LOCATION));
                    // set overrides
                    if (responseOptions.getBoolean(JsonFields.OVERRIDE)) {
                        Overrides overrides = new Overrides(responseOptions.getInt(JsonFields.OVERRIDE_DURATION),
                                responseOptions.getInt(JsonFields.OVERRIDE_ITERATIONS),
                                responseOptions.getInt(JsonFields.OVERRIDE_RAMP_UP),
                                responseOptions.getInt(JsonFields.OVERRIDE_THREADS));
                        testInfo.setOverrides(overrides);
                    }
                }
            }

        } catch (JSONException je) {
            BmLog.error("Error while creating TestInfo from JSON: " + je + "\n" + jsonObject.toString());
        } catch (ClassCastException cce) {
            BmLog.error("Error while creating TestInfo from JSON: " + cce + "\n" + jsonObject.toString());
        }

        return testInfo;
    }

    private static int getEngines(JSONObject responseOptions) throws JSONException {
        Object numberOfEnginesObj = responseOptions.get(JsonFields.NUMBER_OF_ENGINES);
        int numberOfEngines = 0;
        if (numberOfEnginesObj instanceof String) {
            numberOfEngines = Integer.parseInt((String) numberOfEnginesObj);
        }
        if (numberOfEnginesObj instanceof Integer) {
            numberOfEngines = (Integer) numberOfEnginesObj;
        }
        return numberOfEngines;
    }

}
