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

import com.blazemeter.bamboo.plugin.api.UrlManager;
import com.blazemeter.bamboo.plugin.api.UrlManagerV3Impl;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestUrlManager {
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String masterId ="987654321";
    private String sessionId ="r-v3-57230c5251da9";
    private UrlManager bmUrlManager=new UrlManagerV3Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void setServerUrl(){
        bmUrlManager.setServerUrl(TestConstants.mockedApiUrl);
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+UrlManager.V4+ Constants.MASTERS
                + masterId +"/status?events=false&app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.masterStatus(appKey, masterId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+UrlManager.V4+"/tests?limit=10000&workspaceId=1&app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.tests(appKey,1);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }

    @Test
    public void getMultiTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+UrlManager.V4+"/multi-tests?limit=10000&workspaceId=1&app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.multiTests(appKey,1);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }

    @Test
    public void testStop_masters(){
        String expTestStop=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS
                +testId+"/stop?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.masterStop(appKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }

    @Test
    public void testTerminate_masters(){
        String expTestTerminate=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS
                +testId+"/terminate?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;

        String actTestTerminate=bmUrlManager.testTerminate(appKey, testId);
        Assert.assertEquals(expTestTerminate, actTestTerminate);
    }

    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS
                + masterId +"/reports/main/summary?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, masterId);
        Assert.assertEquals(expTestReport, actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+UrlManager.V4+"/user?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.user(appKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getCIStatus(){
        String expCIStatus=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS+ masterId +"/ci-status?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actCIStatus=bmUrlManager.ciStatus(appKey, masterId);
        Assert.assertEquals(expCIStatus,actCIStatus);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS+ masterId +
                "/reports/thresholds?format=junit&app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, masterId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void generatePublicToken_masters(){
        String expGenPublicToken=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS
            + masterId +
                "/"+Constants.PUBLIC_TOKEN+"?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actGenPublicToken=bmUrlManager.generatePublicToken(appKey, masterId);
        Assert.assertEquals(expGenPublicToken,actGenPublicToken);
    }

    @Test
    public void listOfSessions(){
        String expListOfSessionIds=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS
            + masterId +
                "/sessions?app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actListOfSessionsIds=bmUrlManager.listOfSessionIds(appKey, masterId);
        Assert.assertEquals(expListOfSessionIds,actListOfSessionsIds);
    }

    @Test
    public void activeTests(){
        String expActiveTests=bmUrlManager.getServerUrl()+UrlManager.V4+Constants.MASTERS+"?workspaceId=1&active=true&app_key="+appKey+UrlManager.CLIENT_IDENTIFICATION;
        String actActiveTests=bmUrlManager.activeTests(appKey,1);
        Assert.assertEquals(expActiveTests,actActiveTests);
    }


    @Test
    public void properties(){
        String expProperties=bmUrlManager.getServerUrl()+UrlManager.V4 +"/sessions/"+sessionId+"/properties?target=all&app_key="+appKey+
                UrlManager.CLIENT_IDENTIFICATION;
        String actProperties=bmUrlManager.properties(appKey,sessionId);
        Assert.assertEquals(expProperties,actProperties);
    }

    @Test
    public void workspaces(){
        String exp=bmUrlManager.getServerUrl()+UrlManager.V4 +"/workspaces?limit=1000&enabled=true&app_key="+appKey+"&"+"accountId="
            +1+UrlManager.CLIENT_IDENTIFICATION;
        String act=bmUrlManager.workspaces(appKey,1);
        Assert.assertEquals(exp,act);
    }

    @Test
    public void accounts(){
        String exp=bmUrlManager.getServerUrl()+UrlManager.V4 +"/accounts?app_key="+appKey+"&"+UrlManager.CLIENT_IDENTIFICATION;
        String act=bmUrlManager.accounts(appKey);
        Assert.assertEquals(exp,act);
    }
}
