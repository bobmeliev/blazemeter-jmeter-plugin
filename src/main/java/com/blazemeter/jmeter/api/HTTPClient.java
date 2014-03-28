package com.blazemeter.jmeter.api;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.constants.Methods;
import com.blazemeter.jmeter.utils.BmLog;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 3/21/14.
 */
public class HTTPClient {
    private static HTTPClient instance;


    static HTTPClient getInstance() {
        if (instance == null)
            instance = new HTTPClient();
        return instance;
    }

    private HTTPClient() {
    }

    public HttpResponse doHTTPRequest(String method, String url, JSONObject data) throws IOException {

        BmLog.debug("Requesting : " + url);
        HttpRequestBase request = method.equals(Methods.POST) ? new HttpPost(url) : new HttpGet(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null & request instanceof HttpPost) {
            StringEntity stringEntity = null;

            if (data.has(Constants.SAMPLES)) {
                try {
                    stringEntity = new StringEntity(data.getString(Constants.SAMPLES));

                } catch (JSONException je) {
                    BmLog.error("Failed to prepare samples for sending: " + je.getMessage());
                }
            } else {
                stringEntity = new StringEntity(data.toString());
            }
            ((HttpPost) request).setEntity(stringEntity);

        }

        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if (statusCode != 200) {
                BmLog.error(String.format("Wrong response : %d %s", statusCode, error));
            }
        } catch (IOException e) {
            BmLog.error("Wrong response", e);
        }
        return response;
    }

    public HttpResponse getJMX(String url) {
        BmLog.debug("Requesting : " + url);
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Connection", "keep-alive");
        getRequest.setHeader("Host", BmUrlManager.SERVER_URL.substring(8, com.blazemeter.jmeter.api.BmUrlManager.SERVER_URL.length()));
        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if (statusCode != 200) {
                BmLog.error(String.format("Wrong response : %d %s", statusCode, error));
            }

        } catch (IOException ioe) {
            BmLog.error("Wrong response", ioe);
        }
        return response;
    }


    public JSONObject getJson(String method, String url, JSONObject data) {
        JSONObject jo = null;
        HttpResponse response = null;
        try {

            response = doHTTPRequest(method, url, data);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                BmLog.debug(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            BmLog.error("Error while decoding Json: " + e.getMessage());
            BmLog.debug("Error while decoding Json: " + e.getMessage());
        } catch (JSONException e) {
            BmLog.error("Error while decoding Json: " + e.getMessage());
            BmLog.debug("Error while decoding Json: " + e.getMessage());
        } finally {
            return jo;
        }
    }

}
