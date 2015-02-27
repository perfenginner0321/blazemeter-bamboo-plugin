package com.blazemeter.bamboo.plugin.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;


/**
 * 
 * @author 
 *
 */
public interface BlazemeterApi {

    public boolean uploadJmx(String userKey, String testId, String fileName, String pathName);

    public JSONObject uploadFile(String userKey, String testId, String fileName, String pathName);

    public TestInfo getTestRunStatus(String userKey, String testId);

    public String startTest(String userKey, String testId) throws JSONException;

    public int getTestCount(String userKey) throws JSONException, IOException;

    public boolean stopTest(String userKey, String testId) throws JSONException;

    public JSONObject aggregateReport(String userKey, String reportId);

    public HashMap<String, String> getTestList(String userKey) throws IOException;

    public boolean verifyUserKey(String userKey);

}
