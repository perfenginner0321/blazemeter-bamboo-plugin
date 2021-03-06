/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blazemeter.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskState;
import com.blazemeter.bamboo.plugin.ServiceManager;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.ApiImpl;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.Credentials;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
public class TestServiceManager {
    BuildLogger log= Mockito.mock(BuildLogger.class);

    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.startTest();
        MockedAPI.stopTestSession();
        MockedAPI.getMasterStatus();
        MockedAPI.getCIStatus();
        MockedAPI.getReportUrl();
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }

    @Test
    public void getReportUrl(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/?"+Constants.PUBLIC_TOKEN+"=ohImO6c8xstG4qBFqgRnsMSAluCBambtrqsTvAEYEXItmrCfgO#masters/testMasterId/summary";
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        String actReportUrl=ServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID,log);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }



    @Test
    public void getCIStatus_success(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        TaskState state= ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_SUCCESS, log);
        Assert.assertEquals(TaskState.SUCCESS,state);
    }

    @Test
    public void getCIStatus_failure(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        TaskState state= ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_FAILURE, log);
        Assert.assertEquals(TaskState.FAILED,state);
    }

    @Test
    public void getCIStatus_error_61700(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        TaskState state = ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_ERROR_61700, log);
        Assert.assertEquals(TaskState.ERROR,state);
    }


    @Test
    public void getCIStatus_error_0(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        TaskState state = ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_ERROR_0, log);
        Assert.assertEquals(TaskState.FAILED,state);
    }

    @Test
    public void getCIStatus_error_70404(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        TaskState state= ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_ERROR_70404,log);
        Assert.assertEquals(TaskState.FAILED,state);
    }

    @Test
    public void stopTestMaster(){
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        Api api = new ApiImpl(c, TestConstants.mockedApiUrl);
        boolean terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_25, log);
        Assert.assertEquals(terminate, true);
        terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_70, log);
        Assert.assertEquals(terminate, true);
        terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_100, log);
        Assert.assertEquals(terminate, false);
        terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_140, log);
        Assert.assertEquals(terminate, false);
    }

    @Test
    public void getVersion() throws IOException,JSONException {
        String version= ServiceManager.getVersion();
        Assert.assertTrue(version.matches("^(\\d{1,}\\.+\\d{1,2}\\S*)$"));
    }

    @Test
    public void errorsFailed_true_0() throws JSONException, IOException {
        File error_0=new File(TestConstants.RESOURCES+ "/ciStatus_error_0.json");
        String error_0_str= FileUtils.readFileToString(error_0);
        JSONArray error_0_json=new JSONArray(error_0_str);
        Assert.assertTrue(ServiceManager.errorsFailed(error_0_json));
    }

    @Test
    public void errorsFailed_true_70404() throws JSONException, IOException {
        File error=new File(TestConstants.RESOURCES+ "/ciStatus_error_70404.json");
        String error_str=FileUtils.readFileToString(error);
        JSONArray error_json=new JSONArray(error_str);
        Assert.assertTrue(ServiceManager.errorsFailed(error_json));
    }

    @Test
    public void errorsFailed_false_61700() throws JSONException, IOException {
        File error=new File(TestConstants.RESOURCES+ "/ciStatus_error_61700.json");
        String error_str=FileUtils.readFileToString(error);
        JSONArray error_json=new JSONArray(error_str);
        Assert.assertFalse(ServiceManager.errorsFailed(error_json));
    }


}
