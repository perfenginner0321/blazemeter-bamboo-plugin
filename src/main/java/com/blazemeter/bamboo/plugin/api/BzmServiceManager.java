package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.BlazeMeterConstants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Marcel Milea
 *
 */
public class BzmServiceManager {
	private String userKey;
	private String proxyserver;
	private String proxyport;
	private String proxyuser;
	private String proxypass;
    private String apiVersion;
	private BlazemeterApi blazemeterApi;
	
	//Default properties

	private String session;
	private String aggregate;
	
	public BzmServiceManager(){
	}
	
	public BzmServiceManager(String userKey, String apiVersion,String serverName, int serverPort, String username, String password) {
		blazemeterApi = new BlazemeterApiV2Impl(serverName, serverPort, username, password);
		this.userKey = userKey;
        this.apiVersion=apiVersion;
	}

	@NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	/**
	 * returns a hash map with test id as key and test name as value
	 * @return
	 */
	public HashMap<String, String> getTests() {
		try {
			return getAPI().getTestList(userKey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		HashMap<String, String> temp = new HashMap<String, String>();
		return temp;//no tests found, return an empty hashmap
	}

	private void addError(String error, BuildLogger logger, CurrentBuildResult currentBuildResult){
		logger.addErrorLogEntry(error);
		currentBuildResult.addBuildErrors(Arrays.asList(error));
	}
	
	public boolean startTest(String testId, BuildLogger logger, CurrentBuildResult currentBuildResult) {
        JSONObject json;
        int countStartRequests = 0;
        do {
            json = getAPI().startTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	addError("Could not start BlazeMeter Test", logger, currentBuildResult);
                return false;
            }
        } while (json == null);
        
        try {
			if (!json.get("response_code").equals(200)) {
				if (json.get("response_code").equals(500) && json.get("error").toString().startsWith("Test already running")) {
					addError("Test already running, please stop it first", logger, currentBuildResult);
					return false;
				}
                //Try again.
				json = getAPI().startTest(userKey, testId);
                if (!json.get("response_code").equals(200)) {
                	addError("Could not start BlazeMeter Test -" + json.get("error").toString(), logger, currentBuildResult);
                    return false;
                } 				
			}
			session = json.get("session_id").toString();
		} catch (JSONException e) {
			addError("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
		}
		return true;
	}

	public boolean isReportReady(){
        //get testGetArchive information
		JSONObject json = getAPI().aggregateReport(userKey, session);
        try {
            if (json.get("response_code").equals(404))
                return false;
            else
            	if (json.get("response_code").equals(200)){
            		return true;
            	}
        } catch (JSONException e) {
        } 
        return false;
    }
	
	@SuppressWarnings("static-access")
	public boolean waitForReport(BuildLogger logger, CurrentBuildResult currentBuildResult){
        //get testGetArchive information
		JSONObject json = getAPI().aggregateReport(userKey, session);
        for (int i = 0; i < 200; i++) {
            try {
                if (json.get("response_code").equals(404))
                    json = getAPI().aggregateReport(userKey, session);
                else
                    break;
            } catch (JSONException e) {
            } finally {
                try {
					Thread.currentThread().sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
        
        aggregate = null;

        for (int i = 0; i < 30; i++) {
            try {
                if (!json.get("response_code").equals(200)){
                	addError("Error: Requesting aggregate report response code:" + json.get("response_code"), logger, currentBuildResult);
                }
                aggregate = json.getJSONObject("report").get("aggregate").toString();
            } catch (JSONException e) {
            	addError("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
            }

            if (!aggregate.equals("null"))
                break;

            try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            json = getAPI().aggregateReport(userKey, session);
        }

        if (aggregate == null) {
        	addError("Error: Requesting aggregate is not available", logger, currentBuildResult);
            return false;
        }
     
        return true;
    }
	
	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
	public int getReport(int errorFailedThreshold, int errorUnstableThreshold, int responseTimeFailedThreshold, int responseTimeUnstableThreshold, BuildLogger logger, CurrentBuildResult currentBuildResult){
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);

		} catch (IOException e) {
			addError("Error: Requesting aggregate Test Result is not available", logger, currentBuildResult);
			return -1;
		}
		
        if (aggregateTestResult == null) {
        	addError("Error: Requesting aggregate Test Result is not available", logger, currentBuildResult);
            return -1;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
        	addError("Test ended with failure on error percentage threshold", logger, currentBuildResult);
            return -1;
        } else if (errorUnstableThreshold >= 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
        	addError("Test ended with unstable on error percentage threshold", logger, currentBuildResult);
            return 1;
        }

        if (responseTimeFailedThreshold >= 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
        	addError("Test ended with failure on response time threshold", logger, currentBuildResult);
            return -1;
        } else if (responseTimeUnstableThreshold >= 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
        	addError("Test ended with unstable on response time threshold", logger, currentBuildResult);
            return 1;
        }

        return 0;   		
	}

	public boolean uploadJMX(String testId, String filename, String pathname){
		return getAPI().uploadJmx(userKey, testId, filename, pathname);
	}
	
    public void uploadFile(String testId, String dataFolder, String fileName, BuildLogger logger, CurrentBuildResult currentBuildResult) {
        JSONObject json = getAPI().uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get("response_code").equals(new Integer(200))) {
            	addError("Could not upload file " + fileName + " " + json.get("error").toString(), logger, currentBuildResult);
            }
        } catch (JSONException e) {
        	addError("Could not upload file " + fileName + " " + e.getMessage(), logger, currentBuildResult);
        }
    } 	

    public void stopTest(String testId, BuildLogger logger, CurrentBuildResult currentBuildResult){
    	JSONObject json;

    	int countStartRequests = 0;
        do {
        	json = getAPI().stopTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	addError("Could not stop BlazeMeter Test "+ testId, logger, currentBuildResult);
            	return ;
            }
        } while (json == null);
        
        try {
			if (json.get("response_code").equals(200)) {
				logger.addBuildLogEntry("Test stopped succesfully.");
			} else {
				String error = json.get("error").toString();
				addError("Error stopping test. Reported error is: "+error, logger, currentBuildResult);
				addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
			}
		} catch (JSONException e) {
			addError("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
			addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
		}
    	
    }
    
    public TestInfo getTestStatus(String testId){
    	return getAPI().getTestRunStatus(userKey, testId);
    }
    
	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	
	

	public String getProxyserver() {
		return proxyserver;
	}

	public void setProxyserver(String proxyserver) {
		this.proxyserver = proxyserver;
	}

	public String getProxyport() {
		return proxyport;
	}

	public void setProxyport(String proxyport) {
		this.proxyport = proxyport;
	}

	public String getProxyuser() {
		return proxyuser;
	}

	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	public String getProxypass() {
		return proxypass;
	}

	public void setProxypass(String proxypass) {
		this.proxypass = proxypass;
	}

	public String getSession() {
		return session;
	}

	public boolean publishReportArtifact(CurrentBuildResult currentBuildResult) {
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);
			if (aggregateTestResult == null){
				return false;
			} 
			//TODO
	        double errorPercent = aggregateTestResult.getErrorPercentage();
	        double AverageResponseTime = aggregateTestResult.getAverage();

	        currentBuildResult.getCustomBuildData().put(BlazeMeterConstants.REPORT_RESPONSE_TIME, ""+AverageResponseTime);
	        currentBuildResult.getCustomBuildData().put(BlazeMeterConstants.REPORT_ERROR_THRESHOLD, ""+errorPercent);
	        return true;
		} catch (IOException e) {
//			logger.error("Error: Requesting aggregate Test Result is not available");
		}
		return false;
	}
	
	
	public boolean verifyUserKey(String userKey){
		return getAPI().verifyUserKey(userKey);
	}
	
	private BlazemeterApi getAPI(){
		if (blazemeterApi == null){
			int proxyPortInt = -1;
			try{
				proxyPortInt = Integer.parseInt(proxyport);
			} catch (NumberFormatException nfe){
				
			}
			blazemeterApi = new BlazemeterApiV2Impl(proxyserver, proxyPortInt, proxyuser, proxypass);
		}
		
		return blazemeterApi;
	}

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}