package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Marcel Milea
 *
 */
public class BzmServiceManager {
private BzmServiceManager(){
	}

    @NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	/**
	 * returns a hash map with test id as key and test name as value
	 * @return
	 */
	public static LinkedHashMultimap<String, String> getTests(BlazemeterApi api) {
        LinkedHashMultimap<String,String> tests= LinkedHashMultimap.create();
        try {
			tests=api.getTestList();
		} catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return tests;
        }
    }

    public static Map<String, Collection<String>> getTestsAsMap(BlazemeterApi api) {
        return getTests(api).asMap();
    }

	public static String startTest(BlazemeterApi api,String testId, BuildLogger logger) {
        int countStartRequests = 0;
        String session=null;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+api.getUserKey());
            do {
                session=api.startTest(testId);
                countStartRequests++;
                if (countStartRequests > 5) {
                    logger.addErrorLogEntry("Could not start BlazeMeter Test with userKey=" + api.getUserKey() + " testId=" + testId);
                    return session;
                }
            } while (session.length()==0);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with session="+session.toString());
        } catch (JSONException e) {
            logger.addErrorLogEntry("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
        }
        return session;
    }


	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
    public static TestResult getReport(BlazemeterApi api,String session, BuildLogger logger) {
        TestResult testResult = null;
        try {
            logger.addBuildLogEntry("Trying to request aggregate report. UserKey="+api.getUserKey()+" session="+session);
            JSONObject aggregate=api.testReport(session);
            testResult = new TestResult(aggregate);
            logger.addBuildLogEntry(testResult.toString());
        } catch (JSONException e) {
            logger.addErrorLogEntry("Problems with getting aggregate test report...",e);
        } catch (IOException e) {
            logger.addErrorLogEntry("Problems with getting aggregate test report...", e);
        } catch (NullPointerException e){
            logger.addErrorLogEntry("Problems with getting aggregate test report...", e);
        }
        finally {
            return testResult;
        }
    }
	public boolean uploadJMX(BlazemeterApi api,String testId, String filename, String pathname){
		return api.uploadJmx(testId, filename, pathname);
	}
	
    public void uploadFile(BlazemeterApi api,String testId, String dataFolder, String fileName, BuildLogger logger) {
        JSONObject json = api.uploadFile(testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get(JsonConstants.RESPONSE_CODE).equals(new Integer(200))) {
                logger.addErrorLogEntry("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.addErrorLogEntry("Could not upload file " + e.getMessage());
        }
    }

    public static TaskState validateServerTresholds(BlazemeterApi api,String session,BuildLogger logger) {
        JSONObject jo = null;
        TaskState serverTresholdsResult=TaskState.SUCCESS;
        JSONObject result=null;
        logger.addBuildLogEntry("Going to validate server tresholds...");
        try {
            jo=api.getTresholds(session);
            result=jo.getJSONObject(JsonConstants.RESULT);
            serverTresholdsResult=result.getJSONObject(JsonConstants.DATA).getBoolean("success")?TaskState.SUCCESS:TaskState.FAILED;
        } catch (NullPointerException e){
            logger.addBuildLogEntry("Server tresholds validation was not executed");
            logger.addBuildLogEntry(e.getMessage());
        }catch (JSONException je) {
            logger.addBuildLogEntry("Server tresholds validation was not executed");
            logger.addBuildLogEntry("Failed to get tresholds for  session=" + session);
        }finally {
            logger.addBuildLogEntry("Server tresholds validation " +
                    (serverTresholdsResult.equals(TaskState.SUCCESS) ? "passed. Marking build as PASSED" : "failed. Marking build as FAILED"));
            return serverTresholdsResult;
        }
    }


    public static JSONObject updateTestDuration(BlazemeterApi api,
                                                String testId,
                                                String testDuration,
                                                BuildLogger logger) throws Exception{
        JSONObject result;
        JSONObject updateResult=null;
        try {
            JSONObject jo = api.getTestConfig(testId);
            result = jo.getJSONObject(JsonConstants.RESULT);
            JSONObject configuration = result.getJSONObject(JsonConstants.CONFIGURATION);
            JSONObject plugins = configuration.getJSONObject(JsonConstants.PLUGINS);
            String type = configuration.getString(JsonConstants.TYPE);
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject(JsonConstants.OVERRIDE);
            override.put(JsonConstants.DURATION, testDuration);
            override.put("threads", JSONObject.NULL);
            configuration.put("serversCount", JSONObject.NULL);
            updateResult = api.putTestInfo(testId, result);
        } catch (JSONException je) {
            logger.addBuildLogEntry("Received JSONException while updating test duration: " + je);
            throw je;
        } catch (Exception e) {
            logger.addBuildLogEntry("Received Exception while updating test duration: " + e);
            throw e;
        }
        return updateResult;
    }

    public static boolean stopTestSession(BlazemeterApi api, String testId, String sessionId, BuildLogger logger) {
        boolean terminate=false;
        try {

            int statusCode = api.getTestSessionStatusCode(sessionId);
            if (statusCode < 100) {
                api.terminateTest(testId);
                terminate=true;
            }
            if (statusCode >= 100|statusCode ==-1) {
                api.stopTest(testId);
                terminate=false;
            }
        } catch (Exception e) {
            logger.addBuildLogEntry("Error while trying to stop test with testId=" + testId + ", " + e.getMessage());
        }finally {
            return terminate;
        }
    }


    public static List<Exception> prepareTest(BlazemeterApi api,
                                              String testId,
                                              String testDuration,
                                              BuildLogger logger){
        List<Exception> exceptions=new ArrayList<>();
        try {
            if (!testDuration.isEmpty()) {
                updateTestDuration(api, testId, testDuration, logger);
            }
        }catch (Exception e){
            exceptions.add(e);
        }finally {
            return exceptions;
        }
    }
}
