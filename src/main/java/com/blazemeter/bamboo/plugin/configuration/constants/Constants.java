package com.blazemeter.bamboo.plugin.configuration.constants;

public interface Constants {
    //runner properties
	 String USER_KEY = "USER_KEY";
	
//	 String SETTINGS_API_VERSION = "api_version";

    //settings properties
     String SETTINGS_SELECTED_TEST_ID = "selectedtest";
     String SETTINGS_JTL_REPORT = "jtl.report";

     String NOT_IMPLEMENTED="This call is not implemented.";


    //BlazeMeterConfigTask
     String TEST_LIST ="testlist";
     String BLAZEMETER_ERROR ="blazemeter.error.";
     String REPORT_URL ="reportUrl";


    String PROXY_HOST="http.proxyHost";
    String PROXY_PORT="http.proxyPort";
    String PROXY_USER="http.proxyUser";
    String PROXY_PASS="http.proxyPassword";
    String USE_PROXY="http.useProxy";
}
